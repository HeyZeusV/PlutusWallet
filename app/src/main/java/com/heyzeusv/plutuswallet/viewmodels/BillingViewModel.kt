package com.heyzeusv.plutuswallet.viewmodels

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.heyzeusv.plutuswallet.billingrepo.BillingRepository
import com.heyzeusv.plutuswallet.billingrepo.localdb.AugmentedSkuDetails
import com.heyzeusv.plutuswallet.billingrepo.localdb.NoAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

private const val TAG = "BillingViewModel"

/**
 * Notice just how small and simple this BillingViewModel is!!
 *
 * This beautiful simplicity is the result of keeping all the hard work buried inside the
 * [BillingRepository] and only inside the [BillingRepository]. The rest of your app
 * is now free from [com.android.billingclient.api.BillingClient] tentacles!! And this [BillingViewModel] is the one and only
 * object the rest of your Android team need to know about billing.
 */
class BillingViewModel(application: Application) : AndroidViewModel(application) {

    val noAdsLiveData : LiveData<NoAds>
    val inappSkuDetailsListLiveData : LiveData<List<AugmentedSkuDetails>>

    private val viewModelScope : CoroutineScope = CoroutineScope(Job() + Dispatchers.Main)
    private val repository : BillingRepository = BillingRepository.getInstance(application)

    init {
        repository.startDataSourceConnections()

        noAdsLiveData               = repository.adStatusLiveData
        inappSkuDetailsListLiveData = repository.inappSkuDetailsListLiveData
    }

    override fun onCleared() {
        super.onCleared()

        repository.endDataSourceConnections()
        viewModelScope.coroutineContext.cancel()
    }

    fun makePurchase(activity: Activity, augmentedSkuDetails: AugmentedSkuDetails) {

        repository.launchBillingFlow(activity, augmentedSkuDetails)
    }
}