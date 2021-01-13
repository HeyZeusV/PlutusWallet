package com.heyzeusv.plutuswallet.ui.transaction

import androidx.lifecycle.MutableLiveData
import com.heyzeusv.plutuswallet.InstantExecutorExtension
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.FakeRepository
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.Transaction
import com.heyzeusv.plutuswallet.util.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.util.Date

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class)
internal class TransactionViewModelTest {

    // test Fake
    private lateinit var repo: FakeRepository

    // what is being tested
    private lateinit var tranVM: TransactionViewModel

    // dummy data
    private val acc1 = Account(1, "Credit Card")
    private val acc2 = Account(2, "Debit Card")
    private val acc3 = Account(3, "Cash")
    private val accList : MutableList<Account> = mutableListOf(acc1, acc2, acc3)

    private val cat1 = Category(1, "Food", "Expense")
    private val cat2 = Category(2, "Entertainment", "Expense")
    private val cat3 = Category(3, "Salary", "Income")
    private val catList : MutableList<Category> = mutableListOf(cat1, cat2, cat3)

    private val tran1 = Transaction(1, "Party", Date(86400000), BigDecimal("1000.10"), "Cash", "Expense", "Food", "", true, 1, 0, Date(86400000 * 2), true)
    private val tran2 = Transaction(2, "Party2", Date(86400000 * 2), BigDecimal("100.00"), "Cash", "Expense", "Food", "", true, 1, 0, Date(86400000 * 3), false)
    private val tran3 = Transaction(3, "Pay Day", Date(86400000 * 4), BigDecimal("2000.32"), "Debit Card", "Income", "Salary", "", true, 2, 1, Date(86400000 * 11), false)
    private val tran4 = Transaction(4, "Movie Date", Date(86400000 * 5), BigDecimal("55.45"), "Credit Card", "Expense", "Entertainment")
    private var tranList: MutableList<Transaction> = mutableListOf()

    @BeforeEach
    fun setUpViewModel() {

        // some function add/remove data, so want same data at start of every test.
        tranList = mutableListOf(tran1, tran2, tran3, tran4)

        // initialize fake repo with dummy data and pass it to ViewModel
        repo = FakeRepository(accList, catList, tranList)
        tranVM = TransactionViewModel(repo)
    }

    @Test
    @DisplayName("Should take Transaction given and pass its values to LiveData")
    fun setTranData() {

        tranVM.setTranData(tran1)

        assertEquals("Thursday, January 1, 1970", tranVM.date.value)
        assertEquals("Cash", tranVM.account.value)
        assertEquals("1 000 10", tranVM.total.value)
        assertEquals(R.id.tran_expense_chip, tranVM.checkedChip.value)
        assertEquals("Food", tranVM.expenseCat.value)
        assertEquals(true, tranVM.repeatCheck.value)
    }

    @Test
    @DisplayName("Should take LiveData values and pass them to Transaction, save it to Database, and cause saveEvent")
    fun saveTransaction() {

        tranVM.tranLD = MutableLiveData(tran1)
        tranVM.account.value = "Test Account"
        tranVM.total.value = "1000.99"
        tranVM.checkedChip.value = R.id.tran_income_chip
        tranVM.incomeCat.value = "Test Income Category"
        tranVM.repeatCheck.value = false
        val expectedTran = Transaction(
            1,
            "Party",
            Date(86400000),
            BigDecimal("1000.99"),
            "Test Account",
            "Income",
            "Test Income Category",
            "",
            false,
            1,
            0,
            Date(86400000 * 2),
            true
        )

        tranVM.saveTransaction("")
        val saveEvent: Event<Boolean> = tranVM.saveTranEvent.value!!

        assertEquals(expectedTran, tranVM.tranLD.value)
        assertEquals(expectedTran, repo.tranList[0])
        assertEquals(true, saveEvent.getContentIfNotHandled())
    }

    @Test
    @DisplayName("Should cause futureTranEvent when user tries saving a Transaction that has been repeated and date has been changed ")
    fun saveTransactionRepeatWarning() {

        tranVM.tranLD = MutableLiveData(tran1)
        // in order to get dateChanged == true
        tranVM.onDateSelected(Date(86400000 * 3))
        tranVM.account.value = "Test Account"
        tranVM.total.value = "1000.99"
        tranVM.checkedChip.value = R.id.tran_income_chip
        tranVM.incomeCat.value = "Test Income Category"
        tranVM.repeatCheck.value = true
        val expectedTran = Transaction(
            1,
            "Party",
            Date(86400000 * 3),
            BigDecimal("1000.99"),
            "Test Account",
            "Income",
            "Test Income Category",
            "",
            true,
            1,
            0,
            Date(86400000 * 4),
            true
        )

        tranVM.saveTransaction("")
        val futureEvent: Event<Transaction> = tranVM.futureTranEvent.value!!

        assertEquals(expectedTran, futureEvent.getContentIfNotHandled())
    }

    @Test
    @DisplayName("Should cause Transaction to recreate its future date, update it in Database, and cause saveEvent")
    fun futureTranPosFun() {

        val expectedTran: Transaction = tran1
        expectedTran.futureTCreated = false

        tranVM.futureTranPosFun(tran1)
        val saveEvent: Event<Boolean> = tranVM.saveTranEvent.value!!

        assertEquals(expectedTran, repo.tranList[0])
        assertEquals(true, saveEvent.getContentIfNotHandled())
    }

