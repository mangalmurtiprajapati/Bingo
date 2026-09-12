package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BingoCard
import com.example.model.BingoTheme

@Composable
fun BingoCardGrid(
    card: BingoCard,
    theme: BingoTheme,
    onCellClick: (number: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val headers = listOf('B', 'I', 'N', 'G', 'O')

    val infiniteTransition = rememberInfiniteTransition(label = "winGlow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = theme.cardBgColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .then(
                if (card.isBingoDeclared) {
                    Modifier
                        .scale(glowScale)
                        .border(
                            3.dp,
                            Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFF4081))),
                            RoundedCornerShape(16.dp)
                        )
                } else {
                    Modifier.border(
                        1.dp,
                        theme.primaryColor.copy(alpha = 0.3f),
                        RoundedCornerShape(16.dp)
                    )
                }
            )
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card Title Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CARD #${card.id}",
                    color = theme.accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                if (card.isBingoDeclared) {
                    Text(
                        text = "🏆 BINGO!",
                        color = Color(0xFFFFD700),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // B-I-N-G-O Column Headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                headers.forEach { letter ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.verticalGradient(theme.headerBgGradients)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 5x5 Number Grid
            card.grid.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.5.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    row.forEach { cell ->
                        val cellBg by animateColorAsState(
                            targetValue = when {
                                cell.isWinningCell -> Color(0xFFFFD700)
                                cell.isMarked -> theme.cellMarkedBg
                                else -> theme.cellDefaultBg
                            },
                            label = "cellBg"
                        )

                        val cellTextColor by animateColorAsState(
                            targetValue = when {
                                cell.isWinningCell -> Color(0xFF10002B)
                                cell.isMarked -> Color.White
                                else -> theme.textPrimary
                            },
                            label = "cellText"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(cellBg)
                                .then(
                                    if (cell.isWinningCell) {
                                        Modifier.border(1.5.dp, Color.White, RoundedCornerShape(8.dp))
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable {
                                    if (!cell.isFreeSpace) {
                                        onCellClick(cell.number)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (cell.isFreeSpace) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "FREE",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFD700))
                                    )
                                }
                            } else {
                                Text(
                                    text = cell.number.toString(),
                                    color = cellTextColor,
                                    fontSize = 14.sp,
                                    fontWeight = if (cell.isMarked) FontWeight.ExtraBold else FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
