package com.heyzeusv.plutuswallet.data.model

import java.math.BigDecimal
import java.util.Date

/**
 *  Not exactly an entity, but more of a helper object to hold Transaction data that
 *  is displayed on TransactionListCard Composable since not all Transaction data is displayed.
 *  Refer to [Transaction] for parameter details.
 */
data class TranListItem(
    val id: Int,
    val title: String,
    val date: Date,
    val total: BigDecimal,
    val account: String,
    val type: String,
    val category: String
)

/**
 *  Object that holds above [TranListItem] while including additional [formattedTotal] and
 *  [formattedDate]. [formattedTotal] and [formattedDate] used to be properties within
*   [TranListItem], but updating those fields would not cause StateFlow to emit the
 *  [TranListItem] with updated properties. However, by creating new [TranListItemFull] with
 *  updated parameters, StateFlow will emit new values.
 */
data class TranListItemFull(
    val tli: TranListItem,
    val formattedTotal: String,
    val formattedDate: String
)