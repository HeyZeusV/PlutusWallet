package com.heyzeusv.plutuswallet.data

import androidx.lifecycle.LiveData
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.CategoryTotals
import com.heyzeusv.plutuswallet.data.model.ItemViewTransaction
import com.heyzeusv.plutuswallet.data.model.Transaction
import java.util.Date

interface Repository {
    /**
     *  Account Queries
     */
    suspend fun getAccountNamesAsync(): MutableList<String>

    suspend fun getAccountSizeAsync(): Int

    suspend fun deleteAccount(account: Account)

    suspend fun insertAccount(account: Account)

    suspend fun updateAccount(account: Account)

    fun getLDAccounts(): LiveData<List<Account>>

    /**
     *  Category Queries
     */
    suspend fun getCategoryNamesByTypeAsync(type: String): MutableList<String>

    suspend fun getCategorySizeAsync(): Int

    suspend fun deleteCategory(category: Category)

    suspend fun insertCategory(category: Category)

    suspend fun updateCategory(category: Category)

    suspend fun insertCategories(categories: List<Category>)

    fun getLDCategoriesByType(type: String): LiveData<List<Category>>

    /**
     *  Transaction Queries
     */
    suspend fun getDistinctAccountsAsync(): MutableList<String>

    suspend fun getDistinctCatsByTypeAsync(type: String): MutableList<String>

    suspend fun getFutureTransactionsAsync(currentDate: Date): List<Transaction>

    suspend fun getMaxIdAsync(): Int?

    suspend fun getTransactionAsync(id: Int): Transaction

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

    fun getLdIvt(): LiveData<List<ItemViewTransaction>>

    fun getLdIvtA(accounts: List<String>): LiveData<List<ItemViewTransaction>>

    fun getLdIvtAD(accounts: List<String>, start: Date, end: Date): LiveData<List<ItemViewTransaction>>

    fun getLdIvtAT(accounts: List<String>, type: String): LiveData<List<ItemViewTransaction>>

    fun getLdIvtATC(
        accounts: List<String>,
        type: String,
        categories: List<String>
    ): LiveData<List<ItemViewTransaction>>

    fun getLdIvtATD(
        accounts: List<String>,
        type: String,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>>

    fun getLdIvtATCD(
        accounts: List<String>,
        type: String,
        categories: List<String>,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>>

    fun getLdIvtD(start: Date, end: Date): LiveData<List<ItemViewTransaction>>

    fun getLdIvtT(type: String): LiveData<List<ItemViewTransaction>>

    fun getLdIvtTC(type: String, categories: List<String>): LiveData<List<ItemViewTransaction>>

    fun getLdIvtTCD(
        type: String,
        categories: List<String>,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>>

    fun getLdIvtTD(type: String, start: Date, end: Date): LiveData<List<ItemViewTransaction>>
}