package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sound.SoundManager
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class WheelSegment(
    val label: String,
    val color: Color,
    val coinReward: Int
)

val WHEEL_SEGMENTS = listOf(
    WheelSegment("100 Coins", Color(0xFF9C27B0), 100),
    WheelSegment("250 Coins", Color(0xFF1E88E5), 250),
    WheelSegment("500 Coins", Color(0xFF43A047), 500),
    WheelSegment("JACKPOT!", Color(0xFFFFD700), 2500),
    WheelSegment("150 Coins", Color(0xFFFB8C00), 150),
    WheelSegment("300 Coins", Color(0xFFE91E63), 300),
    WheelSegment("750 Coins", Color(0xFF00ACC1), 750),
    WheelSegment("1000 Coins", Color(0xFFFF5252), 1000)
)

@Composable
fun SpinWheelComposable(
    soundManager: SoundManager?,
    onRewardEarned: (WheelSegment) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val rotationAnim = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var winningSegment by remember { mutableStateOf<WheelSegment?>(null) }

    val segmentAngle = 360f / WHEEL_SEGMENTS.size

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(320.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer Glowing Frame
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFFFD700), Color(0xFF8A2BE2), Color(0xFF10002B))
                        )
                    )
                    .border(6.dp, Color(0xFFFFD700), CircleShape)
                    .shadow(16.dp, CircleShape)
            )

            // The Rotating Wheel Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize(0.92f)
                    .rotate(rotationAnim.value)
            ) {
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                WHEEL_SEGMENTS.forEachIndexed { index, segment ->
                    val startAngle = index * segmentAngle - 90f
                    drawArc(
                        color = segment.color,
                        startAngle = startAngle,
                        sweepAngle = segmentAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )

                    // Draw inner border lines
                    val angleRad = (startAngle * PI / 180).toFloat()
                    val endX = center.x + radius * cos(angleRad)
                    val endY = center.y + radius * sin(angleRad)
                    drawLine(
                        color = Color.White.copy(alpha = 0.6f),
                        start = center,
                        end = Offset(endX, endY),
                        strokeWidth = 2f
                    )
                }

                // Draw central hub
                drawCircle(
                    color = Color(0xFFFFD700),
                    radius = radius * 0.22f,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF2A004E),
                    radius = radius * 0.16f,
                    center = center
                )
            }

            // Fixed Pointer Arrow at top
            Canvas(
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.TopCenter)
            ) {
                val path = Path().apply {
                    moveTo(size.width / 2f, size.height)
                    lineTo(0f, 0f)
                    lineTo(size.width, 0f)
                    close()
                }
                drawPath(path, color = Color(0xFFFFD700))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Spin Button
        Button(
            onClick = {
                if (!isSpinning) {
                    isSpinning = true
                    winningSegment = null
                    soundManager?.playClick()

                    coroutineScope.launch {
                        val selectedIndex = Random.nextInt(WHEEL_SEGMENTS.size)
                        val targetDegrees = 360f * 5 + (WHEEL_SEGMENTS.size - selectedIndex - 0.5f) * segmentAngle
                        val currentDegrees = rotationAnim.value % 360f
                        val totalDegreesToAnimate = targetDegrees - currentDegrees

                        rotationAnim.animateTo(
                            targetValue = rotationAnim.value + totalDegreesToAnimate,
                            animationSpec = tween(
                                durationMillis = 4000,
                                easing = FastOutSlowInEasing
                            )
                        )

                        val winner = WHEEL_SEGMENTS[selectedIndex]
                        winningSegment = winner
                        isSpinning = false
                        if (winner.coinReward >= 1000) {
                            soundManager?.playJackpotSound()
                        } else {
                            soundManager?.playWinFanfare()
                        }
                        onRewardEarned(winner)
                    }
                }
            },
            enabled = !isSpinning,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD700),
                contentColor = Color(0xFF10002B)
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp))
        ) {
            Text(
                text = if (isSpinning) "SPINNING..." else "SPIN WHEEL!",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        winningSegment?.let { winner ->
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4A148C)),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "🎉 WINNER: ${winner.label}! (+${winner.coinReward} Coins)",
                    color = Color(0xFFFFD700),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
