package com.example.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import com.example.core.admob.AdManager
import com.example.domain.repository.AdRepository
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun AdBanner(
    adManager: AdManager,
    adRepository: AdRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPremium by adRepository.isPremiumUser.collectAsState(initial = false)
    var isFailedToLoad by remember { mutableStateOf(false) }

    if (isPremium || isFailedToLoad) {
        return
    }

    val screenWidth = LocalConfiguration.current.screenWidthDp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ad_banner_container"),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ad_banner_view"),
            factory = { ctx ->
                AdView(ctx).apply {
                    adUnitId = adManager.getBannerAdUnitId()
                    
                    val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, screenWidth)
                    setAdSize(adSize)

                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            isFailedToLoad = false
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            isFailedToLoad = true
                        }
                    }

                    loadAd(AdRequest.Builder().build())
                }
            },
            onRelease = { adView ->
                adView.destroy()
            }
        )
    }
}
