package com.heyzeusv.financeapplication

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
    Must be annotated with @Entity
    if tableName not provided then class name is used as tableName
*/

@Entity
data class ExpenseCategory (
    @PrimaryKey
    var category : String = "")