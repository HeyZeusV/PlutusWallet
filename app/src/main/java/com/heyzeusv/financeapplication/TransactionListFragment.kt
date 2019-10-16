package com.heyzeusv.financeapplication

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.size
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.heyzeusv.financeapplication.utilities.BaseFragment
import kotlinx.coroutines.launch

import java.text.DateFormat

private const val TAG = "TransactionListFragment"

class TransactionListFragment : BaseFragment() {

    /**
     * Required interface for hosting activities
     * defines work that the fragment needs done by hosting activity
     */
    interface Callbacks {

        fun onTransactionSelected(transactionId : Int, fromFab : Boolean)
    }

    private var callbacks : Callbacks? = null
    private lateinit var transactionRecyclerView : RecyclerView
    private lateinit var transactionAddFab : FloatingActionButton
    private var recyclerViewPosition : Int = 0
    private var startUp : Boolean = true
    // initialize adapter with empty crime list since we have to wait for results from DB
    private var transactionAdapter : TransactionAdapter? = TransactionAdapter(emptyList())

    // provides instance of ViewModel
    private val transactionListViewModel : TransactionListViewModel by lazy {
        ViewModelProviders.of(this).get(TransactionListViewModel::class.java)
    }

    // called when fragment is attached to activity
    override fun onAttach(context : Context) {
        super.onAttach(context)

        // stashing context into callbacks property which is the activity instance hosting fragment
        callbacks = context as Callbacks?
    }

    // inflates the layout and returns the inflated view to hosting activity
    override fun onCreateView(
        inflater : LayoutInflater,
        container : ViewGroup?,
        savedInstanceState : Bundle?
    ) : View? {

        // layout resource id, view's parent, do not immediately add inflated view to parent
        val view = inflater.inflate(R.layout.fragment_transaction_list, container, false)

        transactionRecyclerView =
            view.findViewById(R.id.transaction_recycler_view) as RecyclerView
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

        transactionAddFab =
            view.findViewById(R.id.transaction_add_fab) as FloatingActionButton

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // register an observer on LiveData instance and tie life to another component
        transactionListViewModel.transactionsListLiveData.observe(
            // view's lifecycle owner ensures that updates are only received when view is on screen
            viewLifecycleOwner,
            // executed whenever LiveData gets updated
            Observer { transactions ->
                // if not null
                transactions?.let {
                    Log.i(TAG, "Got crimes ${transactions.size}")
                    updateUI(transactions)
                }
            }
        )

        // register an observer on LiveData instance and tie life to another component
        transactionListViewModel.categorySizeLiveData.observe(
            // view's lifecycle owner ensures that updates are only received when view is on screen
            viewLifecycleOwner,
            // executed whenever LiveData gets updated
            Observer { categorySize ->
                // if not null
                categorySize?.let {
                    Log.d(TAG, "Category Size: $categorySize")
                    if (categorySize == 0) {
                        initializeCategoryTable()
                        Log.d(TAG, "Category Size: $categorySize")
                    }
                }
            }
        )
    }

    override fun onDetach() {
        super.onDetach()

        // afterward you cannot access the activity
        // or count on the activity continuing to exist
        callbacks = null
    }

    override fun onStart() {
        super.onStart()

        transactionAddFab.setOnClickListener {
            val transaction = Transaction()
            callbacks?.onTransactionSelected(transaction.id, true)
            // this will make it so the list will snap to the top after user
            // creates a new Transaction
            recyclerViewPosition = transactionRecyclerView.adapter!!.itemCount
        }
    }

    // this should only be run the very first time a user opens the app
    // fills table with a few predetermined categories
    private fun initializeCategoryTable() {

        val education      = Category("Education")
        val entertainment  = Category("Entertainment")
        val food           = Category("Food")
        val home           = Category("Home")
        val income         = Category("Income")
        val savings        = Category("Savings")
        val transportation = Category("Transportation")
        val utilities      = Category("Utilities")

        val initialCategoryList = arrayOf(education, entertainment, food,
            home, income, savings, transportation, utilities)
        transactionListViewModel.insertCategories(initialCategoryList)
    }

    private fun updateUI(transactions: List<Transaction>) {

        // creates CrimeAdapter to set with RecyclerView
        transactionAdapter = TransactionAdapter(transactions)
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

    // ViewHolder stores a reference to an item's view
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

        // sets the views with transaction data
        fun bind(transaction : Transaction) {

            this.transaction = transaction
            titleTextView.text = this.transaction.title
            dateTextView. text = DateFormat.getDateInstance(DateFormat.FULL).format(this.transaction.date)
            totalTextView.text = String.format("$%.2f", this.transaction.total)
        }

        override fun onClick(v : View?) {

            // test to check if correct Transaction is pressed
            // Toast.makeText(context, "${transaction.title} pressed!", Toast.LENGTH_SHORT).show()

            // the position that the user clicked on
            recyclerViewPosition = this.layoutPosition
            // notifies hosting activity which item was selected
            callbacks?.onTransactionSelected(transaction.id, false)
        }

        // shows AlertDialog asking user if they want to delete Transaction
        override fun onLongClick(v : View?) : Boolean {

            recyclerViewPosition = this.layoutPosition
            // initialize instance of Builder
            val alertDialogBuilder = AlertDialog.Builder(context)
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
            val alertDialog : AlertDialog = alertDialogBuilder.create()
            // display AlertDialog
            alertDialog.show()

            return true
        }
    }

    // creates ViewHolder and binds ViewHolder to data from model layer
    private inner class TransactionAdapter(var transactions: List<Transaction>)
        : RecyclerView.Adapter<TransactionHolder>() {

        // creates view to display, wraps the view in a ViewHolder and returns the result
        override fun onCreateViewHolder(parent : ViewGroup, viewType : Int)
                : TransactionHolder {

            val view = layoutInflater.inflate(R.layout.item_view_transaction, parent, false)
            return TransactionHolder(view)
        }

        override fun getItemCount() = transactions.size

        // populates given holder with transaction from the given position in TransactionList
        override fun onBindViewHolder(holder : TransactionHolder, position : Int) {

            val transaction = transactions[position]
            holder.bind(transaction)
        }
    }

    companion object {

        // can be called by activities to get instance of fragment
        fun newInstance() : TransactionListFragment {

            return TransactionListFragment()
        }
    }
}