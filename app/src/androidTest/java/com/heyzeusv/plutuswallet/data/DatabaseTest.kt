package com.heyzeusv.plutuswallet.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.heyzeusv.plutuswallet.data.daos.AccountDao
import com.heyzeusv.plutuswallet.data.daos.CategoryDao
import com.heyzeusv.plutuswallet.data.daos.TransactionDao
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.CategoryTotals
import com.heyzeusv.plutuswallet.data.model.ItemViewTransaction
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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 *  Testing queries in DAOs, no testing done on Insert/Update/Delete since those are provided.
 *  LD  = LiveData
 *  CT  = CategoryTotals
 *  IVT = ItemViewTransaction
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class DatabaseTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Nested
    @DisplayName("Account Queries")
    inner class AccountQueries {

        @Test
        @DisplayName("List of Account names in alphabetical order")
        fun getAccountNames() {

            val expected : MutableList<String> = mutableListOf("Cash", "Credit Card", "Debit Card")
            assertEquals(expected, runBlocking { accDao.getAccountNames()  })
        }

        @Test
        @DisplayName("Size of table")
        fun getAccountSize() {

            assertEquals(3, runBlocking { accDao.getAccountSize() })
        }

        @Test
        @DisplayName("LD of list of all Accounts in order of name")
        fun getLDAccounts() {

            val ldAccList : List<Account> = accDao.getLDAccounts().blockingObserve()!!
            assertEquals(listOf(acc3, acc1, acc2), ldAccList)
        }
    }

    @Nested
    @DisplayName("Category Queries")
    inner class CategoryQueries {

        @Test
        @DisplayName("List of Category names of type in alphabetical order")
        fun getCategoryNamesByType() {

            val expected : MutableList<String> = mutableListOf("Entertainment", "Food")
            assertEquals(expected, runBlocking { catDao.getCategoryNamesByType("Expense") })
        }

        @Test
        @DisplayName("Size of table")
        fun getAccountSize() {

            assertEquals(3, runBlocking { catDao.getCategorySize() })
        }

        @Test
        @DisplayName("LD of list that holds all Categories of type in order of name")
        fun getLDCategoriesByType() {

            val ldCatList : List<Category> = catDao.getLDCategoriesByType("Expense").blockingObserve()!!
            assertEquals(listOf(cat2, cat1), ldCatList)
        }
    }

    @Nested
    @DisplayName("Transaction Queries")
    inner class TransactionQueries {

        @Test
        @DisplayName("List of unique Accounts")
        fun getDistinctAccounts() {

            val expected : List<String> = listOf("Cash", "Credit Card", "Debit Card")
            assertEquals(expected, runBlocking { tranDao.getDistinctAccounts() })
        }

        @Test
        @DisplayName("List of unique Categories of type")
        fun getDistinctCatsByType() {

            val expected : List<String> = listOf("Entertainment", "Food")
            assertEquals(expected, runBlocking { tranDao.getDistinctCatsByType("Expense") })
        }

        @Test
        @DisplayName("List of all Transactions with futureDate before Date given and futureTCreated is false.")
        fun getFutureTransactions() {

            val expected : List<Transaction> = listOf(tran2)
            assertEquals(expected, runBlocking { tranDao.getFutureTransactions(Date(86400000 * 4)) })
        }

        @Test
        @DisplayName("Highest id in table or null if empty")
        fun getMaxId() {

            assertEquals(4, runBlocking { tranDao.getMaxId() })
        }

        @Test
        @DisplayName("Transaction with given id")
        fun getTransaction() {

            assertEquals(tran3, runBlocking { tranDao.getTransaction(3) })
        }

        @Test
        @DisplayName("LD of Transaction with given id")
        fun getLDTransaction() {

            val tran : Transaction = tranDao.getLDTransaction(2).blockingObserve()!!
            assertEquals(tran2, tran)
        }

        @Nested
        @DisplayName("CategoryTotals Queries")
        inner class CategoryTotalsQueries {

            @Test
            @DisplayName("LD of list of CT w/ non-zero total")
            fun getLdCt() {

                val expected: List<CategoryTotals> =
                    listOf(
                        CategoryTotals("Entertainment", BigDecimal("55.45"), "Expense"),
                        CategoryTotals("Food", BigDecimal("200.1"), "Expense"),
                        CategoryTotals("Salary", BigDecimal("2000.32"), "Income")
                    )
                assertEquals(expected, tranDao.getLdCt().blockingObserve()!!)
            }

            @Test
            @DisplayName("LD of list of CT of given account w/ non-zero total")
            fun getLdCtA() {

                val expected: List<CategoryTotals> =
                    listOf(CategoryTotals("Food", BigDecimal("200.1"), "Expense"))
                assertEquals(expected, tranDao.getLdCtA("Cash").blockingObserve()!!)
            }

            @Test
            @DisplayName("LD of list of CT between given dates")
            fun getLdCtD() {

                val expected: List<CategoryTotals> =
                    listOf(CategoryTotals("Food", BigDecimal("100.1"), "Expense"))
                assertEquals(
                    expected,
                    tranDao.getLdCtD(Date(0), Date(86400010)).blockingObserve()!!
                )
            }

            @Test
            @DisplayName("LD of list of CT of given account and between given dates")
            fun getLdCtAD() {

                val expected: List<CategoryTotals> =
                    listOf(CategoryTotals("Salary", BigDecimal("2000.32"), "Income"))
                assertEquals(
                    expected, tranDao.getLdCtAD(
                        "Debit Card", Date(86400000 * 3),
                        Date(86400010 * 4)
                    ).blockingObserve()!!
                )
            }
        }

        @Nested
        @DisplayName("IVT Queries")
        inner class IVTQueries {

            /**
             *  All of these queries are ordered by date.
             */

            @Test
            @DisplayName("LD of list of IVT")
            fun getLdIvt() {

                assertEquals(listOf(ivt1, ivt2, ivt3, ivt4), tranDao.getLdIvt().blockingObserve())
            }

            @Test
            @DisplayName("LD of list of IVT of given account")
            fun getLdIvtA() {

                assertEquals(listOf(ivt1, ivt2), tranDao.getLdIvtA("Cash").blockingObserve())
            }

            @Test
            @DisplayName("LD of list of IVT of given account and between given dates")
            fun getLdIvtAD() {

                assertEquals(listOf(ivt2),
                    tranDao.getLdIvtAD("Cash", Date(86400010), Date(86400010 * 2)).blockingObserve())
            }

            @Test
            @DisplayName("LD of list of IVT of given account and type")
            fun getLdIvtAT() {

                assertEquals(emptyList<ItemViewTransaction>(),
                    tranDao.getLdIvtAT("Cash", "Income").blockingObserve())
            }

            @Test
            @DisplayName("LD of list of IVT of given account, type, and category")
            fun getLdIvtATC() {

                assertEquals(listOf(ivt1, ivt2),
                    tranDao.getLdIvtATC("Cash", "Expense", "Food").blockingObserve())
            }

            @Test
            @DisplayName("LD of list of IVT of given account, type, and between given dates")
            fun getLdIvtATD() {

                assertEquals(listOf(ivt2), tranDao.getLdIvtATD("Cash", "Expense",
                    Date(86400010), Date(86400010 * 2)).blockingObserve())
            }

            @Test
            @DisplayName("LD of list of IVT of given account, type, category, and between given dates")
            fun getLdIvtATCD() {

                assertEquals(listOf(ivt2), tranDao.getLdIvtATCD("Cash", "Expense", "Food",
                    Date(86400010), Date(86400010 * 2)).blockingObserve())
            }

            @Test
            @DisplayName("LD of list of IVT between given dates")
            fun getLdIvtD() {

                assertEquals(listOf(ivt3, ivt4),
                    tranDao.getLdIvtD(Date(86400010 * 3), Date(86400010 * 6)).blockingObserve())
            }

            @Test
            @DisplayName("LD of list of IVT of given type")
            fun getLdIvtT() {

                assertEquals(listOf(ivt3), tranDao.getLdIvtT("Income").blockingObserve())
            }

            @Test
            @DisplayName("LD of list of IVT of given type and category")
            fun getLdIvtTC() {

                assertEquals(listOf(ivt3), tranDao.getLdIvtTC("Income", "Salary").blockingObserve())
            }

            @Test
            @DisplayName("LD of list of IVT of given type, category, and between given dates")
            fun getLdIvtTCD() {

                assertEquals(listOf(ivt3), tranDao.getLdIvtTCD("Income", "Salary",
                    Date(86400010 * 3), Date(86400010 * 6)).blockingObserve())
            }

            @Test
            @DisplayName("LD of list of IVT of given type and between given dates")
            fun getLdIvtTD() {

                assertEquals(listOf(ivt1, ivt2, ivt4),
                    tranDao.getLdIvtTD("Expense", Date(0), Date(86400010 * 6)).blockingObserve())
            }
        }
    }

    /**
     *  LiveData make queries asynchronous, but they need to be synchronous for unit tests.
     *  Taken from [link](https://stackoverflow.com/questions/44270688/unit-testing-room-and-livedata?rq=1)
     */
    private fun <T> LiveData<T>.blockingObserve() : T? {
        var value : T? = null
        val latch = CountDownLatch(1)

        val observer = Observer { t : T ->
            value = t
            latch.countDown()
        }

        Handler(Looper.getMainLooper()).post {
            observeForever(observer)
        }

        latch.await(2, TimeUnit.SECONDS)
        return value
    }

    companion object {

        private lateinit var db : TransactionDatabase

        private lateinit var accDao  : AccountDao
        private lateinit var catDao  : CategoryDao
        private lateinit var tranDao : TransactionDao

        private val acc1 = Account(1, "Credit Card")
        private val acc2 = Account(2, "Debit Card")
        private val acc3 = Account(3, "Cash")
        private val accList : List<Account> = listOf(acc1, acc2, acc3)

        private val cat1 = Category(1, "Food", "Expense")
        private val cat2 = Category(2, "Entertainment", "Expense")
        private val cat3 = Category(3, "Salary", "Income")
        private val catList : List<Category> = listOf(cat1, cat2, cat3)

        private val tran1 = Transaction(1, "Party", Date(86400000), BigDecimal("100.10"), "Cash", "Expense", "Food", "", true, 1, 0, Date(86400000 * 2), true)
        private val tran2 = Transaction(2, "Party2", Date(86400000 * 2), BigDecimal("100.00"), "Cash", "Expense", "Food", "", true, 1, 0, Date(86400000 * 3), false)
        private val tran3 = Transaction(3, "Pay Day", Date(86400000 * 4), BigDecimal("2000.32"), "Debit Card", "Income", "Salary", "", true, 2, 1, Date(86400000 * 11), false)
        private val tran4 = Transaction(4, "Movie Date", Date(86400000 * 5), BigDecimal("55.45"), "Credit Card", "Expense", "Entertainment")
        private val tranList : List<Transaction> = listOf(tran1, tran2, tran3, tran4)

        private val ivt1 = ItemViewTransaction(1, "Party", Date(86400000), BigDecimal("100.10"), "Cash", "Expense", "Food")
        private val ivt2 = ItemViewTransaction(2, "Party2", Date(86400000 * 2), BigDecimal("100.00"), "Cash", "Expense", "Food")
        private val ivt3 = ItemViewTransaction(3, "Pay Day", Date(86400000 * 4), BigDecimal("2000.32"), "Debit Card", "Income", "Salary")
        private val ivt4 = ItemViewTransaction(4, "Movie Date", Date(86400000 * 5), BigDecimal("55.45"), "Credit Card", "Expense", "Entertainment")

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

