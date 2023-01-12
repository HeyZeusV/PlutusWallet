package com.heyzeusv.plutuswallet.ui.transaction

import com.heyzeusv.plutuswallet.InstantExecutorExtension
import com.heyzeusv.plutuswallet.TestCoroutineExtension
import com.heyzeusv.plutuswallet.data.DummyDataUtil
import com.heyzeusv.plutuswallet.data.FakeRepository
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.SettingsValues
import com.heyzeusv.plutuswallet.data.model.Transaction
import com.heyzeusv.plutuswallet.ui.transaction.TransactionType.EXPENSE
import com.heyzeusv.plutuswallet.ui.transaction.TransactionType.INCOME
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.util.Date
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, TestCoroutineExtension::class)
internal class TransactionViewModelTest {

    // test Fake
    private val repo = FakeRepository()

    // what is being tested
    private lateinit var tranVM: TransactionViewModel

    // dummy data
    private val dd = DummyDataUtil()

    @BeforeEach
    fun setUpViewModel() = runTest {
        // reset fake repo with dummy data and pass it to ViewModel
        repo.resetLists()
        tranVM = TransactionViewModel(repo, SettingsValues())
        tranVM.updatePeriodList(mutableListOf("Days", "Weeks", "Months", "Years"))
        repo.accountNameListEmit(dd.accList.map { it.name })
        repo.expenseCatNameListEmit(dd.catList.filter { it.type == EXPENSE.type }.map { it.name })
        repo.incomeCatNameListEmit(dd.catList.filter { it.type == INCOME.type }.map { it.name })
    }

    @Test
    @DisplayName("Should correctly launch ViewModel with correct data required in its init block")
    fun viewModelInit() {
        assertEquals(dd.accList.map { it.name }.sorted() + "Create New Account", tranVM.accountList.value)
        assertEquals(
            dd.catList.filter { it.type == EXPENSE.type }.map { it.name }.sorted() + "Create New Category",
            tranVM.expenseCatList.value
        )
        assertEquals(
            dd.catList.filter { it.type == INCOME.type }.map { it.name }.sorted() + "Create New Category",
            tranVM.incomeCatList.value
        )
    }

    @Test
    @DisplayName("Should take Transaction given and pass its values to StateFlow")
    fun setTranData() {
        tranVM.setTranData(dd.tran1)

        assertEquals("Thursday, January 1, 1970", tranVM.date.value)
        assertEquals("Cash", tranVM.account.value)
        assertEquals("$1,000.10", tranVM.totalFieldValue.value.text)
        assertEquals(EXPENSE, tranVM.typeSelected.value)
        assertEquals("Food", tranVM.expenseCat.value)
        assertEquals(true, tranVM.repeat.value)
    }

    @Test
    @DisplayName("Should take StateFlow values and pass them to Transaction and save to Database")
    fun saveTransaction() {
        val expectedTran = Transaction(
            1,
            "Party",
            Date(86400000),
            BigDecimal("1000.99"),
            "Test Account",
            "Income",
            "Test Income Category",
            "Catering for party",
            false,
            1,
            0,
            Date(86400000 * 2),
            true
        )

        tranVM.retrieveTransaction(dd.tran1.id)
        tranVM.updateAccount(expectedTran.account)
        tranVM.updateTotalFieldValue(expectedTran.total.toString())
        tranVM.updateTypeSelected(INCOME)
        tranVM.updateIncomeCat(expectedTran.category)
        tranVM.updateRepeat(expectedTran.repeating)
        tranVM.updatePeriod("Days")

        tranVM.saveTransaction()

        assertEquals(expectedTran, tranVM.transaction.value)
        assertEquals(expectedTran, repo.tranList[0])
    }

    @Test
    @DisplayName("Should show future dialog when user tries saving a Transaction that has" +
            " been repeated and date has been changed ")
    fun saveTransactionRepeatWarning() {
        val expectedTran = Transaction(
            1,
            "Party",
            Date(86400000 * 3),
            BigDecimal("1000.99"),
            "Test Account",
            "Income",
            "Test Income Category",
            "Catering for party",
            true,
            1,
            0,
            Date(86400000 * 4),
            true
        )

        tranVM.retrieveTransaction(dd.tran1.id)

        // in order to get dateChanged == true
        tranVM.onDateSelected(expectedTran.date)
        tranVM.updateAccount(expectedTran.account)
        tranVM.updateTotalFieldValue(expectedTran.total.toString())
        tranVM.updateTypeSelected(INCOME)
        tranVM.updateIncomeCat(expectedTran.category)
        tranVM.updateRepeat(expectedTran.repeating)
        tranVM.updatePeriod("Days")

        tranVM.saveTransaction()

        assertEquals(true, tranVM.showFutureDialog.value)
    }

