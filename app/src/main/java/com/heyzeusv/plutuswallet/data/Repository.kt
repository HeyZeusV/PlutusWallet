package com.heyzeusv.plutuswallet.data

import androidx.lifecycle.LiveData
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.CategoryTotals
import com.heyzeusv.plutuswallet.data.model.ItemViewTransaction
import com.heyzeusv.plutuswallet.data.model.Transaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import java.util.Date

interface Repository {
    /**
     *  Account Queries
     */
    suspend fun getAccountNamesAsync(): MutableList<String>

    suspend fun getAccountSizeAsync(): Deferred<Int>

    suspend fun deleteAccount(account: Account)

    suspend fun insertAccount(account: Account)

    suspend fun updateAccount(account: Account)

    fun getLDAccounts(): LiveData<List<Account>>

    /**
     *  Category Queries
     */
    suspend fun getCategoryNamesByTypeAsync(type: String): Deferred<MutableList<String>>

    suspend fun getCategorySizeAsync(): Deferred<Int>

    suspend fun deleteCategory(category: Category): Job

    suspend fun insertCategory(category: Category): Job

    suspend fun updateCategory(category: Category): Job

    suspend fun insertCategories(categories: List<Category>): Job
    fun getLDCategoriesByType(type: String): LiveData<List<Category>>

    /**
     *  Transaction Queries
     */
    suspend fun getDistinctAccountsAsync(): MutableList<String>

    suspend fun getDistinctCatsByTypeAsync(type: String): Deferred<MutableList<String>>

    suspend fun getFutureTransactionsAsync(currentDate: Date): Deferred<List<Transaction>>

    suspend fun getMaxIdAsync(): Deferred<Int?>

    suspend fun getTransactionAsync(id: Int): Deferred<Transaction>

    suspend fun deleteTransaction(transaction: Transaction): Job

    suspend fun upsertTransaction(transaction: Transaction): Job

    suspend fun upsertTransactions(transactions: List<Transaction>): Job

    /**
     *  Ld = LiveData
     *  Ct = CategoryTotals
     *  A  = Account
     *  C  = Category
     *  D  = Date
     *  T  = Type
     */
    fun getLDTransaction(id: Int): LiveData<Transaction?>
    fun getLd(): LiveData<List<ItemViewTransaction>>
    fun getLdA(account: String): LiveData<List<ItemViewTransaction>>
    fun getLdAD(account: String, start: Date, end: Date): LiveData<List<ItemViewTransaction>>
    fun getLdAT(account: String, type: String): LiveData<List<ItemViewTransaction>>
    fun getLdATC(
        account: String,
        type: String,
        category: String
    ): LiveData<List<ItemViewTransaction>>

    fun getLdATD(
        account: String,
        type: String,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>>

    fun getLdATCD(
        account: String,
        type: String,
        category: String,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>>

    fun getLdD(start: Date, end: Date): LiveData<List<ItemViewTransaction>>
    fun getLdT(type: String): LiveData<List<ItemViewTransaction>>
    fun getLdTC(type: String, category: String): LiveData<List<ItemViewTransaction>>
    fun getLdTCD(
        type: String,
        category: String,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>>

    fun getLdTD(type: String, start: Date, end: Date): LiveData<List<ItemViewTransaction>>
    fun getLdCt(): LiveData<List<CategoryTotals>>
    fun getLdCtA(account: String): LiveData<List<CategoryTotals>>
    fun getLdCtAD(account: String, start: Date, end: Date): LiveData<List<CategoryTotals>>
    fun getLdCtD(start: Date, end: Date): LiveData<List<CategoryTotals>>
}