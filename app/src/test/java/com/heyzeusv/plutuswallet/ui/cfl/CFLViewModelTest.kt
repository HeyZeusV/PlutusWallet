package com.heyzeusv.plutuswallet.ui.cfl

import com.heyzeusv.plutuswallet.InstantExecutorExtension
import com.heyzeusv.plutuswallet.TestCoroutineExtension
import com.heyzeusv.plutuswallet.data.model.TransactionInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Date

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, TestCoroutineExtension::class)
internal class CFLViewModelTest {

    // what is being tested
    private lateinit var cflVM: CFLViewModel

    @BeforeEach
    fun setUpViewModel() {

        cflVM = CFLViewModel()
    }

    @Test
    @DisplayName("Should update tInfoLiveDate with given TransactionInfo")
    fun updateTInfo() {

        val tInfo = TransactionInfo(
            account = true, category = true, date = true,
            "Test", listOf("Test"), listOf("Test"), Date(), Date()
        )

        cflVM.updateTInfo(tInfo)

        assertEquals(tInfo, cflVM.tInfoLiveData.value)
    }
}