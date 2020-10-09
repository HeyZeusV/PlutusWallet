package com.heyzeusv.plutuswallet.fragments

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.database.entities.Account
import com.heyzeusv.plutuswallet.databinding.FragmentAccountBinding
import com.heyzeusv.plutuswallet.databinding.ItemViewAccountBinding
import com.heyzeusv.plutuswallet.utilities.AccountDiffUtil
import com.heyzeusv.plutuswallet.utilities.AlertDialogCreator
import com.heyzeusv.plutuswallet.viewmodels.AccountViewModel
import kotlinx.coroutines.launch

/**
 *  Shows all Accounts currently in database and allows users to either edit them or delete them.
 */
class AccountFragment : BaseFragment() {

    // DataBinding
    private lateinit var binding: FragmentAccountBinding

    // RecyclerView Adapter
    private val accountAdapter = AccountAdapter()

    // provides instance of ViewModel
    private val accountVM: AccountViewModel by lazy {
        ViewModelProvider(this).get(AccountViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // setting up DataBinding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.accountRv.adapter = accountAdapter
        binding.accountRv.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )
        binding.accountRv.layoutManager = LinearLayoutManager(context)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accountVM.accountLD.observe(viewLifecycleOwner, {
            // coroutine ensures that lists used by ViewHolders are ready before updating adapter
            launch {
                accountVM.accountNames = accountVM.getAccountNamesAsync().await()
                accountVM.accountsUsed = accountVM.getDistinctAccountsAsync().await()
                accountAdapter.submitList(it)
            }
        })

        // navigates user back to CFLFragment
        binding.accountTopBar.setNavigationOnClickListener { requireActivity().onBackPressed() }

        // handles menu selection
        binding.accountTopBar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.account_new) {
                createDialog()
                true
            } else {
                false
            }
        }
    }

    /**
     *  Creates AlertDialog when user clicks New Account button in TopBar.
     */
    private fun createDialog() {

        // inflates view that holds EditText
        val viewInflated: View = LayoutInflater.from(context)
            .inflate(R.layout.dialog_input_field, view as ViewGroup, false)
        // the EditText to be used
        val input: EditText = viewInflated.findViewById(R.id.dialog_input)

        val posListener = DialogInterface.OnClickListener { _, _ ->
            accountVM.insertAccount(input.text.toString())
        }

        AlertDialogCreator.alertDialogInput(
            requireContext(),
            getString(R.string.account_create), viewInflated,
            getString(R.string.alert_dialog_save), posListener,
            getString(R.string.alert_dialog_cancel), AlertDialogCreator.doNothing
        )
    }

    /**
     *  Creates ViewHolder and binds ViewHolder to data from model layer.
     */
    private inner class AccountAdapter : ListAdapter<Account, AccountHolder>(AccountDiffUtil()) {

        // creates view to display, wraps the view in a ViewHolder and returns the result
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountHolder {

            val accountBinding: ItemViewAccountBinding = ItemViewAccountBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return AccountHolder(accountBinding)
        }

        // populates given holder with Account from the given position in list
        override fun onBindViewHolder(holder: AccountHolder, position: Int) {

            val account: Account = getItem(position)
            holder.bind(account)
        }
    }

    /**
     *  ViewHolder stores a reference to an item's view using [binding] as its layout.
     */
    private inner class AccountHolder(private var binding: ItemViewAccountBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("StringFormatInvalid")
        fun bind(account: Account) {

            // enables delete button if Account is not in use and if there is more than 1 Account
            if (!accountVM.accountsUsed.contains(account.account)
                && accountVM.accountLD.value!!.size > 1
            ) {
                binding.ivaDelete.isEnabled = true
                // AlertDialog to ensure user does want to delete Account
                binding.ivaDelete.setOnClickListener {
                    val posFun = DialogInterface.OnClickListener { _, _ ->
                        accountVM.deleteAccount(account)
                    }

                    AlertDialogCreator.alertDialog(
                        context!!,
                        getString(R.string.alert_dialog_delete_account),
                        getString(R.string.alert_dialog_delete_warning, account.account),
                        getString(R.string.alert_dialog_yes), posFun,
                        getString(R.string.alert_dialog_no), AlertDialogCreator.doNothing
                    )
                }
            }

            // AlertDialog with EditText that allows input for new name
            binding.ivaEdit.setOnClickListener {
                // inflates view that holds EditText
                val viewInflated: View = LayoutInflater.from(context)
                    .inflate(R.layout.dialog_input_field, view as ViewGroup, false)
                // the EditText to be used
                val input: EditText = viewInflated.findViewById(R.id.dialog_input)
                val posFun = DialogInterface.OnClickListener { _, _ ->
                    editAccount(input.text.toString(), account)
                }

                AlertDialogCreator.alertDialogInput(
                    context!!,
                    getString(R.string.alert_dialog_edit_account), viewInflated,
                    getString(R.string.alert_dialog_save), posFun,
                    getString(R.string.alert_dialog_cancel), AlertDialogCreator.doNothing
                )
            }

            binding.account = account
            binding.executePendingBindings()
        }

        /**
         *  Checks if [updatedName] exists already in Account table before updating [account].
         */
        private fun editAccount(updatedName: String, account: Account) {

            // if exists, Snackbar appears telling user so, else, updates Account
            if (accountVM.accountNames.contains(updatedName)) {
                val existBar: Snackbar = Snackbar.make(
                    view!!,
                    getString(R.string.snackbar_exists, updatedName), Snackbar.LENGTH_SHORT
                )
                existBar.anchorView = this@AccountFragment.binding.accountAnchor
                existBar.show()
            } else {
                account.account = updatedName
                binding.account = account
                accountVM.updateAccount(account)
            }
        }
    }
}