package com.heyzeusv.plutuswallet.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.CategoryTotals
import com.heyzeusv.plutuswallet.data.model.ItemViewTransaction
import com.heyzeusv.plutuswallet.data.model.Transaction
import com.heyzeusv.plutuswallet.util.replace
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import java.util.Date

class FakeRepository(
    val accList: MutableList<Account>,
    val catList: MutableList<Category>,
    val tranList: MutableList<Transaction>
) : Repository {

    override suspend fun getAccountNamesAsync(): MutableList<String> {

        val accNames: MutableList<String> = mutableListOf()
        for (acc: Account in accList) {
            accNames.add(acc.account)
        }
        return accNames.sorted() as MutableList<String>
    }

    override suspend fun getAccountSizeAsync(): Deferred<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAccount(account: Account) {

        accList.remove(account)
    }

    override suspend fun insertAccount(account: Account) {

        accList.add(account)
    }

    override suspend fun updateAccount(account: Account) {

        accList.replace(accList.find { it.id == account.id }!!, account)
    }

    override fun getLDAccounts(): LiveData<List<Account>> {

        return MutableLiveData(accList)
    }

    override suspend fun getCategoryNamesByTypeAsync(type: String): MutableList<String> {

        val typeNameList: MutableList<String> = mutableListOf()
        for (cat: Category in catList.filter { it.type == type}) {
            typeNameList.add(cat.category)
        }
        return typeNameList.sorted() as MutableList<String>
    }

    override suspend fun getCategorySizeAsync(): Deferred<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteCategory(category: Category) {

        catList.remove(category)
    }

    override suspend fun insertCategory(category: Category) {

        catList.add(category)
    }

    override suspend fun updateCategory(category: Category) {

        catList.replace(catList.find { it.id == category.id }!!, category)
    }

    override suspend fun insertCategories(categories: List<Category>): Job {
        TODO("Not yet implemented")
    }

    override fun getLDCategoriesByType(type: String): LiveData<List<Category>> {
        TODO("Not yet implemented")
    }

    override suspend fun getDistinctAccountsAsync(): MutableList<String> {

        val accList: MutableList<String> = mutableListOf()
        for (tran: Transaction in tranList) {
            accList.add(tran.account)
        }

        return accList.distinct().sorted() as MutableList<String>
    }

    override suspend fun getDistinctCatsByTypeAsync(type: String): MutableList<String> {

        val catList: MutableList<String> = mutableListOf()
        for (tran: Transaction in tranList.filter { it.type == type }) {
            catList.add(tran.category)
        }

        return catList.distinct().sorted() as MutableList<String>
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

    override suspend fun upsertTransaction(transaction: Transaction) {

        tranList.find { it.id == transaction.id }.let {
            if (it == null) tranList.add(transaction) else tranList.replace(it, transaction)
        }
    }

    override suspend fun upsertTransactions(transactions: List<Transaction>) {

        for (tran: Transaction in transactions) {
            tranList.find { it.id == tran.id }.let {
                if (it == null) tranList.add(tran) else tranList.replace(it, tran)
            }
        }
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