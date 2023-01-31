package com.heyzeusv.plutuswallet.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.CategoryTotals
import com.heyzeusv.plutuswallet.data.model.TranListItem
import com.heyzeusv.plutuswallet.data.model.Transaction
import com.heyzeusv.plutuswallet.ui.transaction.TransactionType.EXPENSE
import com.heyzeusv.plutuswallet.ui.transaction.TransactionType.INCOME
import com.heyzeusv.plutuswallet.util.replace
import java.math.BigDecimal
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow

class FakeAndroidRepository @Inject constructor() : Repository {

    val dd = DummyDataUtil()
    var accList: MutableList<Account> = dd.accList
    var catList: MutableList<Category> = dd.catList
    var tranList: MutableList<Transaction> = dd.tranList

    private val accountListFlow = MutableSharedFlow<List<Account>>()
    suspend fun accountListEmit(value: List<Account>) = accountListFlow.emit(value.sortedBy { it.name })
    private val accountsUsedListFlow = MutableSharedFlow<List<Account>>()
    suspend fun accountsUsedListEmit(value: List<Account>) = accountsUsedListFlow.emit(value)
    private val accountNameListFLow = MutableSharedFlow<List<String>>()
    suspend fun accountNameListEmit(value: List<String>) = accountNameListFLow.emit(value.sorted())
    private val expenseCatNameListFlow = MutableSharedFlow<List<String>>()
    suspend fun expenseCatNameListEmit(value: List<String>) = expenseCatNameListFlow.emit(value.sorted())
    private val incomeCatNameListFlow = MutableSharedFlow<List<String>>()
    suspend fun incomeCatNameListEmit(value: List<String>) = incomeCatNameListFlow.emit(value.sorted())
    private val expenseCatListFlow = MutableSharedFlow<List<Category>>()
    suspend fun expenseCatListEmit(value: List<Category>) = expenseCatListFlow.emit(value.sortedBy { it.name })
    private val incomeCatListFlow = MutableSharedFlow<List<Category>>()
    suspend fun incomeCatListEmit(value: List<Category>) = incomeCatListFlow.emit(value.sortedBy { it.name })
    private val expenseCatUsedListFlow = MutableSharedFlow<List<Category>>()
    suspend fun expenseCatUsedListEmit(value: List<Category>) = expenseCatUsedListFlow.emit(value)
    private val incomeCatUsedListFLow = MutableSharedFlow<List<Category>>()
    suspend fun incomeCatUsedListEmit(value: List<Category>) = incomeCatUsedListFLow.emit(value)

    private val accListLD = MutableLiveData(accList.sortedBy { it.name })
    private val catExListLD =
        MutableLiveData(catList.filter { it.type == "Expense" }.sortedBy { it.name })
    private val catInListLD =
        MutableLiveData(catList.filter { it.type == "Income" }.sortedBy { it.name })
    private val ivtListLD = MutableLiveData<List<TranListItem>>(emptyList())

    fun resetLists() {
        accList = dd.accList
        catList = dd.catList
        tranList = dd.tranList
    }

    override suspend fun getAccountNamesAsync(): MutableList<String> {

        val accNames: MutableList<String> = mutableListOf()
        for (acc: Account in accList) {
            accNames.add(acc.name)
        }
        accNames.sort()
        return accNames
    }

    override suspend fun getAccountNames(): Flow<List<String>> {
        val accNames: MutableList<String> = mutableListOf()
        for (acc: Account in accList) {
            accNames.add(acc.name)
        }
        accNames.sort()
        return flow { emit(accNames) }

    }

    override suspend fun getAccountsUsed(): Flow<List<Account>> {
//        val accUsed: MutableList<String> = mutableListOf()
//        for (tran: Transaction in tranList) {
//            accUsed.add(tran.account)
//        }
//        flow { emit(accList.filter { accUsed.contains(it.name) }.distinct()) }
        return accountsUsedListFlow
    }

    override suspend fun getAccountSizeAsync(): Int {
        return accList.size
    }

    override suspend fun deleteAccount(account: Account) {
        accList.remove(account)
        accountListEmit(accList)
        accListLD.value = accList.sortedBy { it.name }
    }

    override suspend fun insertAccount(account: Account) {
        accList.add(account)
        accountListEmit(accList)
        accountNameListEmit(accList.map { it.name })
        accListLD.value = accList.sortedBy { it.name }
    }

