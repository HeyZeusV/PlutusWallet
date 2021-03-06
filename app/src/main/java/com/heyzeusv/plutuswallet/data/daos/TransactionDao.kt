package com.heyzeusv.plutuswallet.data.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.heyzeusv.plutuswallet.data.model.CategoryTotals
import com.heyzeusv.plutuswallet.data.model.ItemViewTransaction
import com.heyzeusv.plutuswallet.data.model.Transaction
import java.util.Date

/**
 *  Queries that can be applied to Transaction table.
 *
 *  Additional queries that can be applied specifically to this table.
 *  Can have as many as needed, make returnType nullable in case of query returning nothing!
 *  Using LiveData signals Room to run on a background thread.
 *  LiveData object will handle sending data over to main thread and notify any observers.
 */
@Dao
abstract class TransactionDao : BaseDao<Transaction>() {

    /**
     *  Returns list of unique Accounts.
     */
    @Query("""SELECT DISTINCT account
              FROM `transaction`""")
    abstract suspend fun getDistinctAccounts(): MutableList<String>

    /**
     *  Returns list of unique Categories of [type].
     */
    @Query("""SELECT DISTINCT category
              FROM `transaction`
              WHERE type=(:type)""")
    abstract suspend fun getDistinctCatsByType(type: String): MutableList<String>

    /**
     *  Returns list of all Transactions with futureDate before [currentDate]
     *  and futureTCreated is false.
     */
    @Query("""SELECT * 
              FROM `transaction` 
              WHERE futureDate < :currentDate AND futureTCreated == 0""")
    abstract suspend fun getFutureTransactions(currentDate: Date): List<Transaction>

    /**
     *  Returns highest id in table or null if empty.
     */
    @Query("""SELECT MAX(id)
              FROM `transaction`""")
    abstract suspend fun getMaxId() : Int?

    /**
     *  Returns Transaction with given [id].
     */
    @Query("""SELECT *
              FROM `transaction`
              WHERE id=(:id)""")
    abstract suspend fun getTransaction(id: Int): Transaction

    /**
     *  Returns LD of Transaction with given [id].
     */
    @Query("""SELECT * 
              FROM `transaction`
              WHERE id=(:id)""")
    abstract fun getLDTransaction(id: Int): LiveData<Transaction?>

    /**
     *  Following Query function names will use the following abbreviations and Object types.
     *  Ld  = LiveData
     *  Ct  = CategoryTotals
     *  Ivt = ItemViewTransaction
     *  A   = Account
     *  C   = Category
     *  D   = Date
     *  T   = Type
     *
     *  CategoryTotals(CT): Used for ChartFragment contains Category and Total sum of
     *                      chosen Category.
     *  ItemViewTransaction(IVT): Used For TransListFragment contains only what is needed
     *                            to be displayed. Queries are ordered by date.
     *
     *  List of parameters since many repeat
     *  account: the account to be matched against
     *  type: either "Expense" or "Income"
     *  category: the category name to be matched against
     *  start: the start Date to be compared with
     *  end: the end Date to be compared with
     *
     */
    /**
     *  Returns LD of list of CT w/ non-zero total.
     */
    @Query("""SELECT category, SUM(total) AS total, type
              FROM `transaction` 
              GROUP BY category, type
              HAVING SUM(total) > 0""")
    abstract fun getLdCt(): LiveData<List<CategoryTotals>>

    /**
     *  Returns LD of list of CT of given [accounts] w/ non-zero total.
     */
    @Query("""SELECT category, SUM(total) AS total, type
              FROM `transaction` 
              WHERE account IN (:accounts)
              GROUP BY category, type
              HAVING SUM(total) > 0""")
    abstract fun getLdCtA(accounts: List<String>): LiveData<List<CategoryTotals>>

    /**
     *  Returns LD of list of CT between given [start]/[end] dates.
     */
    @Query("""SELECT category, SUM(total) AS total, type
              FROM `transaction` 
              WHERE date BETWEEN :start AND :end
              GROUP BY category, type
              HAVING SUM(total) > 0""")
    abstract fun getLdCtD(start: Date, end: Date): LiveData<List<CategoryTotals>>

    /**
     *  Returns LD of list of CT of given [accounts] and between given [start]/[end] dates.
     */
    @Query("""SELECT category, SUM(total) AS total, type
              FROM `transaction` 
              WHERE account IN (:accounts) AND date BETWEEN :start AND :end
              GROUP BY category, type
              HAVING SUM(total) > 0""")
    abstract fun getLdCtAD(accounts: List<String>, start: Date, end: Date): LiveData<List<CategoryTotals>>

