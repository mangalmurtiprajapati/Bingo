package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserStats
import com.example.sound.SoundManager
import com.example.ui.components.ConfettiParticleSystem
import com.example.ui.theme.ArcadeBackground
import com.example.ui.theme.ArcadeOnSurface
import com.example.ui.theme.ArcadeOnSurfaceVariant
import com.example.ui.theme.ArcadePrimary
import com.example.ui.theme.ArcadePrimaryContainer
import com.example.ui.theme.ArcadeSecondary
import com.example.ui.theme.ArcadeSecondaryContainer
import com.example.ui.theme.ArcadeSurfaceContainer
import com.example.ui.theme.ArcadeSurfaceContainerHigh
import com.example.ui.theme.ArcadeSurfaceContainerHighest
import com.example.ui.theme.ArcadeSurfaceContainerLow
import com.example.ui.theme.ArcadeSurfaceContainerLowest
import com.example.ui.theme.ArcadeTertiary
import com.example.ui.theme.ArcadeTertiaryContainer

@Composable
fun ArcadeVictoryScreen(
    stats: UserStats,
    winnerName: String = stats.userName,
    isLocalPlayerWinner: Boolean = true,
    opponentName: String = "NeonQueen",
    opponentLevel: Int = 3,
    coinsEarned: Int = 2800,
    gemsEarned: Int = 45,
    xpEarned: Int = 350,
    matchDurationText: String = "01:24",
    soundManager: SoundManager? = null,
    onClaimAndContinue: () -> Unit,
    onPlayAgain: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isChestOpened by remember { mutableStateOf(false) }
    var chestCoinsBonus by remember { mutableIntStateOf(0) }

    val infiniteTransition = rememberInfiniteTransition(label = "bannerPulse")
    val bannerScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bannerScale"
    )

    BackHandler {
        onClaimAndContinue()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ArcadeBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Confetti burst on top of screen
        ConfettiParticleSystem(
            isTriggered = true,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .widthIn(max = 560.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // =========================================================
            // 1. Confetti Canvas & Celebration Ambient Glow Layer
            // =========================================================
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCCE0FF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    ArcadePrimary.copy(alpha = 0.15f),
                                    ArcadeSurfaceContainer,
                                    ArcadeSurfaceContainerLow
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Match Context & Victory Ribbon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(ArcadeSurfaceContainerHighest)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = ArcadePrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "NEON BLITZ ARENA · T3 RANKED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArcadePrimary,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(ArcadeSecondaryContainer.copy(alpha = 0.18f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = ArcadeSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "MATCH OVER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ArcadeSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Victory Royale Banner
                        Box(
                            modifier = Modifier
                                .rotate(-1.5f)
                                .shadow(8.dp, RoundedCornerShape(24.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            ArcadeSecondaryContainer,
                                            ArcadeSecondary,
                                            ArcadeSecondaryContainer
                                        )
                                    ),
                                    RoundedCornerShape(24.dp)
                                )
                                .padding(horizontal = 22.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.MilitaryTech,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = if (isLocalPlayerWinner) "VICTORY ROYALE" else "DUEL COMPLETE",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Celebration Hero Card: Winner Spotlight
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerLowest),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, ArcadeSecondaryContainer.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Avatar with Crown & Laurel Ring
                                Box(contentAlignment = Alignment.TopCenter) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 8.dp)
                                            .size(76.dp)
                                            .clip(CircleShape)
                                            .border(
                                                width = 3.dp,
                                                brush = Brush.sweepGradient(
                                                    listOf(
                                                        ArcadeSecondaryContainer,
                                                        ArcadeSecondary,
                                                        Color(0xFFFFD54F),
                                                        ArcadeSecondaryContainer
                                                    )
                                                ),
                                                shape = CircleShape
                                            )
                                            .background(Color(0xFFFFF8E1)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isLocalPlayerWinner) stats.userAvatar else "👑",
                                            fontSize = 36.sp
                                        )
                                    }

                                    // Crown badge
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(ArcadeSecondaryContainer)
                                            .shadow(2.dp, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("👑", fontSize = 14.sp)
                                    }

                                    // #1 rank tag
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ArcadeTertiaryContainer)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "#1",
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = winnerName,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 22.sp,
                                        color = ArcadeOnSurface
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(ArcadePrimary.copy(alpha = 0.12f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "YOU",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp,
                                            color = ArcadePrimary
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Celebration,
                                        contentDescription = null,
                                        tint = ArcadeTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "BINGO CLAIMED in $matchDurationText · Round 3/3",
                                        fontSize = 12.sp,
                                        color = ArcadeOnSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Runner-Up Podium Mini Roster
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ArcadeSurfaceContainerLow)
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // #2 Rank
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(ArcadeSurfaceContainerHigh),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("2", fontSize = 11.sp, fontWeight = FontWeight.Black, color = ArcadeOnSurface)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(opponentName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ArcadeOnSurface)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("(2 Bingos)", fontSize = 10.sp, color = ArcadeOnSurfaceVariant)
                                        }
                                        Text("+1,200 C", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ArcadeSecondary)
                                    }

                                    // #3 Rank
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(ArcadeSurfaceContainerHigh),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("3", fontSize = 11.sp, fontWeight = FontWeight.Black, color = ArcadeOnSurface)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("LuckyStrike", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ArcadeOnSurface)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("(1 Bingo)", fontSize = 10.sp, color = ArcadeOnSurfaceVariant)
                                        }
                                        Text("+400 C", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ArcadeSecondary)
                                    }

                                    // #4 Rank
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(ArcadeSurfaceContainerHigh),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("4", fontSize = 11.sp, fontWeight = FontWeight.Black, color = ArcadeOnSurfaceVariant)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("CyberDauber", fontSize = 12.sp, color = ArcadeOnSurfaceVariant)
                                        }
                                        Text("+100 XP", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ArcadePrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================
            // 2. Grand Rewards Arcade Payout Box
            // =========================================================
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Redeem,
                                contentDescription = null,
                                tint = ArcadeSecondaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Match Rewards",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = ArcadeOnSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(ArcadeTertiary.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "INSTANT PAYOUT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = ArcadeTertiary
                            )
                        }
                    }

                    // Payout Highlights Cards (Coins & Gems)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Coin Reward Block
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFE082)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(ArcadeSecondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        "COIN STASH",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArcadeOnSurfaceVariant
                                    )
                                    Text(
                                        "+${coinsEarned + chestCoinsBonus}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ArcadeSecondary
                                    )
                                }
                            }
                        }

                        // Gems Reward Block
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC)),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF8BBD0)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(ArcadeTertiary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Diamond,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        "GEMS",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArcadeOnSurfaceVariant
                                    )
                                    Text(
                                        "+$gemsEarned",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ArcadeTertiary
                                    )
                                }
                            }
                        }
                    }

                    // Season Pass XP Progress Bar
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerLow),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.WorkspacePremium,
                                        contentDescription = null,
                                        tint = ArcadePrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Season 4 Circuit", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ArcadeOnSurface)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("+$xpEarned XP", fontWeight = FontWeight.Black, fontSize = 12.sp, color = ArcadePrimary)
                                }
                                Text("Tier 28 → 29", fontSize = 10.sp, color = ArcadeOnSurfaceVariant, fontWeight = FontWeight.Bold)
                            }

                            LinearProgressIndicator(
                                progress = { 0.78f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = ArcadePrimary,
                                trackColor = ArcadeSurfaceContainerHighest
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("780 / 900 XP", fontSize = 10.sp, color = ArcadeOnSurfaceVariant)
                                Text("Rank Up in 120 XP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ArcadeSecondary)
                            }
                        }
                    }

                    // Bonus Mystery Loot Chest Badge
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerHigh),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ArcadeTertiaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Inventory2,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "Neon Cyber Chest",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp,
                                            color = ArcadeOnSurface
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(ArcadeTertiaryContainer)
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("NEW", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                    Text(
                                        if (isChestOpened) "Unlocked: +500 Bonus Gold!" else "Tap to crack open exclusive daubers",
                                        fontSize = 11.sp,
                                        color = if (isChestOpened) Color(0xFF2E7D32) else ArcadeOnSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (!isChestOpened) {
                                        isChestOpened = true
                                        chestCoinsBonus = 500
                                        soundManager?.playWinFanfare()
                                        Toast.makeText(context, "🎁 Chest Opened! +500 Coins added!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isChestOpened) Color(0xFF43A047) else ArcadeTertiary
                                ),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isChestOpened) "OPENED ✓" else "OPEN",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // =========================================================
            // 3. Match Performance & Tactical Breakdown (2x2 Bento Matrix)
            // =========================================================
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Analytics,
                                contentDescription = null,
                                tint = ArcadePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tactical Stats", fontSize = 17.sp, fontWeight = FontWeight.Black, color = ArcadeOnSurface)
                        }
                        Text("ROUND 3 FINAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ArcadeOnSurfaceVariant)
                    }

                    // 2x2 Bento Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Accuracy
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerLow),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("ACCURACY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ArcadeOnSurfaceVariant)
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = ArcadePrimary, modifier = Modifier.size(14.dp))
                                }
                                Text("98.4%", fontSize = 18.sp, fontWeight = FontWeight.Black, color = ArcadeOnSurface)
                                Text("Streak Master", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ArcadePrimary)
                            }
                        }

                        // 2. Fastest Daub
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerLow),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("FASTEST DAUB", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ArcadeOnSurfaceVariant)
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = ArcadeSecondary, modifier = Modifier.size(14.dp))
                                }
                                Text("0.38s", fontSize = 18.sp, fontWeight = FontWeight.Black, color = ArcadeOnSurface)
                                Text("Blitz Reflex", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ArcadeSecondary)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 3. Bingos
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerLow),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("BINGOS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ArcadeOnSurfaceVariant)
                                    Icon(Icons.Default.Stars, contentDescription = null, tint = ArcadeTertiary, modifier = Modifier.size(14.dp))
                                }
                                Text("3 Claimed", fontSize = 18.sp, fontWeight = FontWeight.Black, color = ArcadeTertiary)
                                Text("Triple Crown", fontSize = 10.sp, color = ArcadeOnSurfaceVariant)
                            }
                        }

                        // 4. Power Combos
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerLow),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("POWER COMBOS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ArcadeOnSurfaceVariant)
                                    Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = ArcadePrimary, modifier = Modifier.size(14.dp))
                                }
                                Text("4x Blitz", fontSize = 18.sp, fontWeight = FontWeight.Black, color = ArcadePrimary)
                                Text("Full Overcharge", fontSize = 10.sp, color = ArcadeOnSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // =========================================================
            // 4. Interactive Social & Arcade Good Game (GG) Action Bar
            // =========================================================
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Send GG:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArcadeOnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val reactions = listOf("🎉 GG!", "⚡ Rematch?", "🔥 On Fire", "👑 Well Played")
                        items(reactions) { react ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(ArcadeSurfaceContainerLowest)
                                    .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(16.dp))
                                    .clickable {
                                        soundManager?.playClick()
                                        Toast.makeText(context, "$react sent to $opponentName!", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(react, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ArcadeOnSurface)
                            }
                        }
                    }
                }
            }

            // =========================================================
            // 5. High-Impact Arcade Call-To-Action Footer Area
            // =========================================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary Big Arcade Golden Button
                Button(
                    onClick = onClaimAndContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(6.dp, RoundedCornerShape(27.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        ArcadeSecondaryContainer,
                                        ArcadeSecondary,
                                        ArcadeSecondaryContainer
                                    )
                                ),
                                RoundedCornerShape(27.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.DoneAll,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "CLAIM ALL & CONTINUE",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                // Secondary Actions Row: Play Again & Share Victory
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onPlayAgain,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ArcadeSurfaceContainerHigh)
                    ) {
                        Icon(
                            Icons.Default.Replay,
                            contentDescription = null,
                            tint = ArcadePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "PLAY AGAIN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArcadePrimary
                        )
                    }

                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "🏆 I won a Victory Royale in Mind Bingo against $opponentName with 3 Bingos! Can you beat my time of $matchDurationText?"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Victory"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ArcadeSurfaceContainerHigh)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = null,
                            tint = ArcadeOnSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "SHARE VICTORY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArcadeOnSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
