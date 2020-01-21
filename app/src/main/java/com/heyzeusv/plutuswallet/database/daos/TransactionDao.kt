package com.heyzeusv.plutuswallet.database.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.heyzeusv.plutuswallet.database.entities.CategoryTotals
import com.heyzeusv.plutuswallet.database.entities.ItemViewTransaction
import com.heyzeusv.plutuswallet.database.entities.Transaction
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
     *  @return list of unique Accounts
     */
    @Query("""SELECT DISTINCT account
              FROM `transaction`""")
    abstract suspend fun getDistinctAccounts() : List<String>

    /**
     *  @param  currentDate the Date at which this query is ran at.
     *  @return list of all Transactions with futureDate before currentDate and futureTCreated is false.
     */
    @Query("""SELECT * 
              FROM `transaction` 
              WHERE futureDate < :currentDate AND futureTCreated == 0""")
    abstract suspend fun getFutureTransactions(currentDate : Date) : List<Transaction>

    /**
     *  @return highest id in table or null if empty.
     */
    @Query("""SELECT MAX(id)
              FROM `transaction`""")
    abstract suspend fun getMaxId() : Int?

    /**
     *  @param id id of Transaction to be returned.
     *  @return Transaction with given id.
     */
    @Query("""SELECT *
              FROM `transaction`
              WHERE id=(:id)""")
    abstract suspend fun getTransaction(id : Int) : Transaction

    /**
     *  @param id id of Transaction to be returned.
     *  @return LiveData object that holds Transaction to be returned.
     */
    @Query("""SELECT * 
              FROM `transaction`
              WHERE id=(:id)""")
    abstract fun getLDTransaction(id : Int) : LiveData<Transaction?>

    /**
     *  @return LiveData object with list of unique Categories by type.
     */
    @Query("""SELECT DISTINCT category
              FROM `transaction`
              WHERE type=(:type)""")
    abstract fun getLDUniqueCategories(type : String) : LiveData<List<String>>

    /**
     *  Following Queries will using following abbreviations and Object types.
     *  Ld = LiveData
     *  Ct = CategoryTotals
     *  A  = Account
     *  C  = Category
     *  D  = Date
     *  T  = Type
     *
     *  CategoryTotals(CT): Used for ChartFragment contains Category and Total sum of
     *                      chosen Category.
     *  ItemViewTransaction(IVT): Used For TransListFragment contains only what is needed
     *                            to be displayed.
     */
    /**
     *  @return LiveData obj holding list of CT of given type.
     */
    @Query("""SELECT category, SUM(total) AS total, type
              FROM `transaction` 
              GROUP BY category
              HAVING SUM(total) > 0""")
    abstract fun getLdCt() : LiveData<List<CategoryTotals>>

    /**
     *  @param  account the Account to be matched against.
     *  @return LiveData obj holding list of CT of given type and account.
     */
    @Query("""SELECT category, SUM(total) AS total, type
              FROM `transaction` 
              WHERE account=(:account)
              GROUP BY category
              HAVING SUM(total) > 0""")
    abstract fun getLdCtA(account : String?) : LiveData<List<CategoryTotals>>

    /**
     *  @param  start the start Date to be compared with.
     *  @param  end   the end Date to be compared with.
     *  @return LiveData obj holding list of CT of given type and between dates.
     */
    @Query("""SELECT category, SUM(total) AS total, type
              FROM `transaction` 
              WHERE date BETWEEN :start AND :end
              GROUP BY category
              HAVING SUM(total) > 0""")
    abstract fun getLdCtD(start : Date?, end : Date?)
            : LiveData<List<CategoryTotals>>

    /**
     *  @param  account the Account to be matched against.
     *  @param  start   the start Date to be compared with.
     *  @param  end     the end Date to be compared with.
     *  @return LiveData obj holding list of CT of given type, account, and between dates.
     */
    @Query("""SELECT category, SUM(total) AS total, type
              FROM `transaction` 
              WHERE account=(:account) AND date BETWEEN :start AND :end
              GROUP BY category
              HAVING SUM(total) > 0""")
    abstract fun getLdCtAD(account : String?, start : Date?, end : Date?)
            : LiveData<List<CategoryTotals>>

    /**
     *  @return LiveData obj holding list of IVT ordered by date.
     */
    @Query("""SELECT id, title, date, account, total, type, category 
              FROM `transaction`
              ORDER BY date ASC""")
    abstract fun getLd() : LiveData<List<ItemViewTransaction>>

    /**
     *  @param  account the account to be matched against.
     *  @return LiveData obj holding list of IVT of given account ordered by date.
     */
    @Query("""SELECT id, title, date, account, total, type, category  
              FROM `transaction` 
              WHERE account=(:account)
              ORDER BY date ASC""")
    abstract fun getLdA(account : String?) : LiveData<List<ItemViewTransaction>>

    /**
     *  @param  account the account to be matched against.
     *  @param  type    either "Expense" or "Income".
     *  @return LiveData obj holding list of IVT of given account and type ordered by date.
     */
    @Query("""SELECT id, title, date, account, total, type, category  
              FROM `transaction` 
              WHERE account=(:account) AND type=(:type)
              ORDER BY date ASC""")
    abstract fun getLdAT(account : String?, type : String?) : LiveData<List<ItemViewTransaction>>

    /**
     *  @param  account the account to be matched against.
     *  @param  start   the start Date to be compared with.
     *  @param  end     the end Date to be compared with.
     *  @return LiveData obj holding list of IVT of given account and dates ordered by date.
     */
    @Query("""SELECT id, title, date, account, total, type, category  
              FROM `transaction` 
              WHERE account=(:account) AND date BETWEEN :start AND :end
              ORDER BY date ASC""")
    abstract fun getLdAD(account : String?, start : Date?, end : Date?)
            : LiveData<List<ItemViewTransaction>>

    /**
     *  @param  account  the account to be matched against.
     *  @param  type     either "Expense" or "Income".
     *  @param  category the category to be matched against.
     *  @return LiveData obj holding list of IVT of given account, type,
     *          and category ordered by date.
     */
    @Query("""SELECT id, title, date, account, total, type, category  
              FROM `transaction` 
              WHERE account=(:account) AND type=(:type) AND category=(:category)
              ORDER BY date ASC""")
    abstract fun getLdATC(account : String?, type : String?, category : String?)
            : LiveData<List<ItemViewTransaction>>

    /**
     *  @param  account the account to be matched against.
     *  @param  type    either "Expense" or "Income".
     *  @param  start   the start Date to be compared with.
     *  @param  end     the end Date to be compared with.
     *  @return LiveData obj holding list of IVT of given account, type, and dates ordered by date.
     */
    @Query("""SELECT id, title, date, account, total, type, category  
              FROM `transaction` 
              WHERE account=(:account) AND type=(:type) AND date BETWEEN :start AND :end
              ORDER BY date ASC""")
    abstract fun getLdATD(account : String?, type : String?, start : Date?, end : Date?)
            : LiveData<List<ItemViewTransaction>>

    /**
     *  @param  account the account to be matched against.
     *  @param  type    either "Expense" or "Income".
     *  @param  category the category to be matched against.
     *  @param  start   the start Date to be compared with.
     *  @param  end     the end Date to be compared with.
     *  @return LiveData obj holding list of IVT of given account, type,
     *          category, and dates ordered by date.
     */
    @Query("""SELECT id, title, date, account, total, type, category  
              FROM `transaction` 
              WHERE account=(:account) AND type=(:type) 
                AND category=(:category) AND date BETWEEN :start AND :end
              ORDER BY date ASC""")
    abstract fun getLdATCD(account : String?, type : String?, category : String?,
                           start : Date?, end : Date?) : LiveData<List<ItemViewTransaction>>

    /**
     *  @param  type either "Expense" or "Income".
     *  @return LiveData obj holding list of IVT of given type ordered by date.
     */
    @Query("""SELECT id, title, date, account, total, type, category  
              FROM `transaction` 
              WHERE type=(:type)
              ORDER BY date ASC""")
    abstract fun getLdT(type : String?) : LiveData<List<ItemViewTransaction>>

    /**
     *  @param  type     either "Expense" or "Income".
     *  @param  category the category to be matched against.
     *  @return LiveData obj holding list of IVT of given type and category ordered by date.
     */
    @Query("""SELECT id, title, date, account, total, type, category  
              FROM `transaction` 
              WHERE type=(:type) AND category=(:category)
              ORDER BY date ASC""")
    abstract fun getLdTC(type : String?, category : String?) : LiveData<List<ItemViewTransaction>>

    /**
     *  @param  type     either "Expense" or "Income".
     *  @param  start    the start Date to be compared with.
     *  @param  end      the end Date to be compared with.
     *  @return LiveData obj holding list of IVT of given type and dates ordered by date.
     */
    @Query("""SELECT id, title, date, account, total, type, category  
              FROM `transaction` 
              WHERE type=(:type) AND date BETWEEN :start AND :end
              ORDER BY date ASC""")
    abstract fun getLdTD(type : String?, start : Date?, end : Date?)
            : LiveData<List<ItemViewTransaction>>

    /**
     *  @param  type     either "Expense" or "Income".
     *  @param  category the category to be matched against.
     *  @param  start    the start Date to be compared with.
     *  @param  end      the end Date to be compared with.
     *  @return LiveData obj holding list of IVT of given type, category, and dates ordered by date.
     */
    @Query("""SELECT id, title, date, account, total, type, category  
              FROM `transaction` 
              WHERE type=(:type) AND category=(:category) AND date BETWEEN :start AND :end
              ORDER BY date ASC""")
    abstract fun getLdTCD(type : String?, category : String?, start : Date?, end : Date?)
            : LiveData<List<ItemViewTransaction>>

    /**
     *  @param  start the start Date to be compared with.
     *  @param  end   the end Date to be compared with.
     *  @return LiveData obj holding list of IVT of given dates ordered by date.
     */
    @Query("""SELECT id, title, date, account, total, type, category  
              FROM `transaction` 
              WHERE date BETWEEN :start AND :end
              ORDER BY date ASC""")
    abstract fun getLdD(start : Date?, end : Date?) : LiveData<List<ItemViewTransaction>>
}