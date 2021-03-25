package com.heyzeusv.plutuswallet.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 *  Representation of Account table.
 *
 *  @Index unique since account is not primary key and foreign keys must be unique.
 *
 *  @param id   unique id of Account.
 *  @param name name of Account.
 */
@Entity(indices = [Index(value = ["name"],
                         name = "index_account",
                         unique = true)])
data class Account(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var name: String
)