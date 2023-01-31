package com.heyzeusv.plutuswallet.data

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
    suspend fun getAccountNames(): Flow<List<String>>

    suspend fun getAccountsUsed(): Flow<List<Account>>

    suspend fun getAccountSizeAsync(): Int

    suspend fun deleteAccount(account: Account)

    suspend fun insertAccount(account: Account)

    suspend fun updateAccount(account: Account)

    suspend fun getAccounts(): Flow<List<Account>>

    /**
     *  Category Queries
     */
    suspend fun getCategoryNamesByType(type: String): Flow<List<String>>

    suspend fun getCategoriesUsedByType(type: String): Flow<List<Category>>

    suspend fun getCategorySizeAsync(): Int

    suspend fun deleteCategory(category: Category)

    suspend fun insertCategory(category: Category)

    suspend fun updateCategory(category: Category)

    suspend fun insertCategories(categories: List<Category>)

    suspend fun getCategoriesByType(type: String): Flow<List<Category>>

    /**
     *  Transaction Queries
     */
    suspend fun getFutureTransactionsAsync(currentDate: Date): List<Transaction>

    suspend fun getMaxId(): Flow<Int?>

    suspend fun getTransactionAsync(id: Int): Transaction?

    suspend fun deleteTransaction(transaction: Transaction)

    suspend fun upsertTransaction(transaction: Transaction)

    suspend fun upsertTransactions(transactions: List<Transaction>)

    /**
     *  Ct  = CategoryTotals
     *  Tli = TranListItem
     *  A   = Account
     *  C   = Category
     *  D   = Date
     *  T   = Type
     */
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

    suspend fun getTli(): Flow<List<TranListItem>>

    suspend fun getTliA(accounts: List<String>): Flow<List<TranListItem>>

    suspend fun getTliAD(accounts: List<String>, start: Date, end: Date): Flow<List<TranListItem>>

    suspend fun getTliAT(accounts: List<String>, type: String): Flow<List<TranListItem>>

    suspend fun getTliATC(
        accounts: List<String>,
        type: String,
        categories: List<String>
    ): Flow<List<TranListItem>>

    suspend fun getTliATD(
        accounts: List<String>,
        type: String,
        start: Date,
        end: Date
    ): Flow<List<TranListItem>>

    suspend fun getTliATCD(
        accounts: List<String>,
        type: String,
        categories: List<String>,
        start: Date,
        end: Date
    ): Flow<List<TranListItem>>

    suspend fun getTliD(start: Date, end: Date): Flow<List<TranListItem>>

    suspend fun getTliT(type: String): Flow<List<TranListItem>>

    suspend fun getTliTC(type: String, categories: List<String>): Flow<List<TranListItem>>

    suspend fun getTliTCD(
        type: String,
        categories: List<String>,
        start: Date,
        end: Date
    ): Flow<List<TranListItem>>

    suspend fun getTliTD(type: String, start: Date, end: Date): Flow<List<TranListItem>>
}