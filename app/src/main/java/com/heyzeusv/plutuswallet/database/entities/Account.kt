package com.heyzeusv.plutuswallet.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *  Representation of Account table.
 *
 *  @param account name of Account, no two Accounts can have the same name.
 */
@Entity
data class Account(
    @PrimaryKey
    var account : String
)