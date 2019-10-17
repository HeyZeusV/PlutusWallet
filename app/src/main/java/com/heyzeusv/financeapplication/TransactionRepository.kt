package com.heyzeusv.financeapplication

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.heyzeusv.financeapplication.database.CategoryDao
import com.heyzeusv.financeapplication.database.FutureTransactionDao
import com.heyzeusv.financeapplication.database.TransactionDao
import com.heyzeusv.financeapplication.database.TransactionDatabase
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "transaction-database"

class TransactionRepository private constructor(context : Context){

    // Context Object, Database class, Database Name
    private val database : TransactionDatabase = Room.databaseBuilder(
        context.applicationContext,
        TransactionDatabase::class.java,
        DATABASE_NAME
    ).fallbackToDestructiveMigration()
        .build()

    // DAOs
    private val transactionDao : TransactionDao             = database.transactionDao()
    private val categoryDao : CategoryDao                   = database.categoryDao()
    private val futureTransactionDao : FutureTransactionDao = database.futureTransactionDao()

    // starts background thread to run update and insert
    private val executor = Executors.newSingleThreadExecutor()

    // Transaction
    fun getTransactions()                                             : LiveData<List<Transaction>>   = transactionDao.getTransactions()
    fun getTransactions(category : String)                            : LiveData<List<Transaction>>   = transactionDao.getTransactions(category)
    fun getTransactions(start : Date, end : Date)                     : LiveData<List<Transaction>>   = transactionDao.getTransactions(start, end)
    fun getTransactions(category : String, start : Date, end : Date)  : LiveData<List<Transaction>>   = transactionDao.getTransactions(category, start, end)
    fun getTransaction (id  : Int)                                    : LiveData<Transaction?>        = transactionDao.getTransaction(id)
    suspend fun getMaxIdAsync() : Deferred<Int?> = withContext(Dispatchers.IO) {
        async {transactionDao.getMaxId()}}
    suspend fun deleteTransaction(transaction : Transaction) : Job = withContext(Dispatchers.IO) {
        launch {transactionDao.delete(transaction)}}
    suspend fun insertTransaction(transaction : Transaction) : Job = withContext(Dispatchers.IO) {
        launch {transactionDao.insert(transaction)}}
    suspend fun updateTransaction(transaction : Transaction) : Job = withContext(Dispatchers.IO) {
        launch {transactionDao.update(transaction)}}

    // Category
    fun getCategorySize()  : LiveData<Int?>                = categoryDao.getCategorySize()
    fun getCategoryNames() : LiveData<List<String>>        = categoryDao.getCategoryNames()
    fun insertCategories (categories : Array<Category>) {executor.execute {categoryDao   .insert(categories)}}
    suspend fun deleteCategory(category : Category) : Job = withContext(Dispatchers.IO) {
        launch {categoryDao.delete(category)} }
    suspend fun insertCategory(category : Category) : Job = withContext(Dispatchers.IO) {
        launch {categoryDao.insert(category)}}
    suspend fun updateCategory(category : Category) : Job = withContext(Dispatchers.IO) {
        launch {categoryDao.update(category)}}

    // FutureTransaction
    suspend fun getFutureTransactionAsync(transactionId : Int) : Deferred<FutureTransaction?> = withContext(Dispatchers.IO) {
        async {futureTransactionDao.getFutureTransaction(transactionId)}}
    suspend fun deleteFutureTransaction(futureTransaction : FutureTransaction) : Job = withContext(Dispatchers.IO) {
        launch {futureTransactionDao.delete(futureTransaction)}}
    suspend fun insertFutureTransaction(futureTransaction : FutureTransaction) : Job = withContext(Dispatchers.IO) {
        launch {futureTransactionDao.insert(futureTransaction)}}
    suspend fun updateFutureTransaction(futureTransaction : FutureTransaction) : Job = withContext(Dispatchers.IO) {
        launch {futureTransactionDao.update(futureTransaction)}}

    companion object {

        private var INSTANCE : TransactionRepository? = null

        // needed to make repository a singleton (only ever one instance)
        fun initialize(context : Context) {

            if (INSTANCE == null) {

                INSTANCE = TransactionRepository(context)
            }
        }

        fun get() : TransactionRepository {

            return INSTANCE ?:
                    throw IllegalStateException("TransactionRepository must be " +
                            "initialized")
        }
    }
}