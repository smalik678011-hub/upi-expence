package com.example

import com.example.data.repository.NotificationDispatcherImpl
import com.example.data.repository.NotificationFilterImpl
import com.example.data.repository.NotificationValidatorImpl
import com.example.domain.model.NotificationData
import com.example.domain.model.NotificationSource
import com.example.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class NotificationCaptureEngineTest {

    // Mock Logger for test runs
    private val mockLogger = object : com.example.core.log.Logger {
        override fun d(tag: String, message: String) {}
        override fun i(tag: String, message: String) {}
        override fun w(tag: String, message: String, throwable: Throwable?) {}
        override fun e(tag: String, message: String, throwable: Throwable?) {}
    }

    // Mock NotificationRepository for testing dispatcher integration
    private class FakeNotificationRepository : NotificationRepository {
        val savedTexts = mutableListOf<String>()
        override fun getProcessedNotificationTexts(): Flow<List<String>> {
            return flowOf(savedTexts)
        }
        override suspend fun saveProcessedNotificationText(text: String) {
            savedTexts.add(text)
        }
    }

    @Test
    fun `test NotificationSource package mapping`() {
        assertEquals(
            NotificationSource.GOOGLE_PAY,
            NotificationSource.fromPackageName("com.google.android.apps.nbu.paisa.user")
        )
        assertEquals(
            NotificationSource.PHONEPE,
            NotificationSource.fromPackageName("com.phonepe.app")
        )
        assertEquals(
            NotificationSource.PAYTM,
            NotificationSource.fromPackageName("net.one97.paytm")
        )
        assertEquals(
            NotificationSource.NAVI,
            NotificationSource.fromPackageName("com.naviapp")
        )
        assertEquals(
            NotificationSource.OTHER,
            NotificationSource.fromPackageName("com.some.unsupported.app")
        )
    }

    @Test
    fun `test NotificationFilter whitelist behavior`() {
        val filter = NotificationFilterImpl()

        // Google Pay and PhonePe should be supported by default
        assertTrue(filter.isSupportedPackage("com.google.android.apps.nbu.paisa.user"))
        assertTrue(filter.isSupportedPackage("com.phonepe.app"))

        // WhatsApp is not supported by default
        assertFalse(filter.isSupportedPackage("com.whatsapp"))

        // Dynamic addition to whitelist
        filter.addSupportedPackage("com.whatsapp")
        assertTrue(filter.isSupportedPackage("com.whatsapp"))

        // Dynamic removal from whitelist
        filter.removeSupportedPackage("com.whatsapp")
        assertFalse(filter.isSupportedPackage("com.whatsapp"))
    }

    @Test
    fun `test NotificationValidator content rejection`() {
        val filter = NotificationFilterImpl()
        val validator = NotificationValidatorImpl(filter)

        // Valid UPI notification
        assertTrue(
            validator.validate(
                packageName = "com.google.android.apps.nbu.paisa.user",
                title = "GPay payment",
                text = "Received Rs 500 from John",
                isOngoing = false,
                isSilent = false
            )
        )

        // Rejects if text is null or blank
        assertFalse(
            validator.validate(
                packageName = "com.google.android.apps.nbu.paisa.user",
                title = "GPay payment",
                text = "",
                isOngoing = false,
                isSilent = false
            )
        )

        // Rejects if package is unsupported
        assertFalse(
            validator.validate(
                packageName = "com.unsupported.package",
                title = "Unknown",
                text = "Received Rs 500",
                isOngoing = false,
                isSilent = false
            )
        )

        // Rejects if ongoing event
        assertFalse(
            validator.validate(
                packageName = "com.google.android.apps.nbu.paisa.user",
                title = "Syncing",
                text = "Syncing in progress...",
                isOngoing = true,
                isSilent = false
            )
        )

        // Rejects if silent system notification
        assertFalse(
            validator.validate(
                packageName = "com.google.android.apps.nbu.paisa.user",
                title = "Update",
                text = "Background update check",
                isOngoing = false,
                isSilent = true
            )
        )

        // Rejects if title is null/blank
        assertFalse(
            validator.validate(
                packageName = "com.google.android.apps.nbu.paisa.user",
                title = "",
                text = "Received Rs 500",
                isOngoing = false,
                isSilent = false
            )
        )
    }

    @Test
    fun `test NotificationDispatcher forwards data cleanly`() = runBlocking {
        val repository = FakeNotificationRepository()
        val dispatcher = NotificationDispatcherImpl(repository, mockLogger)

        val notification = NotificationData(
            packageName = "com.phonepe.app",
            title = "Received Money",
            text = "You received Rs. 250",
            bigText = null,
            postTime = System.currentTimeMillis(),
            notificationId = 101,
            notificationKey = "test_key",
            tag = null,
            extras = emptyMap()
        )

        dispatcher.dispatch(notification)

        // Verify that the notification dispatcher formatted and forwarded the event to the repository correctly
        assertEquals(1, repository.savedTexts.size)
        assertTrue(repository.savedTexts[0].contains("PhonePe"))
        assertTrue(repository.savedTexts[0].contains("You received Rs. 250"))
    }
}
