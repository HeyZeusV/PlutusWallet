package com.heyzeusv.financeapplication

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *  Representation of IncomeCategory table.
 *
 *  @param category the name of the category
 */
@Entity
data class IncomeCategory(
    @PrimaryKey
    var category : String)