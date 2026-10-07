package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.BingoBannerAd
import com.example.model.BingoTheme
import com.example.model.UserStats
import com.example.sound.SoundManager
import com.example.ui.components.SpinWheelComposable

@Composable
fun SpinWheelScreen(
    stats: UserStats,
    activeTheme: BingoTheme,
    soundManager: SoundManager?,
    onRewardEarned: (Int) -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(activeTheme.backgroundGradients))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "DAILY STARS 🎁",
                    color = Color(0xFF0D47A1),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.border(1.5.dp, Color(0xFFFFB300), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⭐ ${stats.coins}",
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wheel Component
            SpinWheelComposable(
                canSpin = stats.canSpinWheelToday(),
                remainingTimeMs = stats.getRemainingTimeUntilNextSpinMs(),
                soundManager = soundManager,
                onRewardEarned = { segment ->
                    onRewardEarned(segment.starReward)
                }
            )
        }

        BingoBannerAd(modifier = Modifier.align(Alignment.BottomCenter))
    }
}
