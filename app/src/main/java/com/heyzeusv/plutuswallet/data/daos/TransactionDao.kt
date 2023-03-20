package com.heyzeusv.plutuswallet.data.daos

import androidx.room.Dao
import androidx.room.Query
import com.heyzeusv.plutuswallet.data.model.CategoryTotals
import com.heyzeusv.plutuswallet.data.model.TranListItem
import com.heyzeusv.plutuswallet.data.model.Transaction
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.Flow

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
     *  Returns list of all Transactions with futureDate before [currentDate]
     *  and futureTCreated is false.
     */
    @Query("""SELECT * 
              FROM `transaction` 
              WHERE futureDate < :currentDate AND futureTCreated == 0""")
    abstract suspend fun getFutureTransactions(currentDate: ZonedDateTime): List<Transaction>

    /**
     *  Returns highest id in table or null if empty.
     */
    @Query("""SELECT MAX(id)
              FROM `transaction`""")
    abstract fun getMaxId() : Flow<Int?>

    /**
     *  Returns Transaction with given [id].
     */
    @Query("""SELECT *
              FROM `transaction`
              WHERE id=(:id)""")
    abstract suspend fun getTransaction(id: Int): Transaction?

    /**
     *  Following Query function names will use the following abbreviations and Object types.
     *  Ct  = CategoryTotals
     *  Tli = TranListItem
     *  A   = Account
     *  C   = Category
     *  D   = ZonedDateTime
     *  T   = Type
     *
     *  CategoryTotals(CT): Used for ChartScreen contains Category and Total sum of
     *                      chosen Category.
     *  TranListItem(TLI): Used For TranListScreen contains only what is needed
     *                            to be displayed. Queries are ordered by date.
     *
     *  List of parameters since many repeat
     *  account: the account to be matched against
     *  type: either "Expense" or "Income"
     *  category: the category name to be matched against
     *  start: the start ZonedDateTime to be compared with
     *  end: the end ZonedDateTime to be compared with
     *
     */
    /**
     *  Returns list of CT w/ non-zero total.
     */
    @Query("""SELECT category, SUM(total) AS total, type
              FROM `transaction` 
              GROUP BY category, type
              HAVING SUM(total) > 0""")
    abstract fun getCt(): Flow<List<CategoryTotals>>

    /**
     *  Returns list of CT of given [accounts] w/ non-zero total.
     */
    @Query("""SELECT category, SUM(total) AS total, type
              FROM `transaction` 
              WHERE account IN (:accounts)
              GROUP BY category, type
              HAVING SUM(total) > 0""")
    abstract fun getCtA(accounts: List<String>): Flow<List<CategoryTotals>>

    /**
     *  Returns list of CT of given [accounts] and given [categories] with [type].
     */
    @Query("""SELECT category, SUM(total) AS total, type
              FROM `transaction` 
              WHERE account IN (:accounts) AND type=(:type) AND category IN (:categories)
              GROUP BY category, type
              HAVING SUM(total) > 0""")
    abstract fun getCtAC(
        accounts: List<String>,
        type: String,
        categories: List<String>,
    ): Flow<List<CategoryTotals>>

    /**
     *  Returns list of CT of given [accounts] and between given [start]/[end] dates.
     */
    @Query("""SELECT category, SUM(total) AS total, type
              FROM `transaction` 
              WHERE account IN (:accounts) AND date BETWEEN :start AND :end
              GROUP BY category, type
              HAVING SUM(total) > 0""")
    abstract fun getCtAD(
        accounts: List<String>,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<CategoryTotals>>

    /**
     *  Returns list of CT of given [accounts], given [categories] with [type],
     *  and between given [start]/[end] dates.
     */
    @Query("""SELECT category, SUM(total) AS total, type
              FROM `transaction` 
              WHERE account IN (:accounts) AND type=(:type) AND category IN (:categories) 
                AND date BETWEEN :start AND :end
              GROUP BY category, type
              HAVING SUM(total) > 0""")
    abstract fun getCtACD(
        accounts: List<String>,
        type: String,
        categories: List<String>,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<CategoryTotals>>

    /**
     *  Returns list of CT of given [categories] with [type].
     */
    @Query("""SELECT category, SUM(total) AS total, type
              FROM `transaction` 
              WHERE type=(:type) AND category IN (:categories)
              GROUP BY category, type
              HAVING SUM(total) > 0""")
    abstract fun getCtC(type: String, categories: List<String>): Flow<List<CategoryTotals>>

    /**
     *  Returns list of CT of given [categories] with [type] and between given [start]/[end] dates.
     */
    @Query("""SELECT category, SUM(total) AS total, type
              FROM `transaction` 
              WHERE type=(:type) AND category IN (:categories) 
                AND date BETWEEN :start AND :end
              GROUP BY category, type
              HAVING SUM(total) > 0""")
    abstract fun getCtCD(
        type: String,
        categories: List<String>,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<CategoryTotals>>

    /**
     *  Returns list of CT between given [start]/[end] dates.
     */
    @Query("""SELECT category, SUM(total) AS total, type
              FROM `transaction` 
              WHERE date BETWEEN :start AND :end
              GROUP BY category, type
              HAVING SUM(total) > 0""")
    abstract fun getCtD(start: ZonedDateTime, end: ZonedDateTime): Flow<List<CategoryTotals>>

    /**
     *  Returns list of TLI.
     */
    @Query("""SELECT id, title, date, total, account, type, category 
              FROM `transaction`
              ORDER BY date ASC""")
    abstract fun getTli(): Flow<List<TranListItem>>

    /**
     *  Returns list of TLI of given [accounts].
     */
    @Query("""SELECT id, title, date, total, account, type, category  
              FROM `transaction` 
              WHERE account IN (:accounts)
              ORDER BY date ASC""")
    abstract fun getTliA(accounts: List<String>): Flow<List<TranListItem>>

    /**
     *  Returns list of TLI of given [accounts] and between given [start]/[end] dates.
     */
    @Query("""SELECT id, title, date, total, account, type, category  
              FROM `transaction` 
              WHERE account IN (:accounts) AND date BETWEEN :start AND :end
              ORDER BY date ASC""")
    abstract fun getTliAD(
        accounts: List<String>,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<TranListItem>>

    /**
     *  Returns list of TLI of given [accounts] and [type].
     */
    @Query("""SELECT id, title, date, total, account, type, category  
              FROM `transaction` 
              WHERE account IN (:accounts) AND type=(:type)
              ORDER BY date ASC""")
    abstract fun getTliAT(accounts: List<String>, type: String): Flow<List<TranListItem>>

    /**
     *  Returns list of TLI of given [accounts], [type], and [categories].
     */
    @Query("""SELECT id, title, date, total, account, type, category  
              FROM `transaction` 
              WHERE account IN (:accounts) AND type=(:type) AND category IN (:categories)
              ORDER BY date ASC""")
    abstract fun getTliATC(
        accounts: List<String>,
        type: String,
        categories: List<String>
    ): Flow<List<TranListItem>>

    /**
     *  Returns list of TLI of given [accounts], [type], and between given [start]/[end] dates.
     */
    @Query("""SELECT id, title, date, total, account, type, category  
              FROM `transaction` 
              WHERE account IN (:accounts) AND type=(:type) AND date BETWEEN :start AND :end
              ORDER BY date ASC""")
    abstract fun getTliATD(
        accounts: List<String>,
        type: String,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<TranListItem>>

    /**
     *  Returns list of TLI of given [accounts], [type], [categories],
     *  and between given [start]/[end] dates.
     */
    @Query("""SELECT id, title, date, total, account, type, category  
              FROM `transaction` 
              WHERE account IN (:accounts) AND type=(:type) 
                AND category IN (:categories) AND date BETWEEN :start AND :end
              ORDER BY date ASC""")
    abstract fun getTliATCD(
        accounts: List<String>,
        type: String,
        categories: List<String>,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<TranListItem>>

    /**
     *  Returns list of TLI between given [start]/[end] dates.
     */
    @Query("""SELECT id, title, date, total, account, type, category  
              FROM `transaction` 
              WHERE date BETWEEN :start AND :end
              ORDER BY date ASC""")
    abstract fun getTliD(start: ZonedDateTime, end: ZonedDateTime): Flow<List<TranListItem>>

    /**
     *  Returns list of TLI of given [type].
     */
    @Query("""SELECT id, title, date, total, account, type, category  
              FROM `transaction` 
              WHERE type=(:type)
              ORDER BY date ASC""")
    abstract fun getTliT(type: String): Flow<List<TranListItem>>

    /**
     *  Returns list of TLI of given [type] and [categories].
     */
    @Query("""SELECT id, title, date, total, account, type, category  
              FROM `transaction` 
              WHERE type=(:type) AND category IN (:categories)
              ORDER BY date ASC""")
    abstract fun getTliTC(type: String, categories: List<String>): Flow<List<TranListItem>>

    /**
     *  Returns list of TLI of given [type], [categories],
     *  and between given [start]/[end] dates.
     */
    @Query("""SELECT id, title, date, total, account, type, category  
              FROM `transaction` 
              WHERE type=(:type) AND category IN (:categories) AND date BETWEEN :start AND :end
              ORDER BY date ASC""")
    abstract fun getTliTCD(
        type: String,
        categories: List<String>,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<TranListItem>>

    /**
     *  Returns list of TLI of given [type] and between given [start]/[end] dates.
     */
    @Query("""SELECT id, title, date, total, account, type, category  
              FROM `transaction` 
              WHERE type=(:type) AND date BETWEEN :start AND :end
              ORDER BY date ASC""")
    abstract fun getTliTD(
        type: String,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<TranListItem>>
}