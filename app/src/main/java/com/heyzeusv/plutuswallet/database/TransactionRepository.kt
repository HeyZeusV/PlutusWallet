package com.heyzeusv.plutuswallet.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.heyzeusv.plutuswallet.database.daos.CategoryDao
import com.heyzeusv.plutuswallet.database.daos.ExpenseCategoryDao
import com.heyzeusv.plutuswallet.database.daos.IncomeCategoryDao
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

    private val migration19to20 : Migration = object : Migration(19, 20) {

        override fun migrate(database: SupportSQLiteDatabase) {

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
                                            FOREIGN KEY(`category`, `type`) 
                                                REFERENCES `Category`(`category`, `type`) 
                                                ON UPDATE CASCADE ON DELETE NO ACTION)""")
            database.execSQL("""INSERT INTO `Transaction_new` SELECT * FROM `Transaction`""")
            database.execSQL("""DROP TABLE `Transaction`""")
            database.execSQL("""ALTER TABLE `Transaction_new` RENAME TO `Transaction`""")
            database.execSQL("""CREATE INDEX IF NOT EXISTS `index_trans_name_type` 
                                            ON `Transaction` (category, type)""")
        }
    }

    private val migration18to19 : Migration = object : Migration(18, 19) {

        override fun migrate(database: SupportSQLiteDatabase) {

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
                                            FOREIGN KEY(`category`, `type`) 
                                                REFERENCES `Category`(`category`, `type`) 
                                                ON UPDATE CASCADE ON DELETE NO ACTION)""")
            database.execSQL("""INSERT INTO `Transaction_new` SELECT * FROM `Transaction`""")
            database.execSQL("""DROP TABLE `Transaction`""")
            database.execSQL("""ALTER TABLE `Transaction_new` RENAME TO `Transaction`""")
            database.execSQL("""CREATE INDEX index_trans_name_type
                                        ON `Transaction` (category, type)""")
        }
    }

    private val migration17to18 : Migration = object : Migration(17, 18) {

        override fun migrate(database: SupportSQLiteDatabase) {

            database.execSQL("""CREATE UNIQUE INDEX index_cat_name_type
                                        ON `Category` (category, type)""")
            database.execSQL("""CREATE TABLE IF NOT EXISTS `Transaction_new` (
                                            `id` INTEGER NOT NULL, 
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
                                            PRIMARY KEY(`id`), 
                                            FOREIGN KEY(`category`, `type`) 
                                                REFERENCES `Category`(`category`, `type`) 
                                                ON UPDATE CASCADE ON DELETE NO ACTION )""")
            database.execSQL("""INSERT INTO `Transaction_new` SELECT * FROM `Transaction`""")
            database.execSQL("""DROP TABLE `Transaction`""")
            database.execSQL("""ALTER TABLE `Transaction_new` RENAME TO `Transaction`""")
        }
    }

    private val migration16to17 : Migration = object : Migration(16, 17) {

        override fun migrate(database: SupportSQLiteDatabase) {

            database.execSQL("""CREATE TABLE `Category` (
                                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                                        `category` TEXT NOT NULL, 
                                        `type` TEXT NOT NULL)""")
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
        .addMigrations(migration16to17, migration17to18, migration18to19, migration19to20)
        .build()

    /**
     *  DAOs
     */
    private val categoryDao          : CategoryDao        = database.categoryDao       ()
    private val transactionDao       : TransactionDao     = database.transactionDao    ()
    private val expenseCategoryDao   : ExpenseCategoryDao = database.expenseCategoryDao()
    private val incomeCategoryDao    : IncomeCategoryDao  = database.incomeCategoryDao ()

    /**
     *  Transaction Queries
     */
    /**
     *  Ld = LiveData
     *  Ct = CategoryTotals
     *  A  = Account
     *  C  = Category
     *  D  = Date
     *  T  = Type
     */
    fun getLd    (                                                                                 ) : LiveData<List<ItemViewTransaction>> = transactionDao.getLd    (                                   )
    fun getLdA   (account : String?                                                                ) : LiveData<List<ItemViewTransaction>> = transactionDao.getLdA   (account                            )
    fun getLdAT  (account : String?, type : String?                                                ) : LiveData<List<ItemViewTransaction>> = transactionDao.getLdAT  (account, type                      )
    fun getLdAD  (account : String?,                                     start : Date?, end : Date?) : LiveData<List<ItemViewTransaction>> = transactionDao.getLdAD  (account,                 start, end)
    fun getLdATC (account : String?, type : String?, category : String?                            ) : LiveData<List<ItemViewTransaction>> = transactionDao.getLdATC (account, type, category            )
    fun getLdATD (account : String?, type : String?,                     start : Date?, end : Date?) : LiveData<List<ItemViewTransaction>> = transactionDao.getLdATD (account, type,           start, end)
    fun getLdATCD(account : String?, type : String?, category : String?, start : Date?, end : Date?) : LiveData<List<ItemViewTransaction>> = transactionDao.getLdATCD(account, type, category, start, end)
    fun getLdT   (                   type : String?                                                ) : LiveData<List<ItemViewTransaction>> = transactionDao.getLdT   (         type                      )
    fun getLdTC  (                   type : String?, category : String?                            ) : LiveData<List<ItemViewTransaction>> = transactionDao.getLdTC  (         type, category            )
    fun getLdTD  (                   type : String?,                     start : Date?, end : Date?) : LiveData<List<ItemViewTransaction>> = transactionDao.getLdTD  (         type,           start, end)
    fun getLdTCD (                   type : String?, category : String?, start : Date?, end : Date?) : LiveData<List<ItemViewTransaction>> = transactionDao.getLdTCD (         type, category, start, end)
    fun getLdD   (                                                       start : Date?, end : Date?) : LiveData<List<ItemViewTransaction>> = transactionDao.getLdD   (                         start, end)
    fun getLDTransaction   (id   : Int)                                           : LiveData<Transaction?>         = transactionDao.getLDTransaction(id)
    fun getLdCtT  (type : String?)                                                : LiveData<List<CategoryTotals>> = transactionDao.getLdCtT        (type)
    fun getLdCtTA (type : String?, account : String?)                             : LiveData<List<CategoryTotals>> = transactionDao.getLdCtTA       (type, account)
    fun getLdCtTD (type : String?,                    start : Date?, end : Date?) : LiveData<List<CategoryTotals>> = transactionDao.getLdCtTD       (type,          start, end)
    fun getLdCtTAD(type : String?, account : String?, start : Date?, end : Date?) : LiveData<List<CategoryTotals>> = transactionDao.getLdCtTAD      (type, account, start, end)
    suspend fun getAccountsAsync          ()                                 : Deferred<List<String>>      = withContext(Dispatchers.IO) {async  {transactionDao.getAccounts()}}
    suspend fun getFutureTransactionsAsync(currentDate  : Date)              : Deferred<List<Transaction>> = withContext(Dispatchers.IO) {async  {transactionDao.getFutureTransactions(currentDate)}}
    suspend fun getMaxIdAsync             ()                                 : Deferred<Int?>              = withContext(Dispatchers.IO) {async  {transactionDao.getMaxId()}}
    suspend fun getTransactionAsync       (id           : Int)               : Deferred<Transaction>       = withContext(Dispatchers.IO) {async  {transactionDao.getTransaction(id)}}
    suspend fun deleteTransaction         (transaction  : Transaction)       : Job                         = withContext(Dispatchers.IO) {launch {transactionDao.delete(transaction)}}
    suspend fun insertTransaction         (transaction  : Transaction)       : Job                         = withContext(Dispatchers.IO) {launch {transactionDao.insert(transaction)}}
    suspend fun updateTransaction         (transaction  : Transaction)       : Job                         = withContext(Dispatchers.IO) {launch {transactionDao.update(transaction)}}
    suspend fun upsertTransactions        (transactions : List<Transaction>) : Job                         = withContext(Dispatchers.IO) {launch {transactionDao.upsert(transactions)}}

    /**
     *  Category Queries
     */
    fun getLDCategoriesByType(type : String) : LiveData<List<Category>> = categoryDao.getLDCategoriesByType(type)
    suspend fun getCategoriesByTypeAsync(type : String) : Deferred<List<String>> = withContext(Dispatchers.IO) {async {categoryDao.getCategoriesByType(type)}}
    suspend fun getCategorySizeAsync () : Deferred<Int?>           = withContext(Dispatchers.IO) {async {categoryDao.getCategorySize()}}
    suspend fun insertCategory  (category   : Category      ) : Job = withContext(Dispatchers.IO) {launch {categoryDao.insert(category)}}
    suspend fun updateCategory  (category   : Category      ) : Job = withContext(Dispatchers.IO) {launch {categoryDao.update(category)}}
    suspend fun insertCategories(categories : List<Category>) : Job = withContext(Dispatchers.IO) {launch {categoryDao.insert(categories)}}

    /**
     *  ExpenseCategory Queries
     */
    suspend fun getExpenseCategoryNamesAsync() : Deferred<List<String>> = withContext(Dispatchers.IO) {async {expenseCategoryDao.getExpenseCategoryNames()}}
    suspend fun getExpenseCategorySizeAsync () : Deferred<Int?>         = withContext(Dispatchers.IO) {async {expenseCategoryDao.getExpenseCategorySize()}}

    /**
     *  IncomeCategory Queries
     */
    suspend fun getIncomeCategoryNamesAsync() : Deferred<List<String>> = withContext(Dispatchers.IO) {async {incomeCategoryDao.getIncomeCategoryNames()}}
    suspend fun getIncomeCategorySizeAsync () : Deferred<Int?>         = withContext(Dispatchers.IO) {async {incomeCategoryDao.getIncomeCategorySize()}}

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