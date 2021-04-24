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
import java.time.ZoneId
import java.time.ZonedDateTime
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
            assertEquals(listOf(dd.acc3, dd.acc1, dd.acc2), ldAccList)
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
            assertEquals(listOf(dd.cat2, dd.cat1), ldCatList)
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

            val expected : List<Transaction> = listOf(dd.tran2)
            assertEquals(expected, runBlocking {
                tranDao.getFutureTransactions(ZonedDateTime.of(2018, 8, 13, 0, 0, 0, 0, ZoneId.systemDefault()))
            })
        }

        @Test
        @DisplayName("Highest id in table or null if empty")
        fun getMaxId() {

            assertEquals(4, runBlocking { tranDao.getMaxId() })
        }

        @Test
        @DisplayName("Transaction with given id")
        fun getTransaction() {

            assertEquals(dd.tran3, runBlocking { tranDao.getTransaction(3) })
        }

        @Test
        @DisplayName("LD of Transaction with given id")
        fun getLDTransaction() {

            val tran : Transaction = tranDao.getLDTransaction(2).blockingObserve()!!
            assertEquals(dd.tran2, tran)
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
                assertEquals(expected, tranDao.getLdCtA(listOf("Cash")).blockingObserve()!!)
            }

            @Test
            @DisplayName("LD of list of CT between given dates")
            fun getLdCtD() {

                val expected: List<CategoryTotals> =
                    listOf(CategoryTotals("Food", BigDecimal("100.1"), "Expense"))
                assertEquals(
                    expected,
                    tranDao.getLdCtD(
                        ZonedDateTime.of(2018, 8, 10, 0, 0, 0, 0, ZoneId.systemDefault()),
                        ZonedDateTime.of(2018, 8, 10, 10, 0, 0, 0, ZoneId.systemDefault())
                    ).blockingObserve()!!
                )
            }

            @Test
            @DisplayName("LD of list of CT of given account and between given dates")
            fun getLdCtAD() {

                val expected: List<CategoryTotals> =
                    listOf(CategoryTotals("Salary", BigDecimal("2000.32"), "Income"))
                assertEquals(
                    expected, tranDao.getLdCtAD(
                        listOf("Debit Card"),
                        ZonedDateTime.of(2018, 8, 10, 0, 0, 0, 0, ZoneId.systemDefault()),
                        ZonedDateTime.of(2018, 8, 20, 0, 0, 0, 0, ZoneId.systemDefault())
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

                assertEquals(listOf(dd.ivt1, dd.ivt2, dd.ivt3, dd.ivt4), tranDao.getLdIvt().blockingObserve())
            }

            @Test
            @DisplayName("LD of list of IVT of given account")
            fun getLdIvtA() {

                assertEquals(listOf(dd.ivt1, dd.ivt2), tranDao.getLdIvtA(listOf("Cash")).blockingObserve())
            }

            @Test
            @DisplayName("LD of list of IVT of given account and between given dates")
            fun getLdIvtAD() {

                assertEquals(listOf(dd.ivt2),
                    tranDao.getLdIvtAD(
                        listOf("Cash"),
                        ZonedDateTime.of(2018, 8, 10, 0, 0, 0, 0, ZoneId.systemDefault()),
                        ZonedDateTime.of(2018, 8, 10, 0, 0, 0, 0, ZoneId.systemDefault())
                    ).blockingObserve())
            }

            @Test
            @DisplayName("LD of list of IVT of given account and type")
            fun getLdIvtAT() {

                assertEquals(emptyList<ItemViewTransaction>(),
                    tranDao.getLdIvtAT(listOf("Cash"), "Income").blockingObserve())
            }

            @Test
            @DisplayName("LD of list of IVT of given account, type, and category")
            fun getLdIvtATC() {

                assertEquals(listOf(dd.ivt1, dd.ivt2),
                    tranDao.getLdIvtATC(listOf("Cash"), "Expense", listOf("Food")).blockingObserve())
            }

            @Test
            @DisplayName("LD of list of IVT of given account, type, and between given dates")
            fun getLdIvtATD() {

                assertEquals(
                    listOf(dd.ivt2),
                    tranDao.getLdIvtATD(listOf("Cash"), "Expense",
                        ZonedDateTime.of(2018, 8, 11, 0, 0, 0, 0, ZoneId.systemDefault()),
                        ZonedDateTime.of(2018, 8, 11, 12, 0, 0, 0, ZoneId.systemDefault())
                    ).blockingObserve()
                )
            }

            @Test
            @DisplayName("LD of list of IVT of given account, type, category, and between given dates")
            fun getLdIvtATCD() {

                assertEquals(
                    listOf(dd.ivt2),
                    tranDao.getLdIvtATCD(listOf("Cash"), "Expense", listOf("Food"),
                        ZonedDateTime.of(2018, 8, 10, 0, 0, 0, 0, ZoneId.systemDefault()),
                        ZonedDateTime.of(2018, 8, 14, 0, 0, 0, 0, ZoneId.systemDefault())
                    ).blockingObserve()
                )
            }

            @Test
            @DisplayName("LD of list of IVT between given dates")
            fun getLdIvtD() {

                assertEquals(
                    listOf(dd.ivt3, dd.ivt4),
                    tranDao.getLdIvtD(
                        ZonedDateTime.of(2018, 8, 13, 0, 0, 0, 0, ZoneId.systemDefault()),
                        ZonedDateTime.of(2018, 8, 17, 0, 0, 0, 0, ZoneId.systemDefault())
                    ).blockingObserve()
                )
            }

            @Test
            @DisplayName("LD of list of IVT of given type")
            fun getLdIvtT() {

                assertEquals(listOf(dd.ivt3), tranDao.getLdIvtT("Income").blockingObserve())
            }

            @Test
            @DisplayName("LD of list of IVT of given type and category")
            fun getLdIvtTC() {

                assertEquals(listOf(dd.ivt3), tranDao.getLdIvtTC("Income", listOf("Salary")).blockingObserve())
            }

            @Test
            @DisplayName("LD of list of IVT of given type, category, and between given dates")
            fun getLdIvtTCD() {

                assertEquals(
                    listOf(dd.ivt3),
                    tranDao.getLdIvtTCD(
                        "Income", listOf("Salary"),
                        ZonedDateTime.of(2018, 8, 13, 0, 0, 0, 0, ZoneId.systemDefault()),
                        ZonedDateTime.of(2018, 8, 15, 0, 0, 0, 0, ZoneId.systemDefault())
                    ).blockingObserve()
                )
            }

            @Test
            @DisplayName("LD of list of IVT of given type and between given dates")
            fun getLdIvtTD() {

                assertEquals(
                    listOf(dd.ivt1, dd.ivt2, dd.ivt4),
                    tranDao.getLdIvtTD(
                        "Expense",
                        ZonedDateTime.of(2018, 8, 8, 0, 0, 0, 0, ZoneId.systemDefault()),
                        ZonedDateTime.of(2018, 8, 22, 0, 0, 0, 0, ZoneId.systemDefault())
                    ).blockingObserve()
                )
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

        Handler(Looper.getMainLooper()).post { observeForever(observer) }

        latch.await(2, TimeUnit.SECONDS)
        return value
    }

    companion object {

        private lateinit var db : TransactionDatabase

        private lateinit var accDao  : AccountDao
        private lateinit var catDao  : CategoryDao
        private lateinit var tranDao : TransactionDao

        private val dd = DummyDataUtil()

        @BeforeAll
        @JvmStatic
        fun createDb() {

            val context : Context = ApplicationProvider.getApplicationContext()
            db = Room.inMemoryDatabaseBuilder(context, TransactionDatabase::class.java).build()
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

