package com.heyzeusv.plutuswallet.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.heyzeusv.plutuswallet.data.daos.AccountDao
import com.heyzeusv.plutuswallet.data.daos.CategoryDao
import com.heyzeusv.plutuswallet.data.daos.TransactionDao
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
import java.time.ZoneId.systemDefault
import java.time.ZonedDateTime
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
            val expected = listOf(dd.acc3, dd.acc1, dd.acc2)

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
            val expected = listOf(dd.acc3, dd.acc1, dd.acc2, dd.acc4)

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
            val expected = listOf(dd.cat2, dd.cat1)

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
            val expected = listOf(dd.cat2, dd.cat1, dd.cat4)

            assertEquals(expected, catDao.getCategoriesByType("Expense").first())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Nested
    @DisplayName("Transaction Queries")
    inner class TransactionQueries {

        @Test
        @DisplayName("List of all Transactions with futureDate before Date given and " +
                "futureTCreated is false.")
        fun getFutureTransactions() = runTest {
            val expected : List<Transaction> = listOf(dd.tran2)

            assertEquals(
                expected,
                tranDao.getFutureTransactions(
                    ZonedDateTime.of(1980, 1, 14, 1, 0, 0, 0, systemDefault())
                )
            )
        }

        @Test
        @DisplayName("Highest id in table or null if empty")
        fun getMaxId() = runTest {
            assertEquals(4, tranDao.getMaxId().first())
        }

        @Test
        @DisplayName("Transaction with given id")
        fun getTransaction() = runTest {

            assertEquals(dd.tran3, tranDao.getTransaction(3))
        }

        @Nested
        @DisplayName("CategoryTotals Queries")
        inner class CategoryTotalsQueries {

            @Test
            @DisplayName("StateFlow that emits list of CT w/ non-zero total")
            fun getCt() = runTest {
                val expected: List<CategoryTotals> = listOf(
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
                    listOf(
                        CategoryTotals("Salary", BigDecimal("2000.32"), "Income")
                    )
                
                assertEquals(
                    expected, 
                    tranDao.getCtAD(
                        listOf("Debit Card"),
                        ZonedDateTime.of(1980, 1, 13, 1, 0, 0, 0, systemDefault()),
                        ZonedDateTime.of(1980, 1, 14, 1, 0, 0, 0, systemDefault())
                    ).first()
                )
            }

            @Test
            @DisplayName("StateFlow that emits list of CT of given account, given categories," +
                    " and between given dates")
            fun getCtACD() = runTest {
                val expected: List<CategoryTotals> =
                    listOf(
                        CategoryTotals("Salary", BigDecimal("2000.32"), "Income")
                    )

                assertEquals(
                    expected,
                    tranDao.getCtACD(
                        listOf("Debit Card"),
                        "Income", listOf("Salary"),
                        ZonedDateTime.of(1980, 1, 13, 1, 0, 0, 0, systemDefault()),
                        ZonedDateTime.of(1980, 1, 14, 1, 0, 0, 0, systemDefault())
                    ).first()
                )
            }

            @Test
            @DisplayName("StateFlow that emits list of CT of given categories")
            fun getCtC() = runTest {
                val expected: List<CategoryTotals> =
                    listOf(
                        CategoryTotals("Salary", BigDecimal("2000.32"), "Income")
                    )

                assertEquals(expected, tranDao.getCtC("Income", listOf("Salary")).first())
            }

            @Test
            @DisplayName("StateFlow that emits list of CT of given categories, " +
                    "and between given dates")
            fun getCtCD() = runTest {
                val expected: List<CategoryTotals> =
                    listOf(
                        CategoryTotals("Salary", BigDecimal("2000.32"), "Income")
                    )

                assertEquals(
                    expected,
                    tranDao.getCtCD(
                        "Income",
                        listOf("Salary"),
                        ZonedDateTime.of(1980, 1, 13, 1, 0, 0, 0, systemDefault()),
                        ZonedDateTime.of(1980, 1, 14, 1, 0, 0, 0, systemDefault())
                    ).first()
                )
            }

            @Test
            @DisplayName("StateFlow that emits list of CT between given dates")
            fun getCtD() = runTest {
                val expected: List<CategoryTotals> =
                    listOf(CategoryTotals("Food", BigDecimal("100.1"), "Expense"))

                assertEquals(
                    expected,
                    tranDao.getCtD(
                        ZonedDateTime.of(1980, 1, 9, 1, 0, 0, 0, systemDefault()),
                        ZonedDateTime.of(1980, 1, 10, 1, 0, 0, 0, systemDefault())
                    ).first())
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
                val expected = listOf(dd.tli1, dd.tli2, dd.tli3, dd.tli4)

                assertEquals(expected, tranDao.getTli().first())
            }

            @Test
            @DisplayName("StateFlow that emits list of TLI of given account")
            fun getTliA() = runTest {
                val expected = listOf(dd.tli1, dd.tli2)

                assertEquals(expected, tranDao.getTliA(listOf("Cash")).first())
            }

            @Test
            @DisplayName("StateFlow that emits list of TLI of given account and between given dates")
            fun getTliAD() = runTest {
                val expected = listOf(dd.tli2)

                assertEquals(
                    expected,
                    tranDao.getTliAD(
                        listOf("Cash"),
                        ZonedDateTime.of(1980, 1, 10, 1, 0, 0, 0, systemDefault()),
                        ZonedDateTime.of(1980, 1, 11, 1, 0, 0, 0, systemDefault())
                    ).first()
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
                val expected = listOf(dd.tli1, dd.tli2)

                assertEquals(
                    expected,
                    tranDao.getTliATC(listOf("Cash"), "Expense", listOf("Food")).first()
                )
            }

            @Test
            @DisplayName("StateFlow that emits list of TLI of given account, type, " +
                    "and between given dates")
            fun getTliATD() = runTest {
                val expected = listOf(dd.tli2)

                assertEquals(
                    expected,
                    tranDao.getTliATD(
                        listOf("Cash"),
                        "Expense",
                        ZonedDateTime.of(1980, 1, 10, 1, 0, 0, 0, systemDefault()),
                        ZonedDateTime.of(1980, 1, 11, 1, 0, 0, 0, systemDefault())
                    ).first()
                )
            }

            @Test
            @DisplayName("StateFlow that emits list of TLI of given account, type, category, " +
                    "and between given dates")
            fun getTliATCD() = runTest {
                val expected = listOf(dd.tli2)

                assertEquals(
                    expected,
                    tranDao.getTliATCD(
                        listOf("Cash"),
                        "Expense",
                        listOf("Food"),
                        ZonedDateTime.of(1980, 1, 10, 1, 0, 0, 0, systemDefault()),
                        ZonedDateTime.of(1980, 1, 11, 1, 0, 0, 0, systemDefault())
                    ).first()
                )
            }

            @Test
            @DisplayName("StateFlow that emits list of TLI between given dates")
            fun getTliD() = runTest {
                val expected = listOf(dd.tli3, dd.tli4)

                assertEquals(
                    expected,
                    tranDao.getTliD(
                        ZonedDateTime.of(1980, 1, 13, 1, 0, 0, 0, systemDefault()),
                        ZonedDateTime.of(1980, 1, 16, 1, 0, 0, 0, systemDefault())
                    ).first()
                )
            }

            @Test
            @DisplayName("StateFlow that emits list of TLI of given type")
            fun getTliT() = runTest {
                val expected = listOf(dd.tli3)

                assertEquals(expected, tranDao.getTliT("Income").first())
            }

            @Test
            @DisplayName("StateFlow that emits list of TLI of given type and category")
            fun getTliTC() = runTest {
                val expected = listOf(dd.tli3)

                assertEquals(expected, tranDao.getTliTC("Income", listOf("Salary")).first())
            }

            @Test
            @DisplayName("StateFlow that emits list of TLI of given type, category, " +
                    "and between given dates")
            fun getTliTCD() = runTest {
                val expected = listOf(dd.tli3)

                assertEquals(
                    expected,
                    tranDao.getTliTCD(
                        "Income",
                        listOf("Salary"),
                        ZonedDateTime.of(1980, 1, 13, 1, 0, 0, 0, systemDefault()),
                        ZonedDateTime.of(1980, 1, 16, 1, 0, 0, 0, systemDefault())
                    ).first()
                )
            }

            @Test
            @DisplayName("StateFlow that emits list of TLI of given type and between given dates")
            fun getTliTD() = runTest {
                val expected = listOf(dd.tli1, dd.tli2, dd.tli4)
                assertEquals(
                    expected,
                    tranDao.getTliTD(
                        "Expense",
                        ZonedDateTime.of(1980, 1, 9, 1, 0, 0, 0, systemDefault()),
                        ZonedDateTime.of(1980, 1, 16, 1, 0, 0, 0, systemDefault())
                    ).first()
                )
            }
        }
    }

    companion object {

        private lateinit var db : PWDatabase

        private lateinit var accDao  : AccountDao
        private lateinit var catDao  : CategoryDao
        private lateinit var tranDao : TransactionDao

        val dd = DummyAndroidDataUtil()

        @BeforeAll
        @JvmStatic
        fun createDb() {

            val context : Context = ApplicationProvider.getApplicationContext()
            db = Room.inMemoryDatabaseBuilder(context, PWDatabase::class.java).build()
            accDao  = db.accountDao()
            catDao  = db.categoryDao()
            tranDao = db.transactionDao()

            runBlocking {

                accDao .insert(dd.accList)
                catDao .insert(dd.catList)
                tranDao.insert(dd.tranList)
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

