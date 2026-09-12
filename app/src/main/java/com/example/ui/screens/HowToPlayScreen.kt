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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.BingoBannerAd
import com.example.model.BingoTheme

@Composable
fun HowToPlayScreen(
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
                .padding(bottom = 60.dp)
        ) {
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
                    text = "HOW TO PLAY BINGO",
                    color = Color(0xFFFFD700),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            InstructionSection(
                title = "1. Classic 5x5 Grid (Numbers 1 to 25)",
                body = "Each board consists of a 5x5 grid (25 boxes). You assign numbers 1 to 25 into each box:\n" +
                        "• Tap any empty box to place the next number (1, 2, 3... up to 25).\n" +
                        "• Tap 'Auto Fill' to shuffle numbers 1-25 randomly into all 25 boxes instantly!\n" +
                        "• Computer also fills its own board randomly in the background."
            )

            Spacer(modifier = Modifier.height(12.dp))

            InstructionSection(
                title = "2. Turn-Based Cutting (Cross ✕)",
                body = "• You start by choosing any number on your board to cut.\n" +
                        "• When a number is chosen (e.g. 5), it gets crossed out with a red ✕ on BOTH your board and Computer's board!\n" +
                        "• Then Computer takes its turn, picks a number (e.g. 7), which is also cut on both boards!\n" +
                        "• Play continues turn-by-turn until 5 lines are formed."
            )

            Spacer(modifier = Modifier.height(12.dp))

            InstructionSection(
                title = "3. Completing Lines & B-I-N-G-O Letters",
                body = "There are 12 possible lines on the 5x5 grid:\n" +
                        "• 5 Horizontal Rows\n" +
                        "• 5 Vertical Columns\n" +
                        "• 2 Diagonals (Corner to Corner)\n\n" +
                        "Every time all 5 numbers in any line are crossed out, you complete 1 line and strike a letter:\n" +
                        "• 1 Line: B\n" +
                        "• 2 Lines: B - I\n" +
                        "• 3 Lines: B - I - N\n" +
                        "• 4 Lines: B - I - N - G\n" +
                        "• 5 Lines: B - I - N - G - O 🎯"
            )

            Spacer(modifier = Modifier.height(12.dp))

            InstructionSection(
                title = "4. Winning the Match",
                body = "The first player to complete 5 lines shouts BINGO and wins the game! Play with Computer AI, challenge a friend with Pass & Play, or jump into Online Multiplayer!"
            )
        }

        BingoBannerAd(modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun InstructionSection(title: String, body: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B003A)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = body, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}
