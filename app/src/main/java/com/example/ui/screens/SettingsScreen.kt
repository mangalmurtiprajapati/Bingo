package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.BingoBannerAd
import com.example.model.BingoTheme
import com.example.model.UserStats

@Composable
fun SettingsScreen(
    stats: UserStats,
    activeTheme: BingoTheme,
    onToggleSound: (Boolean) -> Unit,
    onToggleMusic: (Boolean) -> Unit,
    onResetData: () -> Unit,
    onBack: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

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
                    text = "SETTINGS",
                    color = Color(0xFFFFD700),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Settings Options List
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SettingToggleItem(
                    title = "Sound Effects",
                    subtitle = "Card pops, ball caller & win chimes",
                    isChecked = stats.isSoundEnabled,
                    onCheckedChange = onToggleSound
                )

                SettingToggleItem(
                    title = "Background Music",
                    subtitle = "Ambient gaming acoustic loop",
                    isChecked = stats.isMusicEnabled,
                    onCheckedChange = onToggleMusic
                )

                SettingActionItem(
                    title = "Privacy Policy",
                    subtitle = "Offline data storage policy",
                    onClick = { showPrivacyDialog = true }
                )

                SettingActionItem(
                    title = "Rate & Share App",
                    subtitle = "Love Lucky Bingo? Share with friends!",
                    onClick = { }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Danger Zone - Reset Progress
                Button(
                    onClick = { showResetDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("RESET GAME PROGRESS", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Reset Confirmation Dialog
        if (showResetDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .clickable { showResetDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A004E)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clickable(enabled = false) {}
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Reset All Progress?", color = Color(0xFFFF5252), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "This will reset your coins, level, unlocked themes, and stats back to default. This action cannot be undone.",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { showResetDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("CANCEL")
                            }
                            Button(
                                onClick = {
                                    showResetDialog = false
                                    onResetData()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("CONFIRM")
                            }
                        }
                    }
                }
            }
        }

        // Privacy Policy Modal Dialog
        if (showPrivacyDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .clickable { showPrivacyDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0038)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clickable(enabled = false) {}
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Privacy Policy", color = Color(0xFFFFD700), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Lucky Bingo Offline is designed to run 100% locally on your device. We do not collect, transmit, or sell any personal information or gameplay telemetry to remote servers.",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showPrivacyDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("CLOSE", color = Color(0xFF10002B), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        BingoBannerAd(modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun SettingToggleItem(title: String, subtitle: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B003A)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
            }
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFFD700), checkedTrackColor = Color(0xFF4A148C))
            )
        }
    }
}

@Composable
private fun SettingActionItem(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B003A)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
        }
    }
}
