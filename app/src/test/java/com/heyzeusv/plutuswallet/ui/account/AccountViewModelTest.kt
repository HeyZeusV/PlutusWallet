package com.heyzeusv.plutuswallet.ui.account

import com.heyzeusv.plutuswallet.InstantExecutorExtension
import com.heyzeusv.plutuswallet.data.FakeRepository
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.Transaction
import com.heyzeusv.plutuswallet.util.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.util.Date

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class)
internal class AccountViewModelTest {

    // test Fake
    private lateinit var repo: FakeRepository

    // What is being tested
    private lateinit var accVM: AccountViewModel

    // dummy data
    private var accList: MutableList<Account> = mutableListOf()
    private var catList: MutableList<Category> = mutableListOf()
    private var tranList: MutableList<Transaction> = mutableListOf()

    private val acc1 = Account(1, "Credit Card")
    private val acc2 = Account(2, "Debit Card")
    private val acc3 = Account(3, "Cash")
    private val acc4 = Account(4, "Unused")

    private val tran1 = Transaction(1, "Party", Date(86400000), BigDecimal("100.10"), "Cash", "Expense", "Food", "", true, 1, 0, Date(86400000 * 2), true)
    private val tran2 = Transaction(2, "Party2", Date(86400000 * 2), BigDecimal("100.00"), "Cash", "Expense", "Food", "", true, 1, 0, Date(86400000 * 3), false)
    private val tran3 = Transaction(3, "Pay Day", Date(86400000 * 4), BigDecimal("2000.32"), "Debit Card", "Income", "Salary", "", true, 2, 1, Date(86400000 * 11), false)
    private val tran4 = Transaction(4, "Movie Date", Date(86400000 * 5), BigDecimal("55.45"), "Credit Card", "Expense", "Entertainment")

    @BeforeEach
    fun setUpViewModel() {

        // some function add/remove data, so want same data at start of every test.
        accList = mutableListOf(acc1, acc2, acc3, acc4)
        tranList = mutableListOf(tran1, tran2, tran3, tran4)

        // initialize fake repo with dummy data and pass it to ViewModel
        repo = FakeRepository(accList, catList, tranList)
        accVM = AccountViewModel(repo)
    }

    @Test
    @DisplayName("Should create edit Event containing Account when edit button is pressed")
    fun editAccountOC() {

        val acc = Account(0, "Test")

        accVM.editAccountOC(acc)
        val value: Event<Account> = accVM.editAccountEvent.value!!

        assertEquals(value.getContentIfNotHandled(), acc)
    }

    @Test
    @DisplayName("Should create delete Event containing Account when delete button is pressed")
    fun deleteAccountOC() {

        val acc = Account(0, "Test")

        accVM.deleteAccountOC(acc)
        val value: Event<Account> = accVM.deleteAccountEvent.value!!

        assertEquals(value.getContentIfNotHandled(), acc)
    }

    @Test
    @DisplayName("Should initialize list containing all Account names and Accounts being used")
    fun initNamesUsedLists() {

        val expectedNames: MutableList<String> = mutableListOf("Cash", "Credit Card", "Debit Card", "Unused")
        val expectedUsed: MutableList<String> = mutableListOf("Cash", "Credit Card", "Debit Card")

        runBlockingTest {
            accVM.initNamesUsedLists()
        }

        assertEquals(expectedNames, accVM.accountNames)
        assertEquals(expectedUsed, accVM.accountsUsed)
    }

    @Test
    @DisplayName("Should delete Account from lists and database")
    fun deleteAccountPosFun() {

        val expectedNames: MutableList<String> = mutableListOf("Cash", "Debit Card", "Unused")
        val expectedUsed: MutableList<String> = mutableListOf("Cash", "Debit Card")
        accVM.accountNames = mutableListOf("Cash", "Credit Card", "Debit Card", "Unused")
        accVM.accountsUsed = mutableListOf("Cash", "Credit Card", "Debit Card")

        accVM.deleteAccountPosFun(acc1)

        assertEquals(expectedNames, accVM.accountNames)
        assertEquals(expectedUsed, accVM.accountsUsed)
        assertEquals(mutableListOf(acc2, acc3, acc4), repo.accList)
    }

    @Test
    @DisplayName("Should edit Account with a new name")
    fun editAccountName() {

        val expectedNames: MutableList<String> = mutableListOf("Cash", "Test", "Debit Card", "Unused")
        val expectedUsed: MutableList<String> = mutableListOf("Cash", "Test", "Debit Card")
        accVM.accountNames = mutableListOf("Cash", "Credit Card", "Debit Card", "Unused")
        accVM.accountsUsed = mutableListOf("Cash", "Credit Card", "Debit Card")

        accVM.editAccountName(acc1, "Test")

        assertEquals(expectedNames, accVM.accountNames)
        assertEquals(expectedUsed, accVM.accountsUsed)
        assertEquals(Account(1, "Test"), repo.accList[0])
    }

    @Test
    @DisplayName("Should edit Account with an existing name which creates an exists Event")
    fun editAccountNameExists() {

        accVM.accountNames = mutableListOf("Cash", "Credit Card", "Debit Card", "Unused")

        accVM.editAccountName(acc1, "Cash")
        val value: Event<String> = accVM.existsAccountEvent.value!!

        assertEquals(value.getContentIfNotHandled(), "Cash")
    }

    @Test
    @DisplayName("Should insert a new unique Account")
    fun insertNewAccount() {

        val newAccount = Account(100, "Test")

        accVM.insertNewAccount(newAccount, "Test")

        assert(repo.accList.contains(newAccount))
        assertEquals(mutableListOf("Test"), accVM.accountNames)
    }

    @Test
    @DisplayName("Should insert Account with an existing name which creates an exist Event")
    fun insertNewAccountExists() {

        val newAccount = Account(100, "Cash")
        accVM.accountNames = mutableListOf("Cash", "Credit Card", "Debit Card", "Unused")

        accVM.insertNewAccount(newAccount, "Cash")
        val value: Event<String> = accVM.existsAccountEvent.value!!

        assertEquals(value.getContentIfNotHandled(), "Cash")
    }
}