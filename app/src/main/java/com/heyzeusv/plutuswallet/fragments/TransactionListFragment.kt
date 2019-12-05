package com.heyzeusv.plutuswallet.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.size
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.database.entities.ExpenseCategory
import com.heyzeusv.plutuswallet.database.entities.IncomeCategory
import com.heyzeusv.plutuswallet.database.entities.ItemViewTransaction
import com.heyzeusv.plutuswallet.database.entities.Transaction
import com.heyzeusv.plutuswallet.utilities.TransactionInfo
import com.heyzeusv.plutuswallet.utilities.Utils
import com.heyzeusv.plutuswallet.viewmodels.FGLViewModel
import com.heyzeusv.plutuswallet.viewmodels.TransactionListViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

private const val TAG = "TransactionListFragment"

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

    // views
    private lateinit var transactionAddFab       : FloatingActionButton
    private lateinit var transactionRecyclerView : RecyclerView
    private lateinit var emptyListTextView       : TextView

    // used to tell if app is first starting up
    private var startUp : Boolean = true

    // holds position of RecyclerView so that it doesn't reset when user returns
    private var recyclerViewPosition : Int = 0

    private var maxId           : Int     = 0

    // initialize adapter with empty crime list since we have to wait for results from DB
    private var transactionAdapter : TransactionAdapter? = TransactionAdapter(emptyList())

    // provides instance of ViewModel
    private val transactionListViewModel : TransactionListViewModel by lazy {
        ViewModelProviders.of(this).get(TransactionListViewModel::class.java)
    }

    private lateinit var fglViewModel : FGLViewModel

    override fun onAttach(context : Context) {
        super.onAttach(context)

        // stashing context into callbacks property which is the activity instance hosting fragment
        callbacks = context as Callbacks?
    }

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View? {

        val view : View = inflater.inflate(R.layout.fragment_transaction_list, container, false)

        // initialize views
        transactionAddFab       = view.findViewById(R.id.transaction_add_fab      ) as FloatingActionButton
        transactionRecyclerView = view.findViewById(R.id.transaction_recycler_view) as RecyclerView
        emptyListTextView       = view.findViewById(R.id.emptyListTextView        ) as TextView

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
        transactionRecyclerView.addItemDecoration(DividerItemDecoration(transactionRecyclerView.context, DividerItemDecoration.VERTICAL))

        // this ensures that this is same FGLViewModel as Filter/GraphFragment use
        fglViewModel = activity!!.let {

            ViewModelProviders.of(it).get(FGLViewModel::class.java)
        }

        return view
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        // values sent to ViewModels
        var category     : Boolean?
        var date         : Boolean?
        var type         : String?
        var categoryName : String?
        var start        : Date?
        var end          : Date?

        // register an observer on LiveData instance and tie life to another component
        fglViewModel.tInfoLiveData.observe(
            // view's lifecycle owner ensures that updates are only received when view is on screen
            viewLifecycleOwner,
            // executed whenever LiveData gets updated
            Observer { newInfo : TransactionInfo ->
                // never null
                newInfo.let {

                    // updating values for ViewModels
                    category     = newInfo.category
                    date         = newInfo.date
                    type         = newInfo.type
                    start        = newInfo.start
                    end          = newInfo.end
                    categoryName = if (newInfo.categoryName == getString(R.string.category_all)) {

                        "All"
                    } else {

                        newInfo.categoryName
                    }
                }

                // tells ViewModel which query to run on Transactions
                val transactionListLiveData : LiveData<List<ItemViewTransaction>> =
                    transactionListViewModel.filteredTransactionList(category, date, type, categoryName, start, end)

                // register an observer on LiveData instance and tie life to another component
                transactionListLiveData.observe(
                    // view's lifecycle owner ensures that updates are only received when view is on screen
                    viewLifecycleOwner,
                    // executed whenever LiveData gets updated
                    Observer { transactions : List<ItemViewTransaction> ->
                        // never null
                        transactions.let {
                            emptyListTextView.isVisible = transactions.isEmpty()
                            updateUI(transactions)
                        }
                    }
                )

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
    }

    override fun onResume() {
        super.onResume()

        futureTransactions()

        // tell RecyclerView that symbol has been changed
        transactionAdapter!!.notifyDataSetChanged()

        launch {

            // retrieves maxId or 0 if null
            maxId = transactionListViewModel.getMaxIdAsync().await() ?: 0
        }
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
     *  @param  date      the date of Transaction.
     *  @param  period    how often Transaction repeats.
     *  @param  frequency how often Transaction repeats.
     *  @return the FutureDate set at the beginning of day.
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

        return Utils.startOfDay(calendar.time)
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
     *  Appends " x####" to the end of Transaction title that has been repeated.
     *
     *  @param title the title of Transaction.
     */
    private fun incrementString(title : String) : String {

        val prefix                        = "x"
        // pattern: x######
        val regex                         = Regex("(x)\\d+")
        // will search for regex in title
        val match          : MatchResult? = regex.find(title)
        // string that match found, if any
        val matchingString : String?      = match?.value
        var newTitle       : String       = title

        if (matchingString != null) {

            // removes "x###" as this will be updated
            newTitle              = newTitle      .replace(regex         , "")
            // returns only the Int and increments it by one
            var noPrefixInt : Int = matchingString.replace("x", "").toInt()
            noPrefixInt          += 1
            // appends the prefix and updated int onto the end of title
            newTitle             += prefix + noPrefixInt
        } else {

            // first time being repeated
            newTitle += " x2"
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

                val education      = ExpenseCategory("Education")
                val entertainment  = ExpenseCategory("Entertainment")
                val food           = ExpenseCategory("Food")
                val home           = ExpenseCategory("Home")
                val transportation = ExpenseCategory("Transportation")
                val utilities      = ExpenseCategory("Utilities")
                val initialExpenseCategories : List<ExpenseCategory> = listOf(
                    education, entertainment, food, home, transportation, utilities)
                transactionListViewModel.insertExpenseCategories(initialExpenseCategories)
            }

            if (incomeSize == 0 || incomeSize == null) {

                val cryptocurrency = IncomeCategory("Cryptocurrency")
                val investments    = IncomeCategory("Investments")
                val salary         = IncomeCategory("Salary")
                val savings        = IncomeCategory("Savings")
                val stocks         = IncomeCategory("Stocks")
                val wages          = IncomeCategory("Wages")
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
    private fun updateUI(transactions : List<ItemViewTransaction>) {

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
    private inner class TransactionAdapter(var transactions : List<ItemViewTransaction>)
        : RecyclerView.Adapter<TransactionHolder>() {

        // creates view to display, wraps the view in a ViewHolder and returns the result
        override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : TransactionHolder {

            val view : View = layoutInflater.inflate(R.layout.item_view_transaction, parent, false)
            return TransactionHolder(view)
        }

        override fun getItemCount() : Int = transactions.size

        // populates given holder with Transaction from the given position in TransactionList
        override fun onBindViewHolder(holder : TransactionHolder, position : Int) {

            val transaction : ItemViewTransaction = transactions[position]
            holder.bind(transaction)
        }
    }

    /**
     *  ViewHolder stores a reference to an item's view.
     */
    private inner class TransactionHolder(view : View)
        : RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener {

        private lateinit var transaction : ItemViewTransaction

        // views in the ItemView
        private val titleTextView    : TextView = itemView.findViewById(R.id.transaction_title   )
        private val dateTextView     : TextView = itemView.findViewById(R.id.transaction_date    )
        private val totalTextView    : TextView = itemView.findViewById(R.id.transaction_total   )
        private val categoryTextView : TextView = itemView.findViewById(R.id.transaction_category)

        init {

            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        // sets the views with Transaction data
        fun bind(transaction : ItemViewTransaction) {

            this.transaction      = transaction
            titleTextView   .text = this.transaction.title
            categoryTextView.text = Utils.translateCategory(context!!, this.transaction.category)
            dateTextView    .text = DateFormat .getDateInstance(dateFormat).format(this.transaction.date)

            // formats the Total correctly
            if (decimalPlaces) {

                if (symbolSide) {

                    totalTextView.text = getString(R.string.total_number_symbol, currencySymbol, decimalFormatter.format(this.transaction.total))
                } else {

                    totalTextView.text = getString(R.string.total_number_symbol, decimalFormatter.format(this.transaction.total), currencySymbol)
                }
            } else {

                if (symbolSide) {

                    totalTextView.text = getString(R.string.total_number_symbol, currencySymbol, integerFormatter.format(this.transaction.total))
                } else {

                    totalTextView.text = getString(R.string.total_number_symbol, integerFormatter.format(this.transaction.total), currencySymbol)
                }
            }
            context?.let {
                // changes the color depending on Type
                if (this.transaction.type == "Expense") {

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
        @SuppressLint("StringFormatInvalid")
        override fun onLongClick(v : View?) : Boolean {

            recyclerViewPosition = this.layoutPosition
            // initialize instance of builder
            val alertDialogBuilder : MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(context)
                // set title of AlertDialog
                .setTitle(getString(R.string.alert_dialog_delete_transaction))
                // set message of AlertDialog
                .setMessage(getString(R.string.alert_dialog_delete_transaction_warning, transaction.title))
                // set positive button and its click listener
                .setPositiveButton(getString(R.string.alert_dialog_yes)) { _ : DialogInterface, _ : Int ->

                    launch {

                        transactionListViewModel.deleteTransaction(transactionListViewModel.getTransactionAsync(transaction.id).await())
                    }
                }
                // set negative button and its click listener
                .setNegativeButton(getString(R.string.alert_dialog_no)) { _ : DialogInterface, _ : Int ->  }
            // make the AlertDialog using the builder
            val alertDialog : AlertDialog = alertDialogBuilder.create()
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
    }
}