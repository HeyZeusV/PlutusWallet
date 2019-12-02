package com.heyzeusv.financeapplication.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.heyzeusv.financeapplication.utilities.Utils
import java.math.BigDecimal
import java.util.Date

/**
 *  Representation of Transaction table.
 *
 *  @param id             unique id of Transaction.
 *  @param title          title of Transaction.
 *  @param date           Date of Transaction.
 *  @param total          the total amount of Transaction.
 *  @param type           either "Expense" or "Income".
 *  @param category       the name of category selected.
 *  @param memo           optional information.
 *  @param repeating      true if Transaction is to occur again in the future.
 *  @param frequency      how often Transaction repeats.
 *  @param period         how often Transaction repeats.
 *  @param futureDate     if repeating true, frequency * period + date.
 *  @param futureTCreated true if this Transaction has had a future Transaction created for it.
 */
@Entity
data class Transaction(
    @PrimaryKey
    var id             : Int        = 0,
    var title          : String     = "",
    var date           : Date       = Utils.startOfDay(Date()),
    var total          : BigDecimal = BigDecimal("0"),
    var type           : String     = "Expense",
    var category       : String     = "",
    var memo           : String     = "",
    var repeating      : Boolean    = false,
    var frequency      : Int        = 1,
    var period         : Int        = 0,
    var futureDate     : Date       = Date(Long.MAX_VALUE),
    var futureTCreated : Boolean    = false)