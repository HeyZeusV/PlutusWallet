package com.heyzeusv.plutuswallet.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.heyzeusv.plutuswallet.database.daos.AccountDao
import com.heyzeusv.plutuswallet.database.daos.CategoryDao
import com.heyzeusv.plutuswallet.database.daos.TransactionDao
import com.heyzeusv.plutuswallet.database.entities.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

private const val DATABASE_NAME = "transaction-database"

/**
 *  Interacts with Room database on behalf of the ViewModels
 *
 *  Creates instance of database and DAOs. Calls upon the queries within the Daos.
 *  Each query must be run using a CoRoutine unless it returns a LiveData object.
 *
 *  @constructor Used to make this class a singleton.
 */
class TransactionRepository private constructor(context : Context){

    private val migration22to23 : Migration = object : Migration(22, 23) {

        override fun migrate(database : SupportSQLiteDatabase) {

            database.execSQL("""DROP TABLE ExpenseCategory""")
            database.execSQL("""DROP TABLE IncomeCategory""")
        }
    }

    private val migration16to22 : Migration = object : Migration(16, 22) {

        override fun migrate(database : SupportSQLiteDatabase) {

            database.execSQL("""CREATE TABLE IF NOT EXISTS `Category` (
                                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                                    `category` TEXT NOT NULL, 
                                    `type` TEXT NOT NULL)""")
            database.execSQL("""CREATE UNIQUE INDEX IF NOT EXISTS index_cat_type
                                    ON `Category` (category, type)""")
            database.execSQL("""CREATE TABLE IF NOT EXISTS `Account` (
                                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                    `account` TEXT NOT NULL)""")
            database.execSQL("""CREATE UNIQUE INDEX IF NOT EXISTS index_account
                                    ON `Account` (account)""")
            database.execSQL("""CREATE TABLE IF NOT EXISTS `Transaction_new` (
                                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                                    `title` TEXT NOT NULL, 
                                    `date` INTEGER NOT NULL,    
                                    `total` TEXT NOT NULL, 
                                    `account` TEXT NOT NULL, 
                                    `type` TEXT NOT NULL, 
                                    `category` TEXT NOT NULL, 
                                    `memo` TEXT NOT NULL, 
                                    `repeating` INTEGER NOT NULL, 
                                    `frequency` INTEGER NOT NULL, 
                                    `period` INTEGER NOT NULL, 
                                    `futureDate` INTEGER NOT NULL,
                                    `futureTCreated` INTEGER NOT NULL,
                                    FOREIGN KEY(`account`) 
                                        REFERENCES `Account`(`account`) 
                                            ON UPDATE CASCADE 
                                            ON DELETE NO ACTION , 
                                    FOREIGN KEY(`category`, `type`) 
                                        REFERENCES `Category`(`category`, `type`)
                                            ON UPDATE CASCADE 
                                            ON DELETE NO ACTION )""")
            database.execSQL("""INSERT INTO `Transaction_new` SELECT * FROM `Transaction`""")
            database.execSQL("""DROP TABLE `Transaction`""")
            database.execSQL("""ALTER TABLE `Transaction_new` RENAME TO `Transaction`""")
            database.execSQL("""CREATE INDEX IF NOT EXISTS index_cat_name_type
                                    ON `Transaction` (category, type)""")
            database.execSQL("""CREATE INDEX IF NOT EXISTS `index_account_name` 
                                    ON `Transaction` (`account`)""")
        }
    }

    /**
     *  Creates database.
     *
     *  Since this class is a singleton, we don't have to worry about the Database
     *  being create more than once. Currently destroys all Database data on schema change.
     *  Room.databaseBuilder(Context object, Database class, Database name)
     */
    private val database : TransactionDatabase = Room.databaseBuilder(
        context.applicationContext,
        TransactionDatabase::class.java,
        DATABASE_NAME)
        .addMigrations(migration16to22, migration22to23)
        .build()

    /**
     *  DAOs
     */
    private val accountDao     : AccountDao     = database.accountDao()
    private val categoryDao    : CategoryDao    = database.categoryDao()
    private val transactionDao : TransactionDao = database.transactionDao()

    /**
     *  Account Queries
     */
    suspend fun getAccountNamesAsync() : Deferred<MutableList<String>> = withContext(Dispatchers.IO) {async {accountDao.getAccountNames()}}
    suspend fun getAccountSizeAsync()  : Deferred<Int>                 = withContext(Dispatchers.IO) {async {accountDao.getAccountSize()}}
    suspend fun deleteAccount(account : Account) : Job = withContext(Dispatchers.IO) {launch {accountDao.delete(account)}}
    suspend fun insertAccount(account : Account) : Job = withContext(Dispatchers.IO) {launch {accountDao.insert(account)}}
    suspend fun updateAccount(account : Account) : Job = withContext(Dispatchers.IO) {launch {accountDao.update(account)}}
    fun getLDAccounts() : LiveData<List<Account>> = accountDao.getLDAccounts()

