package com.heyzeusv.financeapplication

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.heyzeusv.financeapplication.utilities.BaseFragment
import com.heyzeusv.financeapplication.utilities.Utils
import kotlinx.coroutines.*
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

private const val TAG               = "TransactionListFragment"
private const val ARG_CATEGORY      = "category"
private const val ARG_DATE          = "date"
private const val ARG_TYPE          = "type"
private const val ARG_CATEGORY_NAME = "category_name"
private const val ARG_START         = "start"
private const val ARG_END           = "end"
private const val KEY_MAX_ID        = "key_max_id"
private const val KEY_CURRENCY_SYMBOL = "key_currency_symbol"
private const val KEY_DECIMAL_PLACES  = "key_decimal_places"
private const val KEY_SYMBOL_SIDE     = "key_symbol_side"

/**
 *  Will show list of Transactions depending on filters applied.
 */
class TransactionListFragment : BaseFragment() {

    /**
     *  Required interface for hosting fragments.
     *
     *  Defines work that the fragment needs done by hosting activity.
     */
    interface Callbacks {

        /**
         *  Replaces TransactionListFragment, FilterFragment, and GraphFragment with TransactionFragment selected.
         *
         *  @param transactionId id of Transaction selected.
         *  @param fromFab       true if user clicked on FAB to create Transaction.
         */
        fun onTransactionSelected(transactionId : Int, fromFab : Boolean)
    }

    private var callbacks : Callbacks? = null

    // Shared Preferences
    private lateinit var sp     : SharedPreferences
    private lateinit var editor : SharedPreferences.Editor

    // views
    private lateinit var transactionAddFab       : FloatingActionButton
    private lateinit var transactionRecyclerView : RecyclerView

    // used to tell if app is first starting up
    private var startUp : Boolean = true

    // holds position of RecyclerView so that it doesn't reset when user returns
    private var recyclerViewPosition : Int = 0

    // used for SharedPreferences
    private var decimalPlaces : Boolean = true
    private var maxId         : Int     = 0
    private var symbolSide    : Boolean = true
    private var symbolKey     : String  = "dollar"
    private var symbol        : String  = "$"

    // initialize adapter with empty crime list since we have to wait for results from DB
    private var transactionAdapter : TransactionAdapter? = TransactionAdapter(emptyList())

