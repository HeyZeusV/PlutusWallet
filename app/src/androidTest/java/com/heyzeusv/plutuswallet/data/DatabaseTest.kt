package com.heyzeusv.plutuswallet.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.heyzeusv.plutuswallet.data.daos.AccountDao
import com.heyzeusv.plutuswallet.data.daos.CategoryDao
import com.heyzeusv.plutuswallet.data.daos.TransactionDao
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.CategoryTotals
import com.heyzeusv.plutuswallet.data.model.TranListItem
import com.heyzeusv.plutuswallet.data.model.Transaction
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.math.BigDecimal
import java.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

/**
 *  Testing queries in DAOs, no testing done on Insert/Update/Delete since those are provided.
 *  CT  = CategoryTotals
 *  TLI = TranListItem
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class DatabaseTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @OptIn(ExperimentalCoroutinesApi::class)
    @Nested
    @DisplayName("Account Queries")
    inner class AccountQueries {

        @Test
        @DisplayName("List of Account names in alphabetical order")
        fun getAccountNames() = runTest {
            val expected = listOf("Cash", "Credit Card", "Debit Card", "Unused Account")

            assertEquals(expected, accDao.getAccountNames().first())
        }

        @Test
        @DisplayName("StateFlow that emits list of Accounts in use")
        fun getAccountsUsed() = runTest {
            val expected = listOf(acc3, acc1, acc2)

            assertEquals(expected, accDao.getAccountsUsed().first())
        }

        @Test
        @DisplayName("Size of table")
        fun getAccountSize() = runTest {
            assertEquals(4, accDao.getAccountSize())
        }

        @Test
        @DisplayName("StateFlow that emits list of all Accounts in order of name")
        fun getAccounts() = runTest {
            val expected = listOf(acc3, acc1, acc2, acc4)

            assertEquals(expected, accDao.getAccounts().first())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Nested
    @DisplayName("Category Queries")
    inner class CategoryQueries {

        @Test
        @DisplayName("List of Category names of type in alphabetical order")
        fun getCategoryNamesByType() = runTest {
            val expected : MutableList<String> =
                mutableListOf("Entertainment", "Food", "Unused Expense")

            assertEquals(expected, catDao.getCategoryNamesByType("Expense").first())
        }

        @Test
        @DisplayName("StateFlow that emits list of Categories in use by type")
        fun getCategoriesUsedByType() = runTest {
            val expected = listOf(cat2, cat1)

            assertEquals(expected, catDao.getCategoriesUsedByType("Expense").first())
        }


        @Test
        @DisplayName("Size of table")
        fun getCategorySize() = runTest {

            assertEquals(4, catDao.getCategorySize())
        }

        @Test
        @DisplayName("StateFlow that emits list that holds all Categories of type in order of name")
        fun getCategoriesByType() = runTest {
            val expected = listOf(cat2, cat1, cat4)

            assertEquals(expected, catDao.getCategoriesByType("Expense").first())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Nested
    @DisplayName("Transaction Queries")
    inner class TransactionQueries {

        @Test
        @DisplayName("List of all Transactions with futureDate before Date given and futureTCreated is false.")
        fun getFutureTransactions() = runTest {
            val expected : List<Transaction> = listOf(tran2)

            assertEquals(expected, tranDao.getFutureTransactions(Date(86400000 * 4)))
        }

        @Test
        @DisplayName("Highest id in table or null if empty")
        fun getMaxId() = runTest {
            assertEquals(4, tranDao.getMaxId().first())
        }

        @Test
        @DisplayName("Transaction with given id")
        fun getTransaction() = runTest {

            assertEquals(tran3, tranDao.getTransaction(3))
        }

        @Nested
        @DisplayName("CategoryTotals Queries")
        inner class CategoryTotalsQueries {

            @Test
            @DisplayName("StateFlow that emits list of CT w/ non-zero total")
            fun getCt() = runTest {
                val expected: List<CategoryTotals> =
                    listOf(
                        CategoryTotals("Entertainment", BigDecimal("55.45"), "Expense"),
                        CategoryTotals("Food", BigDecimal("200.1"), "Expense"),
                        CategoryTotals("Salary", BigDecimal("2000.32"), "Income")
                    )

                assertEquals(expected, tranDao.getCt().first())
            }

            @Test
            @DisplayName("StateFlow that emits list of CT of given account w/ non-zero total")
            fun getCtA() = runTest {
                val expected: List<CategoryTotals> =
                    listOf(CategoryTotals("Food", BigDecimal("200.1"), "Expense"))

                assertEquals(expected, tranDao.getCtA(listOf("Cash")).first())
            }

            @Test
            @DisplayName("StateFlow that emits list of CT of given account and between given dates")
            fun getCtAD() = runTest {
                val expected: List<CategoryTotals> =
                    listOf(CategoryTotals("Salary", BigDecimal("2000.32"), "Income"))
                
                assertEquals(
                    expected, 
                    tranDao.getCtAD(listOf("Debit Card"), Date(86400000 * 3), Date(86400010 * 4))
                        .first()
                )
            }

            @Test
            @DisplayName("StateFlow that emits list of CT of given account, given categories," +
                    " and between given dates")
            fun getCtACD() = runTest {
                val expected: List<CategoryTotals> =
                    listOf(CategoryTotals("Salary", BigDecimal("2000.32"), "Income"))

                assertEquals(
                    expected,
                    tranDao.getCtACD(
                        listOf("Debit Card"), "Income", listOf("Salary"),
                        Date(86400000 * 3), Date(86400010 * 4)).first()
                )
            }

            @Test
            @DisplayName("StateFlow that emits list of CT of given categories")
            fun getCtC() = runTest {
                val expected: List<CategoryTotals> =
                    listOf(CategoryTotals("Salary", BigDecimal("2000.32"), "Income"))

                assertEquals(expected, tranDao.getCtC("Income", listOf("Salary")).first())
            }

            @Test
            @DisplayName("StateFlow that emits list of CT of given categories, " +
                    "and between given dates")
            fun getCtCD() = runTest {
                val expected: List<CategoryTotals> =
                    listOf(CategoryTotals("Salary", BigDecimal("2000.32"), "Income"))

                assertEquals(
                    expected,
                    tranDao.getCtCD(
                        "Income", listOf("Salary"), Date(86400000 * 3), Date(86400010 * 4)
                    ).first()
                )
            }

            @Test
            @DisplayName("StateFlow that emits list of CT between given dates")
            fun getCtD() = runTest {
                val expected: List<CategoryTotals> =
                    listOf(CategoryTotals("Food", BigDecimal("100.1"), "Expense"))

                assertEquals(expected, tranDao.getCtD(Date(0), Date(86400010)).first())
            }
        }

        @Nested
        @DisplayName("TLI Queries")
        inner class TLIQueries {

            /**
             *  All of these queries are ordered by date.
             */
            @Test
            @DisplayName("StateFlow that emits list of TLI")
            fun getTli() = runTest {
                val expected = listOf(tli1, tli2, tli3, tli4)

                assertEquals(expected, tranDao.getTli().first())
            }

            @Test
            @DisplayName("StateFlow that emits list of TLI of given account")
            fun getTliA() = runTest {
                val expected = listOf(tli1, tli2)

                assertEquals(expected, tranDao.getTliA(listOf("Cash")).first())
            }

            @Test
            @DisplayName("StateFlow that emits list of TLI of given account and between given dates")
            fun getTliAD() = runTest {
                val expected = listOf(tli2)

                assertEquals(
                    expected,
                    tranDao.getTliAD(listOf("Cash"), Date(86400010), Date(86400010 * 2)).first()
                )
            }

            @Test
            @DisplayName("StateFlow that emits list of TLI of given account and type")
            fun getTliAT() = runTest {
                assertEquals(
                    emptyList<TranListItem>(),
                    tranDao.getTliAT(listOf("Cash"), "Income").first()
                )
            }

            @Test
            @DisplayName("StateFlow that emits list of TLI of given account, type, and category")
            fun getTliATC() = runTest {
                val expected = listOf(tli1, tli2)

                assertEquals(
                    expected,
                    tranDao.getTliATC(listOf("Cash"), "Expense", listOf("Food")).first()
                )
            }

            @Test
            @DisplayName("StateFlow that emits list of TLI of given account, type, " +
                    "and between given dates")
            fun getTliATD() = runTest {
                val expected = listOf(tli2)

                assertEquals(
                    expected,
                    tranDao.getTliATD(
                        listOf("Cash"), "Expense", Date(86400010), Date(86400010 * 2)
                    ).first()
                )
            }

            @Test
            @DisplayName("StateFlow that emits list of TLI of given account, type, category, " +
                    "and between given dates")
            fun getTliATCD() = runTest {
                val expected = listOf(tli2)

                assertEquals(
                    expected,
                    tranDao.getTliATCD(
                        listOf("Cash"), "Expense", listOf("Food"),
                        Date(86400010), Date(86400010 * 2)
                    ).first()
                )
            }

            @Test
            @DisplayName("StateFlow that emits list of TLI between given dates")
            fun getTliD() = runTest {
                val expected = listOf(tli3, tli4)

                assertEquals(
                    expected,
                    tranDao.getTliD(Date(86400010 * 3), Date(86400010 * 6)).first()
                )
            }

            @Test
            @DisplayName("StateFlow that emits list of TLI of given type")
            fun getTliT() = runTest {
                val expected = listOf(tli3)

                assertEquals(expected, tranDao.getTliT("Income").first())
            }

            @Test
            @DisplayName("StateFlow that emits list of TLI of given type and category")
            fun getTliTC() = runTest {
                val expected = listOf(tli3)

                assertEquals(expected, tranDao.getTliTC("Income", listOf("Salary")).first())
            }

            @Test
            @DisplayName("StateFlow that emits list of TLI of given type, category, " +
                    "and between given dates")
            fun getTliTCD() = runTest {
                val expected = listOf(tli3)

                assertEquals(
                    expected,
                    tranDao.getTliTCD(
                        "Income", listOf("Salary"),
                        Date(86400010 * 3), Date(86400010 * 6)
                    ).first()
                )
            }

            @Test
            @DisplayName("StateFlow that emits list of TLI of given type and between given dates")
            fun getTliTD() = runTest {
                val expected = listOf(tli1, tli2, tli4)

                assertEquals(
                    expected,
                    tranDao.getTliTD("Expense", Date(0), Date(86400010 * 6)).first()
                )
            }
        }
    }

    companion object {

        private lateinit var db : TransactionDatabase

        private lateinit var accDao  : AccountDao
        private lateinit var catDao  : CategoryDao
        private lateinit var tranDao : TransactionDao

        private val acc1 = Account(1, "Credit Card")
        private val acc2 = Account(2, "Debit Card")
        private val acc3 = Account(3, "Cash")
        private val acc4 = Account(4, "Unused Account")
        private val accList : List<Account> = listOf(acc1, acc2, acc3, acc4)

        private val cat1 = Category(1, "Food", "Expense")
        private val cat2 = Category(2, "Entertainment", "Expense")
        private val cat3 = Category(3, "Salary", "Income")
        private val cat4 = Category(4, "Unused Expense", "Expense")
        private val catList : List<Category> = listOf(cat1, cat2, cat3, cat4)

        private val tran1 = Transaction(1, "Party", Date(86400000), BigDecimal("100.10"), "Cash", "Expense", "Food", "", true, 1, 0, Date(86400000 * 2), true)
        private val tran2 = Transaction(2, "Party2", Date(86400000 * 2), BigDecimal("100.00"), "Cash", "Expense", "Food", "", true, 1, 0, Date(86400000 * 3), false)
        private val tran3 = Transaction(3, "Pay Day", Date(86400000 * 4), BigDecimal("2000.32"), "Debit Card", "Income", "Salary", "", true, 2, 1, Date(86400000 * 11), false)
        private val tran4 = Transaction(4, "Movie Date", Date(86400000 * 5), BigDecimal("55.45"), "Credit Card", "Expense", "Entertainment")
        private val tranList : List<Transaction> = listOf(tran1, tran2, tran3, tran4)

        private val tli1 = TranListItem(1, "Party", Date(86400000), BigDecimal("100.10"), "Cash", "Expense", "Food")
        private val tli2 = TranListItem(2, "Party2", Date(86400000 * 2), BigDecimal("100.00"), "Cash", "Expense", "Food")
        private val tli3 = TranListItem(3, "Pay Day", Date(86400000 * 4), BigDecimal("2000.32"), "Debit Card", "Income", "Salary")
        private val tli4 = TranListItem(4, "Movie Date", Date(86400000 * 5), BigDecimal("55.45"), "Credit Card", "Expense", "Entertainment")

        @BeforeAll
        @JvmStatic
        fun createDb() {

            val context : Context = ApplicationProvider.getApplicationContext()
            db = Room.inMemoryDatabaseBuilder(context, TransactionDatabase::class.java).build()
            accDao  = db.accountDao()
            catDao  = db.categoryDao()
            tranDao = db.transactionDao()

            runBlocking {

                accDao .insert(accList)
                catDao .insert(catList)
                tranDao.insert(tranList)
            }
        }

        @AfterAll
        @JvmStatic
        @Throws(IOException::class)
        fun closeDb() {

            db.close()
        }
    }
}

