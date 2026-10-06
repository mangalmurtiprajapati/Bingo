package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sound.SoundManager
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class WheelSegment(
    val amountText: String,
    val subText: String,
    val color: Color,
    val starReward: Int
)

val WHEEL_SEGMENTS = listOf(
    WheelSegment("50", "STARS", Color(0xFF1E88E5), 50),
    WheelSegment("100", "STARS", Color(0xFF8E24AA), 100),
    WheelSegment("150", "STARS", Color(0xFF00ACC1), 150),
    WheelSegment("500", "MEGA", Color(0xFFFF9800), 500),
    WheelSegment("200", "STARS", Color(0xFFFF5722), 200),
    WheelSegment("250", "STARS", Color(0xFFE91E63), 250),
    WheelSegment("300", "STARS", Color(0xFF5E35B1), 300),
    WheelSegment("1000", "SUPER", Color(0xFF43A047), 1000)
)

@Composable
fun SpinWheelComposable(
    canSpin: Boolean,
    remainingTimeMs: Long,
    soundManager: SoundManager?,
    onRewardEarned: (WheelSegment) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val rotationAnim = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var winningSegment by remember { mutableStateOf<WheelSegment?>(null) }
    var hasJustSpun by remember { mutableStateOf(false) }

    val segmentAngle = 360f / WHEEL_SEGMENTS.size

    val hours = (remainingTimeMs / (1000 * 60 * 60))
    val minutes = (remainingTimeMs / (1000 * 60)) % 60

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Daily limit badge
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (canSpin && !hasJustSpun) Color(0xFF00E676).copy(alpha = 0.2f)
                else Color(0xFFFF9100).copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (canSpin && !hasJustSpun) Icons.Default.CardGiftcard else Icons.Default.HourglassTop,
                    contentDescription = null,
                    tint = if (canSpin && !hasJustSpun) Color(0xFF00E676) else Color(0xFFFF9100),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (canSpin && !hasJustSpun) "1 Free Spin Available Today! 🎁"
                    else "Next Free Spin in ${hours}h ${minutes}m ⏰",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Box(
            modifier = Modifier
                .size(310.dp)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer Glowing Frame
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFFFD54F), Color(0xFFFF9800), Color(0xFF0288D1))
                        )
                    )
                    .border(6.dp, Color(0xFFFFB300), CircleShape)
                    .shadow(16.dp, CircleShape)
            )

            // The Rotating Wheel Canvas with Numbers and Labels
            Canvas(
                modifier = Modifier
                    .fillMaxSize(0.93f)
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
                        color = Color.White.copy(alpha = 0.8f),
                        start = center,
                        end = Offset(endX, endY),
                        strokeWidth = 3f
                    )

                    // Draw Numbers and Labels inside each segment!
                    val midAngleDeg = startAngle + segmentAngle / 2f
                    val midAngleRad = (midAngleDeg * PI / 180f).toFloat()

                    val textRadius = radius * 0.65f
                    val textX = center.x + textRadius * cos(midAngleRad)
                    val textY = center.y + textRadius * sin(midAngleRad)

                    val normAngle = (midAngleDeg % 360f + 360f) % 360f
                    val rotDeg = if (normAngle > 0f && normAngle < 180f) midAngleDeg - 90f else midAngleDeg + 90f

                    drawContext.canvas.nativeCanvas.apply {
                        save()
                        rotate(rotDeg, textX, textY)

                        // Outline stroke for maximum readability on any color background
                        val numberStroke = android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#44000000")
                            style = android.graphics.Paint.Style.STROKE
                            strokeWidth = 6f
                            textSize = radius * 0.17f
                            isFakeBoldText = true
                            isAntiAlias = true
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        drawText(segment.amountText, textX, textY - 2f, numberStroke)

                        // Solid White Number Fill
                        val numberFill = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            style = android.graphics.Paint.Style.FILL
                            textSize = radius * 0.17f
                            isFakeBoldText = true
                            isAntiAlias = true
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        drawText(segment.amountText, textX, textY - 2f, numberFill)

                        // Subtitle Label (e.g. STARS, MEGA, SUPER)
                        val labelPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#FFFDE7")
                            textSize = radius * 0.085f
                            isFakeBoldText = true
                            isAntiAlias = true
                            textAlign = android.graphics.Paint.Align.CENTER
                            setShadowLayer(4f, 0f, 1f, android.graphics.Color.parseColor("#88000000"))
                        }
                        drawText(segment.subText, textX, textY + radius * 0.10f, labelPaint)

                        restore()
                    }
                }

                // Draw central decorative hub with cute Star
                drawCircle(
                    color = Color(0xFFFFD700),
                    radius = radius * 0.24f,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF1E88E5),
                    radius = radius * 0.19f,
                    center = center
                )

                drawContext.canvas.nativeCanvas.apply {
                    val starPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = radius * 0.20f
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    drawText("⭐", center.x, center.y + radius * 0.07f, starPaint)
                }
            }

            // Fixed Pointer Arrow at top
            Canvas(
                modifier = Modifier
                    .size(38.dp)
                    .align(Alignment.TopCenter)
            ) {
                val path = Path().apply {
                    moveTo(size.width / 2f, size.height)
                    lineTo(2f, 0f)
                    lineTo(size.width - 2f, 0f)
                    close()
                }
                drawPath(path, color = Color(0xFFFFD700))
                drawPath(
                    path,
                    color = Color.White,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Spin Button
        val isSpinAllowed = canSpin && !hasJustSpun && !isSpinning

        Button(
            onClick = {
                if (isSpinAllowed) {
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
                        hasJustSpun = true

                        if (winner.starReward >= 500) {
                            soundManager?.playMegaStarCelebration()
                        } else {
                            soundManager?.playWinFanfare()
                        }
                        onRewardEarned(winner)
                    }
                }
            },
            enabled = isSpinAllowed,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF6D00),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFCFD8DC),
                disabledContentColor = Color(0xFF78909C)
            ),
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(26.dp))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (isSpinAllowed) Color.White else Color(0xFF78909C),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        isSpinning -> "SPINNING... 🎈"
                        !canSpin || hasJustSpun -> "CLAIMED TODAY! 🎁"
                        else -> "SPIN FOR STARS! ⭐"
                    },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        winningSegment?.let { winner ->
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .border(2.dp, Color(0xFF00C853), RoundedCornerShape(18.dp))
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "🎉 YAY! You won ${winner.amountText} Stars! ⭐",
                    color = Color(0xFF1B5E20),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }

        if (!canSpin || hasJustSpun) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Kids get 1 fun star surprise every day!\nCome back tomorrow for your next gift! 🎁",
                color = Color(0xFF0D47A1),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}