    override suspend fun updateAccount(account: Account) {
        accList.replace(accList.find { it.id == account.id }!!, account)
        accountListEmit(accList)
        accListLD.value = accList.sortedBy { it.name }
    }

    override suspend fun getAccounts(): Flow<List<Account>> {
//        flow { emit(accList.sortedBy { it.name }) }
        return accountListFlow
    }

    override fun getLDAccounts(): LiveData<List<Account>> {

        accListLD.value = accList.sortedBy { it.name }
        return accListLD
    }

    override suspend fun getCategoryNamesByTypeAsync(type: String): MutableList<String> {

        val typeNameList: MutableList<String> = mutableListOf()
        for (cat: Category in catList.filter { it.type == type}) {
            typeNameList.add(cat.name)
        }
        typeNameList.sort()
        return typeNameList
    }

    override suspend fun getCategoryNamesByType(type: String): Flow<List<String>> {
        val typeNameList: MutableList<String> = mutableListOf()
        for (cat: Category in catList.filter { it.type == type}) {
            typeNameList.add(cat.name)
        }
        typeNameList.sort()
        return flow { emit(typeNameList) }

    }

    override suspend fun getCategoriesUsedByType(type: String): Flow<List<Category>> {
        return if (type == EXPENSE.type) expenseCatUsedListFlow else incomeCatUsedListFLow
    }

    override suspend fun getCategoriesByType(type: String): Flow<List<Category>> {
        return if (type == EXPENSE.type) expenseCatListFlow else incomeCatListFlow
    }

    override suspend fun getCategorySizeAsync(): Int {

        return catList.size
    }

    override suspend fun deleteCategory(category: Category) {
        catList.remove(category)
        expenseCatListEmit(dd.catList.filter { it.type == EXPENSE.type })
        incomeCatListEmit(dd.catList.filter { it.type == INCOME.type })
        catExListLD.postValue(catList.filter { it.type == "Expense" }.sortedBy { it.name })
        catInListLD.postValue(catList.filter { it.type == "Income" }.sortedBy { it.name })
    }

    override suspend fun insertCategory(category: Category) {
        catList.add(category)
        expenseCatListEmit(dd.catList.filter { it.type == EXPENSE.type })
        incomeCatListEmit(dd.catList.filter { it.type == INCOME.type })
        expenseCatNameListEmit(dd.catList.filter { it.type == EXPENSE.type }.map { it.name })
        incomeCatNameListEmit(dd.catList.filter { it.type == INCOME.type }.map { it.name })
        catExListLD.postValue(catList.filter { it.type == "Expense" }.sortedBy { it.name })
        catInListLD.postValue(catList.filter { it.type == "Income" }.sortedBy { it.name })
    }

    override suspend fun updateCategory(category: Category) {
        catList.replace(catList.find { it.id == category.id }!!, category)
        catExListLD.postValue(catList.filter { it.type == "Expense" }.sortedBy { it.name })
        catInListLD.postValue(catList.filter { it.type == "Income" }.sortedBy { it.name })
    }

    override suspend fun insertCategories(categories: List<Category>) {
        catList.addAll(categories)
        expenseCatNameListEmit(dd.catList.filter { it.type == EXPENSE.type }.map { it.name })
        incomeCatNameListEmit(dd.catList.filter { it.type == INCOME.type }.map { it.name })
        catExListLD.postValue(catList.filter { it.type == "Expense" }.sortedBy { it.name })
        catInListLD.postValue(catList.filter { it.type == "Income" }.sortedBy { it.name })
    }

    override fun getLDCategoriesByType(type: String): LiveData<List<Category>> {

        return when (type) {
            "Expense" -> catExListLD
            else -> catInListLD
        }
    }

    override suspend fun getDistinctAccountsAsync(): MutableList<String> {

        val accList: MutableList<String> = mutableListOf()
        for (tran: Transaction in tranList) {
            accList.add(tran.account)
        }

        return accList.distinct().sorted() as MutableList<String>
    }

    override suspend fun getDistinctAccounts(): Flow<List<String>> {
        val accList: MutableList<String> = mutableListOf()
        for (tran: Transaction in tranList) {
            accList.add(tran.account)
        }

        return flow { emit(accList.distinct().sorted()) }
    }

