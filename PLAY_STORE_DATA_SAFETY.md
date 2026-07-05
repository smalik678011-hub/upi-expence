# Google Play Console — Data Safety Form Responses
## UPI Unified Expense Tracker

This document provides the exact answers required when completing the **Data Safety** questionnaire in the Google Play Console for the **UPI Unified Expense Tracker** application.

---

## 1. Data Collection and Sharing

### Question: Does your app collect or share any of the required user data types?
- **Answer**: **No**
> *Justification*: The application operates fully offline-first. All transaction parsing, database operations, and reporting are completed entirely on the physical device. The developer does not maintain any backend servers or cloud services to ingest, store, or process user data.

---

## 2. Security Practices

### Question: Is all user data collected by your app encrypted in transit?
- **Answer**: **No / Not Applicable**
> *Justification*: Since the application does not transmit any user-collected or financial data outside of the physical device, there is no transit mechanism.

### Question: Do you provide a way for users to request that their data be deleted?
- **Answer**: **Yes**
> *Justification*: Users have total, self-service authority over their local database. They can delete individual transactions or perform a complete, irreversible wipe of their entire database instantly via the App's Settings menu (**Settings** $\rightarrow$ **Backup & Restore** $\rightarrow$ **Clear All Data**).

---

## 3. Data Types Handled (Local Processing Only)

Since Google Play Console defines data processed on-device (but not sent to any servers) as "processed locally," the following disclosures apply:

### Financial Info
*   **Financial transaction history / account details**:
    *   **Collected**: **No** (It is stored locally and never sent to a server. Under Play Console rules, if data does not leave the device, select **No** for collection).
    *   **Shared**: **No**

### Messages
*   **Device notifications (Notification Access)**:
    *   **Collected**: **No** (Processed transiently in RAM on-device and discarded immediately after parsing).
    *   **Shared**: **No**

---

## 4. Ads Disclosure (Google Mobile Ads / AdMob)

### Question: Does your app collect or share device or other IDs? (e.g., Advertising ID)
- **Answer**: **Yes** (if personalized ads are enabled by the user).
- **Data Shared**: Device or other IDs are shared with the Google Mobile Ads SDK for operational purposes and ad delivery.
- **Type**: Out of our control, handled directly by the Google Mobile Ads client library.
- **Option to Opt-Out**: **Yes**. The application provides a clear, custom consent card in the Settings menu permitting users to disable personalized ads, which instructs the SDK to serve non-personalized ads or restricts identifier transmission where possible.
