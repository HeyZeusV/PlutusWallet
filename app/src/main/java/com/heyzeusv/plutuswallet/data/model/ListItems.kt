package com.heyzeusv.plutuswallet.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.heyzeusv.plutuswallet.util.ListItemAction
import com.heyzeusv.plutuswallet.util.TransactionType
import com.heyzeusv.plutuswallet.util.TransactionType.EXPENSE

/**
 *  Interface required by items that are to be displayed on ListCard Composable. [id] is unique in
 *  order identify each item when completing actions (insert/edit/delete). [name] is displayed to
 *  user.
 */
interface ListItemInterface {
    val id: Int
    var name: String
}

/**
 *  Representation of Account table.
 *
 *  @Index unique since account is not primary key and foreign keys must be unique.
 *
 *  Each Account has a unique [id] which is auto generated by Room along with a [name] which does
 *  not have to be unique when inserting into Room, however Users are not allowed to have duplicate
 *  names.
 */
@Entity(indices = [Index(value = ["name"],
    name = "index_account",
    unique = true)])
data class Account(
    @PrimaryKey(autoGenerate = true)
    override val id: Int,
    override var name: String
) : ListItemInterface

/**
 *  Representation of Category table.
 *
 *  @Index unique since category and type are not primary keys and foreign keys must be unique.
 *
 *  Each Category has a unique [id] which is auto generated by Room along with a [name] which does
 *  not have to be unique when inserting into Room, however Users are not allowed to have duplicate
 *  names of the same [type], which is either "Expense" or "Income."
 */
@Entity(indices = [Index(value = ["name", "type"],
    name = "index_cat_type",
    unique = true)])
data class Category(
    @PrimaryKey(autoGenerate = true)
    override val id: Int,
    override var name: String,
    var type: String
) : ListItemInterface

/**
 *  Data class used to determine which [action] (insert/edit/delete) to complete on
 *  [ListItemInterface] with [id]. If working with [Category] items, [type] is used to determine
 *  if item is "Expense" or "Income."
 */
data class ListDialog(
    val action: ListItemAction,
    val id: Int,
    val type: TransactionType = EXPENSE
)