package com.heyzeusv.plutuswallet.data

import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.ItemViewTransaction
import com.heyzeusv.plutuswallet.data.model.Transaction
import java.math.BigDecimal
import java.time.ZoneId
import java.time.ZonedDateTime

class DummyDataUtil {

    // dummy data
    val acc1 = Account(1, "Credit Card")
    val acc2 = Account(2, "Debit Card")
    val acc3 = Account(3, "Cash")
    val acc4 = Account(4, "Unused")
    val accList : MutableList<Account> = mutableListOf(acc1, acc2, acc3, acc4)

    val cat1 = Category(1, "Food", "Expense")
    val cat2 = Category(2, "Entertainment", "Expense")
    val cat3 = Category(3, "Salary", "Income")
    val cat4 = Category(4, "Zelle", "Income")
    val cat5 = Category(5, "Unused Expense", "Expense")
    val cat6 = Category(6, "Unused Income", "Income")
    val catList : MutableList<Category> = mutableListOf(cat1, cat2, cat3, cat4, cat5, cat6)

    val tran1 = Transaction(
        1, "Party", ZonedDateTime.of(2018, 8, 10, 0, 0, 0, 0, ZoneId.systemDefault()),
        BigDecimal("1000.10"), "Cash", "Expense", "Food",
        "Catering for party", true, 1, 0,
        ZonedDateTime.of(2018, 8, 11, 0, 0, 0, 0, ZoneId.systemDefault()), true
    )
    val tran2 = Transaction(
        2, "Party2", ZonedDateTime.of(2018, 8, 11, 0, 0, 0, 0, ZoneId.systemDefault()),
        BigDecimal("100.00"), "Cash", "Expense", "Food",
        "", false, 1, 0,
        ZonedDateTime.of(2018, 8, 12, 0, 0, 0, 0, ZoneId.systemDefault()), false
    )
    val tran3 = Transaction(
        3, "Pay Day", ZonedDateTime.of(2018, 8, 14, 0, 0, 0, 0, ZoneId.systemDefault()),
        BigDecimal("2000.32"), "Debit Card", "Income", "Salary",
        "Best day of the month!", true, 1, 2,
        ZonedDateTime.of(2018, 9, 14, 0, 0, 0, 0, ZoneId.systemDefault()), false
    )
    val tran4 = Transaction(
        4, "Movie Date", ZonedDateTime.of(2018, 8, 15, 0, 0, 0, 0, ZoneId.systemDefault()),
        BigDecimal("55.45"), "Credit Card", "Expense", "Entertainment"
    )
    val tranList: MutableList<Transaction> = mutableListOf(tran1, tran2, tran3, tran4)

    val ivt1 = ItemViewTransaction(
        1, "Party", ZonedDateTime.of(2018, 8, 10, 0, 0, 0, 0, ZoneId.systemDefault()),
        BigDecimal("1000.10"), "Cash", "Expense", "Food"
    )
    val ivt2 = ItemViewTransaction(
        2, "Party2", ZonedDateTime.of(2018, 8, 11, 0, 0, 0, 0, ZoneId.systemDefault()),
        BigDecimal("100.00"), "Cash", "Expense", "Food"
    )
    val ivt3 = ItemViewTransaction(
        3, "Pay Day", ZonedDateTime.of(2018, 8, 14, 0, 0, 0, 0, ZoneId.systemDefault()),
        BigDecimal("2000.32"), "Debit Card", "Income", "Salary"
    )
    val ivt4 = ItemViewTransaction(
        4, "Movie Date", ZonedDateTime.of(2018, 8, 15, 0, 0, 0, 0, ZoneId.systemDefault()),
        BigDecimal("55.45"), "Credit Card", "Expense", "Entertainment"
    )

}