package com.heyzeusv.plutuswallet.fragments

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.database.entities.Account
import com.heyzeusv.plutuswallet.utilities.AlertDialogCreator
import com.heyzeusv.plutuswallet.viewmodels.AccountViewModel
import kotlinx.coroutines.launch

private const val TAG = "PWAccountFragment"

/**
 *  Shows all Accounts currently in database and allows users to either edit them or delete them.
 */
class AccountFragment : BaseFragment() {

    // views
    private lateinit var accountRecyclerView : RecyclerView
    private lateinit var anchorTextView      : TextView

    // lists for holding account names
    private var uniqueAccountList : List<String>        = emptyList()
    private var accountNameList   : MutableList<String> = mutableListOf()

    // used to prevent users from deleting all accounts
    private var totalAccounts : Int = 0

    // initialize adapter with empty list since we have to wait for results from DB
    private var accountAdapter : AccountAdapter? = AccountAdapter(emptyList())

    // provides instance of ViewModel
    private val accountViewModel: AccountViewModel by lazy {
        ViewModelProviders.of(this).get(AccountViewModel::class.java)
    }

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?,
                              savedInstanceState : Bundle?) : View? {

        val view : View = inflater.inflate(R.layout.fragment_account, container, false)

        // initialize views
        accountRecyclerView = view.findViewById(R.id.account_recycler_view)
        anchorTextView      = view.findViewById(R.id.account_anchor_view  )

        val linearLayoutManager = LinearLayoutManager(context)
        // RecyclerView NEEDS a LayoutManager to work
        accountRecyclerView.layoutManager = linearLayoutManager
        // set adapter for RecyclerView
        accountRecyclerView.adapter = accountAdapter
        // adds horizontal divider between each item in RecyclerView
        accountRecyclerView.addItemDecoration(
            DividerItemDecoration(accountRecyclerView.context, DividerItemDecoration.VERTICAL))

        return view
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // register an observer on LiveData instance and tie life to this component
        // execute code whenever LiveData gets updated
        accountViewModel.accountLiveData.observe(this, Observer {

            totalAccounts = it.size

            // clears list before adding names again since names can be added or dropped
            accountNameList.clear()
            it.forEach { account : Account ->

                accountNameList.add(account.account)
            }
            launch {

                // retrieves list of Accounts being used by Transactions
                uniqueAccountList = accountViewModel.getDistinctAccountsAsync().await()
                updateUI(it)
            }
        })
    }

    /**
     *  Ensures RecyclerView is up to date with correct data.
     *
     *  @param accounts the list of Accounts to be displayed.
     */
    private fun updateUI(accounts : List<Account>) {

        // creates Adapter with list of accounts and sets it to RecyclerView
        accountAdapter = AccountAdapter(accounts)
        accountRecyclerView.adapter = accountAdapter
    }

    /**
     *  Creates ViewHolder and binds ViewHolder to data from model layer.
     *
     *  @param accounts the list of Accounts.
     */
    private inner class AccountAdapter(var accounts : List<Account>)
        : RecyclerView.Adapter<AccountHolder>() {

        // creates view to display, wraps the view in a ViewHolder and returns the result
        override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : AccountHolder {

            val view : View = layoutInflater.inflate(R.layout.item_view_account,
                parent, false)
            return AccountHolder(view)
        }

        override fun getItemCount() : Int = accounts.size

        // populates given holder with Account name from the given position in list
        override fun onBindViewHolder(holder : AccountHolder, position : Int) {

            val account : Account = accounts[position]
            holder.bind(account)
        }
    }

    /**
     *  ViewHolder stores a reference to an item's view.
     *
     *  @param view ItemView layout.
     */
    private inner class AccountHolder(view : View) : RecyclerView.ViewHolder(view) {

        // views in the ItemView
        private val editButton       : MaterialButton = itemView.findViewById(R.id.account_edit  )
        private val deleteButton     : MaterialButton = itemView.findViewById(R.id.account_delete)
        private val accountTextView  : TextView       = itemView.findViewById(R.id.account_name  )

        @SuppressLint("StringFormatInvalid")
        fun bind(account : Account) {

            accountTextView.text = account.account

            // enables delete button if Account is not in use and if there is more than 1 Account
            if (!uniqueAccountList.contains(account.account) && totalAccounts > 1) {

                deleteButton.isEnabled = true

                // AlertDialog to ensure user does want to delete Account
                deleteButton.setOnClickListener {

                    val posFun = DialogInterface.OnClickListener { _, _ ->

                        deleteAccount(account)
                    }

                    AlertDialogCreator.alertDialog(context!!,
                        getString(R.string.alert_dialog_delete_account),
                        getString(R.string.alert_dialog_delete_warning, account.account),
                        getString(R.string.alert_dialog_yes), posFun,
                        getString(R.string.alert_dialog_no), AlertDialogCreator.doNothing)
                }
            }

            // AlertDialog with EditText that allows input for new name
            editButton.setOnClickListener {

                // inflates view that holds EditText
                val viewInflated : View = LayoutInflater.from(context)
                    .inflate(R.layout.dialog_input_field, view as ViewGroup, false)
                // the EditText to be used
                val input : EditText = viewInflated.findViewById(R.id.dialog_input)
                val posFun = DialogInterface.OnClickListener { _, _ ->

                    editAccount(input.text.toString(), account)
                }

                AlertDialogCreator.alertDialogInput(context!!,
                    getString(R.string.alert_dialog_edit_account),
                    viewInflated,
                    getString(R.string.alert_dialog_save), posFun,
                    getString(R.string.alert_dialog_cancel), AlertDialogCreator.doNothing)
            }
        }

        /**
         *  @param account the Account to be deleted.
         */
        private fun deleteAccount(account : Account) {

            launch {

                accountViewModel.deleteAccount(account)
            }
        }

        /**
         *  Checks if name inputted exists already before editing.
         *
         *  @param updatedName new name of Account.
         *  @param account     Account to be changed.
         */
        private fun editAccount(updatedName : String, account : Account) {

            // if exists, Snackbar appears telling user so, else, updates Account
            if (accountNameList.contains(updatedName)) {

                val existBar : Snackbar = Snackbar.make(view!!,
                    getString(R.string.snackbar_exists, updatedName), Snackbar.LENGTH_SHORT)
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