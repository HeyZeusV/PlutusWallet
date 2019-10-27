package com.heyzeusv.financeapplication

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *  Representation of ExpenseCategory table.
 *
 *  @param category the name of the category
 */
@Entity
data class ExpenseCategory (
    @PrimaryKey
    var category : String)