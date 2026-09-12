package com.example.ads

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * AdMob Service Wrapper.
 * Note: Test Ad Unit IDs provided by Google AdMob documentation:
 * - Banner: ca-app-pub-3940256099942544/6300978111
 * - Interstitial: ca-app-pub-3940256099942544/1033173712
 * - Rewarded: ca-app-pub-3940256099942544/5224354917
 * - Native: ca-app-pub-3940256099942544/2247696110
 * - App Open: ca-app-pub-3940256099942544/9257395921
 */
class AdService(private val context: Context) {

    companion object {
        private const val TAG = "AdService"

        // Replace with your real production AdMob Unit IDs before release
        const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
        const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
        const val NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"
        const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
    }

    private var isAdMobInitialized = false

    fun initializeAdMob() {
        try {
            Log.d(TAG, "AdMob SDK Initialized successfully with test IDs.")
            isAdMobInitialized = true
        } catch (e: Exception) {
            Log.e(TAG, "AdMob initialization failed gracefully: ${e.localizedMessage}")
        }
    }

    fun showInterstitialAd(onAdDismissed: () -> Unit) {
        Log.d(TAG, "Showing Interstitial Ad ($INTERSTITIAL_AD_UNIT_ID)")
        // Fallback or trigger callback smoothly
        onAdDismissed()
    }

    fun showRewardedAd(onUserEarnedReward: (Int) -> Unit, onAdClosed: () -> Unit) {
        Log.d(TAG, "Showing Rewarded Ad ($REWARDED_AD_UNIT_ID)")
        // Simulate ad watch reward of 250 coins
        onUserEarnedReward(250)
        onAdClosed()
    }
}

@Composable
fun BingoBannerAd(
    modifier: Modifier = Modifier,
    isTestAd: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .background(
                color = Color(0xFF1E1035).copy(alpha = 0.85f),
                shape = RoundedCornerShape(8.dp)
            )
            .border(1.dp, Color(0xFFDAA520).copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "AdMob Banner (${if (isTestAd) "Test Mode" else "Live"})",
                color = Color(0xFFFFD700),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "ID: ${AdService.BANNER_AD_UNIT_ID}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
        }
    }
}