    /**
     *  Returns LD of list of IVT.
     */
    @Query("""SELECT id, title, date, total, account, type, category 
              FROM `transaction`
              ORDER BY date ASC""")
    abstract fun getLdIvt(): LiveData<List<ItemViewTransaction>>

    /**
     *  Returns LD of list of IVT of given [accounts].
     */
    @Query("""SELECT id, title, date, total, account, type, category  
              FROM `transaction` 
              WHERE account IN (:accounts)
              ORDER BY date ASC""")
    abstract fun getLdIvtA(accounts: List<String>): LiveData<List<ItemViewTransaction>>

    /**
     *  Returns LD of list of IVT of given [accounts] and between given [start]/[end] dates.
     */
    @Query("""SELECT id, title, date, total, account, type, category  
              FROM `transaction` 
              WHERE account IN (:accounts) AND date BETWEEN :start AND :end
              ORDER BY date ASC""")
    abstract fun getLdIvtAD(
        accounts: List<String>,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>>

    /**
     *  Returns LD of list of IVT of given [accounts] and [type].
     */
    @Query("""SELECT id, title, date, total, account, type, category  
              FROM `transaction` 
              WHERE account IN (:accounts) AND type=(:type)
              ORDER BY date ASC""")
    abstract fun getLdIvtAT(accounts: List<String>, type: String): LiveData<List<ItemViewTransaction>>

    /**
     *  Returns LD of list of IVT of given [accounts], [type], and [categories].
     */
    @Query("""SELECT id, title, date, total, account, type, category  
              FROM `transaction` 
              WHERE account IN (:accounts) AND type=(:type) AND category IN (:categories)
              ORDER BY date ASC""")
    abstract fun getLdIvtATC(
        accounts: List<String>,
        type: String,
        categories: List<String>
    ): LiveData<List<ItemViewTransaction>>

    /**
     *  Returns LD of list of IVT of given [accounts], [type], and between given [start]/[end] dates.
     */
    @Query("""SELECT id, title, date, total, account, type, category  
              FROM `transaction` 
              WHERE account IN (:accounts) AND type=(:type) AND date BETWEEN :start AND :end
              ORDER BY date ASC""")
    abstract fun getLdIvtATD(
        accounts: List<String>,
        type: String,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>>

    /**
     *  Returns LD of list of IVT of given [accounts], [type], [categories],
     *  and between given [start]/[end] dates.
     */
    @Query("""SELECT id, title, date, total, account, type, category  
              FROM `transaction` 
              WHERE account IN (:accounts) AND type=(:type) 
                AND category IN (:categories) AND date BETWEEN :start AND :end
              ORDER BY date ASC""")
    abstract fun getLdIvtATCD(
        accounts: List<String>,
        type: String,
        categories: List<String>,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>>

    /**
     *  Returns LD of list of IVT between given [start]/[end] dates.
     */
    @Query("""SELECT id, title, date, total, account, type, category  
              FROM `transaction` 
              WHERE date BETWEEN :start AND :end
              ORDER BY date ASC""")
    abstract fun getLdIvtD(start: Date, end: Date): LiveData<List<ItemViewTransaction>>

    /**
     *  Returns LD of list of IVT of given [type].
     */
    @Query("""SELECT id, title, date, total, account, type, category  
              FROM `transaction` 
              WHERE type=(:type)
              ORDER BY date ASC""")
    abstract fun getLdIvtT(type: String): LiveData<List<ItemViewTransaction>>

    /**
     *  Returns LD of list of IVT of given [type] and [categories].
     */
    @Query("""SELECT id, title, date, total, account, type, category  
              FROM `transaction` 
              WHERE type=(:type) AND category IN (:categories)
              ORDER BY date ASC""")
    abstract fun getLdIvtTC(type: String, categories: List<String>): LiveData<List<ItemViewTransaction>>

    /**
     *  Returns LD of list of IVT of given [type], [categories],
     *  and between given [start]/[end] dates.
     */
    @Query("""SELECT id, title, date, total, account, type, category  
              FROM `transaction` 
              WHERE type=(:type) AND category IN (:categories) AND date BETWEEN :start AND :end
              ORDER BY date ASC""")
    abstract fun getLdIvtTCD(
        type: String,
        categories: List<String>,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>>

    /**
     *  Returns LD of list of IVT of given [type] and between given [start]/[end] dates.
     */
    @Query("""SELECT id, title, date, total, account, type, category  
              FROM `transaction` 
              WHERE type=(:type) AND date BETWEEN :start AND :end
              ORDER BY date ASC""")
    abstract fun getLdIvtTD(type: String, start: Date, end: Date): LiveData<List<ItemViewTransaction>>
}