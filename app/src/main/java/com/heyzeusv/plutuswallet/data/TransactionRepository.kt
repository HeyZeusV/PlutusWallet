package com.heyzeusv.plutuswallet.data

import androidx.lifecycle.LiveData
import com.heyzeusv.plutuswallet.data.daos.AccountDao
import com.heyzeusv.plutuswallet.data.daos.CategoryDao
import com.heyzeusv.plutuswallet.data.daos.TransactionDao
import com.heyzeusv.plutuswallet.data.model.*
import kotlinx.coroutines.Dispatchers
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

    override suspend fun getAccountSizeAsync(): Int =
        withContext(Dispatchers.IO) { accountDao.getAccountSize() }

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
    override suspend fun getCategoryNamesByTypeAsync(type: String): MutableList<String> =
        withContext(Dispatchers.IO) { categoryDao.getCategoryNamesByType(type) }

    override suspend fun getCategorySizeAsync(): Int =
        withContext(Dispatchers.IO) { categoryDao.getCategorySize() }

    override suspend fun deleteCategory(category: Category): Unit =
        withContext(Dispatchers.IO) { categoryDao.delete(category) }

    override suspend fun insertCategory(category: Category): Unit =
        withContext(Dispatchers.IO) { categoryDao.insert(category) }

    override suspend fun updateCategory(category: Category): Unit =
        withContext(Dispatchers.IO) { categoryDao.update(category) }

    override suspend fun insertCategories(categories: List<Category>): Unit =
        withContext(Dispatchers.IO) { categoryDao.insert(categories) }

    override fun getLDCategoriesByType(type: String): LiveData<List<Category>> =
        categoryDao.getLDCategoriesByType(type)

    /**
     *  Transaction Queries
     */
    override suspend fun getDistinctAccountsAsync(): MutableList<String> =
        withContext(Dispatchers.IO) { transactionDao.getDistinctAccounts() }

    override suspend fun getDistinctCatsByTypeAsync(type: String): MutableList<String> =
        withContext(Dispatchers.IO) { transactionDao.getDistinctCatsByType(type) }

    override suspend fun getFutureTransactionsAsync(currentDate: Date): List<Transaction> =
        withContext(Dispatchers.IO) { transactionDao.getFutureTransactions(currentDate) }

    override suspend fun getMaxIdAsync(): Int? =
        withContext(Dispatchers.IO) { transactionDao.getMaxId() }

    override suspend fun getTransactionAsync(id: Int): Transaction =
        withContext(Dispatchers.IO) { transactionDao.getTransaction(id) }

    override suspend fun deleteTransaction(transaction: Transaction): Unit =
        withContext(Dispatchers.IO) { transactionDao.delete(transaction) }

    override suspend fun upsertTransaction(transaction: Transaction): Unit =
        withContext(Dispatchers.IO) { transactionDao.upsert(transaction) }

    override suspend fun upsertTransactions(transactions: List<Transaction>): Unit =
        withContext(Dispatchers.IO) { transactionDao.upsert(transactions) }

    /**
     *  Ld  = LiveData
     *  Ct  = CategoryTotals
     *  Ivt = ItemViewTransaction
     *  A   = Account
     *  C   = Category
     *  D   = Date
     *  T   = Type
     */
    override fun getLdTransaction(id: Int): LiveData<Transaction?> = transactionDao.getLDTransaction(id)

    override fun getLdCt(): LiveData<List<CategoryTotals>> = transactionDao.getLdCt()

    override fun getLdCtA(accounts: List<String>): LiveData<List<CategoryTotals>> = transactionDao.getLdCtA(accounts)

    override fun getLdCtAD(account: String, start: Date, end: Date): LiveData<List<CategoryTotals>> =
        transactionDao.getLdCtAD(account, start, end)

    override fun getLdCtD(start: Date, end: Date): LiveData<List<CategoryTotals>> =
        transactionDao.getLdCtD(start, end)

    override fun getLdIvt(): LiveData<List<ItemViewTransaction>> = transactionDao.getLdIvt()

    override fun getLdIvtA(accounts: List<String>): LiveData<List<ItemViewTransaction>> =
        transactionDao.getLdIvtA(accounts)

    override fun getLdIvtAD(account: String, start: Date, end: Date): LiveData<List<ItemViewTransaction>> =
        transactionDao.getLdIvtAD(account, start, end)

    override fun getLdIvtAT(account: String, type: String): LiveData<List<ItemViewTransaction>> =
        transactionDao.getLdIvtAT(account, type)

    override fun getLdIvtATC(
        account: String,
        type: String,
        category: String
    ): LiveData<List<ItemViewTransaction>> = transactionDao.getLdIvtATC(account, type, category)

    override fun getLdIvtATD(
        account: String,
        type: String,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>> = transactionDao.getLdIvtATD(account, type, start, end)

    override fun getLdIvtATCD(
        account: String,
        type: String,
        category: String,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>> =
        transactionDao.getLdIvtATCD(account, type, category, start, end)

    override fun getLdIvtD(start: Date, end: Date): LiveData<List<ItemViewTransaction>> =
        transactionDao.getLdIvtD(start, end)

    override fun getLdIvtT(type: String): LiveData<List<ItemViewTransaction>> = transactionDao.getLdIvtT(type)

    override fun getLdIvtTC(type: String, category: String): LiveData<List<ItemViewTransaction>> =
        transactionDao.getLdIvtTC(type, category)

    override fun getLdIvtTCD(
        type: String,
        category: String,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>> = transactionDao.getLdIvtTCD(type, category, start, end)

    override fun getLdIvtTD(type: String, start: Date, end: Date): LiveData<List<ItemViewTransaction>> =
        transactionDao.getLdIvtTD(type, start, end)
}