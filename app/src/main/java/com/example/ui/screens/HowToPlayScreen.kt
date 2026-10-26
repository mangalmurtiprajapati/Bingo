package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BingoTheme
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
import com.example.ui.theme.TournamentGold

@Composable
fun HowToPlayScreen(
    activeTheme: BingoTheme,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var isHindi by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("classic") } // "classic", "blitz", "strategy"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ArcadeBackground,
        topBar = {
            ArcadeTopHeader(
                title = if (isHindi) "नियम और गाइड" else "How To Play",
                subtitle = if (isHindi) "60-सेकंड गाइड" else "Rules & Strategy",
                onBack = onBack
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
                // 1. Language Toggle & Mode Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Translate, contentDescription = null, tint = ArcadePrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isHindi) "भाषा चुनें" else "Language",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArcadeOnSurface
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(ArcadeSurfaceContainerHigh)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (!isHindi) ArcadePrimary else Color.Transparent)
                                .clickable { isHindi = false }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("EN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (!isHindi) Color.White else ArcadeOnSurfaceVariant)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isHindi) ArcadePrimary else Color.Transparent)
                                .clickable { isHindi = true }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("हिंदी (HI)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isHindi) Color.White else ArcadeOnSurfaceVariant)
                        }
                    }
                }

                // 2. Interactive Hero Preview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadePrimary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(listOf(ArcadePrimary, ArcadePrimaryContainer)))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ArcadeSecondaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isHindi) "60 सेकंड गाइड" else "60s Quick Guide",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                            Text(
                                text = "1V1 DUEL ARENA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = ArcadePrimaryFixed
                            )
                        }

                        Text(
                            text = if (isHindi) "5x5 ग्रिड में बिंगो कैसे जीतें" else "Master the 5x5 Grid in Seconds",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        Text(
                            text = if (isHindi)
                                "1 से 25 नंबर अपने बोर्ड पर सेट करें। बारी-बारी से नंबर बोलें, 5 लाइनें पूरी करें और बिंगो क्लेम करें!"
                            else
                                "Numbers 1–25 arranged strategically. Alternate calls with rival, complete 5 distinct lines, and claim B-I-N-G-O!",
                            fontSize = 12.sp,
                            color = ArcadePrimaryFixed,
                            lineHeight = 16.sp
                        )

                        // 3 Quick Metric Pills
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            GuideMetricPill(
                                label = if (isHindi) "जीतने की शर्त" else "Win Condition",
                                value = if (isHindi) "5 लाइनें" else "5 Lines",
                                modifier = Modifier.weight(1f)
                            )
                            GuideMetricPill(
                                label = if (isHindi) "खेलने का तरीका" else "Pace",
                                value = if (isHindi) "बारी-बारी" else "Turn-by-Turn",
                                modifier = Modifier.weight(1f)
                            )
                            GuideMetricPill(
                                label = if (isHindi) "इनाम" else "Reward",
                                value = "+500 🪙",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 3. 4-Step Interactive Loop
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isHindi) "4 आसान कदम" else "4-Step Winning Loop",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ArcadeOnSurface
                    )

                    val steps = if (isHindi) listOf(
                        Triple("1", "बोर्ड तैयार करें (1–25)", "Auto Fill दबाकर या खुद 1 से 25 तक के नंबर बोर्ड के 25 खानों में भरें।"),
                        Triple("2", "बारी-बारी से नंबर कट करें", "आप एक नंबर चुनेंगे जो दोनों खिलाड़ियों के बोर्ड पर कट होगा, फिर प्रतिद्वंद्वी चुनेगा।"),
                        Triple("3", "लाइनें बनाएं", "किसी भी पंक्ति (Horizontal), स्तंभ (Vertical) या विकर्ण (Diagonal) के सभी 5 नंबर कट होने पर 1 लाइन बनती है।"),
                        Triple("4", "5 लाइन = B-I-N-G-O!", "जैसे ही B-I-N-G-O की 5 लाइनें पूरी हों, तुरंत 'CLAIM BINGO' बटन दबाएं और जीतें!")
                    ) else listOf(
                        Triple("1", "Setup Your 5x5 Board (1–25)", "Hit Auto Fill or tap cells to place numbers 1 to 25 across all 25 grid slots."),
                        Triple("2", "Alternate Number Calls", "Pick a number on your turn. It marks that number on BOTH yours and rival's board!"),
                        Triple("3", "Strike Full Lines", "Complete any row, column, or corner diagonal of 5 marked cells to unlock a line."),
                        Triple("4", "Claim 5 Lines for B-I-N-G-O", "First player to complete all 5 letters (B-I-N-G-O) claims instant victory!")
                    )

                    steps.forEach { (num, title, desc) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerLowest),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ArcadePrimaryFixed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = num,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ArcadePrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArcadeOnSurface
                                    )
                                    Text(
                                        text = desc,
                                        fontSize = 11.sp,
                                        color = ArcadeOnSurfaceVariant,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Winning Patterns Library
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerHigh)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isHindi) "जीतने वाले पैटर्न" else "Valid Line Patterns",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArcadeOnSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PatternMiniCard(
                                title = if (isHindi) "क्षैतिज (Rows)" else "5 Rows",
                                subtitle = "Any horizontal",
                                modifier = Modifier.weight(1f)
                            )
                            PatternMiniCard(
                                title = if (isHindi) "लंबवत (Cols)" else "5 Columns",
                                subtitle = "Any vertical",
                                modifier = Modifier.weight(1f)
                            )
                            PatternMiniCard(
                                title = if (isHindi) "विकर्ण (Diagonals)" else "2 Diagonals",
                                subtitle = "Corner-to-corner",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun GuideMetricPill(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = ArcadePrimaryFixed)
            Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
        }
    }
}

@Composable
private fun PatternMiniCard(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerLowest)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(ArcadeSecondaryFixed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = ArcadeSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ArcadeOnSurface)
            Text(text = subtitle, fontSize = 9.sp, color = ArcadeOnSurfaceVariant)
        }
    }
}
