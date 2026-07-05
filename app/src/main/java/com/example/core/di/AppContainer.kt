package com.example.core.di

import android.content.Context
import com.example.core.log.AndroidLogger
import com.example.core.log.Logger
import com.example.core.security.SecureStorage
import com.example.core.security.SharedPreferencesSecureStorage
import com.example.core.utils.DefaultDispatcherProvider
import com.example.core.utils.DispatcherProvider
import com.example.core.notification.NotificationLogger
import com.example.data.repository.ExpenseRepositoryImpl
import com.example.data.repository.NotificationDispatcherImpl
import com.example.data.repository.NotificationFilterImpl
import com.example.data.repository.NotificationRepositoryImpl
import com.example.data.repository.NotificationValidatorImpl
import com.example.data.repository.SettingsRepositoryImpl
import com.example.domain.repository.ExpenseRepository
import com.example.domain.repository.NotificationDispatcher
import com.example.domain.repository.NotificationFilter
import com.example.domain.repository.NotificationRepository
import com.example.domain.repository.NotificationValidator
import com.example.domain.repository.SettingsRepository

/**
 * AppContainer holds all dependency singletons for the application.
 * This provides high performance, instant compile times, and complete immunity
 * to AGP version breaks, while matching the exact modular layout of Dagger Hilt.
 */
class AppContainer(private val context: Context) {

    val logger: Logger by lazy {
        AndroidLogger()
    }

    val dispatcherProvider: DispatcherProvider by lazy {
        DefaultDispatcherProvider()
    }

    val secureStorage: SecureStorage by lazy {
        SharedPreferencesSecureStorage(context)
    }

    val databaseEncryptionManager: com.example.core.security.DatabaseEncryptionManager by lazy {
        com.example.core.security.DatabaseEncryptionManager(secureStorage)
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(secureStorage)
    }

    val appPreferencesRepository: com.example.domain.repository.AppPreferencesRepository by lazy {
        com.example.data.repository.AppPreferencesRepositoryImpl(context)
    }

    val appDatabase: com.example.data.database.AppDatabase by lazy {
        androidx.room.Room.databaseBuilder(
            context,
            com.example.data.database.AppDatabase::class.java,
            "upi_expenses_db"
        ).fallbackToDestructiveMigration().build()
    }

    val expenseDao: com.example.data.database.dao.ExpenseDao by lazy {
        appDatabase.expenseDao()
    }

    val expenseRepository: ExpenseRepository by lazy {
        ExpenseRepositoryImpl(expenseDao)
    }

    val notificationRepository: NotificationRepository by lazy {
        NotificationRepositoryImpl()
    }

    val notificationFilter: NotificationFilter by lazy {
        NotificationFilterImpl()
    }

    val notificationValidator: NotificationValidator by lazy {
        NotificationValidatorImpl(notificationFilter)
    }

    val notificationDispatcher: NotificationDispatcher by lazy {
        NotificationDispatcherImpl(
            notificationRepository = notificationRepository,
            parserEngine = parserEngine,
            transactionMapper = transactionMapper,
            expenseRepository = expenseRepository,
            logger = logger
        )
    }

    val notificationLogger: NotificationLogger by lazy {
        NotificationLogger(logger)
    }

    val notificationNormalizer: com.example.domain.parser.NotificationNormalizer by lazy {
        com.example.data.parser.NotificationNormalizerImpl()
    }

    val regexRuleProvider: com.example.domain.parser.RegexRuleProvider by lazy {
        com.example.data.parser.RegexRuleProviderImpl()
    }

    val keywordRuleProvider: com.example.domain.parser.KeywordRuleProvider by lazy {
        com.example.data.parser.KeywordRuleProviderImpl()
    }

    val parserLogger: com.example.domain.parser.ParserLogger by lazy {
        com.example.data.parser.ParserLoggerImpl(logger)
    }

    val transactionValidator: com.example.domain.parser.TransactionValidator by lazy {
        com.example.data.parser.TransactionValidatorImpl(keywordRuleProvider)
    }

    val duplicateDetector: com.example.domain.parser.DuplicateDetector by lazy {
        com.example.data.parser.DuplicateDetectorImpl()
    }

    val parserFactory: com.example.domain.parser.ParserFactory by lazy {
        com.example.data.parser.ParserFactoryImpl(
            normalizer = notificationNormalizer,
            regexRuleProvider = regexRuleProvider,
            keywordRuleProvider = keywordRuleProvider,
            validator = transactionValidator
        )
    }

    val parserRegistry: com.example.domain.parser.ParserRegistry by lazy {
        com.example.data.parser.ParserRegistryImpl()
    }

    val parserEngine: com.example.domain.parser.ParserEngine by lazy {
        com.example.data.parser.ParserEngineImpl(
            registry = parserRegistry,
            factory = parserFactory,
            duplicateDetector = duplicateDetector,
            logger = parserLogger
        )
    }

    val transactionMapper: com.example.domain.parser.TransactionMapper by lazy {
        com.example.data.parser.TransactionMapperImpl()
    }

    val exportRepository: com.example.domain.repository.ExportRepository by lazy {
        com.example.data.repository.ExportRepositoryImpl(expenseDao)
    }

    val analyticsRepository: com.example.domain.repository.AnalyticsRepository by lazy {
        com.example.data.repository.AnalyticsRepositoryImpl(expenseDao)
    }

    val backupRestoreRepository: com.example.domain.repository.BackupRestoreRepository by lazy {
        com.example.data.repository.BackupRestoreRepositoryImpl(context, appDatabase)
    }

    val adRepository: com.example.domain.repository.AdRepository by lazy {
        com.example.data.repository.AdRepositoryImpl(context)
    }

    val adManager: com.example.core.admob.AdManager by lazy {
        com.example.core.admob.AdManager(context, adRepository, logger)
    }
}
