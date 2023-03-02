package com.heyzeusv.plutuswallet.data

import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.CategoryTotals
import com.heyzeusv.plutuswallet.data.model.ChartInformation
import com.heyzeusv.plutuswallet.data.model.TranListItem
import com.heyzeusv.plutuswallet.data.model.TranListItemFull
import com.heyzeusv.plutuswallet.data.model.Transaction
import java.math.BigDecimal
import java.util.Date

class DummyAndroidDataUtil {

    // dummy data
    val acc1 = Account(1, "Credit Card")
    val acc2 = Account(2, "Debit Card")
    val acc3 = Account(3, "Cash")
    val acc4 = Account(4, "Savings")
    val acc5 = Account(5, "Unused")
    val accList : MutableList<Account> = mutableListOf(acc1, acc2, acc3, acc4, acc5)

    val cat1 = Category(1, "Food", "Expense")
    val cat2 = Category(2, "Entertainment", "Expense")
    val cat3 = Category(3, "Salary", "Income")
    val cat4 = Category(4, "Zelle", "Income")
    val cat5 = Category(5, "Unused Expense", "Expense")
    val cat6 = Category(6, "Unused Income", "Income")
    val cat7 = Category(7, "Housing", "Expense")
    val catList : MutableList<Category> = mutableListOf(cat1, cat2, cat3, cat4, cat5, cat6, cat7)

    val tran1 = Transaction(
        1, "Party", Date(86400000), BigDecimal("1000.10"),
        "Cash", "Expense", "Food", "Catering for party",
        true, 1, 0, Date(86400000 * 2), true
    )
    val tran2 = Transaction(
        2, "Party2", Date(86400000 * 2), BigDecimal("100.00"),
        "Savings", "Expense", "Housing", "",
        false, 1, 0, Date(86400000 * 3), true
    )
    val tran3 = Transaction(
        3, "Pay Day", Date(86400000 * 4), BigDecimal("2000.32"),
        "Debit Card", "Income", "Salary", "Best day of the month!",
        true, 1, 2, Date(86400000 * 11), true
    )
    val tran4 = Transaction(
        4, "Movie Date", Date(86400000 * 5), BigDecimal("55.45"),
        "Credit Card", "Expense", "Entertainment"
    )
    val tranList: MutableList<Transaction> = mutableListOf(tran1, tran2, tran3, tran4)

    val tli1 = TranListItem(
        1, "Party", Date(86400000), BigDecimal("1000.10"),
        "Cash", "Expense", "Food"
    )
    val tli2 = TranListItem(
        2, "Party2", Date(86400000 * 2), BigDecimal("100.00"),
        "Cash", "Expense", "Food"
    )
    val tli3 = TranListItem(
        3, "Pay Day", Date(86400000 * 4), BigDecimal("2000.32"),
        "Debit Card", "Income", "Salary"
    )
    val tli4 = TranListItem(
        4, "Movie Date", Date(86400000 * 5), BigDecimal("55.45"),
        "Credit Card", "Expense", "Entertainment"
    )
    val tliList: MutableList<TranListItem> = mutableListOf(tli1, tli2, tli3, tli4)

    val tlif1 = TranListItemFull(tli1, "\$1,000.10", "Thursday, January 1, 1970")
    val tlif2 = TranListItemFull(tli2, "\$100.00", "Friday, January 2, 1970")
    val tlif3 = TranListItemFull(tli3, "\$2,000.32", "Sunday, January 4, 1970")
    val tlif4 = TranListItemFull(tli4, "\$55.45", "Monday, January 5, 1970")
    val tlifList: MutableList<TranListItemFull> = mutableListOf(tlif1, tlif2, tlif3, tlif4)

    val ct1 = CategoryTotals("Food", BigDecimal("1000.10"), "Expense")
    val ct2 = CategoryTotals("Housing", BigDecimal("100.00"), "Expense")
    val ct3 = CategoryTotals("Salary", BigDecimal("2000.32"), "Income")
    val ct4 = CategoryTotals("Entertainment", BigDecimal("55.45"), "Expense")

    val expenseCi = ChartInformation(listOf(ct1, ct2, ct4), "$1,155.55")
    val expenseCiNoCt1 = ChartInformation(listOf(ct2, ct4), "$155.45")
    val incomeCi = ChartInformation(listOf(ct3), "$2,000.32")
}