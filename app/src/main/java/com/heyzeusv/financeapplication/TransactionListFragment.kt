package com.heyzeusv.financeapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "TransactionListFragment"

class TransactionListFragment : Fragment() {

    private lateinit var transactionRecyclerView : RecyclerView
    private var adapter : TransactionAdapter? = null

    // provides instance of ViewModel
    private val transactionListViewModel : TransactionListViewModel by lazy {
        ViewModelProviders.of(this).get(TransactionListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // logs number of transactions
        Log.d(TAG, "Total crimes: ${transactionListViewModel.transactions.size}")
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

        updateUI()

        return view
    }

    private fun updateUI() {

        // creates CrimeAdapter to set with RecyclerView
        val transactions = transactionListViewModel.transactions
        adapter = TransactionAdapter(transactions)
        transactionRecyclerView.adapter = adapter
    }

    // ViewHolder stores a reference to an item's view
    private inner class TransactionHolder(view : View) : RecyclerView.ViewHolder(view) {

        val titleTextView : TextView = itemView.findViewById(R.id.transaction_title)
        val dateTextView  : TextView = itemView.findViewById(R.id.transaction_date)
        val totalTextView : TextView = itemView.findViewById(R.id.transaction_total)
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
            holder.apply {
                titleTextView.text = transaction.title
                dateTextView. text = transaction.date.toString()
                totalTextView.text = String.format("$%.2f", transaction.total)
            }
        }
    }

    companion object {

        // can be called by activities to get instance of fragment
        fun newInstance() : TransactionListFragment {

            return TransactionListFragment()
        }
    }
}