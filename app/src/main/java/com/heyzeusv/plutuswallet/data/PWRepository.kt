package com.heyzeusv.plutuswallet.data

import com.heyzeusv.plutuswallet.data.daos.AccountDao
import com.heyzeusv.plutuswallet.data.daos.CategoryDao
import com.heyzeusv.plutuswallet.data.daos.TransactionDao
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.CategoryTotals
import com.heyzeusv.plutuswallet.data.model.TranListItem
import com.heyzeusv.plutuswallet.data.model.Transaction
import java.time.ZonedDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 *  Interacts with Room database on behalf of the ViewModels
 *
 *  Calls upon the queries within the given Daos.
 *  Each query must be run using a CoRoutine unless it returns a Flow object.
 */
class PWRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao
) : PWRepositoryInterface {
    /**
     *  Account Queries
     */
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

    /**
     *  Category Queries
     */
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

    /**
     *  Transaction Queries
     */
    override suspend fun getFutureTransactionsAsync(currentDate: ZonedDateTime): List<Transaction> =
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
     *  Tli = TranListItem
     *  A   = Account
     *  C   = Category
     *  D   = ZonedDateTime
     *  T   = Type
     */
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
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<CategoryTotals>> = transactionDao.getCtAD(accounts, start, end)

    override suspend fun getCtACD(
        accounts: List<String>,
        type: String,
        categories: List<String>,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<CategoryTotals>> = transactionDao.getCtACD(accounts, type, categories, start, end)

    override suspend fun getCtC(
        type: String,
        categories: List<String>,
    ): Flow<List<CategoryTotals>> = transactionDao.getCtC(type, categories)

    override suspend fun getCtCD(
        type: String,
        categories: List<String>,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<CategoryTotals>> = transactionDao.getCtCD(type, categories, start, end)

    override suspend fun getCtD(
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<CategoryTotals>> = transactionDao.getCtD(start, end)

    override suspend fun getTli(): Flow<List<TranListItem>> = transactionDao.getTli()

    override suspend fun getTliA(accounts: List<String>): Flow<List<TranListItem>> =
        transactionDao.getTliA(accounts)

    override suspend fun getTliAD(
        accounts: List<String>,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<TranListItem>> = transactionDao.getTliAD(accounts, start, end)

    override suspend fun getTliAT(
        accounts: List<String>,
        type: String
    ): Flow<List<TranListItem>> = transactionDao.getTliAT(accounts, type)

    override suspend fun getTliATC(
        accounts: List<String>,
        type: String,
        categories: List<String>
    ): Flow<List<TranListItem>> = transactionDao.getTliATC(accounts, type, categories)

    override suspend fun getTliATD(
        accounts: List<String>,
        type: String,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<TranListItem>> = transactionDao.getTliATD(accounts, type, start, end)

    override suspend fun getTliATCD(
        accounts: List<String>,
        type: String,
        categories: List<String>,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<TranListItem>> =
        transactionDao.getTliATCD(accounts, type, categories, start, end)

    override suspend fun getTliD(
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<TranListItem>> = transactionDao.getTliD(start, end)

    override suspend fun getTliT(type: String): Flow<List<TranListItem>> =
        transactionDao.getTliT(type)

    override suspend fun getTliTC(
        type: String,
        categories: List<String>
    ): Flow<List<TranListItem>> = transactionDao.getTliTC(type, categories)

    override suspend fun getTliTCD(
        type: String,
        categories: List<String>,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<TranListItem>> = transactionDao.getTliTCD(type, categories, start, end)

    override suspend fun getTliTD(
        type: String,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<TranListItem>> = transactionDao.getTliTD(type, start, end)
}