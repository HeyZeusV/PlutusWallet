package com.heyzeusv.plutuswallet.ui.list

import com.heyzeusv.plutuswallet.data.model.DataDialog
import com.heyzeusv.plutuswallet.data.model.DataInterface
import kotlinx.coroutines.flow.StateFlow

interface ListViewModel {
    val createItemStringId: Int
    val deleteItemStringId: Int
    val editItemStringId: Int
    val listSubtitleStringIds: List<Int>

    val firstItemList: StateFlow<List<DataInterface>>
    val secondItemList: StateFlow<List<DataInterface>>
    val firstUsedItemList: StateFlow<List<DataInterface>>
    val secondUsedItemList: StateFlow<List<DataInterface>>
    val itemExists: StateFlow<String>
    val showDialog: StateFlow<DataDialog>

    fun updateDialog(newValue: DataDialog)
    fun updateItemExists(value: String)
    fun insertItem(name: String)
    fun deleteItem(item: DataInterface)
    fun editItem(item: DataInterface, newName: String)
}