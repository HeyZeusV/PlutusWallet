package com.heyzeusv.plutuswallet.data

import androidx.lifecycle.LiveData
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.CategoryTotals
import com.heyzeusv.plutuswallet.data.model.TranListItem
import com.heyzeusv.plutuswallet.data.model.Transaction
import java.util.Date
import kotlinx.coroutines.flow.Flow

interface Repository {
    /**
     *  Account Queries
     */
    suspend fun getAccountNamesAsync(): MutableList<String>

    suspend fun getAccountNames(): Flow<List<String>>

    suspend fun getAccountsUsed(): Flow<List<Account>>

    suspend fun getAccountSizeAsync(): Int

    suspend fun deleteAccount(account: Account)

    suspend fun insertAccount(account: Account)

    suspend fun updateAccount(account: Account)

    suspend fun getAccounts(): Flow<List<Account>>

    fun getLDAccounts(): LiveData<List<Account>>

    /**
     *  Category Queries
     */
    suspend fun getCategoryNamesByTypeAsync(type: String): MutableList<String>

    suspend fun getCategoryNamesByType(type: String): Flow<List<String>>

    suspend fun getCategoriesUsedByType(type: String): Flow<List<Category>>

    suspend fun getCategorySizeAsync(): Int

    suspend fun deleteCategory(category: Category)

    suspend fun insertCategory(category: Category)

    suspend fun updateCategory(category: Category)

    suspend fun insertCategories(categories: List<Category>)

    suspend fun getCategoriesByType(type: String): Flow<List<Category>>

    fun getLDCategoriesByType(type: String): LiveData<List<Category>>

    /**
     *  Transaction Queries
     */
    suspend fun getDistinctAccountsAsync(): MutableList<String>

    suspend fun getDistinctAccounts(): Flow<List<String>>

    suspend fun getDistinctCatsByTypeAsync(type: String): MutableList<String>

    suspend fun getDistinctCatsByType(type: String): Flow<List<String>>

    suspend fun getFutureTransactionsAsync(currentDate: Date): List<Transaction>

    suspend fun getMaxId(): Flow<Int?>

    suspend fun getTransactionAsync(id: Int): Transaction?

    suspend fun deleteTransaction(transaction: Transaction)

    suspend fun upsertTransaction(transaction: Transaction)

    suspend fun upsertTransactions(transactions: List<Transaction>)

    /**
     *  Ld  = LiveData
     *  Ct  = CategoryTotals
     *  Ivt = ItemViewTransaction
     *  A   = Account
     *  C   = Category
     *  D   = Date
     *  T   = Type
     */
    fun getLdTransaction(id: Int): LiveData<Transaction?>

    fun getLdCt(): LiveData<List<CategoryTotals>>

    fun getLdCtA(accounts: List<String>): LiveData<List<CategoryTotals>>

    fun getLdCtAD(accounts: List<String>, start: Date, end: Date): LiveData<List<CategoryTotals>>

    fun getLdCtD(start: Date, end: Date): LiveData<List<CategoryTotals>>

    suspend fun getCt(): Flow<List<CategoryTotals>>

    suspend fun getCtA(accounts: List<String>): Flow<List<CategoryTotals>>

    suspend fun getCtAC(
        accounts: List<String>,
        type: String,
        categories: List<String>
    ): Flow<List<CategoryTotals>>

    suspend fun getCtAD(accounts: List<String>, start: Date, end: Date): Flow<List<CategoryTotals>>

    suspend fun getCtACD(
        accounts: List<String>,
        type: String,
        categories: List<String>,
        start: Date,
        end: Date
    ): Flow<List<CategoryTotals>>

    suspend fun getCtC(type: String, categories: List<String>): Flow<List<CategoryTotals>>

    suspend fun getCtCD(
        type: String,
        categories: List<String>,
        start: Date,
        end: Date
    ): Flow<List<CategoryTotals>>

    suspend fun getCtD(start: Date, end: Date): Flow<List<CategoryTotals>>

    suspend fun getIvt(): Flow<List<TranListItem>>

    suspend fun getIvtA(accounts: List<String>): Flow<List<TranListItem>>

    suspend fun getIvtAD(accounts: List<String>, start: Date, end: Date): Flow<List<TranListItem>>

    suspend fun getIvtAT(accounts: List<String>, type: String): Flow<List<TranListItem>>

    suspend fun getIvtATC(
        accounts: List<String>,
        type: String,
        categories: List<String>
    ): Flow<List<TranListItem>>

    suspend fun getIvtATD(
        accounts: List<String>,
        type: String,
        start: Date,
        end: Date
    ): Flow<List<TranListItem>>

    suspend fun getIvtATCD(
        accounts: List<String>,
        type: String,
        categories: List<String>,
        start: Date,
        end: Date
    ): Flow<List<TranListItem>>

    suspend fun getIvtD(start: Date, end: Date): Flow<List<TranListItem>>

    suspend fun getIvtT(type: String): Flow<List<TranListItem>>

    suspend fun getIvtTC(type: String, categories: List<String>): Flow<List<TranListItem>>

    suspend fun getIvtTCD(
        type: String,
        categories: List<String>,
        start: Date,
        end: Date
    ): Flow<List<TranListItem>>

    suspend fun getIvtTD(type: String, start: Date, end: Date): Flow<List<TranListItem>>

    fun getLdIvt(): LiveData<List<TranListItem>>

    fun getLdIvtA(accounts: List<String>): LiveData<List<TranListItem>>

    fun getLdIvtAD(accounts: List<String>, start: Date, end: Date): LiveData<List<TranListItem>>

    fun getLdIvtAT(accounts: List<String>, type: String): LiveData<List<TranListItem>>

    fun getLdIvtATC(
        accounts: List<String>,
        type: String,
        categories: List<String>
    ): LiveData<List<TranListItem>>

    fun getLdIvtATD(
        accounts: List<String>,
        type: String,
        start: Date,
        end: Date
    ): LiveData<List<TranListItem>>

    fun getLdIvtATCD(
        accounts: List<String>,
        type: String,
        categories: List<String>,
        start: Date,
        end: Date
    ): LiveData<List<TranListItem>>

    fun getLdIvtD(start: Date, end: Date): LiveData<List<TranListItem>>

    fun getLdIvtT(type: String): LiveData<List<TranListItem>>

    fun getLdIvtTC(type: String, categories: List<String>): LiveData<List<TranListItem>>

    fun getLdIvtTCD(
        type: String,
        categories: List<String>,
        start: Date,
        end: Date
    ): LiveData<List<TranListItem>>

    fun getLdIvtTD(type: String, start: Date, end: Date): LiveData<List<TranListItem>>
}