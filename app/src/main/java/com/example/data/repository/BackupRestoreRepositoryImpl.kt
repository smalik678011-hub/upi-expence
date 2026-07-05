package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.core.error.ErrorModel
import com.example.core.error.ResultWrapper
import com.example.data.database.AppDatabase
import com.example.domain.repository.BackupRestoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.*
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupRestoreRepositoryImpl(
    private val context: Context,
    private val appDatabase: AppDatabase
) : BackupRestoreRepository {

    override suspend fun createBackup(
        uri: Uri,
        onProgress: (Float) -> Unit
    ): ResultWrapper<Unit> = withContext(Dispatchers.IO) {
        val tempDir = File(context.cacheDir, "backup_temp_" + System.currentTimeMillis())
        try {
            onProgress(0.1f)
            if (!tempDir.exists()) tempDir.mkdirs()

            // 1. Checkpoint the Room database to flush WAL logs into the main database file
            try {
                appDatabase.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(TRUNCATE)")
            } catch (e: Exception) {
                // If the DB is not fully open or other issue, proceed anyway
            }
            onProgress(0.2f)

            // 2. Identify and copy files to tempDir
            val dbFile = context.getDatabasePath("upi_expenses_db")
            if (!dbFile.exists()) {
                return@withContext ResultWrapper.Error(
                    ErrorModel(
                        code = "BACKUP_NO_DATA",
                        message = "No database found to backup."
                    )
                )
            }
            val dbBackupFile = File(tempDir, "upi_expenses_db")
            copyFile(dbFile, dbBackupFile)
            onProgress(0.3f)

            // Calculate Checksum of the main database file
            val dbChecksum = calculateSha256(dbBackupFile)

            // Copy DataStore settings
            val dataStoreFile = File(context.filesDir, "datastore/upi_tracker_settings.preferences_pb")
            if (dataStoreFile.exists()) {
                val dataStoreBackupFile = File(tempDir, "upi_tracker_settings.preferences_pb")
                copyFile(dataStoreFile, dataStoreBackupFile)
            }

            // Copy Shared Preferences
            val sharedPrefsFile = File(context.filesDir.parentFile, "shared_prefs/upi_expense_secure_prefs.xml")
            if (sharedPrefsFile.exists()) {
                val sharedPrefsBackupFile = File(tempDir, "upi_expense_secure_prefs.xml")
                copyFile(sharedPrefsFile, sharedPrefsBackupFile)
            }
            onProgress(0.4f)

            // 3. Generate Metadata JSON file
            val metadataJson = JSONObject().apply {
                put("appName", "UPI Unified Expense Tracker")
                put("appVersion", "1.0.0")
                put("databaseVersion", appDatabase.openHelper.readableDatabase.version)
                put("timestamp", System.currentTimeMillis())
                put("dbFileChecksum", dbChecksum)
                
                // Future-proofing placeholders
                put("categoriesPlaceholder", JSONObject.NULL)
                put("notesPlaceholder", JSONObject.NULL)
                put("budgetPlaceholder", JSONObject.NULL)
                put("tagsPlaceholder", JSONObject.NULL)
            }
            
            val metadataFile = File(tempDir, "metadata.json")
            FileWriter(metadataFile).use { writer ->
                writer.write(metadataJson.toString(4))
            }
            onProgress(0.5f)

            // 4. Zip all files directly into the SAF OutputStream
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                    val filesToZip = tempDir.listFiles() ?: emptyArray()
                    for (file in filesToZip) {
                        zipFile(file, "", zipOut)
                    }
                }
            } ?: throw IOException("Could not open output stream for selected Uri")

            onProgress(0.9f)
            // Optional encryption hook (Android Keystore Placeholder)
            encryptPlaceholder()

            onProgress(1.0f)
            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
            ResultWrapper.Error(
                ErrorModel(
                    code = "BACKUP_FAILED",
                    message = "Local backup creation failed: ${e.message}",
                    throwable = e
                )
            )
        } finally {
            deleteRecursive(tempDir)
        }
    }

    override suspend fun restoreBackup(
        uri: Uri,
        onProgress: (Float) -> Unit
    ): ResultWrapper<Unit> = withContext(Dispatchers.IO) {
        val tempDir = File(context.cacheDir, "restore_temp_" + System.currentTimeMillis())
        try {
            onProgress(0.1f)
            if (!tempDir.exists()) tempDir.mkdirs()

            // 1. Copy ZIP content from SAF to a temp file, then extract
            val tempZipFile = File(tempDir, "restore_package.zip")
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempZipFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: throw IOException("Could not open input stream for selected Uri")
            onProgress(0.2f)

            // Decompress the ZIP package
            unzip(tempZipFile, tempDir)
            onProgress(0.4f)

            // 2. Locate and Parse Metadata
            val metadataFile = File(tempDir, "metadata.json")
            if (!metadataFile.exists()) {
                return@withContext ResultWrapper.Error(
                    ErrorModel(
                        code = "RESTORE_INVALID_BACKUP",
                        message = "Invalid backup package: metadata.json is missing."
                    )
                )
            }

            val metadataContent = FileReader(metadataFile).use { it.readText() }
            val metadataJson = JSONObject(metadataContent)
            val appName = metadataJson.optString("appName")
            val dbVersion = metadataJson.optInt("databaseVersion")
            val dbChecksum = metadataJson.optString("dbFileChecksum")

            // Verify compatibility & app identity
            if (appName != "UPI Unified Expense Tracker") {
                return@withContext ResultWrapper.Error(
                    ErrorModel(
                        code = "RESTORE_INCOMPATIBLE_APP",
                        message = "Incompatible backup: This package belongs to another application ($appName)."
                    )
                )
            }

            val currentDbVersion = appDatabase.openHelper.readableDatabase.version
            if (dbVersion > currentDbVersion) {
                return@withContext ResultWrapper.Error(
                    ErrorModel(
                        code = "RESTORE_NEWER_SCHEMA",
                        message = "Cannot restore backup: The backup is from a newer app/database version (v$dbVersion, current is v$currentDbVersion)."
                    )
                )
            }
            onProgress(0.5f)

            // 3. Verify Database File Integrity
            val dbBackupFile = File(tempDir, "upi_expenses_db")
            if (!dbBackupFile.exists()) {
                return@withContext ResultWrapper.Error(
                    ErrorModel(
                        code = "RESTORE_CORRUPTED_DB",
                        message = "Corrupted backup: Main database file is missing."
                    )
                )
            }

            val calculatedChecksum = calculateSha256(dbBackupFile)
            if (dbChecksum.isNotEmpty() && calculatedChecksum != dbChecksum) {
                return@withContext ResultWrapper.Error(
                    ErrorModel(
                        code = "RESTORE_INTEGRITY_FAIL",
                        message = "Backup integrity verification failed. File may be corrupted."
                    )
                )
            }
            onProgress(0.6f)

            // Optional decryption hook (Android Keystore Placeholder)
            decryptPlaceholder()

            // 4. Overwrite application files safely
            // Close active database connections to avoid crash
            appDatabase.close()
            onProgress(0.7f)

            // Copy Database file
            val dbFile = context.getDatabasePath("upi_expenses_db")
            copyFile(dbBackupFile, dbFile)

            // Crucial: Delete any active WAL or SHM temporary sqlite log files to prevent database state collision
            val shmFile = File(dbFile.path + "-shm")
            val walFile = File(dbFile.path + "-wal")
            if (shmFile.exists()) shmFile.delete()
            if (walFile.exists()) walFile.delete()

            // Copy DataStore file if present in backup
            val dataStoreBackupFile = File(tempDir, "upi_tracker_settings.preferences_pb")
            if (dataStoreBackupFile.exists()) {
                val dataStoreFile = File(context.filesDir, "datastore/upi_tracker_settings.preferences_pb")
                dataStoreFile.parentFile?.mkdirs()
                copyFile(dataStoreBackupFile, dataStoreFile)
            }

            // Copy Shared Preferences if present in backup
            val sharedPrefsBackupFile = File(tempDir, "upi_expense_secure_prefs.xml")
            if (sharedPrefsBackupFile.exists()) {
                val sharedPrefsFile = File(context.filesDir.parentFile, "shared_prefs/upi_expense_secure_prefs.xml")
                sharedPrefsFile.parentFile?.mkdirs()
                copyFile(sharedPrefsBackupFile, sharedPrefsFile)
            }

            onProgress(0.9f)
            // Restore Complete - user should either restart or VM will load fresh state
            onProgress(1.0f)
            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
            ResultWrapper.Error(
                ErrorModel(
                    code = "RESTORE_FAILED",
                    message = "Local backup restore failed: ${e.message}",
                    throwable = e
                )
            )
        } finally {
            deleteRecursive(tempDir)
        }
    }

    // --- HELPER UTILS ---

    private fun copyFile(source: File, dest: File) {
        dest.parentFile?.mkdirs()
        FileInputStream(source).use { input ->
            FileOutputStream(dest).use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun calculateSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(8192)
            var bytesRead = input.read(buffer)
            while (bytesRead != -1) {
                digest.update(buffer, 0, bytesRead)
                bytesRead = input.read(buffer)
            }
        }
        val hashBytes = digest.digest()
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun zipFile(fileToZip: File, fileName: String, zipOut: ZipOutputStream) {
        if (fileToZip.isDirectory) {
            val children = fileToZip.listFiles() ?: emptyArray()
            for (child in children) {
                zipFile(child, "$fileName${fileToZip.name}/", zipOut)
            }
            return
        }
        FileInputStream(fileToZip).use { fis ->
            val entryName = "$fileName${fileToZip.name}"
            val zipEntry = ZipEntry(entryName)
            zipOut.putNextEntry(zipEntry)
            val bytes = ByteArray(4096)
            var length: Int
            while (fis.read(bytes).also { length = it } >= 0) {
                zipOut.write(bytes, 0, length)
            }
            zipOut.closeEntry()
        }
    }

    private fun unzip(zipFile: File, targetDir: File) {
        ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
            var entry = zis.nextEntry
            val buffer = ByteArray(4096)
            while (entry != null) {
                val newFile = File(targetDir, entry.name)
                // Secure path traversal check
                if (!newFile.canonicalPath.startsWith(targetDir.canonicalPath)) {
                    throw IOException("Directory traversal attack detected in zip package!")
                }
                if (entry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile?.mkdirs()
                    FileOutputStream(newFile).use { fos ->
                        var len: Int
                        while (zis.read(buffer).also { len = it } > 0) {
                            fos.write(buffer, 0, len)
                        }
                    }
                }
                entry = zis.nextEntry
            }
            zis.closeEntry()
        }
    }

    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) {
            val children = fileOrDirectory.listFiles()
            if (children != null) {
                for (child in children) {
                    deleteRecursive(child)
                }
            }
        }
        fileOrDirectory.delete()
    }

    // Security Placeholder for Keystore Architecture
    private fun encryptPlaceholder() {
        // Placeholder for future Android Keystore backup encryption.
        // E.g., generate a symmetric key in KeyStore, encrypt DB or ZIP output Stream before writing.
    }

    private fun decryptPlaceholder() {
        // Placeholder for decrypting backups using KeyStore key.
    }
}
