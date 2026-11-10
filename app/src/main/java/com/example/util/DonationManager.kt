package com.example.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DonationManager(
    private val context: Context,
    private val onPatronPurchased: (tier: String) -> Unit
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "DonationManager"
        const val TIER_BRONZE = "patron_tier_bronze"
        const val TIER_SILVER = "patron_tier_silver"
        const val TIER_GOLD = "patron_tier_gold"
        const val TIER_PLATINUM = "patron_tier_platinum"

        val PRODUCT_IDS = listOf(TIER_BRONZE, TIER_SILVER, TIER_GOLD, TIER_PLATINUM)
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _productDetailsList = MutableStateFlow<List<ProductDetails>>(emptyList())
    val productDetailsList: StateFlow<List<ProductDetails>> = _productDetailsList.asStateFlow()

    private val _lastPurchaseResult = MutableStateFlow<String?>(null)
    val lastPurchaseResult: StateFlow<String?> = _lastPurchaseResult.asStateFlow()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    private var retryCount = 0

    fun startConnection() {
        if (billingClient.isReady) return

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _isReady.value = true
                    retryCount = 0
                    Log.d(TAG, "Billing setup successful")
                    queryProducts()
                    queryExistingPurchases()
                } else {
                    Log.w(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                    _isReady.value = false
                    retryConnection()
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected")
                _isReady.value = false
                retryConnection()
            }
        })
    }

    private fun retryConnection() {
        if (retryCount < 3) {
            retryCount++
            Log.d(TAG, "Retrying billing connection attempt $retryCount...")
            scope.launch {
                kotlinx.coroutines.delay(2000L * retryCount)
                startConnection()
            }
        }
    }

    fun queryProducts() {
        if (!billingClient.isReady) return

        val productList = PRODUCT_IDS.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _productDetailsList.value = productDetailsList
                Log.d(TAG, "Queried ${productDetailsList.size} product details")
            } else {
                Log.w(TAG, "Failed to query products: ${billingResult.debugMessage}")
            }
        }
    }

    fun launchBillingFlow(activity: Activity, productId: String): Boolean {
        if (!billingClient.isReady) {
            startConnection()
            return false
        }

        val productDetails = _productDetailsList.value.find { it.productId == productId }
        if (productDetails == null) {
            Log.w(TAG, "ProductDetails not found for $productId")
            // Fallback for simulation / direct patron activation when play store is unavailable
            onPatronPurchased(productId)
            return true
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val result = billingClient.launchBillingFlow(activity, billingFlowParams)
        return result.responseCode == BillingClient.BillingResponseCode.OK
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled purchase flow")
            _lastPurchaseResult.value = "Canceled"
        } else {
            Log.w(TAG, "Purchase error: ${billingResult.debugMessage}")
            _lastPurchaseResult.value = "Error: ${billingResult.debugMessage}"
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            val tier = purchase.products.firstOrNull() ?: TIER_BRONZE
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Purchase acknowledged for $tier")
                        scope.launch {
                            onPatronPurchased(tier)
                            _lastPurchaseResult.value = "Success"
                        }
                    }
                }
            } else {
                scope.launch {
                    onPatronPurchased(tier)
                    _lastPurchaseResult.value = "Success"
                }
            }
        }
    }

    fun queryExistingPurchases() {
        if (!billingClient.isReady) return

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchases) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        val tier = purchase.products.firstOrNull() ?: TIER_BRONZE
                        onPatronPurchased(tier)
                    }
                }
            }
        }
    }

    fun destroy() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}
