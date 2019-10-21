package com.heyzeusv.financeapplication

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

/*
    Must be annotated with @Entity
    if tableName not provided then class name is used as tableName
*/

@Entity(foreignKeys = [ForeignKey(entity        = Transaction::class,
                                  parentColumns = arrayOf("id"),
                                  childColumns  = arrayOf("transactionId"),
                                  onDelete      = ForeignKey.CASCADE)],
        indices     = [Index     (value         = ["transactionId"],
                                  name          = "transactionId")])
data class FutureTransaction(
    @PrimaryKey
    var id            : Int,
    var transactionId : Int,
    var futureDate    : Date
)