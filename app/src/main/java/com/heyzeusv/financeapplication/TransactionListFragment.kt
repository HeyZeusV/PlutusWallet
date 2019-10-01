package com.heyzeusv.financeapplication

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

import java.text.DateFormat

private const val TAG = "TransactionListFragment"

class TransactionListFragment : Fragment() {

    /**
     * Required interface for hosting activities
     * defines work that the fragment needs done by hosting activity
     */
    interface Callbacks {

        fun onTransactionSelected(transactionId : Int, fabX : Int, fabY : Int, fromFab : Boolean)
    }

    private var callbacks : Callbacks? = null
    private lateinit var transactionRecyclerView : RecyclerView
    private lateinit var transactionAddFab : FloatingActionButton
    private var fabX : Int = 0
    private var fabY : Int = 0
    private var recyclerViewPosition : Int = 0
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
        // RecyclerView NEEDS a LayoutManager to work
        transactionRecyclerView.layoutManager = LinearLayoutManager(context)
        // set adapter for RecyclerView
        transactionRecyclerView.adapter = transactionAdapter

        transactionAddFab =
            view.findViewById(R.id.transaction_add_fab) as FloatingActionButton
        transactionAddFab.setOnClickListener {
            val transaction = Transaction()
            transactionListViewModel.insert(transaction)
            callbacks?.onTransactionSelected(transaction.id, fabX, fabY, true)
        }

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
            })
    }

    override fun onDetach() {
        super.onDetach()

        // afterward you cannot access the activity
        // or count on the activity continuing to exist
        callbacks = null
    }

    private fun updateUI(transactions: List<Transaction>) {

        // creates CrimeAdapter to set with RecyclerView
        transactionAdapter = TransactionAdapter(transactions)
        transactionRecyclerView.adapter = transactionAdapter
        // adds horizontal divider between each item in RecyclerView
        transactionRecyclerView.addItemDecoration(DividerItemDecoration(
            transactionRecyclerView.context, DividerItemDecoration.VERTICAL))

        // gets location of FAB button in order to start animation from correct location
        val fabLocationArray = IntArray(2)
        transactionAddFab.getLocationOnScreen(fabLocationArray)
        fabX = fabLocationArray[0] + transactionAddFab.width / 2
        fabY = fabLocationArray[1] - transactionAddFab.height
        Log.d(TAG, "fabX: $fabX fabY: $fabY")

        // used to return user to previous position in transactionRecyclerView
        transactionRecyclerView.scrollToPosition(recyclerViewPosition)
    }

    // ViewHolder stores a reference to an item's view
    private inner class TransactionHolder(view : View)
        : RecyclerView.ViewHolder(view), View.OnClickListener {

        private lateinit var transaction : Transaction

        // views in the ItemView
        private val titleTextView : TextView = itemView.findViewById(R.id.transaction_title)
        private val dateTextView  : TextView = itemView.findViewById(R.id.transaction_date)
        private val totalTextView : TextView = itemView.findViewById(R.id.transaction_total)

        init {

            itemView.setOnClickListener(this)
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
            callbacks?.onTransactionSelected(transaction.id, fabX, fabY, false)
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