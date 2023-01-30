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
import kotlinx.coroutines.flow.Flow

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
        withContext(Dispatchers.IO) { accountDao.getAccountNamesAsync() }

    override suspend fun getAccountNames(): Flow<List<String>> = accountDao.getAccountNames()

    override suspend fun getAccountsUsed(): Flow<List<Account>> = accountDao.getAccountsUsed()

    override suspend fun getAccountSizeAsync(): Int =
        withContext(Dispatchers.IO) { accountDao.getAccountSize() }

    override suspend fun deleteAccount(account: Account): Unit =
        withContext(Dispatchers.IO) { accountDao.delete(account) }

    override suspend fun insertAccount(account: Account): Unit =
        withContext(Dispatchers.IO) { accountDao.insert(account) }

    override suspend fun updateAccount(account: Account): Unit =
        withContext(Dispatchers.IO) { accountDao.update(account) }

    override suspend fun getAccounts(): Flow<List<Account>> = accountDao.getAccounts()

    override fun getLDAccounts(): LiveData<List<Account>> = accountDao.getLDAccounts()

    /**
     *  Category Queries
     */
    override suspend fun getCategoryNamesByTypeAsync(type: String): MutableList<String> =
        withContext(Dispatchers.IO) { categoryDao.getCategoryNamesByTypeAsync(type) }

    override suspend fun getCategoryNamesByType(type: String): Flow<List<String>> =
        categoryDao.getCategoryNamesByType(type)

    override suspend fun getCategoriesUsedByType(type: String): Flow<List<Category>> =
        categoryDao.getCategoriesUsedByType(type)

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

    override suspend fun getCategoriesByType(type: String): Flow<List<Category>> =
        categoryDao.getCategoriesByType(type)

    override fun getLDCategoriesByType(type: String): LiveData<List<Category>> =
        categoryDao.getLDCategoriesByType(type)

    /**
     *  Transaction Queries
     */
    override suspend fun getDistinctAccountsAsync(): MutableList<String> =
        withContext(Dispatchers.IO) { transactionDao.getDistinctAccountsAsync() }

    override suspend fun getDistinctAccounts(): Flow<List<String>> =
        transactionDao.getDistinctAccounts()

    override suspend fun getDistinctCatsByTypeAsync(type: String): MutableList<String> =
        withContext(Dispatchers.IO) { transactionDao.getDistinctCatsByTypeAsync(type) }

    override suspend fun getDistinctCatsByType(type: String): Flow<List<String>> =
        transactionDao.getDistinctCatsByType(type)

    override suspend fun getFutureTransactionsAsync(currentDate: Date): List<Transaction> =
        withContext(Dispatchers.IO) { transactionDao.getFutureTransactions(currentDate) }

    override suspend fun getMaxId(): Flow<Int?> = transactionDao.getMaxId()

    override suspend fun getTransactionAsync(id: Int): Transaction? =
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
    override fun getLdTransaction(id: Int): LiveData<Transaction?> =
        transactionDao.getLDTransaction(id)

    override fun getLdCt(): LiveData<List<CategoryTotals>> = transactionDao.getLdCt()

    override fun getLdCtA(accounts: List<String>): LiveData<List<CategoryTotals>> =
        transactionDao.getLdCtA(accounts)

    override fun getLdCtAD(
        accounts: List<String>,
        start: Date,
        end: Date
    ): LiveData<List<CategoryTotals>> = transactionDao.getLdCtAD(accounts, start, end)

    override fun getLdCtD(start: Date, end: Date): LiveData<List<CategoryTotals>> =
        transactionDao.getLdCtD(start, end)

    override suspend fun getCt(): Flow<List<CategoryTotals>> = transactionDao.getCt()

    override suspend fun getCtA(accounts: List<String>): Flow<List<CategoryTotals>> =
        transactionDao.getCtA(accounts)

    override suspend fun getCtAC(
        accounts: List<String>,
        type: String,
        categories: List<String>
    ): Flow<List<CategoryTotals>> = transactionDao.getCtAC(accounts, type, categories)

    override suspend fun getCtAD(
        accounts: List<String>,
        start: Date,
        end: Date
    ): Flow<List<CategoryTotals>> = transactionDao.getCtAD(accounts, start, end)

    override suspend fun getCtACD(
        accounts: List<String>,
        type: String,
        categories: List<String>,
        start: Date,
        end: Date
    ): Flow<List<CategoryTotals>> = transactionDao.getCtACD(accounts, type, categories, start, end)

    override suspend fun getCtC(
        type: String,
        categories: List<String>,
    ): Flow<List<CategoryTotals>> = transactionDao.getCtC(type, categories)

    override suspend fun getCtCD(
        type: String,
        categories: List<String>,
        start: Date,
        end: Date
    ): Flow<List<CategoryTotals>> = transactionDao.getCtCD(type, categories, start, end)

    override suspend fun getCtD(start: Date, end: Date): Flow<List<CategoryTotals>> =
        transactionDao.getCtD(start, end)

    override suspend fun getIvt(): Flow<List<TranListItem>> = transactionDao.getIvt()

    override suspend fun getIvtA(accounts: List<String>): Flow<List<TranListItem>> =
        transactionDao.getIvtA(accounts)

    override suspend fun getIvtAD(
        accounts: List<String>,
        start: Date,
        end: Date
    ): Flow<List<TranListItem>> = transactionDao.getIvtAD(accounts, start, end)

    override suspend fun getIvtAT(
        accounts: List<String>,
        type: String
    ): Flow<List<TranListItem>> = transactionDao.getIvtAT(accounts, type)

    override suspend fun getIvtATC(
        accounts: List<String>,
        type: String,
        categories: List<String>
    ): Flow<List<TranListItem>> = transactionDao.getIvtATC(accounts, type, categories)

    override suspend fun getIvtATD(
        accounts: List<String>,
        type: String,
        start: Date,
        end: Date
    ): Flow<List<TranListItem>> = transactionDao.getIvtATD(accounts, type, start, end)

    override suspend fun getIvtATCD(
        accounts: List<String>,
        type: String,
        categories: List<String>,
        start: Date,
        end: Date
    ): Flow<List<TranListItem>> =
        transactionDao.getIvtATCD(accounts, type, categories, start, end)

    override suspend fun getIvtD(start: Date, end: Date): Flow<List<TranListItem>> =
        transactionDao.getIvtD(start, end)

    override suspend fun getIvtT(type: String): Flow<List<TranListItem>> =
        transactionDao.getIvtT(type)

    override suspend fun getIvtTC(
        type: String,
        categories: List<String>
    ): Flow<List<TranListItem>> = transactionDao.getIvtTC(type, categories)

    override suspend fun getIvtTCD(
        type: String,
        categories: List<String>,
        start: Date,
        end: Date
    ): Flow<List<TranListItem>> = transactionDao.getIvtTCD(type, categories, start, end)

    override suspend fun getIvtTD(
        type: String,
        start: Date,
        end: Date
    ): Flow<List<TranListItem>> = transactionDao.getIvtTD(type, start, end)

    override fun getLdIvt(): LiveData<List<TranListItem>> = transactionDao.getLdIvt()

    override fun getLdIvtA(accounts: List<String>): LiveData<List<TranListItem>> =
        transactionDao.getLdIvtA(accounts)

    override fun getLdIvtAD(
        accounts: List<String>,
        start: Date,
        end: Date
    ): LiveData<List<TranListItem>> = transactionDao.getLdIvtAD(accounts, start, end)

    override fun getLdIvtAT(
        accounts: List<String>,
        type: String
    ): LiveData<List<TranListItem>> = transactionDao.getLdIvtAT(accounts, type)

    override fun getLdIvtATC(
        accounts: List<String>,
        type: String,
        categories: List<String>
    ): LiveData<List<TranListItem>> = transactionDao.getLdIvtATC(accounts, type, categories)

    override fun getLdIvtATD(
        accounts: List<String>,
        type: String,
        start: Date,
        end: Date
    ): LiveData<List<TranListItem>> = transactionDao.getLdIvtATD(accounts, type, start, end)

    override fun getLdIvtATCD(
        accounts: List<String>,
        type: String,
        categories: List<String>,
        start: Date,
        end: Date
    ): LiveData<List<TranListItem>> =
        transactionDao.getLdIvtATCD(accounts, type, categories, start, end)

    override fun getLdIvtD(start: Date, end: Date): LiveData<List<TranListItem>> =
        transactionDao.getLdIvtD(start, end)

    override fun getLdIvtT(type: String): LiveData<List<TranListItem>> =
        transactionDao.getLdIvtT(type)

    override fun getLdIvtTC(
        type: String,
        categories: List<String>
    ): LiveData<List<TranListItem>> = transactionDao.getLdIvtTC(type, categories)

    override fun getLdIvtTCD(
        type: String,
        categories: List<String>,
        start: Date,
        end: Date
    ): LiveData<List<TranListItem>> = transactionDao.getLdIvtTCD(type, categories, start, end)

    override fun getLdIvtTD(
        type: String,
        start: Date,
        end: Date
    ): LiveData<List<TranListItem>> = transactionDao.getLdIvtTD(type, start, end)
}