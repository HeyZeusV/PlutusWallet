package com.heyzeusv.plutuswallet.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.database.entities.Account
import com.heyzeusv.plutuswallet.viewmodels.AccountViewModel
import kotlinx.coroutines.launch

class AccountFragment : BaseFragment() {

    // views
    private lateinit var accountRecyclerView : RecyclerView
    private lateinit var anchorTextView      : TextView

    private var uniqueAccountList : List<String> = emptyList()
    private var accountNameList   : MutableList<String> = mutableListOf()

    private var totalAccounts : Int = 0

    // initialize adapter with empty list since we have to wait for results from DB
    private var accountAdapter : AccountAdapter? = AccountAdapter(emptyList())

    private val accountViewModel: AccountViewModel by lazy {
        ViewModelProviders.of(this).get(AccountViewModel::class.java)
    }

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View? {

        val view : View = inflater.inflate(R.layout.fragment_account, container, false)

        accountRecyclerView = view.findViewById(R.id.account_recycler_view)
        anchorTextView      = view.findViewById(R.id.account_anchor_view  )

        val linearLayoutManager = LinearLayoutManager(context)
        accountRecyclerView.layoutManager = linearLayoutManager
        accountRecyclerView.adapter       = accountAdapter
        accountRecyclerView.addItemDecoration(
            DividerItemDecoration(accountRecyclerView.context, DividerItemDecoration.VERTICAL)
        )

        return view
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accountViewModel.accountLiveData.observe(this, Observer {

            totalAccounts = it.size

            accountNameList.clear()
            it.forEach { account : Account ->

                accountNameList.add(account.account)
            }
            launch {

                uniqueAccountList = accountViewModel.getDistinctAccountsAsync().await()
                updateUI(it)
            }
        })
    }

    private fun updateUI(accounts : List<Account>) {

        accountAdapter = AccountAdapter(accounts)
        accountRecyclerView.adapter = accountAdapter
    }

    private inner class AccountAdapter(var accounts : List<Account>)
        : RecyclerView.Adapter<AccountHolder>() {

        override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : AccountHolder {

            val view : View = layoutInflater.inflate(R.layout.item_view_account, parent, false)
            return AccountHolder(view)
        }

        override fun getItemCount() : Int = accounts.size

        override fun onBindViewHolder(holder : AccountHolder, position : Int) {

            val account : Account = accounts[position]
            holder.bind(account)
        }
    }

    private inner class AccountHolder(view : View) : RecyclerView.ViewHolder(view) {

        private val editButton       : MaterialButton = itemView.findViewById(R.id.account_edit  )
        private val deleteButton     : MaterialButton = itemView.findViewById(R.id.account_delete)
        private val accountTextView  : TextView       = itemView.findViewById(R.id.account_name  )

        fun bind(account : Account) {

            accountTextView.text = account.account

            if (!uniqueAccountList.contains(account.account) && totalAccounts > 1) {

                deleteButton.isEnabled = true

                deleteButton.setOnClickListener {

                    launch {

                        accountViewModel.deleteAccount(account)
                    }
                }
            }

            editButton.setOnClickListener {

                // initialize instance of Builder
                val builder : MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(context)
                    // set title of AlertDialog
                    .setTitle(getString(R.string.category_create))
                // inflates view that holds EditText
                val viewInflated : View = LayoutInflater.from(context)
                    .inflate(R.layout.dialog_new_category, view as ViewGroup, false)
                // the EditText to be used
                val input : EditText = viewInflated.findViewById(R.id.category_Input)
                // sets the view
                builder.setView(viewInflated)
                    // set positive button and its click listener
                    .setPositiveButton(getString(R.string.alert_dialog_save)) { _ : DialogInterface, _ : Int ->

                        editAccount(input.text.toString(), account)
                    }
                    // set negative button and its click listener
                    .setNegativeButton(getString(R.string.alert_dialog_cancel)) { _ : DialogInterface, _ : Int -> }
                // make the AlertDialog using the builder
                val accountAlertDialog : AlertDialog = builder.create()
                // display AlertDialog
                accountAlertDialog.show()
            }
        }

        private fun editAccount(updatedName : String, account : Account) {

            if (accountNameList.contains(updatedName)) {

                val existBar : Snackbar = Snackbar.make(view!!, "$updatedName already exists!", Snackbar.LENGTH_SHORT)
                existBar.anchorView = anchorTextView
                existBar.show()
            } else {

                account.account = updatedName
                launch {

                    accountViewModel.updateAccount(account)
                }
            }
        }
    }

    companion object {

        /**
         *  Initializes instance of AccountFragment
         */
        fun newInstance() : AccountFragment {

            return AccountFragment()
        }
    }
}