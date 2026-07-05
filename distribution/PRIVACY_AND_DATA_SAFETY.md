# UPI Unified Expense Tracker — Privacy Policy & Data Safety Disclosures

This document outlines the strict privacy-first architecture, provides the standard Privacy Policy template required by the Google Play Console, and specifies the precise Data Safety declarations for the Google Play Store publication.

---

## Part 1: Official Privacy Policy Template

*Copy and publish the following markdown text on a public URL (e.g., GitHub Pages or your official website) to satisfy Google Play's mandatory Privacy Policy link requirement.*

```markdown
# Privacy Policy

**Last Updated: July 5, 2026**

UPI Unified Expense Tracker ("we," "our," or "us") is built as an offline-first, client-side personal finance ledger. Your financial privacy is our highest priority. This Privacy Policy outlines our practices regarding data collection, usage, and security.

### 1. 100% Offline Architecture & Data Collection
UPI Unified Expense Tracker is designed to operate completely offline. We do not maintain any cloud databases, remote servers, or secondary service layers.
* **No Server Storage**: All financial transactions, categories, merchant names, and app settings are stored locally on your mobile device in an encrypted SQLite database.
* **No Personal Data Harvesting**: We do not collect, harvest, or request personal identifying information (PII) such as your name, phone number, email address, physical address, or bank account credentials.
* **Zero Cloud Sync**: Your financial ledger remains entirely on your phone. If you uninstall the application or clear its local storage without creating a manual export, your data is permanently deleted and cannot be recovered by us.

### 2. Device Permissions & Services
To deliver offline personal finance tracking safely, the app requests specific system accesses:
* **Notification Access (NotificationListenerService)**: With your explicit, opt-in consent, the app reads push notifications received on your device from trusted Indian payment applications (including Google Pay, Paytm, PhonePe, and Navi). The app parses these messages locally in real-time to log transaction amounts, directions (Sent or Received), and merchant names. **This information is parsed 100% client-side. No notification text is ever sent online.**
* **Battery Optimization Exemption (Optional)**: This permission may be requested to prevent the Android operating system from sleeping our background listener service, ensuring transaction alerts are captured instantly.
* **Storage Access Framework (Export/Backup)**: The app requests local storage permissions solely to let you save your financial ledger as a CSV spreadsheet or backup your encrypted local database file to your local device.

### 3. Sharing of Information
We strictly enforce a **No-Sharing** financial data policy.
* **Zero Third-Party Sharing**: We do not sell, trade, rent, or transmit your financial data, transaction histories, or notification logs to any third-party companies, banks, or analytics platforms.
* **No Database Sync**: Your local ledger operates independent of any network.

### 4. Third-Party Integrations & Advertising
To support ongoing open-source development, the application includes Google AdMob to display non-intrusive banner advertisements.
* **AdMob Integration**: Google AdMob may collect and use device identifiers or advertising identifiers for personalization and analytics. This data collection is governed by Google’s own Privacy Policy. No transaction ledger details, bank amounts, or parsed financial data are ever shared with or accessible by the AdMob SDK.

### 5. Security of Your Data
We employ industry-standard local security measures to safeguard your records:
* **Local Database Encryption**: Your local Room database uses standard mobile encryption parameters to secure raw ledger items.
* **Secure Shared Preferences**: User preferences, onboard status, and theme configurations are secured within Android’s sandboxed system storage.

### 6. User Control & Data Deletion
You retain absolute control over your financial records:
* **Local Clearing**: You can permanently delete all transaction logs or wipe the entire database directly from the app's Settings panel (Settings -> Data Management).
* **Complete Deletion**: Uninstalling the app instantly deletes the entire local sandbox database and all app configurations from your device.

### 7. Children's Privacy
Our services are not designed for or targeted at children under the age of 13. We do not knowingly compile or maintain data from children.

### 8. Changes to This Privacy Policy
We may update our Privacy Policy periodically to reflect technical or policy adjustments. We recommend reviewing this document regularly. Your continued usage of the application indicates your acceptance of any updates.

### 9. Contact Us
If you have any questions, bug reports, or privacy inquiries, contact us at:  
**Support Email Placeholder**: support-upi-tracker@yourdomain.com
```

---

## Part 2: Google Play Console Data Safety Form Specifications

When publishing on Google Play, you must complete the **Data Safety Form** in the Play Console. Use these precise configurations to ensure complete alignment with the app’s actual offline-first architecture.

### 1. Data Collection and Sharing Declarations

- **Does your app collect or share any of the required user data types?**  
  👉 **Yes** (Due to Google AdMob device tracking, and local financial tracking. Ensure you answer "Yes" to accurately reflect modern Play Store requirements).
- **Is all of the user data collected by your app encrypted in transit?**  
  👉 **Yes** (Any diagnostic information or AdMob telemetry uses HTTPS).
- **Do you provide a way for users to request that their data be deleted?**  
  👉 **Yes** (Users can wipe data instantly in the settings or uninstall the app to delete everything).

### 2. Precise Data Type Disclosures

Use the following mapping to fill out the Play Console form fields:

#### Category: Financial Info
- **Data Type**: Personal financial history (Expenses, transaction values, categories, and merchant targets).
- **Collection**: **Yes** (Stored locally in the app's sandbox).
- **Sharing**: **No** (Strictly client-side).
- **Is this data processed ephemerally?**  
  👉 **No** (Stored persistently in the local SQLite Room DB).
- **Is this data required for your app or can users choose to deny/delete it?**  
  👉 **Users can choose** (Users can disable Notification Access, use manual entry, or delete the logs at any time).
- **Why is this data collected?**  
  👉 Check **App Functionality** and **Analytics / Personalization** (Only within the local device environment).

#### Category: Device or Other IDs
- **Data Type**: Device or Other IDs (e.g., Android ID, advertising ID).
- **Collection**: **Yes** (By Google AdMob for ad display).
- **Sharing**: **Yes** (Shared with AdMob for ad routing and anti-fraud monitoring).
- **Is this data processed ephemerally?**  
  👉 **No** (Standard advertising IDs are stored on Google’s secure servers).
- **Is this data required for your app or can users choose to deny/delete it?**  
  👉 **Required** (The AdMob integration requires these identifiers to render ads).
- **Why is this data collected?**  
  👉 Check **Advertising or Marketing** and **Fraud Prevention / Security**.

---

## Part 3: Notification Access Compliance Checklist

Because Google Play strictly reviews applications utilizing background listeners (`NotificationListenerService`), ensure your production submission satisfies these 4 legal/technical checkpoints:

1. **Explicit In-App Consent (Onboarding Screen 3 & Permissions Screen)**:
   - The app clearly explains *why* the listener is needed *before* launching the Android system settings page.
   - It details that notifications are parsed entirely on-device and never uploaded.
2. **Prominent Disclosure Placement**:
   - The onboarding journey must prominently state: *"To automatically map expenses as they occur, enable Notification Access. No database details or transaction texts ever leave this device."*
3. **No Coercion**:
   - If the user denies the permission, the app does not freeze. They must be allowed to click "Continue to App" and log transaction items manually.
4. **Accurate Store Disclosures**:
   - The Play Store description must outline that the background processing is strictly dedicated to parsing and automating the offline budgeting ledger.
