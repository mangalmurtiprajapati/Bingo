package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Token
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArcadeBackground
import com.example.ui.theme.ArcadeOnSurface
import com.example.ui.theme.ArcadeOnSurfaceVariant
import com.example.ui.theme.ArcadeOutlineVariant
import com.example.ui.theme.ArcadePrimary
import com.example.ui.theme.ArcadePrimaryContainer
import com.example.ui.theme.ArcadePrimaryFixed
import com.example.ui.theme.ArcadeSecondaryContainer
import com.example.ui.theme.ArcadeSecondaryFixed
import com.example.ui.theme.ArcadeSurfaceContainer
import com.example.ui.theme.ArcadeSurfaceContainerHigh
import com.example.ui.theme.ArcadeSurfaceContainerLowest
import com.example.ui.theme.TournamentGold

/**
 * Reusable top header matching Stitch Arcade specs with:
 * Optional Back button, Screen Title + Subtitle, Coins + Gems pill, Action button.
 */
@Composable
fun ArcadeTopHeader(
    title: String,
    subtitle: String? = null,
    coins: Int = 4850,
    gems: Int = 120,
    onBack: (() -> Unit)? = null,
    onSoundToggle: (() -> Unit)? = null,
    isSoundMuted: Boolean = false,
    onHelp: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = ArcadeSurfaceContainerLowest,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Back button or app logo dot
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ArcadeSurfaceContainer)
                                .testTag("header_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = ArcadeOnSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    } else {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ArcadePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "B",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Column {
                        Text(
                            text = title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ArcadeOnSurface,
                            maxLines = 1
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArcadeOnSurfaceVariant,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }

                // Right: Economy pills + optional sound/help button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Currency Capsule
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(ArcadeSurfaceContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Coins
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(TournamentGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🪙",
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "%,d".format(coins),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArcadeOnSurface
                            )
                        }

                        // Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(12.dp)
                                .background(ArcadeOutlineVariant)
                        )

                        // Gems
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "💎",
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = gems.toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArcadePrimary
                            )
                        }
                    }

                    // Sound Toggle
                    if (onSoundToggle != null) {
                        IconButton(
                            onClick = onSoundToggle,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ArcadeSurfaceContainer)
                                .testTag("header_sound_toggle")
                        ) {
                            Icon(
                                imageVector = if (isSoundMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = if (isSoundMuted) "Unmute" else "Mute",
                                tint = if (isSoundMuted) ArcadeOnSurfaceVariant else ArcadePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Help Toggle
                    if (onHelp != null) {
                        IconButton(
                            onClick = onHelp,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ArcadeSurfaceContainer)
                                .testTag("header_help_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "Rules & Help",
                                tint = ArcadePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bottom Navigation Bar matching Stitch specifications.
 * 5 destinations: Home, Lobby, Spin, Shop, Badges.
 */
@Composable
fun ArcadeBottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = ArcadeSurfaceContainerLowest,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArcadeNavItem(
                    label = "Home",
                    icon = Icons.Default.Home,
                    isSelected = currentRoute == "home",
                    tag = "nav_tab_home",
                    onClick = { onNavigate("home") }
                )
                ArcadeNavItem(
                    label = "Lobby",
                    icon = Icons.Default.Sensors,
                    isSelected = currentRoute == "online_lobby",
                    tag = "nav_tab_lobby",
                    onClick = { onNavigate("online_lobby") }
                )
                ArcadeNavItem(
                    label = "Spin",
                    icon = Icons.Default.Casino,
                    isSelected = currentRoute == "spin_wheel",
                    tag = "nav_tab_spin",
                    onClick = { onNavigate("spin_wheel") }
                )
                ArcadeNavItem(
                    label = "Shop",
                    icon = Icons.Default.Palette,
                    isSelected = currentRoute == "themes",
                    tag = "nav_tab_shop",
                    onClick = { onNavigate("themes") }
                )
                ArcadeNavItem(
                    label = "Badges",
                    icon = Icons.Default.EmojiEvents,
                    isSelected = currentRoute == "achievements",
                    tag = "nav_tab_badges",
                    onClick = { onNavigate("achievements") }
                )
            }
        }
    }
}

@Composable
private fun ArcadeNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    val tintColor by animateColorAsState(
        targetValue = if (isSelected) ArcadePrimary else ArcadeOnSurfaceVariant,
        label = "nav_tint"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(if (isSelected) ArcadePrimaryFixed else Color.Transparent)
                .padding(horizontal = 14.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tintColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            color = tintColor
        )
    }
}
