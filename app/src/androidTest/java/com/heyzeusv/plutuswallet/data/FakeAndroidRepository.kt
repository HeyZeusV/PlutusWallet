package com.heyzeusv.plutuswallet.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.CategoryTotals
import com.heyzeusv.plutuswallet.data.model.ItemViewTransaction
import com.heyzeusv.plutuswallet.data.model.Transaction
import com.heyzeusv.plutuswallet.util.replace
import java.math.BigDecimal
import java.util.Date
import javax.inject.Inject

class FakeAndroidRepository @Inject constructor() : Repository {

    val dd = DummyAndroidDataUtil()
    val accList: MutableList<Account> = dd.accList
    val catList: MutableList<Category> = dd.catList
    val tranList: MutableList<Transaction> = dd.tranList

    override suspend fun getAccountNamesAsync(): MutableList<String> {

        val accNames: MutableList<String> = mutableListOf()
        for (acc: Account in accList) {
            accNames.add(acc.account)
        }
        accNames.sort()
        return accNames
    }

    override suspend fun getAccountSizeAsync(): Int {

        return accList.size
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
        typeNameList.sort()
        return typeNameList
    }

    override suspend fun getCategorySizeAsync(): Int {

        return catList.size
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

    override suspend fun insertCategories(categories: List<Category>) {

        catList.addAll(categories)
    }

    override fun getLDCategoriesByType(type: String): LiveData<List<Category>> {

        val list: MutableList<Category> = mutableListOf()
        for (cat: Category in catList.filter { it.type == type }) {
            list.add(cat)
        }
        return MutableLiveData(list)
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

    override suspend fun getFutureTransactionsAsync(currentDate: Date): List<Transaction> {

        val futureList: MutableList<Transaction> = mutableListOf()
        for (tran: Transaction in tranList.filter { it.date < currentDate && it.repeating && !it.futureTCreated }) {
            futureList.add(tran)
        }

        return futureList
    }

    override suspend fun getMaxIdAsync(): Int? {

        return if (tranList.isEmpty()) null else tranList[tranList.size - 1].id
    }

    override suspend fun getTransactionAsync(id: Int): Transaction {

        return tranList.single { it.id == id }
    }

    override suspend fun deleteTransaction(transaction: Transaction) {

        tranList.remove(transaction)
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

    override fun getLdTransaction(id: Int): LiveData<Transaction?> {

        return MutableLiveData(tranList.find { it.id == id })
    }

    private fun getCatLists(): List<List<String>> {

        val exCatList: MutableList<String> = mutableListOf()
        for (tran: Transaction in tranList.filter { it.type == "Expense" }) {
            exCatList.add(tran.category)
        }
        val inCatList: MutableList<String> = mutableListOf()
        for (tran: Transaction in tranList.filter { it.type == "Income" }) {
            inCatList.add(tran.category)
        }

        return listOf(exCatList.distinct(), inCatList.distinct())
    }

    private fun createCatTotals(listOfTranLists: MutableList<MutableList<Transaction>>)
            : List<CategoryTotals> {

        val catTotals: MutableList<CategoryTotals> = mutableListOf()
        for (list: MutableList<Transaction> in listOfTranLists) {
            var total = BigDecimal(0)
            for (tran: Transaction in list) {
                total += tran.total
            }
            if (list.isNotEmpty()) {
                val ct = CategoryTotals(list[0].category, total, list[0].type)
                catTotals.add(ct)
            }
        }
        return catTotals
    }

    override fun getLdCt(): LiveData<List<CategoryTotals>> {

        val catLists: List<List<String>> = getCatLists()
        val listOfTranLists: MutableList<MutableList<Transaction>> = mutableListOf()
        for (cat: String in catLists[0]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter { it.category == cat && it.type == "Expense" }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }
        for (cat: String in catLists[1]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter { it.category == cat && it.type == "Income" }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }

        return MutableLiveData(createCatTotals(listOfTranLists))
    }

    override fun getLdCtA(account: String): LiveData<List<CategoryTotals>> {

        val catLists: List<List<String>> = getCatLists()
        val listOfTranLists: MutableList<MutableList<Transaction>> = mutableListOf()
        for (cat: String in catLists[0]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Expense" && it.account == account
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }
        for (cat: String in catLists[1]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Income" && it.account == account
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }

        return MutableLiveData(createCatTotals(listOfTranLists))
    }

    override fun getLdCtAD(
        account: String,
        start: Date,
        end: Date
    ): LiveData<List<CategoryTotals>> {

        val catLists: List<List<String>> = getCatLists()
        val listOfTranLists: MutableList<MutableList<Transaction>> = mutableListOf()
        for (cat: String in catLists[0]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Expense" && it.account == account &&
                        it.date >= start && it.date <= end
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }
        for (cat: String in catLists[1]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Income" && it.account == account &&
                        it.date >= start && it.date <= end
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }

        return MutableLiveData(createCatTotals(listOfTranLists))
    }

    override fun getLdCtD(start: Date, end: Date): LiveData<List<CategoryTotals>> {

        val catLists: List<List<String>> = getCatLists()
        val listOfTranLists: MutableList<MutableList<Transaction>> = mutableListOf()
        for (cat: String in catLists[0]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Expense" && it.date >= start && it.date <= end
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }
        for (cat: String in catLists[1]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Income" && it.date >= start && it.date <= end
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }

        return MutableLiveData(createCatTotals(listOfTranLists))
    }

    override fun getLdIvt(): LiveData<List<ItemViewTransaction>> {

        val ivtList: MutableList<ItemViewTransaction> = mutableListOf()
        for (tran: Transaction in tranList) {
            val ivt = ItemViewTransaction(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        return MutableLiveData(ivtList)
    }

    override fun getLdIvtA(account: String): LiveData<List<ItemViewTransaction>> {

        val ivtList: MutableList<ItemViewTransaction> = mutableListOf()
        for (tran: Transaction in tranList.filter { it.account == account }) {
            val ivt = ItemViewTransaction(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        return MutableLiveData(ivtList)
    }

    override fun getLdIvtAD(
        account: String,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>> {

        val ivtList: MutableList<ItemViewTransaction> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            it.account == account && it.date >= start && it.date <= end
        }) {
            val ivt = ItemViewTransaction(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        return MutableLiveData(ivtList)
    }

    override fun getLdIvtAT(account: String, type: String): LiveData<List<ItemViewTransaction>> {

        val ivtList: MutableList<ItemViewTransaction> = mutableListOf()
        for (tran: Transaction in tranList.filter { it.account == account && it.type == type }) {
            val ivt = ItemViewTransaction(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        return MutableLiveData(ivtList)
    }

    override fun getLdIvtATC(
        account: String,
        type: String,
        category: String
    ): LiveData<List<ItemViewTransaction>> {

        val ivtList: MutableList<ItemViewTransaction> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            it.account == account && it.type == type && it.category == category
        }) {
            val ivt = ItemViewTransaction(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        return MutableLiveData(ivtList)
    }

    override fun getLdIvtATD(
        account: String,
        type: String,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>> {

        val ivtList: MutableList<ItemViewTransaction> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            it.account == account && it.type == type && it.date >= start && it.date <= end
        }) {
            val ivt = ItemViewTransaction(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        return MutableLiveData(ivtList)
    }

    override fun getLdIvtATCD(
        account: String,
        type: String,
        category: String,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>> {

        val ivtList: MutableList<ItemViewTransaction> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            it.account == account && it.type == type && it.category == category &&
                    it.date >= start && it.date <= end
        }) {
            val ivt = ItemViewTransaction(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        return MutableLiveData(ivtList)
    }

    override fun getLdIvtD(start: Date, end: Date): LiveData<List<ItemViewTransaction>> {

        val ivtList: MutableList<ItemViewTransaction> = mutableListOf()
        for (tran: Transaction in tranList.filter { it.date in start..end }) {
            val ivt = ItemViewTransaction(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        return MutableLiveData(ivtList)
    }

    override fun getLdIvtT(type: String): LiveData<List<ItemViewTransaction>> {

        val ivtList: MutableList<ItemViewTransaction> = mutableListOf()
        for (tran: Transaction in tranList.filter { it.type == type }) {
            val ivt = ItemViewTransaction(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        return MutableLiveData(ivtList)
    }

    override fun getLdIvtTC(type: String, category: String): LiveData<List<ItemViewTransaction>> {

        val ivtList: MutableList<ItemViewTransaction> = mutableListOf()
        for (tran: Transaction in tranList.filter { it.type == type && it.category == category }) {
            val ivt = ItemViewTransaction(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        return MutableLiveData(ivtList)
    }

    override fun getLdIvtTCD(
        type: String,
        category: String,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>> {

        val ivtList: MutableList<ItemViewTransaction> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            it.type == type && it.category == category && it.date >= start && it.date <= end
        }) {
            val ivt = ItemViewTransaction(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        return MutableLiveData(ivtList)
    }

    override fun getLdIvtTD(
        type: String,
        start: Date,
        end: Date
    ): LiveData<List<ItemViewTransaction>> {

        val ivtList: MutableList<ItemViewTransaction> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            it.type == type  && it.date >= start && it.date <= end }) {
            val ivt = ItemViewTransaction(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        return MutableLiveData(ivtList)
    }
}