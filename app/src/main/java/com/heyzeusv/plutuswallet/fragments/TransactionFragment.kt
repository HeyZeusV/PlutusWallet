package com.heyzeusv.plutuswallet.fragments

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Spinner
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.database.entities.Transaction
import com.heyzeusv.plutuswallet.databinding.FragmentTransactionBinding
import com.heyzeusv.plutuswallet.utilities.AlertDialogCreator
import com.heyzeusv.plutuswallet.viewmodels.TransactionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.DateFormat
import java.util.Date

private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0

/**
 *  Shows all the information in database of one Transaction and allows users to
 *  edit any field and save changes.
 */
@AndroidEntryPoint
class TransactionFragment : BaseFragment(), DatePickerFragment.Callbacks {

    // DataBinding
    private lateinit var binding: FragmentTransactionBinding

    // provides instance of ViewModel
    private val tranVM: TransactionViewModel by viewModels()

    // arguments from Navigation
    private val args: TransactionFragmentArgs by navArgs()

    // arguments passed
    private var tranId: Int = -1
    private var fromFab: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // retrieve arguments
        tranId = args.tranId
        fromFab = args.fromFab

        // retrieves Transaction if exists
        tranVM.loadTransaction(tranId)

        // array used by PeriodSpinner
        tranVM.periodArray.value = listOf(
            getString(R.string.period_days), getString(R.string.period_weeks),
            getString(R.string.period_months), getString(R.string.period_years)
        )

