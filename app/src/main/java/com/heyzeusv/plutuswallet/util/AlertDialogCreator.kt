package com.heyzeusv.plutuswallet.util

import android.content.Context
import android.content.DialogInterface
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 *  Various ways of creating AlertDialogs.
 */
class AlertDialogCreator {

    companion object {

        // ClickListener that does nothing
        val doNothing = DialogInterface.OnClickListener { _, _ -> }

        /**
         *  Basic AlertDialog.
         *
         *  @param context   needed to inflate view and create Builder.
         *  @param title     title of AlertDialog.
         *  @param posButton positive button text.
         *  @param posFun    function launched on positive button click.
         *  @param negButton negative button text.
         *  @param negFun    function launched on negative button click.
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
         *  AlertDialog with input.
         *
         *  @param context   needed to inflate view and create Builder.
         *  @param title     title of AlertDialog.
         *  @param view      layout containing EditText for input.
         *  @param posButton positive button text.
         *  @param posFun    function launched on positive button click.
         *  @param negButton negative button text.
         *  @param negFun    function launched on negative button click.
         */
        fun alertDialogInput(
            context: Context,
            title: String,
            view: View,
            posButton: String,
            posFun: DialogInterface.OnClickListener,
            negButton: String,
            negFun: DialogInterface.OnClickListener
        ) {

            // initialize and set up Builder
            val builder: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(context)
                // set title
                .setTitle(title)
                // sets the view
                .setView(view)
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
         *  AlertDialog with input.
         *
         *  @param context   needed to inflate view and create Builder.
         *  @param title     title of AlertDialog.
         *  @param view      layout containing EditText for input.
         *  @param posButton positive button text.
         *  @param posFun    function launched on positive button click.
         *  @param negButton negative button text.
         *  @param negFun    function launched on negative button click.
         */
        fun alertDialogInputCancelable(
            context: Context,
            title: String,
            view: View,
            posButton: String,
            posFun: DialogInterface.OnClickListener,
            negButton: String,
            negFun: DialogInterface.OnClickListener,
            cancelFun: DialogInterface.OnCancelListener
        ) {

            // initialize and set up Builder
            val builder: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(context)
                // set title
                .setTitle(title)
                // sets the view
                .setView(view)
                // set positive button and its action
                .setPositiveButton(posButton, posFun)
                // set negative button and its action
                .setNegativeButton(negButton, negFun)
                // set action when user cancels dialog
                .setOnCancelListener(cancelFun)
            // create AlertDialog using builder
            val alertDialog: AlertDialog = builder.create()
            // display AlertDialog
            alertDialog.show()
        }
    }
}
