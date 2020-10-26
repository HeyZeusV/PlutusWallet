package com.heyzeusv.plutuswallet.database

import androidx.lifecycle.LiveData
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
import javax.inject.Inject

/**
 *  Interacts with Room database on behalf of the ViewModels
 *
 *  Calls upon the queries within the Daos.
 *  Each query must be run using a CoRoutine unless it returns a LiveData object.
 */
class TransactionRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao
) {

    /**
     *  Account Queries
     */
    suspend fun getAccountNamesAsync(): Deferred<MutableList<String>> =
        withContext(Dispatchers.IO) { async { accountDao.getAccountNames() } }

    suspend fun getAccountSizeAsync(): Deferred<Int> =
        withContext(Dispatchers.IO) { async { accountDao.getAccountSize() } }

    suspend fun deleteAccount(account: Account): Job =
        withContext(Dispatchers.IO) { launch { accountDao.delete(account) } }

    suspend fun insertAccount(account: Account): Job =
        withContext(Dispatchers.IO) { launch { accountDao.insert(account) } }

    suspend fun updateAccount(account: Account): Job =
        withContext(Dispatchers.IO) { launch { accountDao.update(account) } }

    fun getLDAccounts(): LiveData<List<Account>> = accountDao.getLDAccounts()

    /**
     *  Category Queries
     */
    suspend fun getCategoryNamesByTypeAsync(type: String): Deferred<MutableList<String>> =
        withContext(Dispatchers.IO) { async { categoryDao.getCategoryNamesByType(type) } }

    suspend fun getCategorySizeAsync(): Deferred<Int> =
        withContext(Dispatchers.IO) { async { categoryDao.getCategorySize() } }

    suspend fun deleteCategory(category: Category): Job =
        withContext(Dispatchers.IO) { launch { categoryDao.delete(category) } }

    suspend fun insertCategory(category: Category): Job =
        withContext(Dispatchers.IO) { launch { categoryDao.insert(category) } }

    suspend fun updateCategory(category: Category): Job =
        withContext(Dispatchers.IO) { launch { categoryDao.update(category) } }

    suspend fun insertCategories(categories: List<Category>): Job =
        withContext(Dispatchers.IO) { launch { categoryDao.insert(categories) } }

    fun getLDCategoriesByType(type: String): LiveData<List<Category>> =
        categoryDao.getLDCategoriesByType(type)

    /**
     *  Transaction Queries
     */
    suspend fun getDistinctAccountsAsync(): Deferred<MutableList<String>> =
        withContext(Dispatchers.IO) { async { transactionDao.getDistinctAccounts() } }

    suspend fun getDistinctCatsByTypeAsync(type: String): Deferred<List<String>> =
        withContext(Dispatchers.IO) { async { transactionDao.getDistinctCatsByType(type) } }

    suspend fun getFutureTransactionsAsync(currentDate: Date): Deferred<List<Transaction>> =
        withContext(Dispatchers.IO) { async { transactionDao.getFutureTransactions(currentDate) } }

    suspend fun getMaxIdAsync(): Deferred<Int?> =
        withContext(Dispatchers.IO) { async { transactionDao.getMaxId() } }

    suspend fun getTransactionAsync(id: Int): Deferred<Transaction> =
        withContext(Dispatchers.IO) { async { transactionDao.getTransaction(id) } }

    suspend fun deleteTransaction(transaction: Transaction): Job =
        withContext(Dispatchers.IO) { launch { transactionDao.delete(transaction) } }

    suspend fun upsertTransaction(transaction: Transaction): Job =
        withContext(Dispatchers.IO) { launch { transactionDao.upsert(transaction) } }

    suspend fun upsertTransactions(transactions: List<Transaction>): Job =
        withContext(Dispatchers.IO) { launch { transactionDao.upsert(transactions) } }

    /**
     *  Ld = LiveData
     *  Ct = CategoryTotals
     *  A  = Account
     *  C  = Category
     *  D  = Date
     *  T  = Type
     */
    fun getLDTransaction(id: Int): LiveData<Transaction?> = transactionDao.getLDTransaction(id)

    fun getLd(): LiveData<List<ItemViewTransaction>> = transactionDao.getLd()

    fun getLdA(account: String): LiveData<List<ItemViewTransaction>> =
        transactionDao.getLdA(account)

    fun getLdAD(account: String, start: Date, end: Date): LiveData<List<ItemViewTransaction>> =
        transactionDao.getLdAD(account, start, end)

    fun getLdAT(account: String, type: String): LiveData<List<ItemViewTransaction>> =
        transactionDao.getLdAT(account, type)

    fun getLdATC(
        account: String,
        type: String,
        category: String
    ): LiveData<List<ItemViewTransaction>> = transactionDao.getLdATC(account, type, category)

    fun getLdATD(
        account: String,
        type: String,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>> = transactionDao.getLdATD(account, type, start, end)

    fun getLdATCD(
        account: String,
        type: String,
        category: String,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>> =
        transactionDao.getLdATCD(account, type, category, start, end)

    fun getLdD(start: Date, end: Date): LiveData<List<ItemViewTransaction>> =
        transactionDao.getLdD(start, end)

    fun getLdT(type: String): LiveData<List<ItemViewTransaction>> = transactionDao.getLdT(type)

    fun getLdTC(type: String, category: String): LiveData<List<ItemViewTransaction>> =
        transactionDao.getLdTC(type, category)

    fun getLdTCD(
        type: String,
        category: String,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>> = transactionDao.getLdTCD(type, category, start, end)

    fun getLdTD(type: String, start: Date, end: Date): LiveData<List<ItemViewTransaction>> =
        transactionDao.getLdTD(type, start, end)

    fun getLdCt(): LiveData<List<CategoryTotals>> = transactionDao.getLdCt()

    fun getLdCtA(account: String): LiveData<List<CategoryTotals>> = transactionDao.getLdCtA(account)

    fun getLdCtAD(account: String, start: Date, end: Date): LiveData<List<CategoryTotals>> =
        transactionDao.getLdCtAD(account, start, end)

    fun getLdCtD(start: Date, end: Date): LiveData<List<CategoryTotals>> =
        transactionDao.getLdCtD(start, end)
}