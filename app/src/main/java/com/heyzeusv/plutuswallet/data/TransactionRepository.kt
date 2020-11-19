package com.heyzeusv.plutuswallet.data

import androidx.lifecycle.LiveData
import com.heyzeusv.plutuswallet.data.daos.AccountDao
import com.heyzeusv.plutuswallet.data.daos.CategoryDao
import com.heyzeusv.plutuswallet.data.daos.TransactionDao
import com.heyzeusv.plutuswallet.data.model.*
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
) : Repository {

    /**
     *  Account Queries
     */
    override suspend fun getAccountNamesAsync(): MutableList<String> =
        withContext(Dispatchers.IO) { accountDao.getAccountNames() }

    override suspend fun getAccountSizeAsync(): Deferred<Int> =
        withContext(Dispatchers.IO) { async { accountDao.getAccountSize() } }

    override suspend fun deleteAccount(account: Account): Unit =
        withContext(Dispatchers.IO) { accountDao.delete(account) }

    override suspend fun insertAccount(account: Account): Unit =
        withContext(Dispatchers.IO) { accountDao.insert(account) }

    override suspend fun updateAccount(account: Account): Unit =
        withContext(Dispatchers.IO) { accountDao.update(account) }

    override fun getLDAccounts(): LiveData<List<Account>> = accountDao.getLDAccounts()

    /**
     *  Category Queries
     */
    override suspend fun getCategoryNamesByTypeAsync(type: String): Deferred<MutableList<String>> =
        withContext(Dispatchers.IO) { async { categoryDao.getCategoryNamesByType(type) } }

    override suspend fun getCategorySizeAsync(): Deferred<Int> =
        withContext(Dispatchers.IO) { async { categoryDao.getCategorySize() } }

    override suspend fun deleteCategory(category: Category): Job =
        withContext(Dispatchers.IO) { launch { categoryDao.delete(category) } }

    override suspend fun insertCategory(category: Category): Job =
        withContext(Dispatchers.IO) { launch { categoryDao.insert(category) } }

    override suspend fun updateCategory(category: Category): Job =
        withContext(Dispatchers.IO) { launch { categoryDao.update(category) } }

    override suspend fun insertCategories(categories: List<Category>): Job =
        withContext(Dispatchers.IO) { launch { categoryDao.insert(categories) } }

    override fun getLDCategoriesByType(type: String): LiveData<List<Category>> =
        categoryDao.getLDCategoriesByType(type)

    /**
     *  Transaction Queries
     */
    override suspend fun getDistinctAccountsAsync(): MutableList<String> =
        withContext(Dispatchers.IO) { transactionDao.getDistinctAccounts() }

    override suspend fun getDistinctCatsByTypeAsync(type: String): Deferred<MutableList<String>> =
        withContext(Dispatchers.IO) { async { transactionDao.getDistinctCatsByType(type) } }

    override suspend fun getFutureTransactionsAsync(currentDate: Date): Deferred<List<Transaction>> =
        withContext(Dispatchers.IO) { async { transactionDao.getFutureTransactions(currentDate) } }

    override suspend fun getMaxIdAsync(): Deferred<Int?> =
        withContext(Dispatchers.IO) { async { transactionDao.getMaxId() } }

    override suspend fun getTransactionAsync(id: Int): Deferred<Transaction> =
        withContext(Dispatchers.IO) { async { transactionDao.getTransaction(id) } }

    override suspend fun deleteTransaction(transaction: Transaction): Job =
        withContext(Dispatchers.IO) { launch { transactionDao.delete(transaction) } }

    override suspend fun upsertTransaction(transaction: Transaction): Job =
        withContext(Dispatchers.IO) { launch { transactionDao.upsert(transaction) } }

    override suspend fun upsertTransactions(transactions: List<Transaction>): Job =
        withContext(Dispatchers.IO) { launch { transactionDao.upsert(transactions) } }

    /**
     *  Ld = LiveData
     *  Ct = CategoryTotals
     *  A  = Account
     *  C  = Category
     *  D  = Date
     *  T  = Type
     */
    override fun getLDTransaction(id: Int): LiveData<Transaction?> = transactionDao.getLDTransaction(id)

    override fun getLd(): LiveData<List<ItemViewTransaction>> = transactionDao.getLd()

    override fun getLdA(account: String): LiveData<List<ItemViewTransaction>> =
        transactionDao.getLdA(account)

    override fun getLdAD(account: String, start: Date, end: Date): LiveData<List<ItemViewTransaction>> =
        transactionDao.getLdAD(account, start, end)

    override fun getLdAT(account: String, type: String): LiveData<List<ItemViewTransaction>> =
        transactionDao.getLdAT(account, type)

    override fun getLdATC(
        account: String,
        type: String,
        category: String
    ): LiveData<List<ItemViewTransaction>> = transactionDao.getLdATC(account, type, category)

    override fun getLdATD(
        account: String,
        type: String,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>> = transactionDao.getLdATD(account, type, start, end)

    override fun getLdATCD(
        account: String,
        type: String,
        category: String,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>> =
        transactionDao.getLdATCD(account, type, category, start, end)

    override fun getLdD(start: Date, end: Date): LiveData<List<ItemViewTransaction>> =
        transactionDao.getLdD(start, end)

    override fun getLdT(type: String): LiveData<List<ItemViewTransaction>> = transactionDao.getLdT(type)

    override fun getLdTC(type: String, category: String): LiveData<List<ItemViewTransaction>> =
        transactionDao.getLdTC(type, category)

    override fun getLdTCD(
        type: String,
        category: String,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>> = transactionDao.getLdTCD(type, category, start, end)

    override fun getLdTD(type: String, start: Date, end: Date): LiveData<List<ItemViewTransaction>> =
        transactionDao.getLdTD(type, start, end)

    override fun getLdCt(): LiveData<List<CategoryTotals>> = transactionDao.getLdCt()

    override fun getLdCtA(account: String): LiveData<List<CategoryTotals>> = transactionDao.getLdCtA(account)

    override fun getLdCtAD(account: String, start: Date, end: Date): LiveData<List<CategoryTotals>> =
        transactionDao.getLdCtAD(account, start, end)

    override fun getLdCtD(start: Date, end: Date): LiveData<List<CategoryTotals>> =
        transactionDao.getLdCtD(start, end)
}