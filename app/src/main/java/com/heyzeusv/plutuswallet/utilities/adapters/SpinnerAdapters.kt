package com.heyzeusv.plutuswallet.utilities.adapters

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
 *  Creates ArrayAdapter with data and attaches it to Spinner.
 *
 *  @param entries data to be displayed by Spinner.
 */
@BindingAdapter("entries")
fun Spinner.setEntries(entries : List<String>?) {

    if (entries != null) {

        val arrayAdapter : ArrayAdapter<String> =
            ArrayAdapter(context, R.layout.spinner_item, entries)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter = arrayAdapter
    }
}

/**
 *  @param selection value to set Spinner to.
 */
@BindingAdapter("selectedValue")
fun Spinner.setSelectedValue(selection : String?) {

    if (adapter != null ) {

        @Suppress("UNCHECKED_CAST")
        val position : Int = (adapter as ArrayAdapter<String>).getPosition(selection)
        setSelection(position, false)
        tag = position
    }
}

/**
 *  Sets listener for when selectedValue is changed allowing InverseBinding.
 *
 *  @param inverseBindingListener listener that gets triggered when there is a change in selection.
 */
@BindingAdapter("selectedValueAttrChanged")
fun Spinner.selectedInverseBindingListener(inverseBindingListener : InverseBindingListener?) {

    onItemSelectedListener = when (inverseBindingListener) {
        null -> null
        else -> object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View?,
                                        position: Int, id: Long) {

                if (tag != position) inverseBindingListener.onChange()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}

/**
 *  Gets called when above InverseBindingListener is triggered.
 *
 *  @return currently selected item in Spinner.
 */
@InverseBindingAdapter(attribute = "selectedValue", event = "selectedValueAttrChanged")
fun Spinner.getSelectedValue() : String? {

    return selectedItem as String?
}

/**
 *  @param position position to set Spinner to.
 */
@BindingAdapter("selectedPosition")
fun Spinner.setSelectedPosition(position : Int) {

    if (adapter != null ) {

        @Suppress("UNCHECKED_CAST")
        setSelection(position, false)
        tag = position
    }
}

/**
 *  Sets listener for when selectedPosition is changed allowing InverseBinding.
 *
 *  @param inverseBindingListener listener that gets triggered when there is a change in position.
 */
@BindingAdapter("selectedPositionAttrChanged")
fun Spinner.positionInverseBindingListener(inverseBindingListener : InverseBindingListener?) {

    onItemSelectedListener = when (inverseBindingListener) {
        null -> null
        else -> object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View?,
                                        position: Int, id: Long) {

                if (tag != position) inverseBindingListener.onChange()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}

/**
 *  Gets called when above InverseBindingListener is triggered.
 *
 *  @return position of currently selected item in Spinner.
 */
@InverseBindingAdapter(attribute = "selectedPosition", event = "selectedPositionAttrChanged")
fun Spinner.getSelectedPosition() : Int? {

    return selectedItemPosition
}