    // formatters used for Total
    val decimalFormatter = DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US))
    val integerFormatter = DecimalFormat("#,###", DecimalFormatSymbols.getInstance(Locale.US))

    // provides instance of ViewModel
    private val transactionListViewModel : TransactionListViewModel by lazy {
        ViewModelProviders.of(this).get(TransactionListViewModel::class.java)
    }

    override fun onAttach(context : Context) {
        super.onAttach(context)

        // stashing context into callbacks property which is the activity instance hosting fragment
        callbacks = context as Callbacks?
    }

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View? {

        val view : View = inflater.inflate(R.layout.fragment_transaction_list, container, false)

        transactionAddFab       = view.findViewById(R.id.transaction_add_fab)       as FloatingActionButton
        transactionRecyclerView = view.findViewById(R.id.transaction_recycler_view) as RecyclerView

        val linearLayoutManager = LinearLayoutManager(context)
        // newer items will be displayed at the top of RecyclerView
        linearLayoutManager.reverseLayout = true
        // scrollToPosition will display item scrolled to at top rather than
        // below when this is false
        linearLayoutManager.stackFromEnd = true
        // RecyclerView NEEDS a LayoutManager to work
        transactionRecyclerView.layoutManager = linearLayoutManager
        // set adapter for RecyclerView
        transactionRecyclerView.adapter = transactionAdapter
        // adds horizontal divider between each item in RecyclerView
        transactionRecyclerView.addItemDecoration(DividerItemDecoration(
            transactionRecyclerView.context, DividerItemDecoration.VERTICAL))

        // retrieves any saved preferences
        sp    = PreferenceManager.getDefaultSharedPreferences(activity)
        maxId = sp.getInt(KEY_MAX_ID, 0)

        return view
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        // loads in arguments, if any
        val category     : Boolean? = arguments?.getBoolean     (ARG_CATEGORY)
        val date         : Boolean? = arguments?.getBoolean     (ARG_DATE)
        val type         : String?  = arguments?.getString      (ARG_TYPE)
        val categoryName : String?  = arguments?.getString      (ARG_CATEGORY_NAME)
        val start        : Date?    = arguments?.getSerializable(ARG_START)         as Date?
        val end          : Date?    = arguments?.getSerializable(ARG_END)           as Date?

        Log.d(TAG, "$category, $date, $type, $categoryName, $start, $end")

        // tells ViewModel which query to run on Transactions
        val transactionListLiveData : LiveData<List<Transaction>> =
            transactionListViewModel.filteredTransactionList(category, date, type, categoryName, start, end)

        // register an observer on LiveData instance and tie life to another component
        transactionListLiveData.observe(
            // view's lifecycle owner ensures that updates are only received when view is on screen
            viewLifecycleOwner,
            // executed whenever LiveData gets updated
            Observer { transactions ->
                // if not null
                transactions?.let {

                    Log.i(TAG, "Got crimes $transactions")
                    updateUI(transactions)
                }
            }
        )

        // gets the sizes of the Category tables and sends them to initializeCategoryTables()
        launch {

            val expenseSize : Int? = transactionListViewModel.getExpenseCategorySizeAsync().await()
            val incomeSize  : Int? = transactionListViewModel.getIncomeCategorySizeAsync ().await()

            initializeCategoryTables(expenseSize, incomeSize)
        }
    }

    override fun onStart() {
        super.onStart()

        transactionAddFab.setOnClickListener {
            callbacks?.onTransactionSelected(0, true)
            // this will make it so the list will snap to the top after user
            // creates a new Transaction
            recyclerViewPosition = transactionRecyclerView.adapter!!.itemCount
        }

        // retrieves any saved preferences
        decimalPlaces = sp.getBoolean(KEY_DECIMAL_PLACES, true)
        symbolSide    = sp.getBoolean(KEY_SYMBOL_SIDE, true)
        symbolKey     = sp.getString(KEY_CURRENCY_SYMBOL, "dollar")!!

        // retrieves symbol to be used according to settings
        symbol = Utils.getSymbol(symbolKey)
        // tell RecyclerView that symbol has been changed
        transactionAdapter?.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()

        futureTransactions()
    }

    override fun onPause() {
        super.onPause()

        // saves maxId into SharedPreferences
        editor = sp.edit()
        editor.putInt(KEY_MAX_ID, maxId)
        editor.apply()
    }

    override fun onDetach() {
        super.onDetach()

        // afterward you cannot access the activity
        // or count on the activity continuing to exist
        callbacks = null
    }



    /**
     *  Adds frequency * period to the date on Transaction.
     *
     *  @param date      the date of Transaction.
     *  @param period    how often Transaction repeats.
     *  @param frequency how often Transaction repeats.
     *
     */
    private fun createFutureDate(date : Date, period : Int, frequency : Int) : Date {

        val calendar : Calendar = Calendar.getInstance()
        // set to Transaction date rather than current time due to Users being able
        // to select a Date in the past or future
        calendar.time = date

        // 0 = Day, 1 = Week, 2 = Month, 3 = Year
        when (period) {

            0 -> calendar.add(Calendar.DAY_OF_MONTH, frequency)
            1 -> calendar.add(Calendar.WEEK_OF_YEAR, frequency)
            2 -> calendar.add(Calendar.MONTH       , frequency)
            3 -> calendar.add(Calendar.YEAR        , frequency)
        }

        // reset hour, minutes, seconds and millis to start of day
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.time
    }

    /**
     *  Adds new Transactions depending on existing Transactions futureDate.
     */
    private fun futureTransactions() {

        launch {

            // used to tell if newly created Transaction's futureDate is before Date()
            var moreToCreate = false
            // returns list of all Transactions whose futureDate is before current date
            val futureTransactionList : MutableList<Transaction> = transactionListViewModel.getFutureTransactionsAsync(Date()).await().toMutableList()

            // co-routine is used in order to wait for entire forEach loop to complete
            // without this, not all new Transactions created are saved correctly
            val deferredList : Deferred<MutableList<Transaction>> = async(context = ioContext) {

                // list that will be upserted into database
                val readyToUpsert : MutableList<Transaction> = mutableListOf()

                futureTransactionList.forEach {

                    // id must be unique
                    maxId += 1
                    // gets copy of Transaction attached to this FutureTransaction
                    val transaction : Transaction = it.copy()
                    // changing new Transaction values to updated values
                    transaction.id         = maxId
                    transaction.date       = it.futureDate
                    transaction.title      = incrementString (transaction.title)
                    transaction.futureDate = createFutureDate(transaction.date, transaction.period, transaction.frequency)
                    // if new futureDate is less than Date() then there are more Transactions to be added
                    if (transaction.futureDate < Date()) {

                        moreToCreate = true
                    }
                    // stops this Transaction from being repeated again if user switches its date
                    it.futureTCreated      = true
                    // transaction to be inserted
                    readyToUpsert.add(transaction)
                    // it to be updated
                    readyToUpsert.add(it)
                }

                return@async readyToUpsert
            }

            transactionListViewModel.upsertTransactions(deferredList.await())
            // RecyclerView will scroll to top of the list whenever a Transaction is added
            recyclerViewPosition = maxId
            // recursive call in order to create Transactions until all futureDates are past Date()
            if (moreToCreate) {

                futureTransactions()
            }
        }
    }

    /**
     *  Appends " (R)x####" to the end of Transaction title that has been repeated.
     *
     *  @param title the title of Transaction.
     */
    private fun incrementString(title : String) : String {

        val prefix                        = "(R)x"
        // pattern: (R)x######
        val regex                         = Regex("(\\(R\\)x)\\d+")
        // will search for regex in title
        val match          : MatchResult? = regex.find(title)
        // string that match found, if any
        val matchingString : String?      = match?.value
        var newTitle       : String       = title

        if (matchingString != null) {

            // removes "(R)x###" as this will be updated
            newTitle              = newTitle      .replace(regex         , "")
            // returns only the Int and increments it by one
            var noPrefixInt : Int = matchingString.replace("(R)x", "").toInt()
            noPrefixInt          += 1
            // appends the prefix and updated int onto the end of title
            newTitle             += prefix + noPrefixInt
        } else {

            // first time being repeated
            newTitle += " (R)x1"
        }

        return newTitle
    }

    /**
     *  This should be run the very first time a user opens the app or if they delete
     *  all the categories in one table, fills table with a few predetermined categories.
     *
     *  @param expenseSize size of ExpenseCategory Table.
     *  @param incomeSize  size of IncomeCategory Table.
     */
    private fun initializeCategoryTables(expenseSize : Int?, incomeSize : Int?) {

        launch {

            if (expenseSize == 0 || expenseSize == null) {

                val education      = ExpenseCategory(getString(R.string.education))
                val entertainment  = ExpenseCategory(getString(R.string.entertainment))
                val food           = ExpenseCategory(getString(R.string.food))
                val home           = ExpenseCategory(getString(R.string.home))
                val transportation = ExpenseCategory(getString(R.string.transportation))
                val utilities      = ExpenseCategory(getString(R.string.utilities))
                val initialExpenseCategories : List<ExpenseCategory> = listOf(
                    education, entertainment, food, home, transportation, utilities)
                transactionListViewModel.insertExpenseCategories(initialExpenseCategories)
            }

            if (incomeSize == 0 || incomeSize == null) {

                val cryptocurrency = IncomeCategory(getString(R.string.cryptocurrency))
                val investments    = IncomeCategory(getString(R.string.investments))
                val salary         = IncomeCategory(getString(R.string.salary))
                val savings        = IncomeCategory(getString(R.string.savings))
                val stocks         = IncomeCategory(getString(R.string.stocks))
                val wages          = IncomeCategory(getString(R.string.wages))
                val initialIncomeCategories : List<IncomeCategory> = listOf(
                    cryptocurrency, investments, salary, savings, stocks, wages)
                transactionListViewModel.insertIncomeCategories(initialIncomeCategories)
            }
        }
    }

    /**
     *  Ensures the UI is up to date with correct information.
     *
     *  @param transactions the list of Transactions to be displayed.
     */
    private fun updateUI(transactions : List<Transaction>) {

        // creates TransactionAdapter to set with RecyclerView
        transactionAdapter              = TransactionAdapter(transactions)
        transactionRecyclerView.adapter = transactionAdapter

        // used to return user to previous position in transactionRecyclerView
        transactionRecyclerView.scrollToPosition(recyclerViewPosition)

        // this will only run once, when application is first started
        if (startUp) {

            // will make RecyclerView open up at the last item in list which would
            // be the item at the top of the view since it will be reversed
            transactionRecyclerView.scrollToPosition(transactionRecyclerView.size - 1)
            startUp = false
        }
    }

    /**
     *  Creates ViewHolder and binds ViewHolder to data from model layer.
     *
     *  @param transactions the list of Transactions.
     */
    private inner class TransactionAdapter(var transactions : List<Transaction>)
        : RecyclerView.Adapter<TransactionHolder>() {

        // creates view to display, wraps the view in a ViewHolder and returns the result
        override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : TransactionHolder {

            val view : View = layoutInflater.inflate(R.layout.item_view_transaction, parent, false)
            return TransactionHolder(view)
        }

        override fun getItemCount() = transactions.size

        // populates given holder with Transaction from the given position in TransactionList
        override fun onBindViewHolder(holder : TransactionHolder, position : Int) {

            val transaction : Transaction = transactions[position]
            holder.bind(transaction)
        }
    }

    /**
     *  ViewHolder stores a reference to an item's view.
     */
    private inner class TransactionHolder(view : View)
        : RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener {

        private lateinit var transaction : Transaction

        // views in the ItemView
        private val titleTextView : TextView = itemView.findViewById(R.id.transaction_title)
        private val dateTextView  : TextView = itemView.findViewById(R.id.transaction_date)
        private val totalTextView : TextView = itemView.findViewById(R.id.transaction_total)

        init {

            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        // sets the views with Transaction data
        fun bind(transaction : Transaction) {

            this.transaction   = transaction
            titleTextView.text = this       .transaction.title
            dateTextView .text = DateFormat .getDateInstance(DateFormat.FULL).format(this.transaction.date)
            // formats the Total correctly
            if (decimalPlaces) {

                if (symbolSide) {

                    totalTextView.text = getString(R.string.total_number_symbol, symbol, decimalFormatter.format(this.transaction.total))
                } else {

                    totalTextView.text = getString(R.string.total_number_symbol, decimalFormatter.format(this.transaction.total), symbol)
                }
            } else {

                if (symbolSide) {

                    totalTextView.text = getString(R.string.total_number_symbol, symbol, integerFormatter.format(this.transaction.total))
                } else {

                    totalTextView.text = getString(R.string.total_number_symbol, integerFormatter.format(this.transaction.total), symbol)
                }
            }
            context?.let {
                // changes the color depending on Type
                if (transaction.type == "Expense") {

                    totalTextView.setTextColor(ContextCompat.getColor(it, android.R.color.holo_red_dark))
                } else {

                    totalTextView.setTextColor(ContextCompat.getColor(it, android.R.color.holo_green_dark))
                }
            }
        }

        override fun onClick(v : View?) {

            // the position that the user clicked on
            recyclerViewPosition = this.layoutPosition
            // notifies hosting activity which item was selected
            callbacks?.onTransactionSelected(transaction.id, false)
        }

        /**
         *  Shows AlertDialog asking user if they want to delete Transaction.
         */
        override fun onLongClick(v : View?) : Boolean {

            recyclerViewPosition = this.layoutPosition
            // initialize instance of Builder
            val alertDialogBuilder = MaterialAlertDialogBuilder(context)
            // set title of AlertDialog
            alertDialogBuilder.setTitle("Delete Transaction")
            // set message of AlertDialog
            alertDialogBuilder.setMessage("Are you sure you want to delete ${transaction.title}?")
            // set positive button and its click listener
            alertDialogBuilder.setPositiveButton("YES") { _, _ ->

                launch {

                    transactionListViewModel.deleteTransaction(transaction)
                }
            }
            // set negative button and its click listener
            alertDialogBuilder.setNegativeButton("NO") { _, _ ->  }
            // make the AlertDialog using the builder
            val alertDialog : androidx.appcompat.app.AlertDialog = alertDialogBuilder.create()
            // display AlertDialog
            alertDialog.show()

            return true
        }
    }

    companion object {

        /**
         *  Initializes instance of TransactionListFragment.
         */
        fun newInstance() : TransactionListFragment {

            return TransactionListFragment()
        }

        /**
         *  Initializes instance of TransactionListFragment.
         *
         *  Creates arguments Bundle, creates a Fragment instance, and attaches the
         *  arguments to the Fragment.
         *
         *  @param  category     boolean for category filter.
         *  @param  date         boolean for date filter.
         *  @param  type         either "Expense" or "Income".
         *  @param  categoryName category name to be searched in table of type.
         *  @param  start        starting Date for date filter.
         *  @param  end          ending Date for date filter.
         *  @return TransactionListFragment instance.
         */
        fun newInstance(category : Boolean, date : Boolean, type : String, categoryName : String, start : Date, end : Date) : TransactionListFragment {

            val args : Bundle = Bundle().apply {

                putBoolean     (ARG_CATEGORY     , category)
                putBoolean     (ARG_DATE         , date)
                putString      (ARG_TYPE         , type)
                putString      (ARG_CATEGORY_NAME, categoryName)
                putSerializable(ARG_START        , start)
                putSerializable(ARG_END          , end)
            }

            return TransactionListFragment().apply {

                arguments = args
            }
        }
    }
}