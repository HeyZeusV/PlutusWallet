package com.heyzeusv.financeapplication

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.heyzeusv.financeapplication.database.*
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
    private val transactionDao       : TransactionDao       = database.transactionDao      ()
    private val futureTransactionDao : FutureTransactionDao = database.futureTransactionDao()
    private val expenseCategoryDao   : ExpenseCategoryDao   = database.expenseCategoryDao  ()
    private val incomeCategoryDao    : IncomeCategoryDao    = database.incomeCategoryDao   ()

    // Transaction
    fun getTransactions()                                                                : LiveData<List<Transaction>>   = transactionDao.getTransactions()
    fun getTransactions(type : String?, category : String?)                              : LiveData<List<Transaction>>   = transactionDao.getTransactions(type, category)
    fun getTransactions(type : String?, category : String?, start : Date?, end : Date?)  : LiveData<List<Transaction>>   = transactionDao.getTransactions(type, category, start, end)
    fun getTransactions(                    start : Date?, end : Date?)                  : LiveData<List<Transaction>>   = transactionDao.getTransactions(                start, end)
    fun getTransaction (id   : Int)                                                      : LiveData<Transaction?>        = transactionDao.getTransaction (id)
    suspend fun getMaxIdAsync()                              : Deferred<Int?> = withContext(Dispatchers.IO) {
        async  {transactionDao.getMaxId()}}
    suspend fun deleteTransaction(transaction : Transaction) : Job            = withContext(Dispatchers.IO) {
        launch {transactionDao.delete(transaction)}}
    suspend fun insertTransaction(transaction : Transaction) : Job            = withContext(Dispatchers.IO) {
        launch {transactionDao.insert(transaction)}}
    suspend fun updateTransaction(transaction : Transaction) : Job            = withContext(Dispatchers.IO) {
        launch {transactionDao.update(transaction)}}

    // FutureTransaction
    suspend fun getFutureTransactionAsync(transactionId   : Int)               : Deferred<FutureTransaction?> = withContext(Dispatchers.IO) {
        async {futureTransactionDao.getFutureTransaction(transactionId)}}
    suspend fun deleteFutureTransaction(futureTransaction : FutureTransaction) : Job                          = withContext(Dispatchers.IO) {
        launch {futureTransactionDao.delete(futureTransaction)}}
    suspend fun insertFutureTransaction(futureTransaction : FutureTransaction) : Job                          = withContext(Dispatchers.IO) {
        launch {futureTransactionDao.insert(futureTransaction)}}
    suspend fun updateFutureTransaction(futureTransaction : FutureTransaction) : Job                          = withContext(Dispatchers.IO) {
        launch {futureTransactionDao.update(futureTransaction)}}

    // ExpenseCategory
    fun getExpenseCategoryNames() : LiveData<List<String>> = expenseCategoryDao.getExpenseCategoryNames()
    suspend fun getExpenseCategorySizeAsync()                                       : Deferred<Int?> = withContext(Dispatchers.IO) {
        async  {expenseCategoryDao.getExpenseCategorySize()}}
    suspend fun deleteExpenseCategory(expenseCategory : ExpenseCategory)            : Job            = withContext(Dispatchers.IO) {
        launch {expenseCategoryDao.delete(expenseCategory)}}
    suspend fun insertExpenseCategory(expenseCategory : ExpenseCategory)            : Job            = withContext(Dispatchers.IO) {
        launch {expenseCategoryDao.insert(expenseCategory)}}
    suspend fun updateExpenseCategory(expenseCategory : ExpenseCategory)            : Job            = withContext(Dispatchers.IO) {
        launch {expenseCategoryDao.update(expenseCategory)}}
    suspend fun insertExpenseCategories(expenseCategories : Array<ExpenseCategory>) : Job            = withContext(Dispatchers.IO) {
        launch {expenseCategoryDao.insert(expenseCategories)}}

    // IncomeCategory
    fun getIncomeCategoryNames() : LiveData<List<String>> = incomeCategoryDao.getIncomeCategoryNames()
    suspend fun getIncomeCategorySizeAsync()                                     : Deferred<Int?> = withContext(Dispatchers.IO) {
        async  {incomeCategoryDao.getIncomeCategorySize()}}
    suspend fun deleteIncomeCategory(incomeCategory : IncomeCategory)            : Job = withContext(Dispatchers.IO) {
        launch {incomeCategoryDao.delete(incomeCategory)}}
    suspend fun insertIncomeCategory(incomeCategory : IncomeCategory)            : Job = withContext(Dispatchers.IO) {
        launch {incomeCategoryDao.insert(incomeCategory)}}
    suspend fun updateIncomeCategory(incomeCategory : IncomeCategory)            : Job = withContext(Dispatchers.IO) {
        launch {incomeCategoryDao.update(incomeCategory)}}
    suspend fun insertIncomeCategories(incomeCategories : Array<IncomeCategory>) : Job = withContext(Dispatchers.IO) {
        launch {incomeCategoryDao.insert(incomeCategories)}}

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
                    throw IllegalStateException("TransactionRepository must be initialized")
        }
    }
}