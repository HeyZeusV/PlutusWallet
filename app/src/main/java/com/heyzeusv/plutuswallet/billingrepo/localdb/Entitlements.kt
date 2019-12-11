package com.heyzeusv.plutuswallet.billingrepo.localdb

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *  Normally this would just be an interface. But since each of the entitlements only has
 *  one item/row and so primary key is fixed, we can put the primary key here and so make
 *  the class abstract.
 **/
abstract class Entitlement {

    @PrimaryKey
    var id : Int = 1

    /**
     *  This method tells clients whether a user __should__ buy a particular item at the moment. For
     *  example, if ads are already removed, user should not be able to repurchase. This method is __not__
     *  a reflection on whether Google Play Billing can make a purchase.
     */
    abstract fun mayPurchase() : Boolean
}

@Entity(tableName = "no_ads")
data class NoAds(val entitled : Boolean) : Entitlement() {

    override fun mayPurchase() : Boolean = !entitled
}
