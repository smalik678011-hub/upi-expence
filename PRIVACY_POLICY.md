# Privacy Policy for UPI Unified Expense Tracker
**Last Updated: July 5, 2026**

Your privacy is our highest priority. The **UPI Unified Expense Tracker** (referred to as "the App") is designed from the ground up as a fully **offline-first, zero-leak** application. We do not collect, transmit, or share your personal, financial, or behavioral data. This Privacy Policy outlines our strict data handling practices to reassure you of your absolute ownership over your data.

---

## 1. Executive Summary
- **No Remote Transmission**: No transaction records, parsed messages, or notification logs are ever transmitted to any remote servers.
- **Strictly Local Storage**: All financial records, categories, and analytics are stored locally on your device within a secured SQLite database.
- **No SMS Permissions Required**: The app does not request or use SMS permission (`READ_SMS` or `RECEIVE_SMS`). It uses on-device **Notification Access** to capture transaction messages securely.
- **Complete User Control**: You can delete, modify, export (CSV/JSON), or wipe your entire financial log history at any time without requiring remote account deletion.

---

## 2. Information Collection and Usage

### A. Notification Access & Parsing
- **How it works**: The App utilizes Android's **NotificationListenerService** (which requires explicit user consent during setup) to listen for incoming notification broadcasts from standard UPI payments and banking applications.
- **Processing**: The notification body is parsed on-device in real-time using built-in, local regular expression patterns to extract the transaction amount, merchant name, transaction type (debit/credit), reference ID, and payment application name.
- **Raw Data Disposal**: The raw notification text is immediately discarded from RAM after parsing is complete. The raw text is **never** saved, logged, or shared.

### B. Device Information & Diagnostics
- **Log Management**: The App maintains a minimal local-only system log wrapper for diagnosing issues.
- **Privacy Hardening**: If an error or warning is logged locally, all sensitive financial data (such as bank account digits, transaction amounts, and transaction reference numbers) is automatically obfuscated using local sanitization patterns (e.g., replacement with `[REDACTED]`).
- **No Diagnostics Uploads**: Local logs are stored in private app-specific storage and are never uploaded to any remote diagnostic services.

### C. Optional Integration (Google Mobile Ads)
- The App includes the Google Mobile Ads SDK (AdMob) to display non-intrusive banner advertisements.
- **No Financial Profiling**: No local transaction history, total spend statistics, account names, or financial profiles are ever shared with the AdMob SDK or Google.
- **User Control**: You can easily opt-in or opt-out of personalized advertising via the App's **Settings** menu.

---

## 3. Data Storage, Security, and Encryption

### A. SQLite Room Database
- All transaction details (date, amount, merchant, debit/credit type, reference code, status) are saved locally inside an encrypted SQLite/Room database.
- The database is fully sandboxed inside the App's private internal storage (`/data/data/com.aistudio.upiexpense.ugpxty/databases/`), which is inaccessible to other applications.

### B. File Sharing & Exporting
- When you explicitly request to export your transaction ledger (as CSV, JSON, or PDF), the file is written to your private app-specific cache directory.
- Sharing is facilitated via a secure Android **FileProvider** (`android:exported="false"`), which grants temporary, read-only permissions explicitly to the app you select (e.g., your email client or secure cloud drive) and is automatically revoked immediately after.

---

## 4. Play Store Data Safety Disclosures
In compliance with Google Play Developer Policies, we answer the Data Safety questionnaire as follows:

| Question | Answer | Details |
| :--- | :--- | :--- |
| **Data Collected?** | **NO** | The developer does not collect or store any of your data on any server. |
| **Data Shared?** | **NO** | No personal or financial data is shared with any third-party. |
| **Data Encrypted in Transit?** | **YES / NOT APPLICABLE** | Since no data is transmitted externally, there is no transit phase. |
| **User Deletion Request?** | **YES** | You have full self-service control. You can clear or wipe your entire database from the App's settings instantly. |

---

## 5. Contact & Support
If you have any questions, concerns, or feedback regarding our privacy practices, please contact us:
- **Email**: smalik678011@gmail.com
- **GitHub Pages/Project Support**: [https://github.com/smalik678011/upi-unified-expense-tracker](https://github.com/smalik678011/upi-unified-expense-tracker)
