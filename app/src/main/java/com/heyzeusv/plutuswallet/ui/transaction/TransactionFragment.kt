package com.heyzeusv.plutuswallet.ui.transaction

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.model.Transaction
import com.heyzeusv.plutuswallet.ui.base.BaseFragment

/**
 *  Shows all the information in database of one Transaction and allows users to
 *  edit any field and save changes.
 */
class TransactionFragment : BaseFragment() {

    // provides instance of ViewModel
    private val tranVM: TransactionViewModel by viewModels()

    // arguments from Navigation
    private val args: TransactionFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tranVM.apply {
            // pass argument to ViewModel
            newTran = args.newTran

            // retrieves Transaction if exists
            loadTransaction(args.tranId)

            emptyTitle = getString((R.string.transaction_empty_title))

            // array used by PeriodSpinner
            periodArray.value = listOf(
                getString(R.string.period_days), getString(R.string.period_weeks),
                getString(R.string.period_months), getString(R.string.period_years)
            )

            prepareLists(getString(R.string.account_create), getString(R.string.category_create))
        }
    }

    @SuppressLint("RtlHardcoded")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            // Dispose the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                Surface(
                    color = MaterialTheme.colors.background
                ) {
                    TransactionCompose(
                        tranVM = tranVM,
                        onBackPressed = { requireActivity().onBackPressed() }
                    )
                }
            }
        }
    }

    @ExperimentalStdlibApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tranVM.tranLD.observe(viewLifecycleOwner) { transaction: Transaction? ->
            // assigns new Transaction if null, which will cause observer to be called again
            // then assigns values from Transaction to LiveData used by XML
            if (transaction == null) {
                tranVM.tranLD.value = Transaction()
            } else {
                tranVM.setTranData(transaction)
            }
        }
    }
}