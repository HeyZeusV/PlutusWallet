package com.heyzeusv.plutuswallet.ui.transaction

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.model.Transaction
import com.heyzeusv.plutuswallet.databinding.FragmentTransactionBinding
import com.heyzeusv.plutuswallet.ui.base.BaseFragment
import com.heyzeusv.plutuswallet.util.AlertDialogCreator
import com.heyzeusv.plutuswallet.util.DateUtils
import com.heyzeusv.plutuswallet.util.EventObserver
import com.heyzeusv.plutuswallet.util.bindingadapters.setSelectedValue
import java.util.Date

/**
 *  Shows all the information in database of one Transaction and allows users to
 *  edit any field and save changes.
 */
class TransactionFragment : BaseFragment() {

    // DataBinding
    private lateinit var binding: FragmentTransactionBinding

    // provides instance of ViewModel
    private val tranVM: TransactionViewModel by viewModels()

    // arguments from Navigation
    private val args: TransactionFragmentArgs by navArgs()

    // used to record previously selected item
    private var prevAcc = ""
    private var prevExCat = ""
    private var prevInCat = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // pass argument to ViewModel
        tranVM.newTran = args.newTran

        // retrieves Transaction if exists
        tranVM.loadTransaction(args.tranId)

        // array used by PeriodSpinner
        tranVM.periodArray.value = listOf(
            getString(R.string.period_days), getString(R.string.period_weeks),
            getString(R.string.period_months), getString(R.string.period_years)
        )

        tranVM.setVals = setVals
        tranVM.prepareLists(getString(R.string.account_create), getString(R.string.category_create))
    }

    @SuppressLint("RtlHardcoded")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // setting up DataBinding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_transaction, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.tranVM = tranVM

        return binding.root
    }

    @ExperimentalStdlibApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tranVM.tranLD.observe(viewLifecycleOwner, { transaction: Transaction? ->
            // assigns new Transaction if null, which will cause observer to be called again
            // then assigns values from Transaction to LiveData used by XML
            if (transaction == null) {
                tranVM.tranLD.value = Transaction()
            } else {
                tranVM.setTranData(transaction)
            }
        })

        /**
         *  Triggered whenever user changes selection in Account/Category Spinners.
         *  Launches dialog whenever "Create New.." entry is selected.
         */
        tranVM.account.observe(viewLifecycleOwner, { account: String ->
            if (account == getString(R.string.account_create)) {
                val alertDialogView = createAlertDialogView()
                AlertDialogCreator.alertDialogInput(
                    requireContext(), alertDialogView, account,
                    getString(R.string.alert_dialog_save), getString(R.string.alert_dialog_cancel),
                    getString(R.string.account_create),
                    createDialogListeners(binding.tranAccount, prevAcc), tranVM::insertAccount,
                    null, null, null, null, null
                )
            } else {
                prevAcc = account
            }
        })

        tranVM.expenseCat.observe(viewLifecycleOwner, { category: String ->
            if (category == getString(R.string.category_create)) {
                val alertDialogView = createAlertDialogView()
                AlertDialogCreator.alertDialogInput(
                    requireContext(), alertDialogView, category,
                    getString(R.string.alert_dialog_save), getString(R.string.alert_dialog_cancel),
                    getString(R.string.category_create),
                    createDialogListeners(binding.tranExpenseCat, prevExCat), tranVM::insertCategory,
                    null, null, null, null, null
                )
            } else {
                prevExCat = category
            }
        })

        tranVM.incomeCat.observe(viewLifecycleOwner, { category: String ->
            if (category == getString(R.string.category_create)) {
                val alertDialogView = createAlertDialogView()
                AlertDialogCreator.alertDialogInput(
                    requireContext(), alertDialogView, category,
                    getString(R.string.alert_dialog_save), getString(R.string.alert_dialog_cancel),
                    getString(R.string.category_create),
                    createDialogListeners(binding.tranIncomeCat, prevInCat), tranVM::insertCategory,
                    null, null, null, null, null
                )
            } else {
                prevInCat = category
            }
        })

        tranVM.futureTranEvent.observe(viewLifecycleOwner, EventObserver { tran: Transaction ->
            futureTranDialog(tran, tranVM::futureTranPosFun, tranVM::futureTranNegFun)
        })

        tranVM.saveTranEvent.observe(viewLifecycleOwner, EventObserver {
            // Snackbar alerting user that Transaction has been saved.
            val savedBar: Snackbar = Snackbar.make(
                binding.root, getString(R.string.snackbar_saved), Snackbar.LENGTH_SHORT
            )
            savedBar.anchorView = binding.tranAnchor
            savedBar.show()
        })

        tranVM.selectDateEvent.observe(viewLifecycleOwner, EventObserver { date: Date ->
            val dateDialog: DatePickerDialog =
                DateUtils.datePickerDialog(binding.root, date, tranVM::onDateSelected)
            dateDialog.show()
        })
    }

    @SuppressLint("SetTextI18n")
    @ExperimentalStdlibApi
    override fun onStart() {
        super.onStart()

        // navigates user back to CFLFragment
        binding.tranTopBar.setNavigationOnClickListener { requireActivity().onBackPressed() }

        binding.tranTopBar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.transaction_save) {
                tranVM.saveTransaction(getString(R.string.transaction_empty_title))
                true
            } else {
                false
            }
        }
    }

    /**
     *  Creates negative onClick and onCancel listeners for AlertDialog. Which [spinner] AlertDialog
     *  is being created from and [previous] entry that was selected.
     */
    private fun createDialogListeners(spinner: Spinner, previous: String): List<Any> {

        val negListener = DialogInterface.OnClickListener { _, _ ->
            spinner.setSelectedValue(previous)
        }
        val cancelListener = DialogInterface.OnCancelListener { spinner.setSelectedValue(previous) }

        return listOf(negListener, cancelListener)
    }

    /**
     *  Creates AlertDialog if user changes [tran] date and [tran] has been repeated before.
     *  [posFun]/[negFun] are used as the positive and negative button functions.
     */
    private fun futureTranDialog(
        tran: Transaction,
        posFun: (Transaction) -> Unit,
        negFun: (Transaction) -> Unit
    ) {

        val posListener = DialogInterface.OnClickListener { _, _ ->
            posFun(tran)
        }
        val negListener = DialogInterface.OnClickListener { _, _ ->
            negFun(tran)
        }

        AlertDialogCreator.alertDialog(
            requireContext(),
            getString(R.string.alert_dialog_future_transaction),
            getString(R.string.alert_dialog_future_transaction_warning),
            getString(R.string.alert_dialog_yes), posListener,
            getString(R.string.alert_dialog_no), negListener
        )
    }
}