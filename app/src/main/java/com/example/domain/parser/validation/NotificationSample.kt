package com.example.domain.parser.validation

import com.example.domain.model.NotificationData
import com.example.domain.model.NotificationSource
import com.example.domain.model.TransactionDirection
import com.example.domain.model.TransactionStatus

enum class ExpectedType {
    SUCCESS,
    IGNORED,
    INVALID,
    FAILED
}

data class ExpectedTransactionData(
    val amount: Double,
    val direction: TransactionDirection,
    val status: TransactionStatus,
    val counterpartyName: String,
    val transactionRef: String?
)

data class NotificationSample(
    val id: String,
    val scenario: String,
    val packageName: String,
    val title: String,
    val text: String,
    val expectedType: ExpectedType,
    val expectedData: ExpectedTransactionData? = null
)

object NotificationSampleProvider {
    fun getSamples(): List<NotificationSample> {
        val gpayPkg = NotificationSource.GOOGLE_PAY.packageName
        val phonepePkg = NotificationSource.PHONEPE.packageName
        val paytmPkg = NotificationSource.PAYTM.packageName
        val naviPkg = NotificationSource.NAVI.packageName

        return listOf(
            // ==================== GOOGLE PAY SAMPLES ====================
            NotificationSample(
                id = "gpay_pay_success_1",
                scenario = "GPay Successful Payment",
                packageName = gpayPkg,
                title = "Google Pay",
                text = "You paid Rs 150 to Ramesh Kumar successful. UPI Ref: 312345678901",
                expectedType = ExpectedType.SUCCESS,
                expectedData = ExpectedTransactionData(
                    amount = 150.0,
                    direction = TransactionDirection.SENT,
                    status = TransactionStatus.SUCCESS,
                    counterpartyName = "Ramesh Kumar",
                    transactionRef = "312345678901"
                )
            ),
            NotificationSample(
                id = "gpay_receive_success_1",
                scenario = "GPay Successful Receive",
                packageName = gpayPkg,
                title = "Google Pay",
                text = "Ramesh Kumar paid you Rs 1,200 successful. Ref: 312345678902",
                expectedType = ExpectedType.SUCCESS,
                expectedData = ExpectedTransactionData(
                    amount = 1200.0,
                    direction = TransactionDirection.RECEIVED,
                    status = TransactionStatus.SUCCESS,
                    counterpartyName = "Ramesh Kumar",
                    transactionRef = "312345678902"
                )
            ),
            NotificationSample(
                id = "gpay_pay_pending_1",
                scenario = "GPay Pending Payment",
                packageName = gpayPkg,
                title = "Google Pay",
                text = "Payment of Rs 160 to Suresh Patel is pending. Ref: 312345678903",
                expectedType = ExpectedType.SUCCESS, // Parser extracts it as pending transaction
                expectedData = ExpectedTransactionData(
                    amount = 160.0,
                    direction = TransactionDirection.SENT,
                    status = TransactionStatus.PENDING,
                    counterpartyName = "Suresh Patel",
                    transactionRef = "312345678903"
                )
            ),
            NotificationSample(
                id = "gpay_pay_failed_1",
                scenario = "GPay Failed Payment",
                packageName = gpayPkg,
                title = "Google Pay",
                text = "Payment of Rs 100 to Amit Gupta failed. Ref: 312345678904",
                expectedType = ExpectedType.SUCCESS, // Extracted successfully with FAILED status
                expectedData = ExpectedTransactionData(
                    amount = 100.0,
                    direction = TransactionDirection.SENT,
                    status = TransactionStatus.FAILED,
                    counterpartyName = "Amit Gupta",
                    transactionRef = "312345678904"
                )
            ),
            NotificationSample(
                id = "gpay_refund_1",
                scenario = "GPay Refund",
                packageName = gpayPkg,
                title = "Google Pay",
                text = "Refund of Rs 250 received from GPay Rewards. Ref: 312345678905",
                expectedType = ExpectedType.SUCCESS,
                expectedData = ExpectedTransactionData(
                    amount = 250.0,
                    direction = TransactionDirection.RECEIVED,
                    status = TransactionStatus.SUCCESS,
                    counterpartyName = "GPay Rewards",
                    transactionRef = "312345678905"
                )
            ),

            // ==================== PHONEPE SAMPLES ====================
            NotificationSample(
                id = "phonepe_pay_success_1",
                scenario = "PhonePe Successful Payment",
                packageName = phonepePkg,
                title = "PhonePe",
                text = "Paid Rs 350 to Sharma Grocery. Txn 234567123456",
                expectedType = ExpectedType.SUCCESS,
                expectedData = ExpectedTransactionData(
                    amount = 350.0,
                    direction = TransactionDirection.SENT,
                    status = TransactionStatus.SUCCESS,
                    counterpartyName = "Sharma Grocery",
                    transactionRef = "234567123456"
                )
            ),
            NotificationSample(
                id = "phonepe_receive_success_1",
                scenario = "PhonePe Successful Receive",
                packageName = phonepePkg,
                title = "PhonePe",
                text = "Received Rs 10,000 from Dad. Txn 234567123457",
                expectedType = ExpectedType.SUCCESS,
                expectedData = ExpectedTransactionData(
                    amount = 10000.0,
                    direction = TransactionDirection.RECEIVED,
                    status = TransactionStatus.SUCCESS,
                    counterpartyName = "Dad",
                    transactionRef = "234567123457"
                )
            ),
            NotificationSample(
                id = "phonepe_pay_failed_1",
                scenario = "PhonePe Failed Payment",
                packageName = phonepePkg,
                title = "PhonePe",
                text = "Transaction of Rs 500 to Ramesh Kumar failed. Txn 234567123458",
                expectedType = ExpectedType.SUCCESS,
                expectedData = ExpectedTransactionData(
                    amount = 500.0,
                    direction = TransactionDirection.SENT,
                    status = TransactionStatus.FAILED,
                    counterpartyName = "Ramesh Kumar",
                    transactionRef = "234567123458"
                )
            ),

            // ==================== PAYTM SAMPLES ====================
            NotificationSample(
                id = "paytm_pay_success_1",
                scenario = "Paytm Successful Payment",
                packageName = paytmPkg,
                title = "Paytm",
                text = "Paid Rs 2,500 to Ritu Verma. Ref 112233445566",
                expectedType = ExpectedType.SUCCESS,
                expectedData = ExpectedTransactionData(
                    amount = 2500.0,
                    direction = TransactionDirection.SENT,
                    status = TransactionStatus.SUCCESS,
                    counterpartyName = "Ritu Verma",
                    transactionRef = "112233445566"
                )
            ),
            NotificationSample(
                id = "paytm_receive_success_1",
                scenario = "Paytm Successful Receive",
                packageName = paytmPkg,
                title = "Paytm",
                text = "Received Rs 800 from Ajay. Ref 112233445567",
                expectedType = ExpectedType.SUCCESS,
                expectedData = ExpectedTransactionData(
                    amount = 800.0,
                    direction = TransactionDirection.RECEIVED,
                    status = TransactionStatus.SUCCESS,
                    counterpartyName = "Ajay",
                    transactionRef = "112233445567"
                )
            ),

            // ==================== NAVI SAMPLES ====================
            NotificationSample(
                id = "navi_pay_success_1",
                scenario = "Navi Successful Payment",
                packageName = naviPkg,
                title = "Navi",
                text = "Paid Rs 1,000 to Mutual Fund. Ref 998877665544",
                expectedType = ExpectedType.SUCCESS,
                expectedData = ExpectedTransactionData(
                    amount = 1000.0,
                    direction = TransactionDirection.SENT,
                    status = TransactionStatus.SUCCESS,
                    counterpartyName = "Mutual Fund",
                    transactionRef = "998877665544"
                )
            ),
            NotificationSample(
                id = "navi_receive_success_1",
                scenario = "Navi Successful Receive",
                packageName = naviPkg,
                title = "Navi",
                text = "Received Rs 5,000 from Cashback. Ref 998877665545",
                expectedType = ExpectedType.SUCCESS,
                expectedData = ExpectedTransactionData(
                    amount = 5000.0,
                    direction = TransactionDirection.RECEIVED,
                    status = TransactionStatus.SUCCESS,
                    counterpartyName = "Cashback",
                    transactionRef = "998877665545"
                )
            ),

            // ==================== HINDI & MIXED SAMPLES ====================
            NotificationSample(
                id = "hindi_receive_1",
                scenario = "Hindi Notification (Receive)",
                packageName = paytmPkg,
                title = "Paytm",
                text = "Rs 150 प्राप्त हुए. Ref: 312345678910",
                expectedType = ExpectedType.SUCCESS,
                expectedData = ExpectedTransactionData(
                    amount = 150.0,
                    direction = TransactionDirection.RECEIVED,
                    status = TransactionStatus.SUCCESS,
                    counterpartyName = "Unknown UPI Merchant",
                    transactionRef = "312345678910"
                )
            ),
            NotificationSample(
                id = "mixed_hindi_pay_1",
                scenario = "Mixed Hindi-English (Pay)",
                packageName = gpayPkg,
                title = "Google Pay",
                text = "Ramesh Verma को Rs 155 भेजे गए. Ref: 312345678911",
                expectedType = ExpectedType.SUCCESS,
                expectedData = ExpectedTransactionData(
                    amount = 155.0,
                    direction = TransactionDirection.SENT,
                    status = TransactionStatus.SUCCESS,
                    counterpartyName = "Ramesh Verma",
                    transactionRef = "312345678911"
                )
            ),

            // ==================== AMOUNT EXTREME CASES ====================
            NotificationSample(
                id = "extreme_large_amount",
                scenario = "Large Amount",
                packageName = gpayPkg,
                title = "Google Pay",
                text = "You paid Rs 5,00,000 to Landlord. Ref: 312345678912",
                expectedType = ExpectedType.SUCCESS,
                expectedData = ExpectedTransactionData(
                    amount = 500000.0,
                    direction = TransactionDirection.SENT,
                    status = TransactionStatus.SUCCESS,
                    counterpartyName = "Landlord",
                    transactionRef = "312345678912"
                )
            ),
            NotificationSample(
                id = "extreme_small_amount",
                scenario = "Small Amount",
                packageName = paytmPkg,
                title = "Paytm",
                text = "Paid Rs 0.50 to Tester. Ref: 312345678913",
                expectedType = ExpectedType.SUCCESS,
                expectedData = ExpectedTransactionData(
                    amount = 0.50,
                    direction = TransactionDirection.SENT,
                    status = TransactionStatus.SUCCESS,
                    counterpartyName = "Tester",
                    transactionRef = "312345678913"
                )
            ),
            NotificationSample(
                id = "decimal_amount",
                scenario = "Decimal Amount",
                packageName = phonepePkg,
                title = "PhonePe",
                text = "Received Rs 123.45 from Friend. Txn 234567123490",
                expectedType = ExpectedType.SUCCESS,
                expectedData = ExpectedTransactionData(
                    amount = 123.45,
                    direction = TransactionDirection.RECEIVED,
                    status = TransactionStatus.SUCCESS,
                    counterpartyName = "Friend",
                    transactionRef = "234567123490"
                )
            ),

            // ==================== NON-TRANSACTION / PROMOTIONAL (REJECTED CASES) ====================
            NotificationSample(
                id = "promo_offer",
                scenario = "Offer/Discount Promotion",
                packageName = gpayPkg,
                title = "Google Pay",
                text = "Flat 50% discount on your next ride, win scratch card up to Rs 200!",
                expectedType = ExpectedType.IGNORED
            ),
            NotificationSample(
                id = "promo_cashback_alert",
                scenario = "Cashback/Scratch Card Offer",
                packageName = phonepePkg,
                title = "PhonePe",
                text = "Congratulations! You have a scratch card of Rs 100 waiting for you. Open now!",
                expectedType = ExpectedType.IGNORED
            ),
            NotificationSample(
                id = "promo_reminder",
                scenario = "Bill Due Reminder",
                packageName = paytmPkg,
                title = "Paytm Reminder",
                text = "Your electricity bill due date is approaching. Pay Rs 1,450 now to avoid fine.",
                expectedType = ExpectedType.IGNORED
            ),
            NotificationSample(
                id = "security_alert",
                scenario = "Security Sign-in Alert",
                packageName = gpayPkg,
                title = "Security Notice",
                text = "Logged in from a new Chrome device on Linux. If this wasn't you, secure account.",
                expectedType = ExpectedType.IGNORED
            ),
            NotificationSample(
                id = "kyc_notice",
                scenario = "KYC Re-verification Notice",
                packageName = paytmPkg,
                title = "Paytm KYC",
                text = "Re-KYC verification is required for your wallet account. Submit your documents.",
                expectedType = ExpectedType.IGNORED
            ),

            // ==================== MALFORMED / ERROR INPUTS ====================
            NotificationSample(
                id = "malformed_missing_fields",
                scenario = "Notification with missing fields",
                packageName = gpayPkg,
                title = "Google Pay",
                text = "You paid to Amit Kumar", // Missing amount and reference ID
                expectedType = ExpectedType.INVALID
            ),
            NotificationSample(
                id = "empty_body",
                scenario = "Empty body notification",
                packageName = phonepePkg,
                title = "PhonePe",
                text = "",
                expectedType = ExpectedType.INVALID
            )
        )
    }
}
