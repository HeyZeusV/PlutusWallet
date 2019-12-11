package com.heyzeusv.plutuswallet.billingrepo.localdb

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

/**
 * No update methods necessary since for each table there is ever expecting one row, hence why
 * the primary key is hardcoded.
 */
@Dao
interface EntitlementsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(noAds : NoAds)

    @Update
    fun update(noAds : NoAds)

    @Query("""SELECT *
                   FROM no_ads 
                   LIMIT 1""")
    fun getNoAds() : LiveData<NoAds>

    @Delete
    fun delete(noAds : NoAds)

    /**
     *  This is purely for future convenience.
     */
    @Transaction
    fun insert(vararg entitlements: Entitlement) {
        entitlements.forEach {
            when (it) {
                is NoAds -> insert(it)
            }
        }
    }

    @Transaction
    fun update(vararg entitlements: Entitlement) {
        entitlements.forEach {
            when (it) {
                is NoAds -> update(it)
            }
        }
    }
}