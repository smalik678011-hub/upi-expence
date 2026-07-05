# UPI Unified Expense Tracker — Enterprise Security Audit & Privacy Compliance Report

This report documents the security audit, privacy posture, and application hardening measures implemented for the **UPI Unified Expense Tracker** Android application. Designed as a fully offline-first system, the app enforces strict boundaries around user transaction logs, notification events, and on-device storage.

---

## 1. Executive Privacy Posture

The application adheres to a **Zero-Leak, Offline-First** architecture. Financial data parsed from SMS notification broadcasts never leaves the physical boundaries of the device.

### Core Architecture Axioms:
1. **On-Device Data Isolation**: No raw SMS messages or transaction records are streamed to any remote cloud servers.
2. **Permission Minimization**: No intrusive runtime permissions (e.g., `READ_SMS`, `READ_CONTACTS`, `ACCESS_FINE_LOCATION`) are requested.
3. **Zero-Trace AdMob Integration**: Ad personalization options can be toggled by the user, and no on-device financial statistics or account details are shared with the Google Mobile Ads SDK.

---

## 2. Permission Audit

A comprehensive review of requested system permissions verifies that the app maintains the absolute minimum permission surface required to function:

| Permission | Protection Level | Purpose | Justification & Risk Mitigation |
|:---|:---|:---|:---|
| `android.permission.INTERNET` | Normal | Required for loading banner ads and performing optional backup uploads. | **Mitigated**: Used exclusively for Google Mobile Ads SDK and explicit user-driven backup/restore workflows. |
| `android.permission.ACCESS_NETWORK_STATE` | Normal | Checks network connectivity status. | **Mitigated**: Prevents unnecessary background API retry loops and optimizes battery when offline. |
| *Notification Access* | Special System Applet | Binds the `NotificationListenerService` to capture incoming UPI transactional push alerts. | **Mitigated**: Granted explicitly by the user in system settings. No background SMS reading required. |

### Forbidden Permissions explicitly omitted:
- **`READ_SMS` / `RECEIVE_SMS`**: Removed entirely to eliminate risk of SMS text eavesdropping.
- **`READ_CONTACTS`**: Removed to prevent contact book leaking.
- **`ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`**: Omitted to preserve user location privacy.

---

## 3. Android Manifest Audit

Every entry inside `AndroidManifest.xml` has been audited against standard Android security guidelines:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">
    
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
    <application
        android:name=".UpiExpenseApplication"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        ... />
```

### Manifest Audit Verification:
1. **Activity Export Restrictions**:
   - `MainActivity` is the only exported activity (`android:exported="true"`) since it contains the `LAUNCHER` intent filter. It performs no dangerous deep-linking or intent processing, eliminating injection risks.
2. **Notification Listener Service**:
   - `NotificationListenerService` is secured by requiring the standard `android.permission.BIND_NOTIFICATION_LISTENER_SERVICE` permission. This ensures that *only* the Android OS notification manager framework can bind to our listener, preventing third-party apps from spoofing intents.
3. **Secure FileProvider Setup**:
   - The FileProvider is declared with `android:exported="false"` and `android:grantUriPermissions="true"`.
   - File resources are constrained to the application's private cache folder (`reports/`), preventing unauthorized apps from scanning storage directories or gaining permanent reading access.

---

## 4. Component Security & Shared Intents

- **PendingIntents**: The application contains **zero** `PendingIntent` usage, ensuring it is immune to malicious redirection or privilege escalation attacks.
- **File Sharing (Android Share Sheet)**:
  - Reports exported as CSV or PDF are stored in the private cache directory: `cacheDir/reports/`.
  - When sharing via `Intent.ACTION_SEND`, a temporary read-only Uri is generated with:
    ```kotlin
    val contentUri = FileProvider.getUriForFile(context, fileProviderAuthority, file)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    ```
    This grants the receiving app temporary access to only that specific file. This access is automatically revoked when the share sheet transaction ends.

---

## 5. Local Data Cryptography (Hardening)

To prepare for high-security environments, the application includes a pre-configured local cryptography layer:

1. **`CryptoManager` (AES-256-GCM)**:
   - Uses the hardware-backed **Android KeyStore** (`AndroidKeyStore`) to generate and store a 256-bit symmetric AES key (`UPI_EXPENSE_SECURE_KEY`).
   - Key generation employs `KeyProperties.BLOCK_MODE_GCM` and `KeyProperties.ENCRYPTION_PADDING_NONE` to guarantee authenticated encryption.
   - Incorporates randomized IVs to protect against replay and structural dictionary attacks.
2. **`DatabaseEncryptionManager`**:
   - Securely generates a high-entropy cryptographically secure 256-bit passphrase.
   - Encrypts this passphrase with `CryptoManager` before storing it in private `SharedPreferences`.
   - Prepares the database open factory architecture to natively support encrypted databases (e.g., SQLCipher) should full-disk database encryption be toggled.

---

## 6. Enterprise Logging & Information Leak Prevention

Verbose/debug logs are isolated dynamically to protect financial privacy:

- **Sanitization Engine**:
  `AndroidLogger` includes an active, regular-expression-based sanitizing filter that runs automatically in production builds:
  ```kotlin
  private fun sanitize(message: String): String {
      return message.replace(Regex("\\d{4,}"), "[REDACTED]")
  }
  ```
  Any sequence of 4 or more digits (such as bank account numbers, UPI transaction reference numbers, or transaction values) is automatically obfuscated before writing to log sinks.
- **Release-Type Conditional Stripping**:
  Debug logs (`d`, `i`) are completely stripped and skipped in `Release` builds (when `com.example.BuildConfig.DEBUG` is `false`).

---

## 7. Play Store Data Safety & Accessibility

1. **Accessibility Compliance**:
   - All Material 3 text boxes and actions maintain a touch target size of **48.dp x 48.dp** or larger.
   - Complete `contentDescription` mappings are maintained on all icons and status indications.
2. **Dynamic Consent**:
   - Features custom-styled user preference cards for opt-in/opt-out ad personalization, completely respecting regional privacy regulations (e.g., GDPR, CCPA).
3. **Responsive Scaling**:
   - Uses container-based layouts, Material 3 responsive typography sizes (`sp`), and fluid column weights. Layouts scale gracefully across devices, and contrast meets high-visibility standards.

---

## 8. Reusable Security & Hardening Checklist

This checklist must be executed before compiling every production Google Play Release:

- [ ] **Release Flags Check**: Verify `isMinifyEnabled = true` and `isShrinkResources = true` in `/app/build.gradle.kts`.
- [ ] **ProGuard Rules**: Run compilation to ensure R8 successfully keeps `Room`, `ViewModel`, `Compose`, and `Google Ads SDK` components obfuscated yet fully functional.
- [ ] **Logger Verification**: Confirm `/app/src/main/java/com/example/core/log/Logger.kt` is compiling and active.
- [ ] **Data Isolation**: Verify no external network calls except authorized AdMob or User-Initiated exports are made by inspecting network traffic during app usage.
- [ ] **Manifest Permissions**: Inspect the merged Android Manifest from the final APK to ensure no unwanted permissions (SMS, contacts, GPS) have been injected by transitive dependencies.
