package com.heyzeusv.financeapplication

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

/*
    Must be annotated with @Entity
    if tableName not provided then class name is used as tableName
*/

@Entity(foreignKeys = [ForeignKey(entity = Transaction::class,
                                  parentColumns = arrayOf("id"),
                                  childColumns = arrayOf("transactionId"),
                                  onDelete = ForeignKey.CASCADE)])
data class FutureTransaction(
    @PrimaryKey(autoGenerate = true)
    var id            : Int,
    var transactionId : Int,
    var futureDate    : Date
)