package com.heyzeusv.plutuswallet.data

import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.CategoryTotals
import com.heyzeusv.plutuswallet.data.model.TranListItem
import com.heyzeusv.plutuswallet.data.model.Transaction
import com.heyzeusv.plutuswallet.util.TransactionType.EXPENSE
import com.heyzeusv.plutuswallet.util.TransactionType.INCOME
import com.heyzeusv.plutuswallet.util.isAfterEqual
import com.heyzeusv.plutuswallet.util.isBeforeEqual
import com.heyzeusv.plutuswallet.util.replace
import java.math.BigDecimal
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow

class FakeRepository @Inject constructor() : PWRepositoryInterface {

    val dd = DummyDataUtil()
    var accList: MutableList<Account> = dd.accList
    var catList: MutableList<Category> = dd.catList
    var tranList: MutableList<Transaction> = dd.tranList

    private val accountListFlow = MutableSharedFlow<List<Account>>()
    suspend fun accountListEmit(value: List<Account>) =
        accountListFlow.emit(value.sortedBy { it.name })
    private val accountsUsedListFlow = MutableSharedFlow<List<Account>>()
    suspend fun accountsUsedListEmit(value: List<Account>) = accountsUsedListFlow.emit(value)
    private val accountNameListFLow = MutableSharedFlow<List<String>>()
    suspend fun accountNameListEmit(value: List<String>) = accountNameListFLow.emit(value.sorted())
    private val expenseCatNameListFlow = MutableSharedFlow<List<String>>()
    suspend fun expenseCatNameListEmit(value: List<String>) =
        expenseCatNameListFlow.emit(value.sorted())
    private val incomeCatNameListFlow = MutableSharedFlow<List<String>>()
    suspend fun incomeCatNameListEmit(value: List<String>) =
        incomeCatNameListFlow.emit(value.sorted())
    private val expenseCatListFlow = MutableSharedFlow<List<Category>>()
    suspend fun expenseCatListEmit(value: List<Category>) =
        expenseCatListFlow.emit(value.sortedBy { it.name })
    private val incomeCatListFlow = MutableSharedFlow<List<Category>>()
    suspend fun incomeCatListEmit(value: List<Category>) =
        incomeCatListFlow.emit(value.sortedBy { it.name })
    private val expenseCatUsedListFlow = MutableSharedFlow<List<Category>>()
    suspend fun expenseCatUsedListEmit(value: List<Category>) = expenseCatUsedListFlow.emit(value)
    private val incomeCatUsedListFLow = MutableSharedFlow<List<Category>>()
    suspend fun incomeCatUsedListEmit(value: List<Category>) = incomeCatUsedListFLow.emit(value)

    fun clearAccCatLists() {
        accList.clear()
        catList.clear()
    }

    fun resetLists() {
        accList = dd.accList
        catList = dd.catList
        tranList = dd.tranList
    }

    override suspend fun getAccountNames(): Flow<List<String>> = accountNameListFLow

    override suspend fun getAccountsUsed(): Flow<List<Account>> = accountsUsedListFlow


    override suspend fun getAccountSizeAsync(): Int {
        return accList.size
    }

    override suspend fun deleteAccount(account: Account) {
        accList.remove(account)
        accountListEmit(accList)
    }

    override suspend fun insertAccount(account: Account) {
        accList.add(account)
        accountListEmit(accList)
        accountNameListEmit(accList.map { it.name })
    }

    override suspend fun updateAccount(account: Account) {
        accList.replace(accList.find { it.id == account.id }!!, account)
        accountListEmit(accList)
    }

    override suspend fun getAccounts(): Flow<List<Account>> = accountListFlow

    override suspend fun getCategoryNamesByType(type: String): Flow<List<String>> {
        return if (type == EXPENSE.type) expenseCatNameListFlow else incomeCatNameListFlow
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
    }

    override suspend fun insertCategory(category: Category) {
        catList.add(category)
        expenseCatListEmit(dd.catList.filter { it.type == EXPENSE.type })
        incomeCatListEmit(dd.catList.filter { it.type == INCOME.type })
        expenseCatNameListEmit(dd.catList.filter { it.type == EXPENSE.type }.map { it.name })
        incomeCatNameListEmit(dd.catList.filter { it.type == INCOME.type }.map { it.name })
    }

    override suspend fun updateCategory(category: Category) {
        catList.replace(catList.find { it.id == category.id }!!, category)
    }

    override suspend fun insertCategories(categories: List<Category>) {
        catList.addAll(categories)
        expenseCatNameListEmit(dd.catList.filter { it.type == EXPENSE.type }.map { it.name })
        incomeCatNameListEmit(dd.catList.filter { it.type == INCOME.type }.map { it.name })
    }