        tranVM.prepareLists(getString(R.string.account_create), getString(R.string.category_create))
    }

    @SuppressLint("RtlHardcoded")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // setting up DataBinding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_transaction, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.tranVM = tranVM
        binding.setVals = setVals

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
                tranVM.account.value = transaction.account
                // Date to String
                tranVM.date.value =
                    DateFormat.getDateInstance(setVals.dateFormat).format(transaction.date)
                // BigDecimal to String
                tranVM.total.value = if (setVals.decimalPlaces) {
                    when (transaction.total.toString()) {
                        "0" -> ""
                        "0.00" -> ""
                        else -> tranVM.formatDecimal(
                            transaction.total,
                            setVals.thousandsSymbol, setVals.decimalSymbol
                        )
                    }
                } else {
                    if (transaction.total.toString() == "0") {
                        ""
                    } else {
                        tranVM.formatInteger(transaction.total, setVals.thousandsSymbol)
                    }
                }
                if (transaction.type == "Expense") {
                    tranVM.checkedChip.value = R.id.tran_expense_chip
                    tranVM.expenseCat.value = transaction.category
                } else {
                    tranVM.checkedChip.value = R.id.tran_income_chip
                    tranVM.incomeCat.value = transaction.category
                }
                tranVM.repeatCheck.value = transaction.repeating
            }
        })

        /**
         *  Triggered whenever user changes selection in Account/Category Spinners.
         *  Launches dialog whenever "Create New.." entry is selected.
         */
        tranVM.account.observe(viewLifecycleOwner, { account: String ->
            if (account == getString(R.string.account_create)) {
                createDialog(binding.tranAccount, 0)
            }
        })

        tranVM.expenseCat.observe(viewLifecycleOwner, { category: String ->
            if (category == getString(R.string.category_create)) {
                createDialog(binding.tranExpenseCat, 1)
            }
        })

        tranVM.incomeCat.observe(viewLifecycleOwner, { category: String ->
            if (category == getString(R.string.category_create)) {
                createDialog(binding.tranIncomeCat, 1)
            }
        })
    }

    @SuppressLint("SetTextI18n")
    @ExperimentalStdlibApi
    override fun onStart() {
        super.onStart()

        // Launches DatePickerFragment which is essentially a Calender dialog
        tranVM.dateOnClick.value = View.OnClickListener {
            DatePickerFragment.newInstance(tranVM.tranLD.value!!.date).apply {
                // fragment that will be target and request code
                setTargetFragment(this@TransactionFragment, REQUEST_DATE)
                // want requireFragmentManager from TransactionFragment, so need outer scope
                show(this@TransactionFragment.parentFragmentManager, DIALOG_DATE)
            }
        }

        // navigates user back to CFLFragment
        binding.tranTopBar.setNavigationOnClickListener { requireActivity().onBackPressed() }

        binding.tranTopBar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.transaction_save) {
                // reassigns LiveData values that couldn't be used directly from Transaction
                // back to it and saves or updates Transaction
                tranVM.tranLD.value?.let {
                    // assigns new id if new Transaction
                    if (fromFab) it.id = tranVM.maxId + 1

                    // gives Transaction simple title if user doesn't enter any
                    if (it.title.trim().isEmpty()) it.title =
                        getString(R.string.transaction_empty_title) + it.id

                    // is empty if account hasn't been changed so defaults to first account
                    it.account = if (tranVM.account.value == "") {
                        tranVM.accountList.value!![0]
                    } else {
                        tranVM.account.value!!
                    }

                    // converts the totalField from String into BigDecimal
                    it.total = when {
                        tranVM.total.value!!.isEmpty() && setVals.decimalPlaces ->
                            BigDecimal("0.00")
                        tranVM.total.value!!.isEmpty() -> BigDecimal("0")
                        else -> BigDecimal(
                            tranVM.total.value!!
                                .replace(setVals.thousandsSymbol.toString(), "")
                                .replace(setVals.decimalSymbol.toString(), ".")
                        )
                    }

                    // sets type depending on Chip selected
                    // cat values are empty if they haven't been changed so defaults to first category
                    if (tranVM.checkedChip.value == R.id.tran_expense_chip) {
                        it.type = "Expense"
                        it.category = if (tranVM.expenseCat.value == "") {
                            tranVM.expenseCatList.value!![0]
                        } else {
                            tranVM.expenseCat.value!!
                        }
                    } else {
                        it.type = "Income"
                        it.category = if (tranVM.incomeCat.value == "") {
                            tranVM.incomeCatList.value!![0]
                        } else {
                            tranVM.incomeCat.value!!
                        }
                    }

                    it.repeating = tranVM.repeatCheck.value!!
                    if (it.repeating) it.futureDate = tranVM.createFutureDate()
                    // frequency must always be at least 1
                    if (it.frequency < 1) it.frequency = 1

                    // coroutine that Save/Updates/warns user of FutureDate
                    launch {
                        if (it.futureTCreated && tranVM.dateChanged && it.repeating) {
                            // AlertDialog that asks user if they want Transaction to repeat again
                            val posFun = DialogInterface.OnClickListener { _, _ ->
                                it.futureTCreated = false
                                launch { tranVM.upsertTransaction(it) }
                                createSnackbar()
                            }
                            val negFun = DialogInterface.OnClickListener { _, _ ->
                                tranVM.dateChanged = false
                                launch { tranVM.upsertTransaction(it) }
                                createSnackbar()
                            }
                            AlertDialogCreator.alertDialog(
                                requireContext(),
                                getString(R.string.alert_dialog_future_transaction),
                                getString(R.string.alert_dialog_future_transaction_warning),
                                getString(R.string.alert_dialog_yes), posFun,
                                getString(R.string.alert_dialog_no), negFun
                            )
                        } else {
                            // upsert Transaction
                            tranVM.upsertTransaction(it)
                            createSnackbar()
                            tranVM.loadTransaction(it.id)
                        }
                    }
                }
                true
            } else {
                false
            }
        }
    }

    /**
     *  Creates AlertDialog when user selects "Create New ..."
     *  in [type] (Account/Category) of [spinner].
     */
    @ExperimentalStdlibApi
    private fun createDialog(spinner: Spinner, type: Int) {

        // inflates view that holds EditText
        val viewInflated: View = LayoutInflater.from(context)
            .inflate(R.layout.dialog_input_field, view as ViewGroup, false)
        // the EditText to be used
        val input: EditText = viewInflated.findViewById(R.id.dialog_input)
        val title: String = if (type == 0) {
            getString(R.string.account_create)
        } else {
            getString(R.string.category_create)
        }

        // Listeners
        val cancelListener = DialogInterface.OnCancelListener { spinner.setSelection(0) }
        val negListener = DialogInterface.OnClickListener { _, _ ->
            spinner.setSelection(0)
        }
        val posListener = DialogInterface.OnClickListener { _, _ ->
            if (type == 0) {
                tranVM.insertAccount(input.text.toString(), getString(R.string.account_create))
            } else {
                tranVM.insertCategory(input.text.toString(), getString(R.string.category_create))
            }
        }

        AlertDialogCreator.alertDialogInputCancelable(
            requireContext(),
            title, viewInflated,
            getString(R.string.alert_dialog_save), posListener,
            getString(R.string.alert_dialog_cancel), negListener,
            cancelListener
        )
    }

    /**
     *  Snackbar alerting user that Transaction has been saved.
     */
    private fun createSnackbar() {

        val savedBar: Snackbar = Snackbar.make(
            binding.root,
            getString(R.string.snackbar_saved), Snackbar.LENGTH_SHORT
        )
        savedBar.anchorView = binding.tranAnchor
        savedBar.show()
    }

    /**
     *  Will update Transaction date with [date] selected from DatePickerFragment.
     */
    override fun onDateSelected(date: Date) {

        tranVM.dateChanged = tranVM.tranLD.value!!.date != date
        tranVM.tranLD.value!!.date = date
        tranVM.date.value = DateFormat.getDateInstance(setVals.dateFormat).format(date)
    }
}