package com.example.core.security

import android.util.Base64
import java.security.SecureRandom

/**
 * Enterprise DatabaseEncryptionManager that generates, encrypts, and maintains
 * a cryptographically secure 256-bit passphrase for optional Room SQLite encryption.
 */
class DatabaseEncryptionManager(
    private val secureStorage: SecureStorage
) {
    companion object {
        private const val DB_PASSPHRASE_KEY = "db_encryption_passphrase"
    }

    /**
     * Returns the existing passphrase or generates a new 256-bit high-entropy secure passphrase.
     * The generated passphrase is encrypted via CryptoManager (Android Keystore) before storage.
     */
    @Synchronized
    fun getOrCreateDatabasePassphrase(): String {
        val encryptedPassphrase = secureStorage.getString(DB_PASSPHRASE_KEY, null)
        if (!encryptedPassphrase.isNullOrEmpty()) {
            val decrypted = CryptoManager.decrypt(encryptedPassphrase)
            if (!decrypted.isNullOrEmpty()) {
                return decrypted
            }
        }

        // Generate a new 256-bit secure passphrase (32 bytes of cryptographically strong random values)
        val secureRandom = SecureRandom()
        val keyBytes = ByteArray(32)
        secureRandom.nextBytes(keyBytes)
        val newPassphrase = Base64.encodeToString(keyBytes, Base64.NO_WRAP)

        // Encrypt using our hardware-backed AES-256-GCM Keystore manager
        val encrypted = CryptoManager.encrypt(newPassphrase)
        if (!encrypted.isNullOrEmpty()) {
            secureStorage.putString(DB_PASSPHRASE_KEY, encrypted)
        }
        return newPassphrase
    }
}
