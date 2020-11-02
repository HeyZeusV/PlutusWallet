package com.heyzeusv.plutuswallet.util.bindingadapters

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.heyzeusv.plutuswallet.R

/**
 *  DataBinding Custom Spinner Binding Adapters.
 */
/**
 *  Creates ArrayAdapter with [entries] and attaches it to Spinner.
 */
@BindingAdapter("entries")
fun Spinner.setEntries(entries: List<String>?) {

    if (entries != null) {
        val arrayAdapter: ArrayAdapter<String> =
            ArrayAdapter(context, R.layout.spinner_item, entries)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter = arrayAdapter
    }
}

/**
 *  Sets Spinner to [selection].
 */
@BindingAdapter("selectedValue")
fun Spinner.setSelectedValue(selection: String?) {

    if (adapter != null) {
        @Suppress("UNCHECKED_CAST")
        val position: Int = (adapter as ArrayAdapter<String>).getPosition(selection)
        setSelection(position, false)
        tag = position
    }
}

/**
 *  Sets listener for when selectedValue is changed allowing InverseBinding.
 *  [inverseBindingListener] gets triggered when there is a change in selection.
 */
@BindingAdapter("selectedValueAttrChanged")
fun Spinner.selectedInverseBindingListener(inverseBindingListener: InverseBindingListener?) {

    onItemSelectedListener = if (inverseBindingListener == null) {
        null
    } else {
        object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {

                if (tag != pos) inverseBindingListener.onChange()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}

/**
 *  Returns selection when InverseBindingListener is triggered.
 */
@InverseBindingAdapter(attribute = "selectedValue", event = "selectedValueAttrChanged")
fun Spinner.getSelectedValue(): String? = selectedItem as String?

/**
 *  Sets Spinner selection to [position].
 */
@BindingAdapter("selectedPosition")
fun Spinner.setSelectedPosition(position: Int) {

    if (adapter != null) {
        setSelection(position, false)
        tag = position
    }
}

/**
 *  Sets listener for when selectedPosition is changed allowing InverseBinding.
 *  [inverseBindingListener] gets triggered when there is a change in position.
 */
@BindingAdapter("selectedPositionAttrChanged")
fun Spinner.positionInverseBindingListener(inverseBindingListener: InverseBindingListener?) {

    onItemSelectedListener = if (inverseBindingListener == null) {
        null
    } else {
        object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {

                if (tag != pos) inverseBindingListener.onChange()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}

/**
 *  Returns position of selected item if InverseBindingListener is triggered.
 */
@InverseBindingAdapter(attribute = "selectedPosition", event = "selectedPositionAttrChanged")
fun Spinner.getSelectedPosition(): Int? = selectedItemPosition