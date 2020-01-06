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
     *  Returns list of Accounts with no repeats
     *
     *  @return list of unique Accounts
     */
    @Query("""SELECT DISTINCT account
                   FROM `transaction`""")
    abstract suspend fun getDistinctAccounts() : List<String>

    /**
     *  Returns all transactions where futureDate is before currentDate and futureTCreated is false.
     *
     *  @param  currentDate the Date at which this query is ran at.
     *  @return list of all Transactions with futureDate before currentDate and futureTCreated is false.
     */
    @Query("""SELECT * 
            FROM `transaction` 
            WHERE futureDate < :currentDate AND futureTCreated == 0""")
    abstract suspend fun getFutureTransactions(currentDate : Date) : List<Transaction>

    /**
     *  Returns the highest id within the table
     *
     *  @return can be null if table is empty!
     */
    @Query("""SELECT MAX(id)
            FROM `transaction`""")
    abstract suspend fun getMaxId() : Int?

    /**
     *  Returns Transaction with given id.
     *
     *  @param id id of Transaction to be returned.
     *  @return LiveData object that holds Transaction to be returned.
     */
    @Query("""SELECT *
            FROM `transaction`
            WHERE id=(:id)""")
    abstract suspend fun getTransaction(id : Int) : Transaction

    /**
     *  Returns Transaction with given id.
     *
     *  @param id id of Transaction to be returned.
     *  @return LiveData object that holds Transaction to be returned.
     */
    @Query("""SELECT * 
            FROM `transaction`
            WHERE id=(:id)""")
    abstract fun getLDTransaction(id : Int) : LiveData<Transaction?>

    /**
     *  Returns LiveData object holding life of Categories with no repeats
     *
     *  @return LiveData object with list of unique Categories by type
     */
    @Query("""SELECT DISTINCT category
                FROM `transaction`
                WHERE type=(:type)""")
    abstract fun getLDUniqueCategories(type : String) : LiveData<List<String>>

    /**
     *  Ld = LiveData
     *  Ct = CategoryTotals
     *  A  = Account
     *  C  = Category
     *  D  = Date
     *  T  = Type
     */
    /**
     *  Returns Transaction whose Type is of that given
     *
     *  Returned as CategoryTotals which is just Category and Total as that is all that is need
     *
     *  @param  type the type of Transactions to be returned.
     *  @return CategoryTotal is helper object that holds Category and the sum of Totals
     */
    @Query("""SELECT category, total
            FROM `transaction` 
            WHERE type=(:type)""")
    abstract fun getLdCtT(type : String?) : LiveData<List<CategoryTotals>>

    /**
     *  Returns Transaction whose Account and Type is of that given
     *
     *  Returned as CategoryTotals which is just Category and Total as that is all that is need
     *
     *  @param  account the Account to be matched against.
     *  @param  type    the type of Transactions to be returned.
     *  @return CategoryTotal is helper object that holds Category and the sum of Totals
     */
    @Query("""SELECT category, total
            FROM `transaction` 
            WHERE account=(:account) AND type=(:type)""")
    abstract fun getLdCtTA(type : String?, account : String?) : LiveData<List<CategoryTotals>>

    /**
     *  Returns Transactions whose Type and Date is between those given.
     *
     *  Returned as CategoryTotals which is just Category and Total as that is all that is need
     *
     *  @param  type  the type of Transactions to be returned.
     *  @param  start the start Date to be compared with.
     *  @param  end   the end Date to be compared with.
     *  @return CategoryTotal is helper object that holds Category and the sum of Totals
     */
    @Query("""SELECT category, total 
            FROM `transaction` 
            WHERE type=(:type) AND date BETWEEN :start AND :end""")
    abstract fun getLdCtTD(type : String?, start : Date?, end : Date?) : LiveData<List<CategoryTotals>>

    /**
     *  Returns Transactions whose Account, Type, and Date is between those given.
     *
     *  Returned as CategoryTotals which is just Category and Total as that is all that is need
     *
     *  @param  account the Account to be matched against.
     *  @param  type    the type of Transactions to be returned.
     *  @param  start   the start Date to be compared with.
     *  @param  end     the end Date to be compared with.
     *  @return CategoryTotal is helper object that holds Category and the sum of Totals
     */
    @Query("""SELECT category, total 
            FROM `transaction` 
            WHERE account=(:account) AND type=(:type) AND date BETWEEN :start AND :end""")
    abstract fun getLdCtTAD(type : String?, account : String?, start : Date?, end : Date?) : LiveData<List<CategoryTotals>>

    /**
     *  Returns all Transactions.
     *
     *  @return LiveData object that holds List of all Transactions.
     */
    @Query("""SELECT id, title, date, account, total, type, category 
            FROM `transaction`
            ORDER BY date ASC""")
    abstract fun getLd() : LiveData<List<ItemViewTransaction>>

    /**
     *  Returns all Transaction with given Account.
     *
     *  @param  account the account to be matched against.
     *  @return LiveData object that holds List of all Transactions with given Account.
     */
    @Query("""SELECT id, title, date, account, total, type, category  
            FROM `transaction` 
            WHERE account=(:account)
            ORDER BY date ASC""")
    abstract fun getLdA(account : String?) : LiveData<List<ItemViewTransaction>>

    /**
     *  Returns all Transaction with given Account and Type.
     *
     *  @param  account the account to be matched against.
     *  @param  type    either "Expense" or "Income".
     *  @return LiveData object that holds List of all Transactions with given Account and Type.
     */
    @Query("""SELECT id, title, date, account, total, type, category  
            FROM `transaction` 
            WHERE account=(:account) AND type=(:type)
            ORDER BY date ASC""")
    abstract fun getLdAT(account : String?, type : String?) : LiveData<List<ItemViewTransaction>>

    /**
     *  Returns all Transaction with given Account, Type, and Category.
     *
     *  @param  account the account to be matched against.
     *  @param  start   the start Date to be compared with.
     *  @param  end     the end Date to be compared with.
     *  @return LiveData object that holds List of all Transactions with given Account, and Dates
     */
    @Query("""SELECT id, title, date, account, total, type, category  
            FROM `transaction` 
            WHERE account=(:account) AND date BETWEEN :start AND :end
            ORDER BY date ASC""")
    abstract fun getLdAD(account : String?, start : Date?, end : Date?) : LiveData<List<ItemViewTransaction>>

    /**
     *  Returns all Transaction with given Account, Type, and Category.
     *
     *  @param  account  the account to be matched against.
     *  @param  type     either "Expense" or "Income".
     *  @param  category the category to be matched against.
     *  @return LiveData object that holds List of all Transactions with given Account, Type, and Category
     */
    @Query("""SELECT id, title, date, account, total, type, category  
            FROM `transaction` 
            WHERE account=(:account) AND type=(:type) AND category=(:category)
            ORDER BY date ASC""")
    abstract fun getLdATC(account : String?, type : String?, category : String?) : LiveData<List<ItemViewTransaction>>

    /**
     *  Returns all Transaction with given Account, Type, and Category.
     *
     *  @param  account the account to be matched against.
     *  @param  type    either "Expense" or "Income".
     *  @param  start   the start Date to be compared with.
     *  @param  end     the end Date to be compared with.
     *  @return LiveData object that holds List of all Transactions with given Account, Type, and Dates
     */
    @Query("""SELECT id, title, date, account, total, type, category  
            FROM `transaction` 
            WHERE account=(:account) AND type=(:type) AND date BETWEEN :start AND :end
            ORDER BY date ASC""")
    abstract fun getLdATD(account : String?, type : String?, start : Date?, end : Date?) : LiveData<List<ItemViewTransaction>>

    /**
     *  Returns all Transaction with given Account, Type, and Category.
     *
     *  @param  account the account to be matched against.
     *  @param  type    either "Expense" or "Income".
     *  @param  category the category to be matched against.
     *  @param  start   the start Date to be compared with.
     *  @param  end     the end Date to be compared with.
     *  @return LiveData object that holds List of all Transactions with given Account, Type, Category, and Dates
     */
    @Query("""SELECT id, title, date, account, total, type, category  
            FROM `transaction` 
            WHERE account=(:account) AND type=(:type) AND category=(:category) AND date BETWEEN :start AND :end
            ORDER BY date ASC""")
    abstract fun getLdATCD(account : String?, type : String?, category : String?, start : Date?, end : Date?) : LiveData<List<ItemViewTransaction>>

    /**
     *  Returns all Transaction with given Type.
     *
     *  @param  type either "Expense" or "Income".
     *  @return LiveData object that holds List of all Transactions with given Type.
     */
    @Query("""SELECT id, title, date, account, total, type, category  
            FROM `transaction` 
            WHERE type=(:type)
            ORDER BY date ASC""")
    abstract fun getLdT(type : String?) : LiveData<List<ItemViewTransaction>>

    /**
     *  Returns all Transactions with given Type and Category.
     *
     *  @param  type     either "Expense" or "Income".
     *  @param  category the category to be matched against.
     *  @return LiveData object that holds List of all Transactions with given Type and category.
     */
    @Query("""SELECT id, title, date, account, total, type, category  
            FROM `transaction` 
            WHERE type=(:type) AND category=(:category)
            ORDER BY date ASC""")
    abstract fun getLdTC(type : String?, category : String?) : LiveData<List<ItemViewTransaction>>

    /**
     *  Returns all Transactions with given Type and within given Dates.
     *
     *  @param  type     either "Expense" or "Income".
     *  @param  start    the start Date to be compared with.
     *  @param  end      the end Date to be compared with.
     *  @return LiveData object that holds List of all Transactions with given Type and within given Dates.
     */
    @Query("""SELECT id, title, date, account, total, type, category  
            FROM `transaction` 
            WHERE type=(:type) AND date BETWEEN :start AND :end
            ORDER BY date ASC""")
    abstract fun getLdTD(type : String?, start : Date?, end : Date?) : LiveData<List<ItemViewTransaction>>

    /**
     *  Returns all Transactions with given Type and Category and within given Dates.
     *
     *  @param  type     either "Expense" or "Income".
     *  @param  category the category to be matched against.
     *  @param  start    the start Date to be compared with.
     *  @param  end      the end Date to be compared with.
     *  @return LiveDate object that holds list of all Transactions with given Type and Category and within given Dates.
     */
    @Query("""SELECT id, title, date, account, total, type, category  
            FROM `transaction` 
            WHERE type=(:type) AND category=(:category) AND date BETWEEN :start AND :end
            ORDER BY date ASC""")
    abstract fun getLdTCD(type : String?, category : String?, start : Date?, end : Date?) : LiveData<List<ItemViewTransaction>>

    /**
     *  Returns all Transaction within given Dates.
     *
     *  @param  start the start Date to be compared with.
     *  @param  end   the end Date to be compared with.
     *  @return LiveData object that holds List of all Transactions within given Dates in table.
     */
    @Query("""SELECT id, title, date, account, total, type, category  
            FROM `transaction` 
            WHERE date BETWEEN :start AND :end
            ORDER BY date ASC""")
    abstract fun getLdD(start : Date?, end : Date?) : LiveData<List<ItemViewTransaction>>
}