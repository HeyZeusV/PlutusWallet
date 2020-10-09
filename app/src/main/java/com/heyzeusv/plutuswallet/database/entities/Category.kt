package com.heyzeusv.plutuswallet.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 *  Representation of Category table.
 *
 *  @Index unique since category and type are not primary keys and foreign keys must be unique.
 *
 *  @param id       unique id for all Categories
 *  @param category the name of the category
 *  @param type     either "Expense" or "Income"
 */
@Entity(indices = [Index(value = ["category", "type"],
                         name = "index_cat_type",
                         unique = true)])
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    var category: String,
    var type: String
)