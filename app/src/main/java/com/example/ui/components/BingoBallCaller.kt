package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BingoTheme

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BingoBallCaller(
    currentNumber: Int?,
    calledHistory: List<Int>,
    timerProgress: Float,
    theme: BingoTheme,
    modifier: Modifier = Modifier
) {
    fun getLetter(num: Int): Char = when (num) {
        in 1..15 -> 'B'
        in 16..30 -> 'I'
        in 31..45 -> 'N'
        in 46..60 -> 'G'
        else -> 'O'
    }

    fun getBallColors(letter: Char): List<Color> = when (letter) {
        'B' -> listOf(Color(0xFF2196F3), Color(0xFF0D47A1))
        'I' -> listOf(Color(0xFFE91E63), Color(0xFF880E4F))
        'N' -> listOf(Color(0xFFFF9800), Color(0xFFE65100))
        'G' -> listOf(Color(0xFF4CAF50), Color(0xFF1B5E20))
        else -> listOf(Color(0xFF9C27B0), Color(0xFF4A148C))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Main Big Active Ball Caller
            Box(
                modifier = Modifier
                    .size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                // Circular Timer Ring
                CircularProgressIndicator(
                    progress = { timerProgress },
                    modifier = Modifier.size(80.dp),
                    color = Color(0xFFFFD700),
                    trackColor = Color.White.copy(alpha = 0.2f),
                    strokeWidth = 4.dp
                )

                if (currentNumber != null) {
                    val letter = getLetter(currentNumber)
                    val colors = getBallColors(letter)

                    AnimatedContent(
                        targetState = currentNumber,
                        transitionSpec = { scaleIn() togetherWith scaleOut() },
                        label = "ballAnim"
                    ) { num ->
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(colors))
                                .border(2.dp, Color.White, CircleShape)
                                .shadow(8.dp, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = getLetter(num).toString(),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = num.toString(),
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "READY",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // History Strip
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "RECENT BALLS (${calledHistory.size}/75)",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(calledHistory.takeLast(8).reversed()) { num ->
                        val letter = getLetter(num)
                        val colors = getBallColors(letter)

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(colors))
                                .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$letter$num",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