    /**
     *  Category Queries
     */
    suspend fun getCategoryNamesByTypeAsync(type : String) : Deferred<MutableList<String>> = withContext(Dispatchers.IO) {async {categoryDao.getCategoryNamesByType(type)}}
    suspend fun getCategorySizeAsync()                     : Deferred<Int>                 = withContext(Dispatchers.IO) {async {categoryDao.getCategorySize()}}
    suspend fun deleteCategory  (category : Category) : Job = withContext(Dispatchers.IO) {launch {categoryDao.delete(category)}}
    suspend fun insertCategory  (category : Category) : Job = withContext(Dispatchers.IO) {launch {categoryDao.insert(category)}}
    suspend fun updateCategory  (category : Category) : Job = withContext(Dispatchers.IO) {launch {categoryDao.update(category)}}
    suspend fun insertCategories(categories : List<Category>) : Job = withContext(Dispatchers.IO) {launch {categoryDao.insert(categories)}}
    fun getLDCategoriesByType(type : String) : LiveData<List<Category>> = categoryDao.getLDCategoriesByType(type)

    /**
     *  Transaction Queries
     */
    suspend fun getDistinctAccountsAsync  (                  ) : Deferred<List<String>>      = withContext(Dispatchers.IO) {async {transactionDao.getDistinctAccounts()}}
    suspend fun getDistinctCatsByTypeAsync(type : String     ) : Deferred<List<String>>      = withContext(Dispatchers.IO) {async {transactionDao.getDistinctCatsByType(type)}}
    suspend fun getFutureTransactionsAsync(currentDate : Date) : Deferred<List<Transaction>> = withContext(Dispatchers.IO) {async {transactionDao.getFutureTransactions(currentDate)}}
    suspend fun getMaxIdAsync             (                  ) : Deferred<Int?>              = withContext(Dispatchers.IO) {async {transactionDao.getMaxId()}}
    suspend fun getTransactionAsync       (id : Int          ) : Deferred<Transaction>       = withContext(Dispatchers.IO) {async {transactionDao.getTransaction(id)}}
    suspend fun deleteTransaction (transaction  : Transaction)       : Job = withContext(Dispatchers.IO) {launch {transactionDao.delete(transaction)}}
    suspend fun upsertTransaction (transaction  : Transaction)       : Job = withContext(Dispatchers.IO) {launch {transactionDao.upsert(transaction)}}
    suspend fun upsertTransactions(transactions : List<Transaction>) : Job = withContext(Dispatchers.IO) {launch {transactionDao.upsert(transactions)}}
    /**
     *  Ld = LiveData
     *  Ct = CategoryTotals
     *  A  = Account
     *  C  = Category
     *  D  = Date
     *  T  = Type
     */
    fun getLDTransaction(id : Int) : LiveData<Transaction?> = transactionDao.getLDTransaction(id)
    fun getLd    (                                                                            ) : LiveData<List<ItemViewTransaction>> = transactionDao.getLd    (                                   )
    fun getLdA   (account : String                                                            ) : LiveData<List<ItemViewTransaction>> = transactionDao.getLdA   (account                            )
    fun getLdAD  (account : String,                                   start : Date, end : Date) : LiveData<List<ItemViewTransaction>> = transactionDao.getLdAD  (account,                 start, end)
    fun getLdAT  (account : String, type : String                                             ) : LiveData<List<ItemViewTransaction>> = transactionDao.getLdAT  (account, type                      )
    fun getLdATC (account : String, type : String, category : String                          ) : LiveData<List<ItemViewTransaction>> = transactionDao.getLdATC (account, type, category            )
    fun getLdATD (account : String, type : String,                    start : Date, end : Date) : LiveData<List<ItemViewTransaction>> = transactionDao.getLdATD (account, type,           start, end)
    fun getLdATCD(account : String, type : String, category : String, start : Date, end : Date) : LiveData<List<ItemViewTransaction>> = transactionDao.getLdATCD(account, type, category, start, end)
    fun getLdD   (                                                    start : Date, end : Date) : LiveData<List<ItemViewTransaction>> = transactionDao.getLdD   (                         start, end)
    fun getLdT   (                  type : String                                             ) : LiveData<List<ItemViewTransaction>> = transactionDao.getLdT   (         type                      )
    fun getLdTC  (                  type : String, category : String                          ) : LiveData<List<ItemViewTransaction>> = transactionDao.getLdTC  (         type, category            )
    fun getLdTCD (                  type : String, category : String, start : Date, end : Date) : LiveData<List<ItemViewTransaction>> = transactionDao.getLdTCD (         type, category, start, end)
    fun getLdTD  (                  type : String,                    start : Date, end : Date) : LiveData<List<ItemViewTransaction>> = transactionDao.getLdTD  (         type,           start, end)
    fun getLdCt  (                                          ) : LiveData<List<CategoryTotals>> = transactionDao.getLdCt  (                   )
    fun getLdCtA (account : String                          ) : LiveData<List<CategoryTotals>> = transactionDao.getLdCtA (account            )
    fun getLdCtAD(account : String, start : Date, end : Date) : LiveData<List<CategoryTotals>> = transactionDao.getLdCtAD(account, start, end)
    fun getLdCtD (                  start : Date, end : Date) : LiveData<List<CategoryTotals>> = transactionDao.getLdCtD (         start, end)

    companion object {

        private var INSTANCE : TransactionRepository? = null

        /**
         *  Initializes instance of TransactionRepository.
         *
         *  Ensures that this class is a singleton.
         *
         *  @param context environment data.
         */
        fun initialize(context : Context) {

            if (INSTANCE == null) {

                INSTANCE = TransactionRepository(context)
            }
        }

        /**
         *  Returns the instance of TransactionRepository.
         *
         *  Will throw an exception if an instance hasn't been initialized.
         */
        fun get() : TransactionRepository {

            return INSTANCE ?:
            throw IllegalStateException("TransactionRepository must be initialized")
        }
    }
}