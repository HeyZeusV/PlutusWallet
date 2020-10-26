package com.heyzeusv.plutuswallet.utilities

/**
 *  Replace first instance of [old] with [new].
 */
fun <T> MutableList<T>.replace(old: T, new: T) {

    this[indexOf(old)] = new
}