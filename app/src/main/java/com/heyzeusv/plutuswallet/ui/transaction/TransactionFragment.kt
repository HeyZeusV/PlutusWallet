package com.heyzeusv.plutuswallet.ui.transaction

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
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

        binding.composeView.apply {
            // Dispose the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                Column {
                    TransactionTextInput("", "Title", "Helper Text", maxLength = 10)
                    TransactionDate(tranVM)
                    TransactionDropDownMenu(tranVM)
                    TransactionCurrencyInput(sharedPref)
                }
            }
        }

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
                binding.tranAccount.setText(transaction.account, false)
                if (transaction.type == "Expense") {
                    binding.tranExpenseCat.setText(transaction.category, false)
                } else {
                    binding.tranIncomeCat.setText(transaction.category, false)
                }
                if (tranVM.repeat.value!!) binding.tranRepeatMotion.transitionToEnd()
                binding.tranPeriod.setText(tranVM.period, false)
            }
        })

        binding.tranDate.setOnFocusChangeListener { _, focused: Boolean ->
            if (focused) {
                tranVM.selectDateOC(tranVM.tranLD.value!!.date)
            }
        }

        binding.tranAccount.setOnItemClickListener { adapterView: AdapterView<*>, _, i: Int, _ ->
            val selected: String = adapterView.adapter.getItem(i).toString()
            if (selected == getString(R.string.account_create)) {
                val alertDialogView: View = createAlertDialogView()
                AlertDialogCreator.alertDialogInput(
                    requireContext(), alertDialogView, selected,
                    getString(R.string.alert_dialog_save), getString(R.string.alert_dialog_cancel),
                    getString(R.string.account_create),
                    createDialogListeners(binding.tranAccount, tranVM.account), tranVM::insertAccount,
                    null, null, null, null, null
                )
            } else {
                tranVM.account = selected
            }
        }

        binding.tranExpenseCat.setOnItemClickListener { adapterView: AdapterView<*>, _, i: Int, _ ->
            val selected: String = adapterView.adapter.getItem(i).toString()
            if (selected == getString(R.string.category_create)) {
                val alertDialogView: View = createAlertDialogView()
                AlertDialogCreator.alertDialogInput(
                    requireContext(), alertDialogView, selected,
                    getString(R.string.alert_dialog_save), getString(R.string.alert_dialog_cancel),
                    getString(R.string.category_create),
                    createDialogListeners(binding.tranExpenseCat, tranVM.expenseCat),
                    tranVM::insertCategory, null, null, null, null, null
                )
            } else {
                tranVM.expenseCat = selected
            }
        }

        binding.tranIncomeCat.setOnItemClickListener { adapterView: AdapterView<*>, _, i: Int, _ ->
            val selected: String = adapterView.adapter.getItem(i).toString()
            if (selected == getString(R.string.category_create)) {
                val alertDialogView: View = createAlertDialogView()
                AlertDialogCreator.alertDialogInput(
                    requireContext(), alertDialogView, selected,
                    getString(R.string.alert_dialog_save), getString(R.string.alert_dialog_cancel),
                    getString(R.string.category_create),
                    createDialogListeners(binding.tranIncomeCat, tranVM.incomeCat),
                    tranVM::insertCategory, null, null, null, null, null
                )
            } else {
                tranVM.incomeCat = selected
            }
        }

        binding.tranRepeat.setOnClickListener {
            tranVM.repeat.value = !tranVM.repeat.value!!
            if (tranVM.repeat.value!!) {
                binding.tranRepeatMotion.transitionToEnd()
                // after a short delay, scrolls to the bottom of ScrollView
                binding.tranScrollView.postDelayed({
                    binding.tranScrollView.smoothScrollTo(0, binding.tranScrollView.bottom)
                }, 450)
            } else {
                binding.tranRepeatMotion.transitionToStart()
            }
        }

        binding.tranPeriod.setOnItemClickListener { adapterView: AdapterView<*>, _, i: Int, _ ->
            tranVM.period = adapterView.adapter.getItem(i).toString()
        }

        tranVM.createEvent.observe(viewLifecycleOwner, EventObserver { info: Pair<Int, String> ->
            when (info.first) {
                0 -> binding.tranAccount.setText(info.second, false)
                1 -> binding.tranExpenseCat.setText(info.second, false)
                2 -> binding.tranIncomeCat.setText(info.second, false)
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
            savedBar.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorSnackbarText))
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
    private fun createDialogListeners(spinner: AutoCompleteTextView, previous: String): List<Any> {

        val negListener = DialogInterface.OnClickListener { _, _ ->
            spinner.setText(previous, false)
        }
        val cancelListener = DialogInterface.OnCancelListener { spinner.setText(previous, false) }

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