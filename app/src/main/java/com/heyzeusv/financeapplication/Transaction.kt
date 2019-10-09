package com.heyzeusv.financeapplication

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.*

/*
    Must be annotated with @Entity
    if tableName not provided then class name is used as tableName
*/

@Entity
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    var id        : Int = 0,
    var title     : String     = "",
    var date      : Date       = Date(),
    var total     : BigDecimal = BigDecimal("0"),
    var memo      : String     = "",
    var category  : String     = "Education",
    var repeating : Boolean    = false,
    var frequency : Int        = 1,
    var period    : Int        = 0)