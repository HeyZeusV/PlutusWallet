package com.heyzeusv.plutuswallet.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.ui.base.BaseFragment

/**
 *  Shows all the information in database of one Transaction and allows users to
 *  edit any field and save changes.
 */
class TransactionFragment : BaseFragment() {

    // provides instance of ViewModel
    private val tranVM: TransactionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tranVM.apply {
            emptyTitle = getString((R.string.transaction_empty_title))
            // array used by PeriodSpinner
            updatePeriodList(
                mutableListOf(
                    getString(R.string.period_days), getString(R.string.period_weeks),
                    getString(R.string.period_months), getString(R.string.period_years)
                )
            )
            prepareLists(getString(R.string.account_create), getString(R.string.category_create))
            retrieveTransaction()
        }
    }

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
}