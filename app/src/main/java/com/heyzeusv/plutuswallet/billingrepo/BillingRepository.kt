package com.heyzeusv.plutuswallet.billingrepo

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.heyzeusv.plutuswallet.billingrepo.localdb.AugmentedSkuDetails
import com.heyzeusv.plutuswallet.billingrepo.localdb.Entitlement
import com.heyzeusv.plutuswallet.billingrepo.localdb.LocalBillingDb
import com.heyzeusv.plutuswallet.billingrepo.localdb.NoAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.util.HashSet

private const val TAG = "BillingRepository"

class BillingRepository private constructor(private val application : Application) :
    PurchasesUpdatedListener, BillingClientStateListener {

    private lateinit var playStoreBillingClient : BillingClient

    // used for when Play Store is unavailable
    private lateinit var localCacheBillingClient : LocalBillingDb

    // in-app products available
    val inappSkuDetailsListLiveData : LiveData<List<AugmentedSkuDetails>> by lazy {

        if (!::localCacheBillingClient.isInitialized) {

            localCacheBillingClient = LocalBillingDb.getInstance(application)
        }
        localCacheBillingClient.skuDetailsDao().getInappSkuDetails()
    }

    /**
     *  Tracks whether this user is entitles to ads being removed. This call returns data from the
     *  app's own local DB; this way if Play Store and secure server are unavailable, users still
     *  have access to features they purchased.
     */
    val adStatusLiveData : LiveData<NoAds> by lazy {

        if (!::localCacheBillingClient.isInitialized) {

            localCacheBillingClient = LocalBillingDb.getInstance(application)
        }
        localCacheBillingClient.entitlementsDao().getNoAds()
    }

    /**
     *  Correlated data sources belong inside a repository module so that the rest of
     *  the app can have appropriate access to the data it needs. Still, it may be effective to
     *  track the opening (and sometimes closing) of data source connections based on lifecycle
     *   events. One convenient way of doing that is by calling this
     *  [startDataSourceConnections] when the [com.heyzeusv.plutuswallet.viewmodels.BillingViewModel]
     *  is instantiated and [endDataSourceConnections] inside [ViewModel.onCleared]
     */
    fun startDataSourceConnections() {

        Log.d(TAG, "startDataSourceConnections")
        instantiateAndConnectToPlayBillingService()
        localCacheBillingClient = LocalBillingDb.getInstance(application)
    }

    fun endDataSourceConnections() {

        playStoreBillingClient.endConnection()
        // normally you don't worry about closing a DB connection unless you have more than
        // one DB open. so no need to call 'localCacheBillingClient.close()'
        Log.d(TAG, "startDataSourceConnections")
    }

    private fun instantiateAndConnectToPlayBillingService() {

        playStoreBillingClient = BillingClient.newBuilder(application.applicationContext)
            .enablePendingPurchases() // required or app will crash
            .setListener(this).build()
        connectToPlayBillingService()
    }

    private fun connectToPlayBillingService() : Boolean {

        Log.d(TAG, "connectToPlayBillingService")
        if (!playStoreBillingClient.isReady) {

            playStoreBillingClient.startConnection(this)
            return true
        }
        return false
    }

    /**
     *  This is the callback for when connection to the Play [BillingClient] has been successfully
     *  established. It might make sense to get [SkuDetails] and [Purchase] at this point.
     */
    override fun onBillingSetupFinished(billingResult : BillingResult) {
        when (billingResult.responseCode) {

            BillingClient.BillingResponseCode.OK -> {

                Log.d(TAG, "onBillingSetupFinished successfully")
                querySkuDetailsAsync(BillingClient.SkuType.INAPP, AppSku.INAPP_SKUS)
                queryPurchasesAsync()
            }
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {

                //Some apps may choose to make decisions based on this knowledge.
                Log.d(TAG, billingResult.debugMessage)
            }
            else -> {

                //do nothing. Someone else will connect it through retry policy.
                //May choose to send to server though
                Log.d(TAG, billingResult.debugMessage)
            }
        }
    }

    /**
     *  This method is called when the app has inadvertently disconnected from the [BillingClient].
     *  An attempt should be made to reconnect using a retry policy. Note the distinction between
     *  [endConnection][BillingClient.endConnection] and disconnected:
     *  - disconnected means it's okay to try reconnecting.
     *  - endConnection means the [playStoreBillingClient] must be re-instantiated and then start
     *   a new connection because a [BillingClient] instance is invalid after endConnection has
     *   been called.
     */
    override fun onBillingServiceDisconnected() {

        Log.d(TAG, "onBillingServiceDisconnected")
        connectToPlayBillingService()
    }

    /**
     *   Grabs all the active purchases of this user and makes them
     *  available to this app instance. Whereas this method plays a central role in the billing
     *  system, it should be called at key junctures, such as when the app starts.
     *
     *  Because purchase data is vital to the rest of the app, this method is called each time
     *  the [com.heyzeusv.plutuswallet.viewmodels.BillingViewModel] successfully establishes connection with the Play [BillingClient]:
     *  the call comes through [onBillingSetupFinished]. Recall also from Figure 4 that this method
     *  gets called from inside [onPurchasesUpdated] in the event that a purchase is "already
     *  owned," which can happen if a user buys the item around the same time
     *  on a different device.
     */
    private fun queryPurchasesAsync() {

        Log.d(TAG, "queryPurchasesAsync called")
        val purchasesResult = HashSet<Purchase>()
        val result : Purchase.PurchasesResult? = playStoreBillingClient.queryPurchases(BillingClient.SkuType.INAPP)
        Log.d(TAG, "queryPurchasesAsync INAPP results: ${result?.purchasesList?.size}")
        result?.purchasesList?.apply {

            purchasesResult.addAll(this)
        }
        processPurchases(purchasesResult)
    }

    private fun processPurchases(purchasesResult : Set<Purchase>) : Job =
        CoroutineScope(Job() + Dispatchers.IO).launch {

            Log.d(TAG, "processPurchases called")
            val validPurchases : MutableList<Purchase> = mutableListOf()
            Log.d(TAG, "processPurchases newBatch content $purchasesResult")
            purchasesResult.forEach { purchase : Purchase ->

                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {

                    if (isSignatureValid(purchase)) {

                        validPurchases.add(purchase)
                    }
                } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {

                    Log.d(TAG, "Received a pending purchase of SKU: ${purchase.sku}")
                    // handle pending purchases, e.g. confirm with users about the pending
                    // purchases, prompt them to complete it, etc.
                }
            }
            /**
             *  As is being done in this sample, for extra reliability you may store the
             *  receipts/purchases to a your own remote/local database for until after you
             *  disburse entitlements. That way if the Google Play Billing library fails at any
             *  given point, you can independently verify whether entitlements were accurately
             *  disbursed. In this sample, the receipts are then removed upon entitlement
             *  disbursement.
             */
            val testing = localCacheBillingClient.purchaseDao().getPurchases()
            Log.d(TAG, "processPurchases purchases in the lcl db ${testing.size}")
            localCacheBillingClient.purchaseDao().insert(*validPurchases.toTypedArray())
            acknowledgeNonConsumablePurchasesAsync(validPurchases)
        }

    /**
     *  If you do not acknowledge a purchase, the Google Play Store will provide a refund to the
     *  users within a few days of the transaction. Therefore you have to implement
     *  [BillingClient.acknowledgePurchase] inside your app.
     */
    private fun acknowledgeNonConsumablePurchasesAsync(nonConsumables : List<Purchase>) {

        nonConsumables.forEach { purchase : Purchase ->

            val params : AcknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken).build()
            playStoreBillingClient.acknowledgePurchase(params) { billingResult : BillingResult ->

                when (billingResult.responseCode) {

                    BillingClient.BillingResponseCode.OK -> {

                        disburseNonConsumableEntitlement(purchase)
                    }
                    else -> Log.d(TAG, "acknowledgeNonConsumablePurchasesAsync response is ${billingResult.debugMessage}")
                }
            }

        }
    }

    /**
     *  This is the final step, where purchases/receipts are converted to premium contents.
     *  Once the entitlement is disbursed the receipt is thrown out.
     */
    private fun disburseNonConsumableEntitlement(purchase : Purchase) : Job =
        CoroutineScope(Job() + Dispatchers.IO).launch {

            when (purchase.sku) {

                AppSku.REMOVE_ADS -> {

                    val noAds = NoAds(true)
                    insert(noAds)
                    localCacheBillingClient.skuDetailsDao()
                        .insertOrUpdate(purchase.sku, noAds.mayPurchase())
                }
            }
            localCacheBillingClient.purchaseDao().delete(purchase)
        }

    /**
     *  Ideally your implementation will comprise a secure server, rendering this check
     *  unnecessary. @see [Security]
     */
    private fun isSignatureValid(purchase : Purchase) : Boolean {

        return Security.verifyPurchase(
            Security.BASE_64_ENCODED_PUBLIC_KEY, purchase.originalJson, purchase.signature)
    }

    /**
     *  Presumably a set of SKUs has been defined on the Google Play Developer Console. This
     *  method is for requesting a (improper) subset of those SKUs. Hence, the method accepts a list
     *  of product IDs and returns the matching list of SkuDetails.
     *
     *  The result is passed to [com.android.billingclient.api.SkuDetailsResponseListener.onSkuDetailsResponse]
     */
    @Suppress("SameParameterValue")
    private fun querySkuDetailsAsync(@BillingClient.SkuType skuType : String, skuList : List<String>) {

        val params : SkuDetailsParams = SkuDetailsParams.newBuilder().setSkusList(skuList)
            .setType(skuType).build()
        Log.d(TAG, "querySkuDetailsAsync for $skuType")
        playStoreBillingClient.querySkuDetailsAsync(params)
        { billingResult : BillingResult, skuDetailsList ->

            when (billingResult.responseCode) {

                BillingClient.BillingResponseCode.OK -> {

                    if (skuDetailsList.orEmpty().isNotEmpty()) {

                        skuDetailsList.forEach {deets : SkuDetails ->

                            CoroutineScope(Job() + Dispatchers.IO).launch {
                                localCacheBillingClient.skuDetailsDao().insertOrUpdate(deets)
                            }
                        }
                    }
                }
                else -> {

                    Log.e(TAG, billingResult.debugMessage)
                }
            }
        }
    }

    /**
     *  This is the function to call when user wishes to make a purchase. This function will
     *  launch the Google Play Billing flow. The response to this call is returned in
     *  [onPurchasesUpdated]
     */
    fun launchBillingFlow(activity : Activity, augmentedSkuDetails : AugmentedSkuDetails) : Unit =
        launchBillingFlow(activity, SkuDetails(augmentedSkuDetails.originalJson))

    private fun launchBillingFlow(activity: Activity, skuDetails: SkuDetails) {

        val purchaseParams : BillingFlowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails).build()
        playStoreBillingClient.launchBillingFlow(activity, purchaseParams)
    }

    /**
     *  This method is called by the [playStoreBillingClient] when new purchases are detected.
     *  The purchase list in this method is not the same as the one in
     *   [BillingClient.queryPurchases]. Whereas queryPurchases returns everything
     *   this user owns, [onPurchasesUpdated] only returns the items that were just now purchased or
     *  billed.
     *
     *  The purchases provided here should be passed along to the secure server for
     *  [verification](https://developer.android.com/google/play/billing/billing_library_overview#Verify)
     *  and safekeeping. And if this purchase is consumable, it should be consumed, and the secure
     *  server should be told of the consumption. All that is accomplished by calling
     *  [queryPurchasesAsync].
     */
    override fun onPurchasesUpdated(billingResult : BillingResult, purchases : MutableList<Purchase>?) {

        when (billingResult.responseCode) {

            BillingClient.BillingResponseCode.OK -> {

                // will handle server verification, consumables, and updating the local cache
                purchases?.apply {

                    processPurchases(this.toSet())
                }
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {

                // item already owned? call queryPurchasesAsync to verify and process all such items
                Log.d(TAG, billingResult.debugMessage)
                queryPurchasesAsync()
            }
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {

                connectToPlayBillingService()
            }
            else -> {

                Log.i(TAG, billingResult.debugMessage)
            }
        }
    }

    @WorkerThread
    private suspend fun insert(entitlement : Entitlement) = withContext(Dispatchers.IO) {

        localCacheBillingClient.entitlementsDao().insert(entitlement)
    }

    companion object {

        @Volatile
        private var INSTANCE : BillingRepository? = null

        fun getInstance(application : Application) : BillingRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: BillingRepository(application)
                        .also { INSTANCE = it }
            }
    }

    private object AppSku {

        const val REMOVE_ADS = "com.heyzeusv.plutuswallet.remove_ads"

        val INAPP_SKUS : List<String> = listOf(REMOVE_ADS)
    }
}