package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.error.ResultWrapper
import com.example.core.log.Logger
import com.example.core.utils.DispatcherProvider
import com.example.data.database.AppDatabase
import com.example.data.database.dao.ExpenseDao
import com.example.data.parser.*
import com.example.data.repository.ExpenseRepositoryImpl
import com.example.domain.model.*
import com.example.presentation.screens.HomeUiState
import com.example.presentation.screens.HomeViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PipelineIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var expenseDao: ExpenseDao
    private lateinit var expenseRepository: ExpenseRepositoryImpl
    private lateinit var testDispatcherProvider: DispatcherProvider

    // Parser components
    private lateinit var normalizer: NotificationNormalizerImpl
    private lateinit var regexRuleProvider: RegexRuleProviderImpl
    private lateinit var keywordRuleProvider: KeywordRuleProviderImpl
    private lateinit var validator: TransactionValidatorImpl
    private lateinit var duplicateDetector: DuplicateDetectorImpl
    private lateinit var parserRegistry: ParserRegistryImpl
    private lateinit var parserFactory: ParserFactoryImpl
    private lateinit var parserEngine: ParserEngineImpl
    private lateinit var mapper: TransactionMapperImpl

    private lateinit var viewModel: HomeViewModel

    private val mockLogger = object : Logger {
        override fun d(tag: String, message: String) {}
        override fun i(tag: String, message: String) {}
        override fun w(tag: String, message: String, throwable: Throwable?) {}
        override fun e(tag: String, message: String, throwable: Throwable?) {}
    }

    private val parserLogger = ParserLoggerImpl(mockLogger)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        // 1. Database & Repository Setup
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        expenseDao = db.expenseDao()
        expenseRepository = ExpenseRepositoryImpl(expenseDao)

        // 2. Parser Engine Setup
        normalizer = NotificationNormalizerImpl()
        regexRuleProvider = RegexRuleProviderImpl()
        keywordRuleProvider = KeywordRuleProviderImpl()
        validator = TransactionValidatorImpl(keywordRuleProvider)
        duplicateDetector = DuplicateDetectorImpl()
        
        parserRegistry = ParserRegistryImpl()
        parserFactory = ParserFactoryImpl(normalizer, regexRuleProvider, keywordRuleProvider, validator)
        
        parserEngine = ParserEngineImpl(
            registry = parserRegistry,
            factory = parserFactory,
            duplicateDetector = duplicateDetector,
            logger = parserLogger
        )
        mapper = TransactionMapperImpl()

        // 3. Dispatchers & ViewModel Setup
        testDispatcherProvider = object : DispatcherProvider {
            override val main: CoroutineDispatcher = Dispatchers.Unconfined
            override val io: CoroutineDispatcher = Dispatchers.Unconfined
            override val default: CoroutineDispatcher = Dispatchers.Unconfined
            override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
        }

        viewModel = HomeViewModel(expenseRepository, testDispatcherProvider, mockLogger)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun simulateIncomingNotification(notification: NotificationData): Boolean {
        // Step A: Parse
        val parseResult = parserEngine.parseNotification(notification)
        if (parseResult is ParseResult.Success) {
            // Step B: Map to domain
            val expense = mapper.toExpense(parseResult.transaction)
            
            // Step C: Save to Repository/Database
            val saveResult = expenseRepository.insertExpense(expense)
            return saveResult is ResultWrapper.Success
        }
        return false
    }

    @Test
    fun testValidTransactionNotificationIntegrationFlow() = runBlocking {
        // Create an incoming GPay transaction notification
        val notification = NotificationData(
            packageName = NotificationSource.GOOGLE_PAY.packageName,
            title = "Google Pay",
            text = "You paid Rs 450 to Pizza Corner successful. UPI Ref: 998877665544",
            bigText = null,
            postTime = System.currentTimeMillis(),
            notificationId = 1001,
            notificationKey = "gpay_pizza_1001",
            tag = null,
            extras = emptyMap()
        )

        // Simulate delivery
        val processed = simulateIncomingNotification(notification)
        assertTrue("Notification should be processed successfully", processed)

        // Verify Data Stored Correctly in DB
        val dbCount = expenseDao.getExpensesCount()
        assertEquals(1, dbCount)
        val expenseInDb = expenseDao.getExpenseById("998877665544")
        assertNotNull(expenseInDb)
        assertEquals(450.0, expenseInDb!!.amount, 0.001)
        assertEquals("Pizza Corner", expenseInDb.merchantName)

        // Verify UI Reflects Data Reactively
        val state = viewModel.uiState.first { it !is HomeUiState.Loading }
        assertTrue("UI state should be Success", state is HomeUiState.Success)
        val successState = state as HomeUiState.Success
        assertEquals(1, successState.expenses.size)
        assertEquals(450.0, successState.totalSent, 0.001)
        assertEquals(0.0, successState.totalReceived, 0.001)
        assertEquals(-450.0, successState.netBalance, 0.001)
    }

    @Test
    fun testDuplicateTransactionNotificationIntegrationFlow() = runBlocking {
        duplicateDetector.clearCache()

        val notification = NotificationData(
            packageName = NotificationSource.PHONEPE.packageName,
            title = "PhonePe",
            text = "Paid Rs 300 to Kiran Stores. Txn 123456781234",
            bigText = null,
            postTime = System.currentTimeMillis(),
            notificationId = 1002,
            notificationKey = "phonepe_kiran_1002",
            tag = null,
            extras = emptyMap()
        )

        // First delivery
        val firstProcessed = simulateIncomingNotification(notification)
        assertTrue(firstProcessed)

        // Second delivery (Duplicate)
        val secondProcessed = simulateIncomingNotification(notification)
        assertFalse("Duplicate notification should be blocked by duplicate detector", secondProcessed)

        // Verify only 1 record stored in database
        val dbCount = expenseDao.getExpensesCount()
        assertEquals(1, dbCount)
    }

    @Test
    fun testNonTransactionNotificationIntegrationFlow() = runBlocking {
        // Non-transaction / Promotional message
        val promotionalNotification = NotificationData(
            packageName = NotificationSource.GOOGLE_PAY.packageName,
            title = "Google Pay Promotion",
            text = "Get flat 10% cash back on recharge of Rs. 199. Tap to claim your scratch card!",
            bigText = null,
            postTime = System.currentTimeMillis(),
            notificationId = 1003,
            notificationKey = "gpay_promo_1003",
            tag = null,
            extras = emptyMap()
        )

        val processed = simulateIncomingNotification(promotionalNotification)
        assertFalse("Promotional message should be ignored and not processed", processed)

        // Verify nothing stored in database
        val dbCount = expenseDao.getExpensesCount()
        assertEquals(0, dbCount)

        // Verify UI state remains Empty
        val state = viewModel.uiState.first { it !is HomeUiState.Loading }
        assertEquals(HomeUiState.Empty, state)
    }
}
