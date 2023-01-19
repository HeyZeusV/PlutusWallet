package com.heyzeusv.plutuswallet.ui.account

import com.heyzeusv.plutuswallet.InstantExecutorExtension
import com.heyzeusv.plutuswallet.TestCoroutineExtension
import com.heyzeusv.plutuswallet.data.DummyDataUtil
import com.heyzeusv.plutuswallet.data.FakeRepository
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.DataDialog
import com.heyzeusv.plutuswallet.ui.transaction.DataListSelectedAction.CREATE
import com.heyzeusv.plutuswallet.ui.transaction.DataListSelectedAction.DELETE
import com.heyzeusv.plutuswallet.ui.transaction.DataListSelectedAction.EDIT
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, TestCoroutineExtension::class)
internal class AccountViewModelTest {

    // test Fake
    private val repo = FakeRepository()

    // What is being tested
    private lateinit var accVM: AccountViewModel

    // dummy data
    private val dd = DummyDataUtil()

    @BeforeEach
    fun setUpViewModel() = runTest {
        // reset fake repo with dummy data and pass it to ViewModel
        repo.resetLists()
        accVM = AccountViewModel(repo)
        repo.accountListEmit(dd.accList)
        repo.accountsUsedListEmit(
            dd.accList.filter { acc -> dd.tranList.any { it.account == acc.name }}.distinct()
        )
    }

    @Test
    @DisplayName("Should have 2 lists at start up, a list of all Accounts " +
            "and a list of Accounts in use")
    fun viewModelInit() {
        val expectedAccountList = dd.accList.sortedBy { it.name }
        val expectedAccountsUsedList = listOf(dd.acc1, dd.acc2, dd.acc3)

        assertEquals(expectedAccountList, accVM.accountList.value)
        assertEquals(expectedAccountsUsedList, accVM.accountsUsedList.value)
    }

    @Test
    @DisplayName("Should delete Account from database and update showDialog")
    fun deleteAccount() = runTest {
        val expectedAccounts = listOf(dd.acc3, dd.acc1, dd.acc2)

        accVM.updateDialog(DataDialog(DELETE, 4))
        accVM.deleteAccount(dd.acc4)

        assertEquals(expectedAccounts, accVM.accountList.value)
        assertEquals(DataDialog(DELETE, -1), accVM.showDialog.value)
    }

    @Test
    @DisplayName("Should edit Account with a new name and update showDialog")
    fun editAccount() = runTest {
        val expectedAccounts = listOf( dd.acc3, dd.acc2, Account(1, "Test"), dd.acc4)
        val expectedAccountsUsed = listOf(Account(1, "Test"), dd.acc2, dd.acc3)

        accVM.updateDialog(DataDialog(EDIT, 1))
        accVM.editAccount(dd.acc1, "Test")

        assertEquals(expectedAccounts, accVM.accountList.value)
        assertEquals(expectedAccountsUsed, accVM.accountsUsedList.value)
        assertEquals(DataDialog(EDIT, -1), accVM.showDialog.value)
    }

    @Test
    @DisplayName("Should edit Account with an existing name which updates accountExists")
    fun editAccountExists() {
        accVM.editAccount(dd.acc1, dd.acc3.name)

        assertEquals(dd.acc3.name, accVM.accountExists.value)
    }

    @Test
    @DisplayName("Should create a new unique Account")
    fun createNewAccount() {
        val newAccount = Account(0, "Test")
        val expectedAccounts = listOf(dd.acc3, dd.acc1, dd.acc2, newAccount, dd.acc4)

        accVM.createNewAccount(newAccount.name)

        assert(repo.accList.contains(newAccount))
        assertEquals(expectedAccounts, accVM.accountList.value)
        assertEquals(DataDialog(CREATE, -1), accVM.showDialog.value)
    }

    @Test
    @DisplayName("Should insert Account with an existing name which updates accountExists")
    fun createNewAccountExists() {
        accVM.createNewAccount(dd.acc3.name)

        assertEquals(dd.acc3.name, accVM.accountExists.value)
    }
}