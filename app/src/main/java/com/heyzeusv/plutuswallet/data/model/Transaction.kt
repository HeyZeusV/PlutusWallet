package com.heyzeusv.plutuswallet.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import com.heyzeusv.plutuswallet.util.DateUtils
import java.math.BigDecimal
import java.util.Date

/**
 *  Representation of Transaction table.
 *
 *  @ForeignKey one Account/Category to many Transactions. Update in parent table will cause
 *              change in corresponding child row.
 *  @Index indices on foreign keys.
 *
 *  @param id             unique id of Transaction.
 *  @param title          title of Transaction.
 *  @param date           Date of Transaction.
 *  @param total          the total amount of Transaction.
 *  @param account        account this Transaction will belong to.
 *  @param type           either "Expense" or "Income".
 *  @param category       the name of category selected.
 *  @param memo           optional information.
 *  @param repeating      true if Transaction is to occur again in the future.
 *  @param frequency      how often Transaction repeats.
 *  @param period         how often Transaction repeats.
 *  @param futureDate     if repeating true, frequency * period + date.
 *  @param futureTCreated true if this Transaction has had a future Transaction created for it.
 */
@Entity(foreignKeys = [ForeignKey(entity = Account::class,
                                  parentColumns = arrayOf("name"),
                                  childColumns = arrayOf("account"),
                                  onUpdate = CASCADE),
                       ForeignKey(entity = Category::class,
                                  parentColumns = arrayOf("name", "type"),
                                  childColumns = arrayOf("category", "type"),
                                  onUpdate = CASCADE)],
        indices = [Index(value = ["category", "type"],
                         name = "index_cat_name_type"),
                   Index(value = ["account"],
                         name = "index_account_name")])
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var title: String = "",
    var date: Date = DateUtils.startOfDay(Date()),
    var total: BigDecimal = BigDecimal("0"),
    var account: String = "",
    var type: String = "Expense",
    var category: String = "",
    var memo: String = "",
    var repeating: Boolean = false,
    var frequency: Int = 1,
    var period: Int = 0,
    var futureDate: Date = Date(Long.MAX_VALUE),
    var futureTCreated: Boolean = false
)