    override suspend fun getDistinctCatsByTypeAsync(type: String): MutableList<String> {

        val catList: MutableList<String> = mutableListOf()
        for (tran: Transaction in tranList.filter { it.type == type }) {
            catList.add(tran.category)
        }

        return catList.distinct().sorted() as MutableList<String>
    }

    override suspend fun getDistinctCatsByType(type: String): Flow<List<String>> {
        val catList: MutableList<String> = mutableListOf()
        for (tran: Transaction in tranList.filter { it.type == type }) {
            catList.add(tran.category)
        }

        return flow { emit(catList.distinct().sorted()) }
    }

    override suspend fun getFutureTransactionsAsync(currentDate: Date): List<Transaction> {

        val futureList: MutableList<Transaction> = mutableListOf()
        for (tran: Transaction in tranList.filter { it.date < currentDate && it.repeating && !it.futureTCreated }) {
            futureList.add(tran)
        }

        return futureList
    }

    override suspend fun getMaxId(): Flow<Int?> {

        return if (tranList.isEmpty()) {
            flow { emit(null) }
        } else {
            flow { emit(tranList[tranList.size - 1].id) }
        }
    }

    override suspend fun getTransactionAsync(id: Int): Transaction? = tranList.find { it.id == id }

    override suspend fun deleteTransaction(transaction: Transaction) {
        tranList.remove(transaction)
        if (ivtListLD.value!!.isNotEmpty()) {
            (ivtListLD.value as MutableList).removeIf { it.id == transaction.id }
            ivtListLD.postValue(ivtListLD.value!!)
        }
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

    override fun getLdCtA(accounts: List<String>): LiveData<List<CategoryTotals>> {

        val catLists: List<List<String>> = getCatLists()
        val listOfTranLists: MutableList<MutableList<Transaction>> = mutableListOf()
        for (cat: String in catLists[0]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Expense" && accounts.contains(it.account)
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }
        for (cat: String in catLists[1]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Income" && accounts.contains(it.account)
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }

        return MutableLiveData(createCatTotals(listOfTranLists))
    }

    override fun getLdCtAD(
        accounts: List<String>,
        start: Date,
        end: Date
    ): LiveData<List<CategoryTotals>> {

        val catLists: List<List<String>> = getCatLists()
        val listOfTranLists: MutableList<MutableList<Transaction>> = mutableListOf()
        for (cat: String in catLists[0]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Expense" && accounts.contains(it.account) &&
                        it.date >= start && it.date <= end
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }
        for (cat: String in catLists[1]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Income" && accounts.contains(it.account) &&
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

    override suspend fun getCt(): Flow<List<CategoryTotals>> {
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

        return flow { emit(createCatTotals(listOfTranLists)) }
    }

    override suspend fun getCtA(accounts: List<String>): Flow<List<CategoryTotals>> {
        val catLists: List<List<String>> = getCatLists()
        val listOfTranLists: MutableList<MutableList<Transaction>> = mutableListOf()
        for (cat: String in catLists[0]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Expense" && accounts.contains(it.account)
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }
        for (cat: String in catLists[1]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Income" && accounts.contains(it.account)
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }

        return flow { emit(createCatTotals(listOfTranLists)) }
    }

    override suspend fun getCtAC(
        accounts: List<String>,
        type: String,
        categories: List<String>
    ): Flow<List<CategoryTotals>> {
        val catLists: List<List<String>> = getCatLists()
        val listOfTranLists: MutableList<MutableList<Transaction>> = mutableListOf()
        for (cat: String in catLists[0]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Expense" && accounts.contains(it.account) &&
                        categories.contains(it.category)
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }
        for (cat: String in catLists[1]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Income" && accounts.contains(it.account) &&
                        categories.contains(it.category)
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }

        return flow { emit(createCatTotals(listOfTranLists)) }
    }

    override suspend fun getCtAD(
        accounts: List<String>,
        start: Date,
        end: Date
    ): Flow<List<CategoryTotals>> {
        val catLists: List<List<String>> = getCatLists()
        val listOfTranLists: MutableList<MutableList<Transaction>> = mutableListOf()
        for (cat: String in catLists[0]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Expense" && accounts.contains(it.account) &&
                        it.date >= start && it.date <= end
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }
        for (cat: String in catLists[1]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Income" && accounts.contains(it.account) &&
                        it.date >= start && it.date <= end
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }

        return flow { emit(createCatTotals(listOfTranLists)) }
    }

    override suspend fun getCtACD(
        accounts: List<String>,
        type: String,
        categories: List<String>,
        start: Date,
        end: Date
    ): Flow<List<CategoryTotals>> {
        val catLists: List<List<String>> = getCatLists()
        val listOfTranLists: MutableList<MutableList<Transaction>> = mutableListOf()
        for (cat: String in catLists[0]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Expense" && accounts.contains(it.account) &&
                        categories.contains(it.category) && it.date >= start && it.date <= end
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }
        for (cat: String in catLists[1]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Income" && accounts.contains(it.account) &&
                        categories.contains(it.category) && it.date >= start && it.date <= end
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }

        return flow { emit(createCatTotals(listOfTranLists)) }
    }

    override suspend fun getCtC(type: String, categories: List<String>): Flow<List<CategoryTotals>> {
        val catLists: List<List<String>> = getCatLists()
        val listOfTranLists: MutableList<MutableList<Transaction>> = mutableListOf()
        for (cat: String in catLists[0]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Expense" && categories.contains(it.category)
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }
        for (cat: String in catLists[1]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Income" && categories.contains(it.category)
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }

        return flow { emit(createCatTotals(listOfTranLists)) }
    }

    override suspend fun getCtCD(
        type: String,
        categories: List<String>,
        start: Date,
        end: Date
    ): Flow<List<CategoryTotals>> {
        val catLists: List<List<String>> = getCatLists()
        val listOfTranLists: MutableList<MutableList<Transaction>> = mutableListOf()
        for (cat: String in catLists[0]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Expense" && categories.contains(it.category) &&
                        it.date >= start && it.date <= end
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }
        for (cat: String in catLists[1]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Income" && categories.contains(it.category) &&
                        it.date >= start && it.date <= end
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }

        return flow { emit(createCatTotals(listOfTranLists)) }
    }

    override suspend fun getCtD(start: Date, end: Date): Flow<List<CategoryTotals>> {
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

        return flow { emit(createCatTotals(listOfTranLists)) }
    }

    override fun getLdIvt(): LiveData<List<TranListItem>> {

        val ivtList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList) {
            val ivt = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        ivtListLD.value = ivtList
        return ivtListLD
    }

    override fun getLdIvtA(accounts: List<String>): LiveData<List<TranListItem>> {

        val ivtList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter { accounts.contains(it.account) }) {
            val ivt = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        ivtListLD.value = ivtList
        return ivtListLD
    }

    override fun getLdIvtAD(
        accounts: List<String>,
        start: Date,
        end: Date
    ): LiveData<List<TranListItem>> {

        val ivtList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            accounts.contains(it.account) && it.date >= start && it.date <= end
        }) {
            val ivt = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        ivtListLD.value = ivtList
        return ivtListLD
    }

    override fun getLdIvtAT(
        accounts: List<String>,
        type: String
    ): LiveData<List<TranListItem>> {

        val ivtList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter { accounts.contains(it.account) && it.type == type }) {
            val ivt = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        ivtListLD.value = ivtList
        return ivtListLD
    }

    override fun getLdIvtATC(
        accounts: List<String>,
        type: String,
        categories: List<String>
    ): LiveData<List<TranListItem>> {

        val ivtList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            accounts.contains(it.account) && it.type == type && categories.contains(it.category)
        }) {
            val ivt = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        ivtListLD.value = ivtList
        return ivtListLD
    }

    override fun getLdIvtATD(
        accounts: List<String>,
        type: String,
        start: Date,
        end: Date
    ): LiveData<List<TranListItem>> {

        val ivtList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            accounts.contains(it.account) && it.type == type && it.date >= start && it.date <= end
        }) {
            val ivt = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        ivtListLD.value = ivtList
        return ivtListLD
    }

