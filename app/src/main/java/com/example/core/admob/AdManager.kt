package com.example.core.admob

import android.app.Activity
import android.content.Context
import com.example.BuildConfig
import com.example.core.log.Logger
import com.example.domain.repository.AdRepository
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class AdManager(
    private val context: Context,
    private val adRepository: AdRepository,
    private val logger: Logger
) {
    private val isInitialized = AtomicBoolean(false)
    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val TAG = "AdManager"
        const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
        const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
        private const val INTERSTITIAL_THRESHOLD = 3
    }

    fun initialize() {
        if (isInitialized.getAndSet(true)) return

        coroutineScope.launch {
            try {
                logger.i(TAG, "Initializing Google Mobile Ads SDK asynchronously...")
                MobileAds.initialize(context) { status ->
                    logger.i(TAG, "Google Mobile Ads SDK Initialization complete.")
                    preloadInterstitial()
                }
            } catch (e: Exception) {
                logger.e(TAG, "Failed to initialize Google Mobile Ads SDK", e)
            }
        }
    }

    fun getBannerAdUnitId(): String {
        return if (BuildConfig.DEBUG) {
            TEST_BANNER_ID
        } else {
            try {
                val propId = BuildConfig::class.java.getField("PROD_BANNER_AD_UNIT_ID").get(null) as? String
                if (!propId.isNullOrBlank()) propId else TEST_BANNER_ID
            } catch (e: Exception) {
                TEST_BANNER_ID
            }
        }
    }

    fun getInterstitialAdUnitId(): String {
        return if (BuildConfig.DEBUG) {
            TEST_INTERSTITIAL_ID
        } else {
            try {
                val propId = BuildConfig::class.java.getField("PROD_INTERSTITIAL_AD_UNIT_ID").get(null) as? String
                if (!propId.isNullOrBlank()) propId else TEST_INTERSTITIAL_ID
            } catch (e: Exception) {
                TEST_INTERSTITIAL_ID
            }
        }
    }

    fun preloadInterstitial() {
        coroutineScope.launch {
            if (adRepository.isPremiumUser.first()) {
                logger.d(TAG, "Premium user detected. Skipping interstitial preloading.")
                return@launch
            }

            if (interstitialAd != null || isInterstitialLoading) {
                return@launch
            }

            isInterstitialLoading = true
            val adUnitId = getInterstitialAdUnitId()
            logger.d(TAG, "Loading Interstitial Ad with ID: $adUnitId")

            val adRequest = AdRequest.Builder().build()

            launch(Dispatchers.Main) {
                InterstitialAd.load(
                    context,
                    adUnitId,
                    adRequest,
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(ad: InterstitialAd) {
                            interstitialAd = ad
                            isInterstitialLoading = false
                            logger.i(TAG, "Interstitial Ad loaded successfully.")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            isInterstitialLoading = false
                            interstitialAd = null
                            logger.e(TAG, "Interstitial Ad failed to load: ${error.message} (code: ${error.code})")
                        }
                    }
                )
            }
        }
    }

    fun checkAndShowInterstitial(activity: Activity, force: Boolean = false, onAdClosed: () -> Unit) {
        coroutineScope.launch {
            val isPremium = adRepository.isPremiumUser.first()
            if (isPremium) {
                logger.d(TAG, "Ad skipped: User is premium.")
                launch(Dispatchers.Main) { onAdClosed() }
                return@launch
            }

            adRepository.incrementInterstitialTriggerCount()
            val currentCount = adRepository.interstitialTriggerCount.first()
            logger.d(TAG, "Interstitial trigger count: $currentCount / $INTERSTITIAL_THRESHOLD")

            if (force || currentCount >= INTERSTITIAL_THRESHOLD) {
                adRepository.resetInterstitialTriggerCount()
                val ad = interstitialAd
                if (ad != null) {
                    launch(Dispatchers.Main) {
                        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                logger.i(TAG, "Interstitial Ad dismissed.")
                                interstitialAd = null
                                preloadInterstitial()
                                onAdClosed()
                            }

                            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                                logger.e(TAG, "Interstitial Ad failed to show: ${error.message}")
                                interstitialAd = null
                                preloadInterstitial()
                                onAdClosed()
                            }

                            override fun onAdShowedFullScreenContent() {
                                logger.i(TAG, "Interstitial Ad shown successfully.")
                            }
                        }
                        ad.show(activity)
                    }
                } else {
                    logger.d(TAG, "Ad was null at display time. Preloading and dismissing.")
                    preloadInterstitial()
                    launch(Dispatchers.Main) { onAdClosed() }
                }
            } else {
                launch(Dispatchers.Main) { onAdClosed() }
            }
        }
    }
}
