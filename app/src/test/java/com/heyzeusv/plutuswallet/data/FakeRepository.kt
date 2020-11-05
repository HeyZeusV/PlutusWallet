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

class FakeRepository : Repository {

    override suspend fun getAccountNamesAsync(): Deferred<MutableList<String>> {
        TODO("Not yet implemented")
    }

    override suspend fun getAccountSizeAsync(): Deferred<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAccount(account: Account): Job {
        TODO("Not yet implemented")
    }

    override suspend fun insertAccount(account: Account): Job {
        TODO("Not yet implemented")
    }

    override suspend fun updateAccount(account: Account): Job {
        TODO("Not yet implemented")
    }

    override fun getLDAccounts(): LiveData<List<Account>> {
        TODO("Not yet implemented")
    }

    override suspend fun getCategoryNamesByTypeAsync(type: String): Deferred<MutableList<String>> {
        TODO("Not yet implemented")
    }

    override suspend fun getCategorySizeAsync(): Deferred<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteCategory(category: Category): Job {
        TODO("Not yet implemented")
    }

    override suspend fun insertCategory(category: Category): Job {
        TODO("Not yet implemented")
    }

    override suspend fun updateCategory(category: Category): Job {
        TODO("Not yet implemented")
    }

    override suspend fun insertCategories(categories: List<Category>): Job {
        TODO("Not yet implemented")
    }

    override fun getLDCategoriesByType(type: String): LiveData<List<Category>> {
        TODO("Not yet implemented")
    }

    override suspend fun getDistinctAccountsAsync(): Deferred<MutableList<String>> {
        TODO("Not yet implemented")
    }

    override suspend fun getDistinctCatsByTypeAsync(type: String): Deferred<MutableList<String>> {
        TODO("Not yet implemented")
    }

    override suspend fun getFutureTransactionsAsync(currentDate: Date): Deferred<List<Transaction>> {
        TODO("Not yet implemented")
    }

    override suspend fun getMaxIdAsync(): Deferred<Int?> {
        TODO("Not yet implemented")
    }

    override suspend fun getTransactionAsync(id: Int): Deferred<Transaction> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteTransaction(transaction: Transaction): Job {
        TODO("Not yet implemented")
    }

    override suspend fun upsertTransaction(transaction: Transaction): Job {
        TODO("Not yet implemented")
    }

    override suspend fun upsertTransactions(transactions: List<Transaction>): Job {
        TODO("Not yet implemented")
    }

    override fun getLDTransaction(id: Int): LiveData<Transaction?> {
        TODO("Not yet implemented")
    }

    override fun getLd(): LiveData<List<ItemViewTransaction>> {
        TODO("Not yet implemented")
    }

    override fun getLdA(account: String): LiveData<List<ItemViewTransaction>> {
        TODO("Not yet implemented")
    }

    override fun getLdAD(
        account: String,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>> {
        TODO("Not yet implemented")
    }

    override fun getLdAT(account: String, type: String): LiveData<List<ItemViewTransaction>> {
        TODO("Not yet implemented")
    }

    override fun getLdATC(
        account: String,
        type: String,
        category: String
    ): LiveData<List<ItemViewTransaction>> {
        TODO("Not yet implemented")
    }

    override fun getLdATD(
        account: String,
        type: String,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>> {
        TODO("Not yet implemented")
    }

    override fun getLdATCD(
        account: String,
        type: String,
        category: String,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>> {
        TODO("Not yet implemented")
    }

    override fun getLdD(start: Date, end: Date): LiveData<List<ItemViewTransaction>> {
        TODO("Not yet implemented")
    }

    override fun getLdT(type: String): LiveData<List<ItemViewTransaction>> {
        TODO("Not yet implemented")
    }

    override fun getLdTC(type: String, category: String): LiveData<List<ItemViewTransaction>> {
        TODO("Not yet implemented")
    }

    override fun getLdTCD(
        type: String,
        category: String,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>> {
        TODO("Not yet implemented")
    }

    override fun getLdTD(
        type: String,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>> {
        TODO("Not yet implemented")
    }

    override fun getLdCt(): LiveData<List<CategoryTotals>> {
        TODO("Not yet implemented")
    }

    override fun getLdCtA(account: String): LiveData<List<CategoryTotals>> {
        TODO("Not yet implemented")
    }

    override fun getLdCtAD(
        account: String,
        start: Date,
        end: Date
    ): LiveData<List<CategoryTotals>> {
        TODO("Not yet implemented")
    }

    override fun getLdCtD(start: Date, end: Date): LiveData<List<CategoryTotals>> {
        TODO("Not yet implemented")
    }


}