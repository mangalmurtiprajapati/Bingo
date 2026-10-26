package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BingoTheme
import com.example.model.ThemeId
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
import com.example.ui.theme.ArcadeSurfaceContainerHighest
import com.example.ui.theme.ArcadeSurfaceContainerLowest
import com.example.ui.theme.ArcadeTertiary
import com.example.ui.theme.ArcadeTertiaryContainer
import com.example.ui.theme.ArcadeTertiaryFixed
import com.example.ui.theme.EmeraldWin
import com.example.ui.theme.TournamentGold

data class ArcadeThemeItem(
    val themeId: ThemeId,
    val name: String,
    val description: String,
    val cost: Int,
    val primaryColor: Color,
    val secondaryColor: Color,
    val emoji: String,
    val isPremium: Boolean
)

@Composable
fun ThemesScreen(
    stats: UserStats,
    activeTheme: BingoTheme,
    onSelectTheme: (ThemeId) -> Unit,
    onUnlockTheme: (ThemeId, Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var selectedFilter by remember { mutableStateOf("ALL") }
    var interactiveTileMarked by remember { mutableStateOf(false) }

    val allThemes = listOf(
        ArcadeThemeItem(ThemeId.CLASSIC, "Classic Blue", "Clean tournament layout with high contrast.", 0, ArcadePrimary, ArcadePrimaryContainer, "🔷", false),
        ArcadeThemeItem(ThemeId.NEON, "Cyber Neon", "Futuristic synthwave laser beams and glow.", 1200, Color(0xFF00E5FF), Color(0xFFDD2269), "⚡", false),
        ArcadeThemeItem(ThemeId.ROYAL_GOLD, "Royal 24K", "Championship grandmaster finish with gold trims.", 1500, TournamentGold, ArcadeSecondaryContainer, "👑", true),
        ArcadeThemeItem(ThemeId.CANDY, "Candy Land", "Sweet pastel sugar fiesta with colorful gems.", 800, ArcadeTertiaryContainer, Color(0xFFFF80AB), "🍭", false),
        ArcadeThemeItem(ThemeId.WOOD, "Rustic Wood", "Warm polished mahogany with vintage arcade feel.", 1000, Color(0xFF8D6E63), Color(0xFF4E342E), "🪵", false),
        ArcadeThemeItem(ThemeId.GALAXY, "Cosmic Void", "Deep space nebula with starlight trajectories.", 2000, Color(0xFF7C4DFF), Color(0xFF311B92), "🌌", true),
        ArcadeThemeItem(ThemeId.DARK, "Stealth Dark", "Midnight charcoal aesthetic with high visibility.", 900, Color(0xFF37474F), Color(0xFF263238), "🌑", false),
        ArcadeThemeItem(ThemeId.PREMIUM, "Grandmaster Pulse", "Ultra-rare glowing arcade prestige chassis.", 3500, TournamentGold, Color(0xFFB78103), "💎", true)
    )

    val filteredThemes = when (selectedFilter) {
        "UNLOCKED" -> allThemes.filter { stats.unlockedThemeIds.contains(it.themeId) || it.cost == 0 }
        "PREMIUM" -> allThemes.filter { it.isPremium }
        else -> allThemes
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ArcadeBackground,
        topBar = {
            ArcadeTopHeader(
                title = "Themes Shop",
                subtitle = "Board Skins & VFX",
                coins = stats.coins,
                gems = 120,
                onBack = onBack
            )
        },
        bottomBar = {
            ArcadeBottomNavigationBar(
                currentRoute = "themes",
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
                // 1. Seasonal Featured Hero Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadeInverseSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ArcadeTertiaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "SEASONAL EXCLUSIVE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }

                            Text(
                                text = "🪙 %,d".format(stats.coins),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = TournamentGold
                            )
                        }

                        Text(
                            text = "Board Themes & FX",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = ArcadeInverseOnSurface
                        )

                        Text(
                            text = "Revamp your 5x5 board, strike trails, and victory celebrations with arcade aesthetics!",
                            fontSize = 12.sp,
                            color = ArcadeInverseOnSurface.copy(alpha = 0.8f)
                        )
                    }
                }

                // 2. Filter Category Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL", "UNLOCKED", "PREMIUM").forEach { category ->
                        val isSelected = selectedFilter == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) ArcadePrimary else ArcadeSurfaceContainerHigh)
                                .clickable { selectedFilter = category }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = category,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) Color.White else ArcadeOnSurfaceVariant
                            )
                        }
                    }
                }

                // 3. Equipped Showcase Spotlight Card with Live 3x3 Sample
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadeInverseSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(ArcadeTertiaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = activeTheme.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(ArcadePrimaryContainer)
                                                .padding(horizontal = 6.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "EQUIPPED",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Custom SFX & Laser Beam Included",
                                        fontSize = 10.sp,
                                        color = ArcadeInverseOnSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            Button(
                                onClick = { interactiveTileMarked = !interactiveTileMarked },
                                colors = ButtonDefaults.buttonColors(containerColor = ArcadeSurfaceContainerHigh.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = null,
                                    tint = TournamentGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("TAP SAMPLE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }

                        // 3x3 Live Interactive Board Canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.widthIn(max = 240.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (r in 0 until 3) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        for (c in 0 until 3) {
                                            val isCenter = r == 1 && c == 1
                                            val isMarked = (r == 0 && c == 1) || (interactiveTileMarked && r == 2 && c == 2)
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        when {
                                                            isCenter -> ArcadePrimaryContainer
                                                            isMarked -> ArcadeTertiaryContainer
                                                            else -> ArcadeSurfaceContainerHigh.copy(alpha = 0.25f)
                                                        }
                                                    )
                                                    .clickable {
                                                        if (!isCenter) interactiveTileMarked = !interactiveTileMarked
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isCenter) {
                                                    Icon(
                                                        imageVector = Icons.Default.Bolt,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                } else {
                                                    Text(
                                                        text = "${(r * 3 + c + 7)}",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color.White
                                                    )
                                                }

                                                if (isMarked) {
                                                    Text(
                                                        text = "✕",
                                                        fontSize = 20.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Themes Catalog Grid / Stack
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Available Skins (${filteredThemes.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ArcadeOnSurface
                    )

                    filteredThemes.forEach { themeItem ->
                        val isEquipped = activeTheme.id == themeItem.themeId
                        val isUnlocked = stats.unlockedThemeIds.contains(themeItem.themeId) || themeItem.cost == 0

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(1.dp, RoundedCornerShape(16.dp))
                                .testTag("theme_card_${themeItem.themeId.name}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerLowest)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
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
                                                Brush.linearGradient(listOf(themeItem.primaryColor, themeItem.secondaryColor))
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = themeItem.emoji, fontSize = 22.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = themeItem.name,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ArcadeOnSurface
                                            )
                                            if (themeItem.isPremium) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(ArcadeSecondaryFixed)
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text("VIP", fontSize = 8.sp, fontWeight = FontWeight.Black, color = ArcadeSecondary)
                                                }
                                            }
                                        }
                                        Text(
                                            text = themeItem.description,
                                            fontSize = 11.sp,
                                            color = ArcadeOnSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                when {
                                    isEquipped -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(18.dp))
                                                .background(EmeraldWin.copy(alpha = 0.15f))
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "EQUIPPED",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                color = EmeraldWin
                                            )
                                        }
                                    }
                                    isUnlocked -> {
                                        Button(
                                            onClick = { onSelectTheme(themeItem.themeId) },
                                            colors = ButtonDefaults.buttonColors(containerColor = ArcadePrimary),
                                            shape = RoundedCornerShape(18.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Text("Equip", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                    else -> {
                                        Button(
                                            onClick = { onUnlockTheme(themeItem.themeId, themeItem.cost) },
                                            colors = ButtonDefaults.buttonColors(containerColor = ArcadeSecondaryContainer),
                                            shape = RoundedCornerShape(18.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("%,d 🪙".format(themeItem.cost), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
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
