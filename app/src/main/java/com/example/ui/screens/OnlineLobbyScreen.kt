package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BingoTheme
import com.example.viewmodel.ClassicMatchState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineLobbyScreen(
    state: ClassicMatchState,
    theme: BingoTheme,
    onCreateRoom: () -> Unit,
    onJoinRoom: (String) -> Unit,
    onQuickMatch: () -> Unit,
    onProceedToSetup: () -> Unit,
    onBack: () -> Unit
) {
    var joinCodeInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Play Online Multiplayer",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = theme.headerBgGradients.first()
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Brush.verticalGradient(theme.backgroundGradients))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Banner
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cardBgColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Public,
                            contentDescription = null,
                            tint = theme.accentColor,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                "Live Online Arena",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = theme.textPrimary
                            )
                            Text(
                                "Create a private room for your friends or jump into a quick online match!",
                                fontSize = 12.sp,
                                color = theme.textPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // 1. Quick Match Option
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cardBgColor.copy(alpha = 0.9f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = Color(0xFF00E676))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Quick Match (1 vs 1)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = theme.textPrimary
                            )
                        }

                        Text(
                            "Instantly pair up with an active online player for a fast 1-25 BINGO game.",
                            fontSize = 12.sp,
                            color = theme.textPrimary.copy(alpha = 0.8f)
                        )

                        Button(
                            onClick = {
                                onQuickMatch()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("quick_match_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (state.isOnlineSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Searching for Player...")
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Find Match Now", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 2. Private Room (Create / Join)
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cardBgColor.copy(alpha = 0.9f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = theme.accentColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Private Room Code",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = theme.textPrimary
                            )
                        }

                        // Create room section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Your Room Code:",
                                    fontSize = 12.sp,
                                    color = theme.textPrimary.copy(alpha = 0.7f)
                                )
                                Text(
                                    "#${state.roomCode}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = theme.accentColor
                                )
                            }

                            OutlinedButton(
                                onClick = onCreateRoom,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.accentColor),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("New Code")
                            }
                        }

                        // Join room section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = joinCodeInput,
                                onValueChange = { if (it.length <= 6) joinCodeInput = it },
                                label = { Text("Enter 6-digit code") },
                                placeholder = { Text("e.g. 742918") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = theme.textPrimary,
                                    unfocusedTextColor = theme.textPrimary
                                )
                            )

                            Button(
                                onClick = {
                                    if (joinCodeInput.isNotBlank()) {
                                        onJoinRoom(joinCodeInput)
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor)
                            ) {
                                Text("Join")
                            }
                        }

                        // Status message
                        Text(
                            text = "Status: ${state.onlineStatusMessage}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF00E676)
                        )
                    }
                }

                // Ready to setup button
                Button(
                    onClick = onProceedToSetup,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("online_proceed_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "SET UP YOUR BOARD 🎯",
                        color = Color(0xFF1A0033),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
