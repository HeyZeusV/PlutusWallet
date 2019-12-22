package com.heyzeusv.plutuswallet.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *  Representation of Category table.
 *
 *  @param id       unique id for all Categories
 *  @param category the name of the category
 *  @param type     either "Expense" or "Income"
 */
@Entity
class Category(
    @PrimaryKey(autoGenerate = true)
    val id       : Int,
    var category : String,
    var type     : String
)