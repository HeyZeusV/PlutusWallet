package com.heyzeusv.plutuswallet.util

import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category

/**
 *  Various ways of creating AlertDialogs.
 */
class AlertDialogCreator {

    companion object {

        // ClickListener that does nothing
        val doNothing = DialogInterface.OnClickListener { _, _ -> }

        /**
         *  Basic AlertDialog. Requires [context], [title], [message], [posButton]/[negButton]
         *  string, and [posFun]/[negFun] click listeners. Runs different functions depending on
         *  following parameters that are passed.
         */
        fun alertDialog(
            context: Context,
            title: String,
            message: String,
            posButton: String,
            posFun: DialogInterface.OnClickListener,
            negButton: String,
            negFun: DialogInterface.OnClickListener
        ) {

            // initialize and set up Builder
            val builder: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(context)
                // set title
                .setTitle(title)
                // set message
                .setMessage(message)
                // set positive button and its action
                .setPositiveButton(posButton, posFun)
                // set negative button and its action
                .setNegativeButton(negButton, negFun)
            // create AlertDialog using builder
            val alertDialog: AlertDialog = builder.create()
            // display AlertDialog
            alertDialog.show()
        }

        /**
         *  AlertDialog with input. Requires [context], custom [dialogView], [title],
         *  and [posButton]/[negButton] string. Runs different functions depending on following
         *  parameters that are passed.
         *
         *  TransactionFragment
         *  [tranString] is "Create New..." translated string. [listenersTran] contains negative and
         *  cancel listeners for AlertDialog. [posFunTran] runs when the positive button is pressed.
         *
         *  AccountFragment
         *  [account] is either the new or existing Account. [posFunAcc] runs when the positive
         *  button is pressed.
         *
         *  CategoryFragment
         *  [category] is either the new or existing Category. [catType] 0 = Expense, 1 = Income.
         *  [posFunCat] runs when the positive button is pressed.
         */
        fun alertDialogInput(
            context: Context,
            dialogView: View,
            title: String,
            posButton: String,
            negButton: String,
            tranString: String?,
            listenersTran: List<Any>?,
            posFunTran: ((String, String) -> Unit)?,
            account: Account?,
            posFunAcc: ((Account, String) -> Unit)?,
            category: Category?,
            catType: Int?,
            posFunCat: ((Category, String, Int) -> Unit)?
        ) {

            // used to determine if AlertDialog can close on positive button press
            var closeDialog = false

            // AlertDialog Views
            val inputLayout: TextInputLayout = dialogView.findViewById(R.id.dialog_layout)
            val input: EditText = dialogView.findViewById(R.id.dialog_input)

            // sets text (if any) from parameter
            if (account != null) input.setText(account.name)
            if (category != null) input.setText(category.name)

            // negative onClick and onCancel listeners
            var negFun = doNothing
            var canFun = DialogInterface.OnCancelListener { }
            if (listenersTran != null) {
                negFun = listenersTran[0] as DialogInterface.OnClickListener
                canFun = listenersTran[1] as DialogInterface.OnCancelListener
            }

            // initialize and set up Builder
            val builder: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(context)
                // set title
                .setTitle(title)
                // sets the view
                .setView(dialogView)
                // set positive button and its action
                .setPositiveButton(posButton, doNothing)
                // set negative button and its action
                .setNegativeButton(negButton, negFun)
                // set action when user cancels dialog
                .setOnCancelListener(canFun)
            // create AlertDialog using builder
            val dialog: AlertDialog = builder.create()
            // display AlertDialog
            dialog.show()

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                // will display error is input is blank
                if (input.text.isBlank()) {
                    inputLayout.error = context.getString(R.string.alert_dialog_input_error)
                } else {
                    // runs function that was passed as parameter
                    when {
                        posFunTran != null -> posFunTran(input.text.toString(), tranString!!)
                        posFunAcc != null -> posFunAcc(account!!, input.text.toString())
                        posFunCat != null -> posFunCat(category!!, input.text.toString(), catType!!)
                    }
                    // removes error
                    inputLayout.error = null
                    // can now close AlertDialog
                    closeDialog = true
                }
                // closes AlertDialog if there is no error
                if (closeDialog) dialog.dismiss()
            }
        }
    }
}