    @Test
    @DisplayName("Should cause Transaction to recreate its future date and update it in Database" +
            "when confirming future dialog")
    fun futureDialogConfirm() {
        val expectedTran: Transaction = dd.tran1
        expectedTran.futureTCreated = false

        tranVM.retrieveTransaction(dd.tran1.id)
        tranVM.futureDialogConfirm()

        assertEquals(expectedTran, repo.tranList[0])
        assertEquals(true, tranVM.saveSuccess.value)
        assertEquals(false, tranVM.showFutureDialog.value)
    }

    @Test
    @DisplayName("Should update Transaction in Database when dismissing future dialog")
    fun futureDialogDismiss() {
        tranVM.retrieveTransaction(dd.tran1.id)
        tranVM.futureDialogDismiss()

        assertEquals(dd.tran1, repo.tranList[0])
        assertEquals(true, tranVM.saveSuccess.value)
        assertEquals(false, tranVM.showFutureDialog.value)
    }

    @Test
    @DisplayName("Should create new Account and add it to database")
    fun insertAccount() = runTest {
        val expectedList =
            mutableListOf("Cash", "Credit Card", "Debit Card", "Test", "Unused", "Create New Account")
        val expectedAcc = Account(0, "Test")

        tranVM.insertAccount("Test")

        assertEquals(expectedList, tranVM.accountList.value)
        assertEquals(expectedAcc, repo.accList[repo.accList.size - 1])
        assertEquals("Test", tranVM.account.value)
    }

    @Test
    @DisplayName("Should set account value to existing Account from list")
    fun insertAccountExists() {
        val expectedList =
            mutableListOf("Cash", "Credit Card", "Debit Card", "Unused", "Create New Account")
        val expectedListSize: Int = repo.accList.size

        tranVM.insertAccount("Debit Card")

        assertEquals(expectedList, tranVM.accountList.value)
        assertEquals(expectedListSize, repo.accList.size)
        assertEquals("Debit Card", tranVM.account.value)
    }

    @Test
    @DisplayName("Should create new Category and add it to database")
    fun insertCategory() {
        val expectedExList =
            mutableListOf("ETest", "Entertainment", "Food", "Unused Expense", "Create New Category")
        val expectedExCat = Category(0, "ETest", "Expense")
        val expectedInList =
            mutableListOf("ITest", "Salary", "Unused Income", "Zelle", "Create New Category")
        val expectedInCat = Category(0, "ITest", "Income")

        tranVM.updateTypeSelected(EXPENSE)
        tranVM.insertCategory("ETest")
        tranVM.updateTypeSelected(INCOME)
        tranVM.insertCategory("ITest")

        assertEquals(expectedExList, tranVM.expenseCatList.value)
        assertEquals(expectedExCat, repo.catList[repo.catList.size - 2])
        assertEquals("ETest", tranVM.expenseCat.value)
        assertEquals(expectedInList, tranVM.incomeCatList.value)
        assertEquals(expectedInCat, repo.catList[repo.catList.size - 1])
        assertEquals("ITest", tranVM.incomeCat.value)
    }

    @Test
    @DisplayName("Should set category value to existing Category from list")
    fun insertCategoryExists() {
        val expectedExList =
            mutableListOf("Entertainment", "Food", "Unused Expense", "Create New Category")
        val expectedInList = mutableListOf("Salary", "Unused Income", "Zelle", "Create New Category")
        val expectedCatRepoSize: Int = repo.catList.size

        tranVM.updateTypeSelected(EXPENSE)
        tranVM.insertCategory("Food")
        tranVM.updateTypeSelected(INCOME)
        tranVM.insertCategory("Zelle")

        assertEquals(expectedExList, tranVM.expenseCatList.value)
        assertEquals("Food", tranVM.expenseCat.value)
        assertEquals(expectedInList, tranVM.incomeCatList.value)
        assertEquals("Zelle", tranVM.incomeCat.value)
        assertEquals(expectedCatRepoSize, repo.catList.size)
    }

    @Test
    @DisplayName("Should set Transaction date to newly selected Date and format it to be displayed")
    fun onDateSelected() {
        val expectedFormattedDate = "Saturday, January 3, 1970"

        tranVM.retrieveTransaction(dd.tran2.id)
        tranVM.onDateSelected(Date(86400000*3))

        assertEquals(Date(86400000*3), tranVM.transaction.value.date)
        assertEquals(expectedFormattedDate, tranVM.date.value)
    }

    @Test
    @DisplayName("Should retrieve lists of Accounts and Categories by type, add 'Create'," +
            " and retrieve highest ID from Database")
    fun prepareLists() {
        val expectedAccList = mutableListOf("Cash", "Credit Card", "Debit Card", "Unused", "Create New Account")
        val expectedExCatList = mutableListOf("Entertainment", "Food", "Unused Expense", "Create New Category")
        val expectedInCatList = mutableListOf("Salary", "Unused Income", "Zelle", "Create New Category")

        assertEquals(expectedAccList, tranVM.accountList.value)
        assertEquals(expectedExCatList, tranVM.expenseCatList.value)
        assertEquals(expectedInCatList, tranVM.incomeCatList.value)
    }
}