    override suspend fun getFutureTransactionsAsync(currentDate: ZonedDateTime): List<Transaction> {

        val futureList: MutableList<Transaction> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            it.date < currentDate && it.repeating && !it.futureTCreated }
        ) {
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
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<CategoryTotals>> {
        val catLists: List<List<String>> = getCatLists()
        val listOfTranLists: MutableList<MutableList<Transaction>> = mutableListOf()
        for (cat: String in catLists[0]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Expense" && accounts.contains(it.account) &&
                        it.date.isAfterEqual(start) && it.date.isBeforeEqual(end)
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }
        for (cat: String in catLists[1]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Income" && accounts.contains(it.account) &&
                        it.date.isAfterEqual(start) && it.date.isBeforeEqual(end)
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
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<CategoryTotals>> {
        val catLists: List<List<String>> = getCatLists()
        val listOfTranLists: MutableList<MutableList<Transaction>> = mutableListOf()
        for (cat: String in catLists[0]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Expense" && accounts.contains(it.account) &&
                        categories.contains(it.category) && it.date.isAfterEqual(start) &&
                        it.date.isBeforeEqual(end)
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }
        for (cat: String in catLists[1]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Income" && accounts.contains(it.account) &&
                        categories.contains(it.category) && it.date.isAfterEqual(start) &&
                        it.date.isBeforeEqual(end)
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }

        return flow { emit(createCatTotals(listOfTranLists)) }
    }

    override suspend fun getCtC(
        type: String,
        categories: List<String>
    ): Flow<List<CategoryTotals>> {
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
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<CategoryTotals>> {
        val catLists: List<List<String>> = getCatLists()
        val listOfTranLists: MutableList<MutableList<Transaction>> = mutableListOf()
        for (cat: String in catLists[0]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Expense" && categories.contains(it.category) &&
                        it.date.isAfterEqual(start) && it.date.isBeforeEqual(end)
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }
        for (cat: String in catLists[1]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Income" && categories.contains(it.category) &&
                        it.date.isAfterEqual(start) && it.date.isBeforeEqual(end)
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }

        return flow { emit(createCatTotals(listOfTranLists)) }
    }

    override suspend fun getCtD(start: ZonedDateTime, end: ZonedDateTime): Flow<List<CategoryTotals>> {
        val catLists: List<List<String>> = getCatLists()
        val listOfTranLists: MutableList<MutableList<Transaction>> = mutableListOf()
        for (cat: String in catLists[0]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Expense" &&
                        it.date.isAfterEqual(start) && it.date.isBeforeEqual(end)
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }
        for (cat: String in catLists[1]) {
            val listOfTran: MutableList<Transaction> = mutableListOf()
            for (tran: Transaction in tranList.filter {
                it.category == cat && it.type == "Income" &&
                        it.date.isAfterEqual(start) && it.date.isBeforeEqual(end)
            }) {
                listOfTran.add(tran)
            }
            listOfTranLists.add(listOfTran)
        }

        return flow { emit(createCatTotals(listOfTranLists)) }
    }

    override suspend fun getTli(): Flow<List<TranListItem>> {
        val tliList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList) {
            val tli = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            tliList.add(tli)
        }

        return flow { emit(tliList) }
    }

    override suspend fun getTliA(accounts: List<String>): Flow<List<TranListItem>> {
        val tliList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter { accounts.contains(it.account) }) {
            val tli = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            tliList.add(tli)
        }

        return flow { emit(tliList) }
    }

    override suspend fun getTliAD(
        accounts: List<String>,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<TranListItem>> {
        val tliList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            accounts.contains(it.account) && it.date.isAfterEqual(start) &&
                    it.date.isBeforeEqual(end)
        }) {
            val tli = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            tliList.add(tli)
        }

        return flow { emit(tliList) }
    }

    override suspend fun getTliAT(
        accounts: List<String>,
        type: String
    ): Flow<List<TranListItem>> {
        val tliList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            accounts.contains(it.account) && it.type == type
        }) {
            val tli = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            tliList.add(tli)
        }

        return flow { emit(tliList) }
    }

    override suspend fun getTliATC(
        accounts: List<String>,
        type: String,
        categories: List<String>
    ): Flow<List<TranListItem>> {
        val tliList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            accounts.contains(it.account) && it.type == type && categories.contains(it.category)
        }) {
            val tli = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            tliList.add(tli)
        }

        return flow { emit(tliList) }
    }

    override suspend fun getTliATD(
        accounts: List<String>,
        type: String,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<TranListItem>> {
        val tliList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            accounts.contains(it.account) && it.type == type &&
                    it.date.isAfterEqual(start) && it.date.isBeforeEqual(end)
        }) {
            val tli = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            tliList.add(tli)
        }

        return flow { emit(tliList) }
    }

    override suspend fun getTliATCD(
        accounts: List<String>,
        type: String,
        categories: List<String>,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<TranListItem>> {
        val tliList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            accounts.contains(it.account) && it.type == type && categories.contains(it.category) &&
                    it.date.isAfterEqual(start) && it.date.isBeforeEqual(end)
        }) {
            val tli = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            tliList.add(tli)
        }

        return flow { emit(tliList) }
    }

    override suspend fun getTliD(
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<TranListItem>> {
        val tliList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter { it.date in start..end }) {
            val tli = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            tliList.add(tli)
        }

        return flow { emit(tliList) }
    }

    override suspend fun getTliT(type: String): Flow<List<TranListItem>> {
        val tliList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter { it.type == type }) {
            val tli = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            tliList.add(tli)
        }

        return flow { emit(tliList) }
    }

    override suspend fun getTliTC(
        type: String,
        categories: List<String>
    ): Flow<List<TranListItem>> {
        val tliList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            it.type == type && categories.contains(it.category)
        }) {
            val tli = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            tliList.add(tli)
        }

        return flow { emit(tliList) }
    }

    override suspend fun getTliTCD(
        type: String,
        categories: List<String>,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<TranListItem>> {
        val tliList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            it.type == type && categories.contains(it.category) &&
                    it.date.isAfterEqual(start) && it.date.isBeforeEqual(end)
        }) {
            val tli = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            tliList.add(tli)
        }

        return flow { emit(tliList) }
    }

    override suspend fun getTliTD(
        type: String,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Flow<List<TranListItem>> {
        val tliList: MutableList<TranListItem> = mutableListOf()
        for (tran: Transaction in tranList.filter {
            it.type == type && it.date.isAfterEqual(start) && it.date.isBeforeEqual(end)
        }) {
            val tli = TranListItem(
                tran.id, tran.title, tran.date, tran.total, tran.account, tran.type, tran.category
            )
            tliList.add(tli)
        }

        return flow { emit(tliList) }
    }
}