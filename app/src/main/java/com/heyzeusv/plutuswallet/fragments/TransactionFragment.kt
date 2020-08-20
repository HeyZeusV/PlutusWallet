 package com.heyzeusv.plutuswallet.fragments

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Spinner
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.database.entities.Transaction
import com.heyzeusv.plutuswallet.databinding.FragmentTransactionBinding
import com.heyzeusv.plutuswallet.utilities.AlertDialogCreator
import com.heyzeusv.plutuswallet.utilities.Utils
import com.heyzeusv.plutuswallet.viewmodels.TransactionViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.DateFormat
import java.util.Date
import kotlin.math.hypot

private const val ARG_TRANSACTION_ID  = "transaction_id"
private const val ARG_FAB_X           = "fab_X"
private const val ARG_FAB_Y           = "fab_Y"
private const val ARG_FROM_FAB        = "from_fab"
private const val DIALOG_DATE         = "DialogDate"
private const val REQUEST_DATE        = 0

/**
 *  Shows all the information in database of one Transaction and allows users to
 *  edit any field and save changes.
 *
 *  @param tranID  id of current Transaction
 *  @param fromFab true if user clicked on FAB to create a new Transaction
 */
class TransactionFragment(private val tranID : Int, private var fromFab : Boolean)
    : BaseFragment(), DatePickerFragment.Callbacks {

    // DataBinding
    private lateinit var binding : FragmentTransactionBinding

    // provides instance of ViewModel
    private val tranVM : TransactionViewModel by lazy {
        ViewModelProvider(this).get(TransactionViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // retrieves Transaction if exists
        tranVM.loadTransaction(tranID)

        // array used by PeriodSpinner
        tranVM.periodArray.value = listOf(
            getString(R.string.period_days), getString(R.string.period_weeks),
            getString(R.string.period_months), getString(R.string.period_years))

        tranVM.prepareLists(getString(R.string.account_create), getString(R.string.category_create))
    }

    @SuppressLint("RtlHardcoded")
    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?,
                              savedInstanceState : Bundle?) : View? {

        // setting up DataBinding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_transaction, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.tranVM         = tranVM
        binding.setVals        = setVals

        val view : View = binding.root

        // checks to see how user arrived to TransactionFragment
        if (fromFab) {

            // animation that plays on user presses FAB
            view.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {

                override fun onLayoutChange(v : View, left : Int, top : Int, right : Int, bot : Int,
                                            oLeft : Int, oTop : Int, oRight : Int, oButton : Int) {

                    v.removeOnLayoutChangeListener(this)
                    val location = IntArray(2)
                    binding.tranSave.getLocationOnScreen(location)
                    val fabX        : Int   = location[0] + binding.tranSave.width/2
                    val fabY        : Int   = location[1] - binding.tranSave.height
                    val finalRadius : Float =
                        hypot(view.width.toDouble(), view.height.toDouble()).toFloat()
                    // view, initialX, initialY, startingRadius, endRadius
                    val anim : Animator = ViewAnimationUtils.createCircularReveal(
                        v, fabX, fabY, 10.0F, finalRadius)
                    // time to complete animation
                    anim.duration = 1000
                    anim.start()
                }
            })
        }

        return view
    }

    @ExperimentalStdlibApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // register an observer on LiveData instance and tie life to this component
        // execute code whenever LiveData gets updated
        tranVM.tranLD.observe(viewLifecycleOwner, { transaction : Transaction? ->

            // assigns new Transaction if null, which will cause observer to be called again
            // then assigns values from Transaction to LiveData used by XML
            when (transaction) {
                null -> tranVM.tranLD.value = Transaction()
                else -> {
                    tranVM.account.value = transaction.account
                    // Date to String
                    tranVM.date.value =
                        DateFormat.getDateInstance(setVals.dateFormat).format(transaction.date)
                    // BigDecimal to String
                    tranVM.total.value = when {
                        setVals.decimalPlaces -> when (transaction.total.toString()) {
                            "0"    -> ""
                            "0.00" -> ""
                            else   -> Utils.formatDecimal(transaction.total.toString(),
                                setVals.thousandsSymbol, setVals.decimalSymbol)
                        }
                        else -> when (transaction.total.toString()) {
                            "0"  -> ""
                            else -> Utils.formatInteger(
                                transaction.total.toString(), setVals.thousandsSymbol)
                        }
                    }
                    when(transaction.type) {
                        "Expense" -> {
                            tranVM.checkedChip.value = R.id.tran_expense_chip
                            tranVM.expenseCat.value  = transaction.category
                        }
                        else -> {
                            tranVM.checkedChip.value = R.id.tran_income_chip
                            tranVM.incomeCat.value   = transaction.category
                        }
                    }
                    tranVM.repeatCheck.value = transaction.repeating
                }
            }
        })

        /**
         *  Triggered whenever user changes selection in Account/Category Spinners.
         *  Launches dialog whenever "Create New.." entry is selected.
         */
        tranVM.account.observe(viewLifecycleOwner, { account : String ->

            if (account == getString(R.string.account_create)) {

                createDialog(binding.tranAccount, 0)
            }
        })

        tranVM.expenseCat.observe(viewLifecycleOwner, { category : String ->

            if (category == getString(R.string.category_create)) {

                createDialog(binding.tranExpenseCat, 1)
            }
        })

        tranVM.incomeCat.observe(viewLifecycleOwner, { category : String ->

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

        // reassigns LiveData values that couldn't be used directly from Transaction back to it
        // and saves or updates Transaction
        tranVM.saveOnClick.value = View.OnClickListener { view : View ->

            tranVM.tranLD.value?.let {

                // assigns new id if new Transaction
                if (fromFab) it.id = tranVM.maxId + 1

                // gives Transaction simple title if user doesn't enter any
                if (it.title.trim().isEmpty()) it.title =
                    getString(R.string.transaction_empty_title) + it.id

                // is empty if account hasn't been changed so defaults to first account
                it.account = when(tranVM.account.value) {
                    "" -> tranVM.accountList.value!![0]
                    else -> tranVM.account.value!!
                }

                // converts the totalField from String into BigDecimal
                it.total = when {
                    tranVM.total.value!!.isEmpty() && setVals.decimalPlaces -> BigDecimal("0.00")
                    tranVM.total.value!!.isEmpty() -> BigDecimal("0")
                    else -> BigDecimal(tranVM.total.value!!
                        .replace(setVals.thousandsSymbol.toString(), "")
                        .replace(setVals.decimalSymbol.toString(), "."))
                }

                // sets type depending on Chip selected
                // cat values are empty if they haven't been changed so defaults to first category
                when (tranVM.checkedChip.value) {
                    R.id.tran_expense_chip -> {
                        it.type     = "Expense"
                        it.category = when(tranVM.expenseCat.value) {
                            ""   -> tranVM.expenseCatList.value!![0]
                            else -> tranVM.expenseCat.value!!
                        }
                    }
                    else -> {
                        it.type     = "Income"
                        it.category = when(tranVM.incomeCat.value) {
                            ""   -> tranVM.incomeCatList.value!![0]
                            else -> tranVM.incomeCat.value!!
                        }
                    }
                }

                it.repeating = tranVM.repeatCheck.value!!
                if (it.repeating) it.futureDate = tranVM.createFutureDate()

                // frequency must always be at least 1
                if (it.frequency < 1) it.frequency = 1

                launch {

                    when {
                        // AlertDialog that asks user if they want Transaction to repeat again
                        it.futureTCreated && tranVM.dateChanged && it.repeating -> {
                            val posFun = DialogInterface.OnClickListener { _, _ ->

                                it.futureTCreated = false
                                launch { tranVM.upsertTransaction(it) }
                                createSnackbar(view)
                            }
                            val negFun = DialogInterface.OnClickListener { _, _ ->

                                tranVM.dateChanged = false
                                launch { tranVM.upsertTransaction(it) }
                                createSnackbar(view)
                            }

                            AlertDialogCreator.alertDialog(requireContext(),
                                getString(R.string.alert_dialog_future_transaction),
                                getString(R.string.alert_dialog_future_transaction_warning),
                                getString(R.string.alert_dialog_yes), posFun,
                                getString(R.string.alert_dialog_no), negFun)
                        }
                        // upsert Transaction
                        else -> {
                            tranVM.upsertTransaction(it)
                            createSnackbar(view)
                            tranVM.loadTransaction(it.id)
                        }
                    }
                }
            }
        }
    }

    /**
     *  Creates AlertDialog when user selects "Create New ..."
     *
     *  @param spinner Spinner to be applied to.
     *  @param type    0 = Account, 1 = Category.
     */
    @ExperimentalStdlibApi
    private fun createDialog(spinner : Spinner, type : Int) {

        // inflates view that holds EditText
        val viewInflated : View = LayoutInflater.from(context)
            .inflate(R.layout.dialog_input_field, view as ViewGroup, false)
        // the EditText to be used
        val input : EditText = viewInflated.findViewById(R.id.dialog_input)

        val title : String = when (type) {
            0    -> getString(R.string.account_create)
            else -> getString(R.string.category_create)
        }

        // Listeners
        val cancelListener = DialogInterface.OnCancelListener {

            spinner.setSelection(0)
        }
        val negListener = DialogInterface.OnClickListener { _, _ ->

            spinner.setSelection(0)
        }
        val posListener = DialogInterface.OnClickListener { _, _ ->

            when (type) {
                0    -> tranVM.insertAccount(input.text.toString(), getString(R.string.account_create))
                else -> tranVM.insertCategory(input.text.toString(), getString(R.string.category_create))
            }
        }

        AlertDialogCreator.alertDialogInputCancelable(requireContext(),
            title,
            viewInflated,
            getString(R.string.alert_dialog_save), posListener,
            getString(R.string.alert_dialog_cancel), negListener,
            cancelListener)
    }

    /**
     *  Snackbar alerting user that Transaction has been saved.
     *
     *  @param view the view that Snackbar will be anchored to.
     */
    private fun createSnackbar(view : View) {

        val savedBar : Snackbar = Snackbar.make(view,
            getString(R.string.snackbar_saved), Snackbar.LENGTH_SHORT)
        savedBar.anchorView = view
        savedBar.show()
    }

    /**
     *  Will update Transaction date with date selected from DatePickerFragment.
     *
     *  @param date the date selected by the user.
     */
    override fun onDateSelected(date : Date) {

        tranVM.dateChanged = tranVM.tranLD.value!!.date != date
        tranVM.tranLD.value!!.date = date
        tranVM.date.value  = DateFormat.getDateInstance(setVals.dateFormat).format(date)
    }

    companion object {

        /**
         *  Initializes instance of TransactionFragment.
         *
         *  Creates arguments Bundle, creates a Fragment instance, and attaches the
         *  arguments to the Fragment.
         *
         *  @param transactionId id of Transaction selected.
         *  @param fabX          the x position of FAB
         *  @param fabY          the y position of FAB
         *  @param fromFab       true if user clicked on FAB to create Transaction.
         */
        fun newInstance(transactionId : Int, fabX : Int, fabY : Int, fromFab : Boolean) : TransactionFragment {

            val args : Bundle = Bundle().apply {

                putInt    (ARG_TRANSACTION_ID, transactionId)
                putInt    (ARG_FAB_X         , fabX         )
                putInt    (ARG_FAB_Y         , fabY         )
                putBoolean(ARG_FROM_FAB      , fromFab      )
            }

            return TransactionFragment(transactionId, fromFab).apply {

                arguments = args
            }
        }
    }
}