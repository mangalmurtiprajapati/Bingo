package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BingoTheme

@Composable
fun BingoLettersBar(
    completedLinesCount: Int,
    theme: BingoTheme,
    title: String = "YOUR BINGO",
    modifier: Modifier = Modifier
) {
    val letters = listOf('B', 'I', 'N', 'G', 'O')
    val struckCount = minOf(5, completedLinesCount)
    val hasFullBingo = completedLinesCount >= 5

    val infiniteTransition = rememberInfiniteTransition(label = "bingoGlow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = theme.cardBgColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (hasFullBingo) {
                    Modifier
                        .scale(glowScale)
                        .border(
                            2.5.dp,
                            Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFF4081))),
                            RoundedCornerShape(16.dp)
                        )
                } else {
                    Modifier.border(
                        1.dp,
                        theme.primaryColor.copy(alpha = 0.4f),
                        RoundedCornerShape(16.dp)
                    )
                }
            )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = theme.accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "$completedLinesCount / 5 Lines Completed",
                    color = if (hasFullBingo) Color(0xFFFFD700) else Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                letters.forEachIndexed { index, letter ->
                    val isStruck = index < struckCount

                    val letterBg by animateColorAsState(
                        targetValue = if (isStruck) {
                            Color(0xFFFFD700)
                        } else {
                            theme.cellDefaultBg
                        },
                        label = "letterBg"
                    )

                    val letterTextColor by animateColorAsState(
                        targetValue = if (isStruck) Color(0xFF10002B) else Color.White.copy(alpha = 0.85f),
                        label = "letterColor"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(letterBg)
                            .border(
                                width = if (isStruck) 2.dp else 1.dp,
                                color = if (isStruck) Color(0xFFFF4081) else theme.primaryColor.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .then(if (isStruck) Modifier.shadow(4.dp, RoundedCornerShape(10.dp)) else Modifier),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter.toString(),
                            color = letterTextColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )

                        // Red/Accent strikethrough slash when cut
                        if (isStruck) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawLine(
                                    color = Color(0xFFFF1744),
                                    start = Offset(x = 6.dp.toPx(), y = size.height - 6.dp.toPx()),
                                    end = Offset(x = size.width - 6.dp.toPx(), y = 6.dp.toPx()),
                                    strokeWidth = 3.5.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
