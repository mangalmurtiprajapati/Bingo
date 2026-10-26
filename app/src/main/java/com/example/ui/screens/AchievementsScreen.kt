package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Achievement
import com.example.model.BingoTheme
import com.example.model.UserStats
import com.example.ui.components.ArcadeBottomNavigationBar
import com.example.ui.components.ArcadeTopHeader
import com.example.ui.theme.ArcadeBackground
import com.example.ui.theme.ArcadeInverseOnSurface
import com.example.ui.theme.ArcadeInverseSurface
import com.example.ui.theme.ArcadeOnPrimary
import com.example.ui.theme.ArcadeOnSecondary
import com.example.ui.theme.ArcadeOnSurface
import com.example.ui.theme.ArcadeOnSurfaceVariant
import com.example.ui.theme.ArcadeOutlineVariant
import com.example.ui.theme.ArcadePrimary
import com.example.ui.theme.ArcadePrimaryContainer
import com.example.ui.theme.ArcadePrimaryFixed
import com.example.ui.theme.ArcadeSecondary
import com.example.ui.theme.ArcadeSecondaryContainer
import com.example.ui.theme.ArcadeSecondaryFixed
import com.example.ui.theme.ArcadeSurfaceContainer
import com.example.ui.theme.ArcadeSurfaceContainerHigh
import com.example.ui.theme.ArcadeSurfaceContainerLowest
import com.example.ui.theme.ArcadeTertiary
import com.example.ui.theme.ArcadeTertiaryContainer
import com.example.ui.theme.ArcadeTertiaryFixed
import com.example.ui.theme.EmeraldWin
import com.example.ui.theme.TournamentGold

data class ArcadeBadge(
    val id: String,
    val title: String,
    val description: String,
    val progress: Int,
    val maxProgress: Int,
    val rewardCoins: Int,
    val isClaimed: Boolean,
    val isReadyToClaim: Boolean,
    val emoji: String
)

@Composable
fun AchievementsScreen(
    achievements: List<Achievement>,
    stats: UserStats,
    activeTheme: BingoTheme,
    onClaimReward: (id: String, coins: Int, xp: Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var selectedFilter by remember { mutableStateOf("ALL") }

    val badges = listOf(
        ArcadeBadge("first_blood", "First Blood", "Win your first 1v1 duel against any opponent.", 1, 1, 300, false, true, "⚔️"),
        ArcadeBadge("speed_demon", "Speed Demon", "Declare Bingo in under 60 seconds.", 45, 60, 500, false, true, "⚡"),
        ArcadeBadge("pentagram", "Five-Line Frenzy", "Complete all 5 B-I-N-G-O lines in 1 match.", 4, 5, 800, false, false, "🌟"),
        ArcadeBadge("streak_master", "Hot Streak", "Maintain a 5-day daily spin streak.", 5, 5, 1000, true, false, "🔥"),
        ArcadeBadge("coin_vault", "Coin Vault", "Amass 5,000 career coins.", stats.coins.coerceAtMost(5000), 5000, 1500, false, stats.coins >= 5000, "🪙"),
        ArcadeBadge("dauber_elite", "Grand Dauber", "Play 50 classic 1v1 matches.", stats.totalGamesPlayed.coerceAtMost(50), 50, 2000, false, stats.totalGamesPlayed >= 50, "👑")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ArcadeBackground,
        topBar = {
            ArcadeTopHeader(
                title = "Badges & Trophies",
                subtitle = "Trophy Hall & Masteries",
                coins = stats.coins,
                gems = 120,
                onBack = onBack
            )
        },
        bottomBar = {
            ArcadeBottomNavigationBar(
                currentRoute = "achievements",
                onNavigate = { route ->
                    if (route == "home") onBack()
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Rank Showcase & Total Progress Hero
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadePrimary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(ArcadePrimary, ArcadePrimaryContainer)
                                )
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ArcadeSecondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MilitaryTech,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Grand Dauber",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(ArcadeSecondaryContainer)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("TIER 4", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                                        }
                                    }
                                    Text(
                                        text = "Trophy Hall & Masteries",
                                        fontSize = 11.sp,
                                        color = ArcadePrimaryFixed
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.25f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = TournamentGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "1,420",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }

                        // Progress track
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.25f))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "ACHIEVEMENT SCORE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "28 / 45 Unlocked (62%)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArcadeSecondaryFixed
                                )
                            }

                            LinearProgressIndicator(
                                progress = { 0.62f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = TournamentGold,
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Next: Neon Champion Crest",
                                    fontSize = 11.sp,
                                    color = ArcadePrimaryFixed
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ArcadeTertiaryContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("2 LEFT!", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // 2. Weekly Bounty Chase Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerHigh)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ArcadeSecondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Weekly Bounty Chase",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ArcadeOnSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(ArcadeSecondaryFixed)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("2D LEFT", fontSize = 8.sp, fontWeight = FontWeight.Black, color = ArcadeSecondary)
                                    }
                                }
                                Text(
                                    text = "Win 3 ranked duels with all 5 lines cleared.",
                                    fontSize = 11.sp,
                                    color = ArcadeOnSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "+1,500 🪙",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = ArcadeSecondary
                        )
                    }
                }

                // 3. Badges Grid / List
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Milestone Badges",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ArcadeOnSurface
                    )

                    badges.forEach { badge ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(1.dp, RoundedCornerShape(16.dp))
                                .testTag("badge_card_${badge.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerLowest)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (badge.isClaimed) ArcadeSurfaceContainerHigh else ArcadeSecondaryFixed
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = badge.emoji, fontSize = 22.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = badge.title,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ArcadeOnSurface
                                        )
                                        Text(
                                            text = badge.description,
                                            fontSize = 11.sp,
                                            color = ArcadeOnSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${badge.progress}/${badge.maxProgress} completed",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ArcadePrimary
                                        )
                                    }
                                }

                                when {
                                    badge.isClaimed -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(ArcadeSurfaceContainerHigh)
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("CLAIMED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ArcadeOutlineVariant)
                                        }
                                    }
                                    badge.isReadyToClaim -> {
                                        Button(
                                            onClick = { onClaimReward(badge.id, badge.rewardCoins, 100) },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldWin),
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Text("CLAIM +${badge.rewardCoins} 🪙", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                                        }
                                    }
                                    else -> {
                                        Text(
                                            text = "+${badge.rewardCoins} 🪙",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ArcadeSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
