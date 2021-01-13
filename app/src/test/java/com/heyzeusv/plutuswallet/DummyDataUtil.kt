package com.heyzeusv.plutuswallet

import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.Transaction
import java.math.BigDecimal
import java.util.Date

object DummyDataUtil {

    // dummy data
    private val acc1 = Account(1, "Credit Card")
    private val acc2 = Account(2, "Debit Card")
    private val acc3 = Account(3, "Cash")
    val accList : MutableList<Account> = mutableListOf(acc1, acc2, acc3)

    private val cat1 = Category(1, "Food", "Expense")
    private val cat2 = Category(2, "Entertainment", "Expense")
    private val cat3 = Category(3, "Salary", "Income")
    val catList : MutableList<Category> = mutableListOf(cat1, cat2, cat3)

    private val tran1 = Transaction(1, "Party", Date(86400000), BigDecimal("1000.10"), "Cash", "Expense", "Food", "", true, 1, 0, Date(86400000 * 2), true)
    private val tran2 = Transaction(2, "Party2", Date(86400000 * 2), BigDecimal("100.00"), "Cash", "Expense", "Food", "", true, 1, 0, Date(86400000 * 3), false)
    private val tran3 = Transaction(3, "Pay Day", Date(86400000 * 4), BigDecimal("2000.32"), "Debit Card", "Income", "Salary", "", true, 2, 1, Date(86400000 * 11), false)
    private val tran4 = Transaction(4, "Movie Date", Date(86400000 * 5), BigDecimal("55.45"), "Credit Card", "Expense", "Entertainment")
    val tranList: MutableList<Transaction> = mutableListOf(tran1, tran2, tran3, tran4)
}