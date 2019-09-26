package com.heyzeusv.financeapplication

import java.math.BigDecimal
import java.util.*

/*
    Must be annotated with @Entity
    if tableName not provided then class name is used as tableName
*/
data class Transaction(var id        : Int        = 0,
                       var title     : String     = "",
                       var date      : Date       = Date(),
                       var total     : BigDecimal = BigDecimal("0.00"),
                       val itemList  : Int        = id,
                       var memo      : String     = "",
                       var category  : String     = "",
                       var repeating : Boolean    = false,
                       var frequency : Int        = 0)