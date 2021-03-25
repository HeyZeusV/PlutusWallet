package com.heyzeusv.plutuswallet.ui.account

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.databinding.FragmentAccountBinding
import com.heyzeusv.plutuswallet.ui.base.BaseFragment
import com.heyzeusv.plutuswallet.util.AlertDialogCreator
import com.heyzeusv.plutuswallet.util.EventObserver
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
    ): View {

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
            val alertDialogView = createAlertDialogView()
            AlertDialogCreator.alertDialogInput(
                requireContext(), alertDialogView,
                getString(R.string.alert_dialog_edit_account),
                getString(R.string.alert_dialog_save), getString(R.string.alert_dialog_cancel),
                null, null, null,
                account, accountVM::editAccountName,
                null, null, null
            )
        })

        accountVM.existsAccountEvent.observe(viewLifecycleOwner, EventObserver { name: String ->
            val existBar: Snackbar = Snackbar.make(
                binding.root, getString(R.string.snackbar_exists, name), Snackbar.LENGTH_SHORT
            )
            existBar.anchorView = binding.accountAnchor
            existBar.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorSnackbarText))
            existBar.show()
        })

        accountVM.deleteAccountEvent.observe(viewLifecycleOwner, EventObserver { account: Account ->
            val posFun = DialogInterface.OnClickListener { _, _ ->
                accountVM.deleteAccountPosFun(account)
            }

            AlertDialogCreator.alertDialog(
                requireContext(), getString(R.string.alert_dialog_delete_account),
                getString(R.string.alert_dialog_delete_warning, account.name),
                getString(R.string.alert_dialog_yes), posFun,
                getString(R.string.alert_dialog_no), AlertDialogCreator.doNothing
            )
        })

        // navigates user back to CFLFragment
        binding.accountTopBar.setNavigationOnClickListener { requireActivity().onBackPressed() }

        // handles menu selection
        binding.accountTopBar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.account_new) {
                val alertDialogView = createAlertDialogView()
                val newAccount = Account(0, "")
                AlertDialogCreator.alertDialogInput(
                    requireContext(), alertDialogView,
                    getString(R.string.alert_dialog_create_account),
                    getString(R.string.alert_dialog_save), getString(R.string.alert_dialog_cancel),
                    null, null, null,
                    newAccount, accountVM::insertNewAccount,
                    null, null, null
                )
                true
            } else {
                false
            }
        }
    }
}