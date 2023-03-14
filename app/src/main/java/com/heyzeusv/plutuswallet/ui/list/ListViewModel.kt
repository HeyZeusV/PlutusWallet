package com.heyzeusv.plutuswallet.ui.list

import com.heyzeusv.plutuswallet.data.model.ListDialog
import com.heyzeusv.plutuswallet.data.model.ListItemInterface
import kotlinx.coroutines.flow.StateFlow

interface ListViewModel {
    val createItemStringId: Int
    val deleteItemStringId: Int
    val editItemStringId: Int
    val listSubtitleStringIds: List<Int>

    val firstItemList: StateFlow<List<ListItemInterface>>
    val secondItemList: StateFlow<List<ListItemInterface>>
    val firstUsedItemList: StateFlow<List<ListItemInterface>>
    val secondUsedItemList: StateFlow<List<ListItemInterface>>
    val itemExists: StateFlow<String>
    val showDialog: StateFlow<ListDialog>

    fun updateDialog(newValue: ListDialog)
    fun updateItemExists(value: String)
    fun insertItem(name: String)
    fun deleteItem(item: ListItemInterface)
    fun editItem(item: ListItemInterface, newName: String)
}