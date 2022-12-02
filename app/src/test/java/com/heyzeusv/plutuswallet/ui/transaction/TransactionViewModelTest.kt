package com.heyzeusv.plutuswallet.ui.transaction

import com.heyzeusv.plutuswallet.InstantExecutorExtension
import com.heyzeusv.plutuswallet.TestCoroutineExtension
import com.heyzeusv.plutuswallet.data.DummyDataUtil
import com.heyzeusv.plutuswallet.data.FakeRepository
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.SettingsValues
import com.heyzeusv.plutuswallet.data.model.Transaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.util.Date
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
    fun setUpViewModel() {

        // reset fake repo with dummy data and pass it to ViewModel
        repo.resetLists()
        tranVM = TransactionViewModel(repo, SettingsValues())
        tranVM.updatePeriodList(mutableListOf("Days", "Weeks", "Months", "Years"))
    }

    @Test
    @DisplayName("Should take Transaction given and pass its values to StateFlow")
    fun setTranData() {
        tranVM.setTranData(dd.tran1)

        assertEquals("Thursday, January 1, 1970", tranVM.date.value)
        assertEquals("Cash", tranVM.account.value)
        assertEquals("$1,000.10", tranVM.totalFieldValue.value.text)
        assertEquals(TransactionType.EXPENSE, tranVM.typeSelected.value)
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
        tranVM.updateTypeSelected(TransactionType.INCOME)
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
        tranVM.updateTypeSelected(TransactionType.INCOME)
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
    fun insertAccount() {
        val expectedList: MutableList<String> = mutableListOf("Test1", "Test2", "Test3", "Create")
        val expectedAcc = Account(0, "Test2")

        tranVM.updateAccountList(mutableListOf("Test1", "Test3", "Create"))

        tranVM.insertAccount("Test2", "Create")

        assertEquals(expectedList, tranVM.accountList.value)
        assertEquals(expectedAcc, repo.accList[repo.accList.size - 1])
        assertEquals("Test2", tranVM.account.value)
    }

    @Test
    @DisplayName("Should set account value to existing Account from list")
    fun insertAccountExists() {
        val expectedList: MutableList<String> = mutableListOf("Test1", "Test2", "Test3", "Create")
        val expectedListSize: Int = repo.accList.size

        tranVM.updateAccountList(mutableListOf("Test1", "Test2", "Test3", "Create"))

        tranVM.insertAccount("Test3", "Create")

        assertEquals(expectedList, tranVM.accountList.value)
        assertEquals(expectedListSize, repo.accList.size)
        assertEquals("Test3", tranVM.account.value)
    }

    @Test
    @DisplayName("Should create new Category and add it to database")
    fun insertCategory() {
        val expectedExList: MutableList<String> = mutableListOf("ETest1", "ETest2", "ETest3", "Create")
        val expectedExCat = Category(0, "ETest2", "Expense")
        val expectedInList: MutableList<String> = mutableListOf("ITest1", "ITest2", "ITest3", "Create")
        val expectedInCat = Category(0, "ITest2", "Income")

        tranVM.updateExpenseCatList(mutableListOf("ETest1", "ETest3", "Create"))
        tranVM.updateIncomeCatList(mutableListOf("ITest1", "ITest3", "Create"))

        tranVM.updateTypeSelected(TransactionType.EXPENSE)
        tranVM.insertCategory("ETest2", "Create")
        tranVM.updateTypeSelected(TransactionType.INCOME)
        tranVM.insertCategory("ITest2", "Create")

        assertEquals(expectedExList, tranVM.expenseCatList.value)
        assertEquals(expectedExCat, repo.catList[repo.catList.size - 2])
        assertEquals("ETest2", tranVM.expenseCat.value)
        assertEquals(expectedInList, tranVM.incomeCatList.value)
        assertEquals(expectedInCat, repo.catList[repo.catList.size - 1])
        assertEquals("ITest2", tranVM.incomeCat.value)
    }

    @Test
    @DisplayName("Should set category value to existing Category from list")
    fun insertCategoryExists() {
        val expectedExList: MutableList<String> = mutableListOf("ETest1", "ETest2", "ETest3", "Create")
        val expectedInList: MutableList<String> = mutableListOf("ITest1", "ITest2", "ITest3", "Create")
        val expectedCatRepoSize: Int = repo.catList.size

        tranVM.updateExpenseCatList(mutableListOf("ETest1", "ETest2", "ETest3", "Create"))
        tranVM.updateIncomeCatList(mutableListOf("ITest1", "ITest2", "ITest3", "Create"))

        tranVM.updateTypeSelected(TransactionType.EXPENSE)
        tranVM.insertCategory("ETest2", "Create")
        tranVM.updateTypeSelected(TransactionType.INCOME)
        tranVM.insertCategory("ITest2", "Create")

        assertEquals(expectedExList, tranVM.expenseCatList.value)
        assertEquals("ETest2", tranVM.expenseCat.value)
        assertEquals(expectedInList, tranVM.incomeCatList.value)
        assertEquals("ITest2", tranVM.incomeCat.value)
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
        val expectedAccList = mutableListOf("Cash", "Credit Card", "Debit Card", "Unused", "Create")
        val expectedExCatList = mutableListOf("Entertainment", "Food", "Unused Expense", "Create")
        val expectedInCatList = mutableListOf("Salary", "Unused Income", "Zelle", "Create")

        tranVM.prepareLists("Create", "Create")

        assertEquals(expectedAccList, tranVM.accountList.value)
        assertEquals(expectedExCatList, tranVM.expenseCatList.value)
        assertEquals(expectedInCatList, tranVM.incomeCatList.value)
    }
}