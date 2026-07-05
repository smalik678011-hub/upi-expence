# Enterprise Release Engineering & Production Verification Checklist
## UPI Unified Expense Tracker — Google Play Release Playbook

This document details the production verification guidelines, release engineering protocols, and optimization verification steps to ensure maximum stability, performance, battery efficiency, and privacy compliance for the Google Play Store release.

---

## 1. Startup Optimization & Performance Verification

### Cold Start Validation
- [ ] **Methodology**: Measure launch times using `am start-activity` to capture Time to First Draw (TTFD).
- [ ] **Command**:
  ```bash
  adb shell am start-activity -W -n com.aistudio.upiexpense.ugpxty/com.example.MainActivity
  ```
- [ ] **Target**: Ensure `TotalTime` is under **800ms** on mid-range devices (e.g., Android Go / low-spec devices).
- [ ] **Baseline Profiles Verification**: Confirm `/app/src/main/baseline-prof.txt` is compiled and packaged in the release AAB.
- [ ] **Lazy Loading Check**: Verify `AppContainer.kt` initializes all repositories and DAOs using `by lazy` delegators to ensure 0ms main thread overhead during application startup.

---

## 2. Battery & Wakeup Efficiency

### Background Process Verification
- [ ] **Doze Mode Compliance**: Ensure the `NotificationListenerService` does not hold any continuous `WakeLock` or execute background loops when the device enters a low-power state.
- [ ] **CPU & Wakeups Check**: Run `batterystats` to verify that the application triggers **0 periodic wakeups** during idle.
  ```bash
  adb shell dumpsys batterystats --reset
  # Leave device idle for 30 minutes
  adb shell dumpsys batterystats > batterystats_report.txt
  ```
- [ ] **Doze Mode Testing**: Run the following commands to force the device into Doze mode and verify that the application handles notifications gracefully upon waking:
  ```bash
  adb shell dumpsys deviceidle step
  ```

---

## 3. Memory Profile & Leak Prevention

### LeakCanary & Profiler Validation
- [ ] **Activity Leak Check**: Perform multiple configuration changes (screen rotations) and navigate back-and-forth between screens (Home $\rightarrow$ Settings $\rightarrow$ Ads $\rightarrow$ Export) using Android Studio Profiler to verify that no `Activity` or `Context` references are retained.
- [ ] **Coroutines Scope Lifecycle**: Ensure all launch jobs in UI components utilize `viewLifecycleOwner.lifecycleScope` or `collectAsStateWithLifecycle()` to prevent dangling background listeners.
- [ ] **AdBanner Resource Release**: Verify that `AdView` containers release resources and clear references when composables are disposed.

---

## 4. Database Hardening & Stress Testing

### 100k+ Transaction Scaling
- [ ] **Bulk Insertion Stress Test**: Seed the local database with **150,000 synthetic transaction records** to ensure queries remain under **50ms**.
- [ ] **Room Index Verification**: Ensure that the database indexes on the `expenses` table (`dateLong`, `merchantName`, `transactionRef`, `uType`, `category`, `status`) are fully utilized by inspecting query plans with `EXPLAIN QUERY PLAN` in Room/SQLite.
- [ ] **No Main Thread Queries**: Verify that no database reads/writes occur on the main (UI) thread; all transactions must stream via Kotlin `Flow` or run on the `Dispatchers.IO` thread pool.

---

## 5. Security & Privacy Compliance (Play Store Readiness)

### Zero-Leak Privacy Policy
- [ ] **Logger Hardening**: Verify that `com.example.BuildConfig.DEBUG` is set to `false` in production. Check that all verbose/debug log messages are compiled out by R8 or skipped by `AndroidLogger`.
- [ ] **PII Redaction**: Verify that sensitive notification titles/bodies are completely redacted in warning or error logs using the local pattern recognizer:
  ```kotlin
  message.replace(Regex("\\d{4,}"), "[REDACTED]")
  ```
- [ ] **FileProvider Security**: Confirm that the internal storage directories shared via `FileProvider` are non-exported (`android:exported="false"`) and grant permissions strictly on-demand.
- [ ] **Export Integrity**: Verify that exported CSV or JSON expense reports are stored securely in the app-specific cache or public download directories with temporary grant permissions.

---

## 6. Build Shrinking & AAB Size Optimization

### R8 & ProGuard Verification
- [ ] **R8 Optimization**: Compile the final bundle with `-Dandroid.enableR8=true` and verify the compilation logs.
- [ ] **Compilation Command**:
  ```bash
  gradle :app:bundleRelease
  ```
- [ ] **Size Budget**: Verify that the final production `.aab` is under **4.5 MB** by stripping unused assets and dependencies.
- [ ] **Mapping File Retention**: Ensure that `/app/build/outputs/mapping/release/mapping.txt` is retained and uploaded to the Google Play Console for deobfuscating crash traces.
