package com.heyzeusv.plutuswallet.billingrepo.localdb

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.billingclient.api.SkuDetails

/**
 *  The Play [com.android.billingclient.api.BillingClient] provides a [SkuDetails] list that the
 *  [com.heyzeusv.plutuswallet.billingrepo.BillingRepository] could pass along to clients to tell
 *  them what the app sells. With that approach, however, clients would have to figure out all
 *  correlations between SkuDetails and [Entitlement].
 *
 *  Therefore, in the spirit of being client-friendly, whereas the [com.heyzeusv.plutuswallet.billingrepo.BillingRepository] is in a
 *  better position to determine the correlations between a [SkuDetails] and its [Entitlement],
 *  the API should provide an [AugmentedSkuDetails] object instead of the basic [SkuDetails].
 *  This object not only passes to clients the actual [SkuDetails] object from Google, but also
 *  tells clients whether a user is allowed to purchase that item at this particular moment.
 */
@Entity
data class AugmentedSkuDetails(
    val canPurchase  : Boolean, /* Not in SkuDetails; it's the augmentation */
    @PrimaryKey
    val sku          : String,
    val type         : String?,
    val price        : String?,
    val title        : String?,
    val description  : String?,
    val originalJson : String?
)