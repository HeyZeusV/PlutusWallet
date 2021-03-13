package com.heyzeusv.plutuswallet.util

import android.view.View
import androidx.core.view.children
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

/**
 *  Replace first instance of [old] with [new].
 */
fun <T> MutableList<T>.replace(old: T, new: T) {

    this[indexOf(old)] = new
}

/**
 *  Adds only unique entries from [list].
 */
fun <T> MutableList<T>.addAllUnique(list: MutableList<T>) {

    for (entry: T in list) {
        if (!this.contains(entry)) add(entry)
    }
}

/**
 *  Checks all Chips in ChipGroup.
 */
fun ChipGroup.checkAll() {

    // retrieves ChipGroup child Chips
    val chipChildren: Sequence<View> = this.children
    // iterator for child Chips sequence
    val chipIterator: Iterator<View> = chipChildren.iterator()
    // check each child Chip
    chipIterator.forEach {
        (it as Chip).isChecked = true
    }
}