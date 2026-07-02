package com.example.data.repository

import com.example.core.log.Logger
import com.example.domain.model.NotificationData
import com.example.domain.model.ParseResult
import com.example.domain.repository.NotificationDispatcher
import com.example.domain.repository.NotificationRepository
import com.example.domain.repository.ExpenseRepository
import com.example.domain.parser.ParserEngine
import com.example.domain.parser.TransactionMapper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class NotificationDispatcherImpl(
    private val notificationRepository: NotificationRepository,
    private val logger: Logger,
    private val parserEngine: ParserEngine? = null,
    private val transactionMapper: TransactionMapper? = null,
    private val expenseRepository: ExpenseRepository? = null
) : NotificationDispatcher {

    private val _notificationFlow = MutableSharedFlow<NotificationData>(replay = 10)
    val notificationFlow: SharedFlow<NotificationData> = _notificationFlow.asSharedFlow()

    override suspend fun dispatch(notificationData: NotificationData) {
        logger.i("NotificationDispatcher", "Dispatching notification from ${notificationData.source.displayName}: ID=${notificationData.notificationId}")
        
        _notificationFlow.emit(notificationData)

        val displayText = "[Captured Notification - ${notificationData.source.displayName}] " +
                "Title: ${notificationData.title} | Text: ${notificationData.text}"
        notificationRepository.saveProcessedNotificationText(displayText)

        // Run parser engine and persist parsed expense if available
        if (parserEngine != null && transactionMapper != null && expenseRepository != null) {
            try {
                when (val result = parserEngine.parseNotification(notificationData)) {
                    is ParseResult.Success -> {
                        val parsedTx = result.transaction
                        val expense = transactionMapper.toExpense(parsedTx)
                        val insertResult = expenseRepository.insertExpense(expense)
                        logger.i("NotificationDispatcher", "Successfully parsed & auto-persisted UPI expense: Rs. ${expense.amount} to ${expense.merchantName}, Result: $insertResult")
                    }
                    is ParseResult.Ignored -> {
                        logger.d("NotificationDispatcher", "Notification ignored by parsing engine: ${result.reason}")
                    }
                    is ParseResult.Invalid -> {
                        logger.w("NotificationDispatcher", "Notification invalid for transaction parsing: ${result.reason}")
                    }
                    is ParseResult.Failed -> {
                        logger.e("NotificationDispatcher", "Parsing failed for notification: ${result.reason}", result.error)
                    }
                }
            } catch (e: Exception) {
                logger.e("NotificationDispatcher", "Error processing notification through parser pipeline", e)
            }
        }
    }
}
