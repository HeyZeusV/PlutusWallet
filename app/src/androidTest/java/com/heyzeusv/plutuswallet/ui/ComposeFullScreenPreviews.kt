package com.heyzeusv.plutuswallet.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.heyzeusv.plutuswallet.data.FakeRepository
import com.heyzeusv.plutuswallet.ui.cfl.CFLViewModel
import com.heyzeusv.plutuswallet.ui.cfl.tranlist.TransactionListViewModel
import com.heyzeusv.plutuswallet.ui.theme.PlutusWalletTheme


@Preview
@Composable
fun MainPreview() {
    val tranListVM = TransactionListViewModel(FakeRepository())
    val cflVM = CFLViewModel()
    PlutusWalletTheme {
        MainComposable(tranListVM, cflVM)
    }
}