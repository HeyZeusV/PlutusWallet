package com.heyzeusv.plutuswallet.ui.account

import com.heyzeusv.plutuswallet.DummyDataUtil
import com.heyzeusv.plutuswallet.InstantExecutorExtension
import com.heyzeusv.plutuswallet.data.FakeRepository
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.util.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class)
internal class AccountViewModelTest {

    // test Fake
    private lateinit var repo: FakeRepository

    // What is being tested
    private lateinit var accVM: AccountViewModel

    // dummy data
    private lateinit var dd: DummyDataUtil

    @BeforeEach
    fun setUpViewModel() {

        // some function add/remove data, so want same data at start of every test.
        dd = DummyDataUtil()

        // initialize fake repo with dummy data and pass it to ViewModel
        repo = FakeRepository(dd.accList, dd.catList, dd.tranList)
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

        accVM.deleteAccountPosFun(dd.acc1)

        assertEquals(expectedNames, accVM.accountNames)
        assertEquals(expectedUsed, accVM.accountsUsed)
        assertEquals(mutableListOf(dd.acc2, dd.acc3, dd.acc4), repo.accList)
    }

    @Test
    @DisplayName("Should edit Account with a new name")
    fun editAccountName() {

        val expectedNames: MutableList<String> = mutableListOf("Cash", "Test", "Debit Card", "Unused")
        val expectedUsed: MutableList<String> = mutableListOf("Cash", "Test", "Debit Card")
        accVM.accountNames = mutableListOf("Cash", "Credit Card", "Debit Card", "Unused")
        accVM.accountsUsed = mutableListOf("Cash", "Credit Card", "Debit Card")

        accVM.editAccountName(dd.acc1, "Test")

        assertEquals(expectedNames, accVM.accountNames)
        assertEquals(expectedUsed, accVM.accountsUsed)
        assertEquals(Account(1, "Test"), repo.accList[0])
    }

    @Test
    @DisplayName("Should edit Account with an existing name which creates an exists Event")
    fun editAccountNameExists() {

        accVM.accountNames = mutableListOf("Cash", "Credit Card", "Debit Card", "Unused")

        accVM.editAccountName(dd.acc1, "Cash")
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