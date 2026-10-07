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
import androidx.compose.foundation.layout.size
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

@Composable
fun StatsScreen(
    stats: UserStats,
    activeTheme: BingoTheme,
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
            // Header
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
                    text = "CAREER STATS",
                    color = Color(0xFFFFD700),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Level Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A004E)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🏅 LEVEL ${stats.level}", color = Color(0xFFFFD700), fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Text(text = "${stats.xp} / ${stats.xpForNextLevel} XP to Next Level", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Grid
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatBox(title = "Total Games", value = "${stats.totalGamesPlayed}", accentColor = Color(0xFF00E5FF), modifier = Modifier.weight(1f))
                    StatBox(title = "Total Wins", value = "${stats.totalWins}", accentColor = Color(0xFF76FF03), modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatBox(title = "Total Losses", value = "${stats.totalLosses}", accentColor = Color(0xFFFF5252), modifier = Modifier.weight(1f))
                    StatBox(title = "Best Win Streak", value = "${stats.bestStreak}", accentColor = Color(0xFFFFD700), modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatBox(title = "Numbers Daubed", value = "${stats.totalDaubs}", accentColor = Color(0xFFFF4081), modifier = Modifier.weight(1f))
                    StatBox(title = "Total Stars", value = "${stats.coins} ⭐", accentColor = Color(0xFFFFD700), modifier = Modifier.weight(1f))
                }
            }
        }

        BingoBannerAd(modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun StatBox(title: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B003A)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                maxLines = 2,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = accentColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
