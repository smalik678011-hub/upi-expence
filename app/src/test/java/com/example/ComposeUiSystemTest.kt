package com.example

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.core.error.ErrorModel
import com.example.core.error.ResultWrapper
import com.example.domain.model.Expense
import com.example.domain.repository.AppPreferencesRepository
import com.example.domain.repository.ExpenseRepository
import com.example.presentation.screens.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class ComposeUiSystemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUpDispatcher() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDownDispatcher() {
        Dispatchers.resetMain()
    }

    // -----------------------------------------------------------------
    // FAKES
    // -----------------------------------------------------------------
    
    private class FakeExpenseRepository(
        val expensesFlow: MutableStateFlow<ResultWrapper<List<Expense>>>
    ) : ExpenseRepository {
        override fun getExpenses(): Flow<ResultWrapper<List<Expense>>> = expensesFlow
        override suspend fun getExpenseById(id: String) = ResultWrapper.Error(ErrorModel("500", "Not implemented"))
        override suspend fun insertExpense(expense: Expense) = ResultWrapper.Success(Unit)
        override suspend fun deleteExpense(id: String) = ResultWrapper.Success(Unit)
        override suspend fun clearAllExpenses() = ResultWrapper.Success(Unit)
    }

    private class FakeAppPreferencesRepository : AppPreferencesRepository {
        override val darkThemePreference: Flow<Boolean?> = flowOf(null)
        override val isDynamicColorEnabled: Flow<Boolean> = flowOf(true)
        override val isFirstLaunch: Flow<Boolean> = flowOf(false)
        override val isPermissionTutorialShown: Flow<Boolean> = flowOf(true)
        override val isDeveloperModeEnabled: Flow<Boolean> = flowOf(false)
        override val languagePreference: Flow<String> = flowOf("EN")

        override suspend fun setDarkThemePreference(enabled: Boolean?) {}
        override suspend fun setDynamicColorEnabled(enabled: Boolean) {}
        override suspend fun setFirstLaunchCompleted(completed: Boolean) {}
        override suspend fun setPermissionTutorialShown(shown: Boolean) {}
        override suspend fun setDeveloperModeEnabled(enabled: Boolean) {}
        override suspend fun setLanguagePreference(language: String) {}
        override suspend fun clearAllPreferences() {}
    }

    // -----------------------------------------------------------------
    // TESTS
    // -----------------------------------------------------------------

    @Test
    fun testTransactionExplorerEmptyStateUi() {
        val expensesFlow = MutableStateFlow<ResultWrapper<List<Expense>>>(ResultWrapper.Success(emptyList()))
        val fakeRepo = FakeExpenseRepository(expensesFlow)
        val viewModel = TransactionExplorerViewModel(fakeRepo)

        composeTestRule.setContent {
            MyApplicationTheme {
                TransactionExplorerScreen(
                    viewModel = viewModel,
                    onBack = {},
                    onNavigateToDetails = {}
                )
            }
        }

        // Advance coroutine clock past the 300ms debounce threshold
        testDispatcher.scheduler.advanceTimeBy(400)

        // Wait until UI handles loading and transitions to empty state
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("explorer_empty_title").fetchSemanticsNodes().isNotEmpty()
        }

        // Verify Search Input exists
        composeTestRule.onNodeWithTag("search_input").assertExists()

        // Verify that empty title and description are rendered
        composeTestRule.onNodeWithTag("explorer_empty_title").assertExists()
        composeTestRule.onNodeWithTag("explorer_empty_desc").assertExists()
    }

    @Test
    fun testTransactionExplorerRendersListAndSearchAndFilter() {
        val now = System.currentTimeMillis()
        val expenses = listOf(
            Expense("e1", 150.0, "Burger King", "DEBIT", Date(now), "ref_bk", "Google Pay", "Paid 150", "Food"),
            Expense("e2", 2000.0, "Salary Credited", "CREDIT", Date(now - 5000), "ref_sal", "HDFC Bank", "Credited 2000", "Salary"),
            Expense("e3", 45.5, "Tea Stall", "DEBIT", Date(now - 10000), "ref_tea", "PhonePe", "Paid 45.5", "Food")
        )
        val expensesFlow = MutableStateFlow<ResultWrapper<List<Expense>>>(ResultWrapper.Success(expenses))
        val fakeRepo = FakeExpenseRepository(expensesFlow)
        val viewModel = TransactionExplorerViewModel(fakeRepo)

        composeTestRule.setContent {
            MyApplicationTheme {
                TransactionExplorerScreen(
                    viewModel = viewModel,
                    onBack = {},
                    onNavigateToDetails = {}
                )
            }
        }

        // Advance coroutine clock past the 300ms debounce threshold
        testDispatcher.scheduler.advanceTimeBy(400)

        // Wait until UI transitions from loading to displaying list
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("explorer_transaction_list").fetchSemanticsNodes().isNotEmpty()
        }

        // Verify List view exists
        composeTestRule.onNodeWithTag("explorer_transaction_list").assertExists()

        // Verify list items are displayed
        composeTestRule.onNodeWithTag("transaction_item_e1").assertExists()
        composeTestRule.onNodeWithTag("transaction_item_e2").assertExists()

        // Verify Search Input is interactive and accepts search string
        composeTestRule.onNodeWithTag("search_input")
            .assertExists()
            .performTextInput("Burger")

        // Advance time for search text debounce
        testDispatcher.scheduler.advanceTimeBy(400)

        // Filter button
        composeTestRule.onNodeWithTag("btn_all_filters").assertExists().performClick()
    }

    @Test
    fun testPermissionScreenLayoutAndInteractions() {
        val fakePrefs = FakeAppPreferencesRepository()
        val viewModel = PermissionViewModel(fakePrefs)

        composeTestRule.setContent {
            MyApplicationTheme {
                PermissionScreen(
                    viewModel = viewModel,
                    onNavigateToHome = {}
                )
            }
        }

        // Scaffold / Root container
        composeTestRule.onNodeWithTag("permission_screen_scaffold").assertExists()

        // Status Card showing required/granted details
        composeTestRule.onNodeWithTag("permission_status_card").assertExists()

        // Action Buttons
        composeTestRule.onNodeWithTag("grant_permission_button").assertExists().assertHasClickAction()
        composeTestRule.onNodeWithTag("refresh_status_button").assertExists().assertHasClickAction()
    }
}
