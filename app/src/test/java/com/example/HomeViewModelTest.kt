package com.example

import com.example.core.error.ErrorModel
import com.example.core.error.ResultWrapper
import com.example.core.log.Logger
import com.example.core.utils.DispatcherProvider
import com.example.domain.model.Expense
import com.example.domain.repository.ExpenseRepository
import com.example.presentation.screens.HomeUiState
import com.example.presentation.screens.HomeViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HomeViewModelTest {

    private lateinit var fakeRepository: FakeExpenseRepository
    private lateinit var testDispatcherProvider: DispatcherProvider
    private lateinit var viewModel: HomeViewModel

    private val mockLogger = object : Logger {
        override fun d(tag: String, message: String) {}
        override fun i(tag: String, message: String) {}
        override fun w(tag: String, message: String, throwable: Throwable?) {}
        override fun e(tag: String, message: String, throwable: Throwable?) {}
    }

    // A simple, reliable fake repository that runs deterministically on test threads
    private class FakeExpenseRepository : ExpenseRepository {
        val expensesFlow = MutableStateFlow<ResultWrapper<List<Expense>>>(ResultWrapper.Success(emptyList()))
        val insertedExpenses = mutableListOf<Expense>()

        override fun getExpenses(): Flow<ResultWrapper<List<Expense>>> = expensesFlow

        override suspend fun getExpenseById(id: String): ResultWrapper<Expense> {
            val item = insertedExpenses.find { it.id == id }
            return if (item != null) ResultWrapper.Success(item) else ResultWrapper.Error(ErrorModel("404", "Not found"))
        }

        override suspend fun insertExpense(expense: Expense): ResultWrapper<Unit> {
            insertedExpenses.add(expense)
            val currentList = (expensesFlow.value as? ResultWrapper.Success)?.data ?: emptyList()
            expensesFlow.value = ResultWrapper.Success(currentList + expense)
            return ResultWrapper.Success(Unit)
        }

        override suspend fun deleteExpense(id: String): ResultWrapper<Unit> {
            insertedExpenses.removeAll { it.id == id }
            val currentList = (expensesFlow.value as? ResultWrapper.Success)?.data ?: emptyList()
            expensesFlow.value = ResultWrapper.Success(currentList.filter { it.id != id })
            return ResultWrapper.Success(Unit)
        }

        override suspend fun clearAllExpenses(): ResultWrapper<Unit> {
            insertedExpenses.clear()
            expensesFlow.value = ResultWrapper.Success(emptyList())
            return ResultWrapper.Success(Unit)
        }
    }

    @Before
    fun setUp() {
        fakeRepository = FakeExpenseRepository()
        
        // Provide Unconfined Dispatcher to run everything synchronously
        testDispatcherProvider = object : DispatcherProvider {
            override val main: CoroutineDispatcher = Dispatchers.Unconfined
            override val io: CoroutineDispatcher = Dispatchers.Unconfined
            override val default: CoroutineDispatcher = Dispatchers.Unconfined
            override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
        }

        viewModel = HomeViewModel(fakeRepository, testDispatcherProvider, mockLogger)
    }

    @Test
    fun testInitialStateIsLoading() = runTest {
        // By using a separate flow or delayed repository emission, we can verify Loading state
        val delayedRepo = object : ExpenseRepository {
            private val flow = MutableStateFlow<ResultWrapper<List<Expense>>>(ResultWrapper.Success(emptyList()))
            override fun getExpenses(): Flow<ResultWrapper<List<Expense>>> = flow
            override suspend fun getExpenseById(id: String) = ResultWrapper.Error(ErrorModel("DELAYED", "Delayed"))
            override suspend fun insertExpense(expense: Expense) = ResultWrapper.Success(Unit)
            override suspend fun deleteExpense(id: String) = ResultWrapper.Success(Unit)
            override suspend fun clearAllExpenses() = ResultWrapper.Success(Unit)
        }
        
        val delayedViewModel = HomeViewModel(delayedRepo, testDispatcherProvider, mockLogger)
        assertEquals(HomeUiState.Loading, delayedViewModel.uiState.value)
    }

    @Test
    fun testEmptyState() = runTest {
        fakeRepository.expensesFlow.value = ResultWrapper.Success(emptyList())
        val state = viewModel.uiState.first()
        assertEquals(HomeUiState.Empty, state)
    }

    @Test
    fun testSuccessStateCalculation() = runTest {
        val now = System.currentTimeMillis()
        val e1 = Expense("1", 100.0, "Merchant 1", "DEBIT", Date(now), "ref1", "GPay", "Sms 1", "Food")
        val e2 = Expense("2", 150.0, "Merchant 2", "DEBIT", Date(now), "ref2", "GPay", "Sms 2", "Utilities")
        val e3 = Expense("3", 500.0, "Employer", "CREDIT", Date(now), "ref3", "Bank", "Sms 3", "Salary")

        fakeRepository.expensesFlow.value = ResultWrapper.Success(listOf(e1, e2, e3))

        val state = viewModel.uiState.first()
        assertTrue(state is HomeUiState.Success)
        
        val successState = state as HomeUiState.Success
        assertEquals(3, successState.expenses.size)
        assertEquals(250.0, successState.totalSent, 0.001)
        assertEquals(500.0, successState.totalReceived, 0.001)
        assertEquals(250.0, successState.netBalance, 0.001)
    }

    @Test
    fun testErrorState() = runTest {
        fakeRepository.expensesFlow.value = ResultWrapper.Error(ErrorModel("ERROR", "Connection failed"))
        
        val state = viewModel.uiState.first()
        assertTrue(state is HomeUiState.Error)
        assertEquals("Connection failed", (state as HomeUiState.Error).message)
    }

    @Test
    fun testDeleteTransaction() = runTest {
        val now = System.currentTimeMillis()
        val e = Expense("1", 100.0, "Merchant 1", "DEBIT", Date(now), "ref1", "GPay", "Sms 1", "Food")
        
        fakeRepository.insertExpense(e)
        viewModel.deleteTransaction("1")

        val state = viewModel.uiState.first()
        assertEquals(HomeUiState.Empty, state)
    }
}