    @Test
    @DisplayName("Should update Transaction in Database, and cause saveEvent")
    fun futureTranNegFun() {

        tranVM.futureTranNegFun(tran1)
        val saveEvent: Event<Boolean> = tranVM.saveTranEvent.value!!

        assertEquals(tran1, repo.tranList[0])
        assertEquals(true, saveEvent.getContentIfNotHandled())
    }

    @Test
    @DisplayName("Should create new Account and add it to database")
    fun insertAccount() {

        tranVM.accountList.value = mutableListOf("Test1", "Test3", "")
        val expectedList: MutableList<String> = mutableListOf("Test1", "Test2", "Test3", "")
        val expectedAcc = Account(0, "Test2")

        tranVM.insertAccount("Test2", "")

        assertEquals(expectedList, tranVM.accountList.value)
        assertEquals(expectedAcc, repo.accList[repo.accList.size - 1])
        assertEquals("Test2", tranVM.account.value)

    }

    @Test
    @DisplayName("Should set account value to existing Account from list")
    fun insertAccountExists() {

        tranVM.accountList.value = mutableListOf("Test1", "Test2", "Test3", "")
        val expectedList: MutableList<String> = mutableListOf("Test1", "Test2", "Test3", "")
        val expectedAccRepoSize: Int = repo.accList.size

        tranVM.insertAccount("Test3", "")

        assertEquals(expectedList, tranVM.accountList.value)
        assertEquals("Test3", tranVM.account.value)
        assertEquals(expectedAccRepoSize, repo.accList.size)
    }

    @Test
    @DisplayName("Should create new Category and add it to database")
    fun insertCategory() {

        tranVM.expenseCatList.value = mutableListOf("ETest1", "ETest3", "")
        val expectedExList: MutableList<String> = mutableListOf("ETest1", "ETest2", "ETest3", "")
        val expectedExCat = Category(0, "ETest2", "Expense")
        tranVM.incomeCatList.value = mutableListOf("ITest1", "ITest3", "")
        val expectedInList: MutableList<String> = mutableListOf("ITest1", "ITest2", "ITest3", "")
        val expectedInCat = Category(0, "ITest2", "Income")

        tranVM.checkedChip.value = R.id.tran_expense_chip
        tranVM.insertCategory("ETest2", "")
        tranVM.checkedChip.value = R.id.tran_income_chip
        tranVM.insertCategory("ITest2", "")

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

        tranVM.expenseCatList.value = mutableListOf("ETest1", "ETest2", "ETest3", "")
        val expectedExList: MutableList<String> = mutableListOf("ETest1", "ETest2", "ETest3", "")
        tranVM.incomeCatList.value = mutableListOf("ITest1", "ITest2", "ITest3", "")
        val expectedInList: MutableList<String> = mutableListOf("ITest1", "ITest2", "ITest3", "")
        val expectedCatRepoSize: Int = repo.catList.size

        tranVM.checkedChip.value = R.id.tran_expense_chip
        tranVM.insertCategory("ETest2", "")
        tranVM.checkedChip.value = R.id.tran_income_chip
        tranVM.insertCategory("ITest2", "")

        assertEquals(expectedExList, tranVM.expenseCatList.value)
        assertEquals("ETest2", tranVM.expenseCat.value)
        assertEquals(expectedInList, tranVM.incomeCatList.value)
        assertEquals("ITest2", tranVM.incomeCat.value)
        assertEquals(expectedCatRepoSize, repo.catList.size)
    }

    @Test
    @DisplayName("Should create selectDateEvent containing Date when dateButton is pressed")
    fun selectDateOC() {

        tranVM.selectDateOC(Date(86400000))
        val selectDateEvent: Event<Date> = tranVM.selectDateEvent.value!!

        assertEquals(Date(86400000), selectDateEvent.getContentIfNotHandled())
    }

    @Test
    @DisplayName("Should set Transaction date to newly selected Date and format it to be displayed")
    fun onDateSelected() {

        tranVM.tranLD.value = tran2
        val expectedFormattedDate = "Saturday, January 3, 1970"

        tranVM.onDateSelected(Date(86400000*3))

        assertEquals(Date(86400000*3), tranVM.tranLD.value!!.date)
        assertEquals(expectedFormattedDate, tranVM.date.value)
    }

    @Test
    @DisplayName("Should retrieve lists of Accounts and Categories by type, add 'Create New...', and retrieve highest ID from Database")
    fun prepareLists() {

        val expectedAccList: MutableList<String> = mutableListOf("Cash", "Credit Card", "Debit Card", "Create New...")
        val expectedExCatList: MutableList<String> = mutableListOf("Entertainment", "Food", "Create New...")
        val expectedInCatList: MutableList<String> = mutableListOf("Salary", "Create New...")

        tranVM.prepareLists("Create New...", "Create New...")

        assertEquals(expectedAccList, tranVM.accountList.value)
        assertEquals(expectedExCatList, tranVM.expenseCatList.value)
        assertEquals(expectedInCatList, tranVM.incomeCatList.value)
    }
}