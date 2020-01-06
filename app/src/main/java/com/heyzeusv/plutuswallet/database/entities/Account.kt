package com.heyzeusv.plutuswallet.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 *  Representation of Account table.
 *
 *  @param account name of Account, no two Accounts can have the same name.
 */
@Entity(indices = [Index(value  = ["account"],
                         name   = "index_account",
                         unique = true)])
data class Account(
    @PrimaryKey(autoGenerate = true)
    var id      : Int,
    var account : String
)