    override fun getLdIvtATCD(
        accounts: List<String>,
        type: String,
        categories: List<String>,
        start: Date,
        end: Date
    ): LiveData<List<TranListItem>> {

        val ivtList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            accounts.contains(it.account) && it.type == type && categories.contains(it.category) &&
                    it.date >= start && it.date <= end
        }) {
            val ivt = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        ivtListLD.value = ivtList
        return ivtListLD
    }

    override fun getLdIvtD(start: Date, end: Date): LiveData<List<TranListItem>> {

        val ivtList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter { it.date in start..end }) {
            val ivt = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        ivtListLD.value = ivtList
        return ivtListLD
    }

    override fun getLdIvtT(type: String): LiveData<List<TranListItem>> {

        val ivtList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter { it.type == type }) {
            val ivt = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        ivtListLD.value = ivtList
        return ivtListLD
    }

    override fun getLdIvtTC(
        type: String,
        categories: List<String>
    ): LiveData<List<TranListItem>> {

        val ivtList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter { it.type == type && categories.contains(it.category) }) {
            val ivt = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        ivtListLD.value = ivtList
        return ivtListLD
    }

    override fun getLdIvtTCD(
        type: String,
        categories: List<String>,
        start: Date,
        end: Date
    ): LiveData<List<TranListItem>> {

        val ivtList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            it.type == type && categories.contains(it.category) && it.date >= start && it.date <= end
        }) {
            val ivt = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        ivtListLD.value = ivtList
        return ivtListLD
    }

    override fun getLdIvtTD(
        type: String,
        start: Date,
        end: Date
    ): LiveData<List<TranListItem>> {

        val ivtList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            it.type == type  && it.date >= start && it.date <= end }) {
            val ivt = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        ivtListLD.value = ivtList
        return ivtListLD
    }

    override suspend fun getIvt(): Flow<List<TranListItem>> {

        val ivtList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList) {
            val ivt = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        return flow { emit(ivtList) }
    }

    override suspend fun getIvtA(accounts: List<String>): Flow<List<TranListItem>> {

        val ivtList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter { accounts.contains(it.account) }) {
            val ivt = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        return flow { emit(ivtList) }
    }

    override suspend fun getIvtAD(
        accounts: List<String>,
        start: Date,
        end: Date
    ): Flow<List<TranListItem>> {

        val ivtList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            accounts.contains(it.account) && it.date >= start && it.date <= end
        }) {
            val ivt = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        return flow { emit(ivtList) }
    }

    override suspend fun getIvtAT(
        accounts: List<String>,
        type: String
    ): Flow<List<TranListItem>> {

        val ivtList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter { accounts.contains(it.account) && it.type == type }) {
            val ivt = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        return flow { emit(ivtList) }
    }

    override suspend fun getIvtATC(
        accounts: List<String>,
        type: String,
        categories: List<String>
    ): Flow<List<TranListItem>> {

        val ivtList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            accounts.contains(it.account) && it.type == type && categories.contains(it.category)
        }) {
            val ivt = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        return flow { emit(ivtList) }
    }

    override suspend fun getIvtATD(
        accounts: List<String>,
        type: String,
        start: Date,
        end: Date
    ): Flow<List<TranListItem>> {

        val ivtList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            accounts.contains(it.account) && it.type == type && it.date >= start && it.date <= end
        }) {
            val ivt = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        return flow { emit(ivtList) }
    }

    override suspend fun getIvtATCD(
        accounts: List<String>,
        type: String,
        categories: List<String>,
        start: Date,
        end: Date
    ): Flow<List<TranListItem>> {

        val ivtList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            accounts.contains(it.account) && it.type == type && categories.contains(it.category) &&
                    it.date >= start && it.date <= end
        }) {
            val ivt = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        return flow { emit(ivtList) }
    }

    override suspend fun getIvtD(start: Date, end: Date): Flow<List<TranListItem>> {

        val ivtList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter { it.date in start..end }) {
            val ivt = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        return flow { emit(ivtList) }
    }

    override suspend fun getIvtT(type: String): Flow<List<TranListItem>> {

        val ivtList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter { it.type == type }) {
            val ivt = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        return flow { emit(ivtList) }
    }

    override suspend fun getIvtTC(
        type: String,
        categories: List<String>
    ): Flow<List<TranListItem>> {

        val ivtList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter { it.type == type && categories.contains(it.category) }) {
            val ivt = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        return flow { emit(ivtList) }
    }

    override suspend fun getIvtTCD(
        type: String,
        categories: List<String>,
        start: Date,
        end: Date
    ): Flow<List<TranListItem>> {

        val ivtList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            it.type == type && categories.contains(it.category) && it.date >= start && it.date <= end
        }) {
            val ivt = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        return flow { emit(ivtList) }
    }

    override suspend fun getIvtTD(
        type: String,
        start: Date,
        end: Date
    ): Flow<List<TranListItem>> {

        val ivtList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            it.type == type  && it.date >= start && it.date <= end }) {
            val ivt = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            ivtList.add(ivt)
        }

        return flow { emit(ivtList) }
    }
}