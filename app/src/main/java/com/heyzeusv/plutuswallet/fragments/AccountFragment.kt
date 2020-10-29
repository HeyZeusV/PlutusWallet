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
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.database.entities.Account
import com.heyzeusv.plutuswallet.databinding.FragmentAccountBinding
import com.heyzeusv.plutuswallet.utilities.AlertDialogCreator
import com.heyzeusv.plutuswallet.utilities.EventObserver
import com.heyzeusv.plutuswallet.utilities.adapters.AccountAdapter
import com.heyzeusv.plutuswallet.viewmodels.AccountViewModel
import kotlinx.coroutines.launch

/**
 *  Shows all Accounts currently in database and allows users to either edit them or delete them.
 */
class AccountFragment : BaseFragment() {

    // DataBinding
    private lateinit var binding: FragmentAccountBinding

    // RecyclerView Adapter
    private lateinit var accountAdapter: AccountAdapter

    // provides instance of AccountViewModel
    private val accountVM: AccountViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        accountAdapter = AccountAdapter(accountVM)
        accountVM.accountAdapter = accountAdapter

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

    @SuppressLint("StringFormatInvalid")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accountVM.accountLD.observe(viewLifecycleOwner, { list: List<Account> ->
            // only need to retrieve all names/in use lists once
            if (accountVM.accountNames.isEmpty()) {
                launch {
                    accountVM.initNamesUsedLists()
                    accountAdapter.submitList(list)
                }
            } else {
                accountAdapter.submitList(list)
            }
        })

        accountVM.editAccountEvent.observe(viewLifecycleOwner, EventObserver { account: Account ->
            createDialog(
                getString(R.string.alert_dialog_edit_account),
                account, accountVM::editAccountName
            )
        })

        accountVM.existsAccountEvent.observe(viewLifecycleOwner, EventObserver { name: String ->
            val existBar: Snackbar = Snackbar.make(
                binding.root, getString(R.string.snackbar_exists, name), Snackbar.LENGTH_SHORT
            )
            existBar.show()
        })

        accountVM.deleteAccountEvent.observe(viewLifecycleOwner, EventObserver { account: Account ->
            val posFun = DialogInterface.OnClickListener { _, _ ->
                accountVM.deleteAccountPosFun(account)
            }

            AlertDialogCreator.alertDialog(
                requireContext(), getString(R.string.alert_dialog_delete_account),
                getString(R.string.alert_dialog_delete_warning, account.account),
                getString(R.string.alert_dialog_yes), posFun,
                getString(R.string.alert_dialog_no), AlertDialogCreator.doNothing
            )
        })

        // navigates user back to CFLFragment
        binding.accountTopBar.setNavigationOnClickListener { requireActivity().onBackPressed() }

        // handles menu selection
        binding.accountTopBar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.account_new) {
                val newAccount = Account(0, "")
                createDialog(
                    getString(R.string.alert_dialog_create_account),
                    newAccount, accountVM::insertNewAccount
                )
                true
            } else {
                false
            }
        }
    }

    /**
     *  Creates AlertDialog that allows user input for given [action]
     *  that performs [posFun] on positive button click on [account].
     */
    private fun createDialog(action: String, account: Account, posFun: (Account, String) -> Unit) {

        // inflates view that holds EditText
        val viewInflated: View = LayoutInflater.from(context)
            .inflate(R.layout.dialog_input_field, view as ViewGroup, false)
        // the EditText to be used
        val input: EditText = viewInflated.findViewById(R.id.dialog_input)

        val posListener = DialogInterface.OnClickListener { _, _ ->
            posFun(account, input.text.toString())
        }

        AlertDialogCreator.alertDialogInput(
            requireContext(),
            action, viewInflated,
            getString(R.string.alert_dialog_save), posListener,
            getString(R.string.alert_dialog_cancel), AlertDialogCreator.doNothing
        )
    }
}