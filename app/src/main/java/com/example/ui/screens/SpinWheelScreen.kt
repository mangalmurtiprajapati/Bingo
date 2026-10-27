package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.BingoTheme
import com.example.model.UserStats
import com.example.sound.SoundManager
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
import com.example.ui.theme.RedPenStrike
import com.example.ui.theme.TournamentGold
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class WheelPrize(
    val label: String,
    val displayAmount: String,
    val displayIcon: String,
    val amount: Int,
    val isGems: Boolean,
    val color: Color
)

@Composable
fun SpinWheelScreen(
    stats: UserStats,
    activeTheme: BingoTheme,
    soundManager: SoundManager?,
    onRewardEarned: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val prizes = listOf(
        WheelPrize("5,000 Coins", "5,000", "🪙", 5000, false, ArcadePrimary),
        WheelPrize("50 Gems", "50", "💎", 50, true, ArcadeTertiary),
        WheelPrize("1,000 XP", "1,000", "⚡", 1000, false, Color(0xFF00897B)),
        WheelPrize("1,500 Coins", "1,500", "💰", 1500, false, ArcadeSecondary),
        WheelPrize("800 Coins", "800", "🪙", 800, false, ArcadeTertiaryContainer),
        WheelPrize("50,000 ⭐", "50K", "🌟", 50000, false, TournamentGold),
        WheelPrize("25 Gems", "25", "💎", 25, true, ArcadePrimaryFixed),
        WheelPrize("2,000 Boost", "2,000", "🎟️", 2000, false, ArcadeSecondaryContainer)
    )

    var currentAngle by remember { mutableFloatStateOf(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var wonPrize by remember { mutableStateOf<WheelPrize?>(null) }
    var showWinDialog by remember { mutableStateOf(false) }

    val animatedAngle by animateFloatAsState(
        targetValue = currentAngle,
        animationSpec = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
        finishedListener = {
            isSpinning = false
            // Calculate slice aligning with top pointer (270 degrees in polar coordinates)
            val winningAngle = (270f - (currentAngle % 360f) + 360f) % 360f
            val index = (winningAngle / 45f).toInt() % prizes.size
            val prize = prizes[index]
            wonPrize = prize
            onRewardEarned(prize.amount)
            showWinDialog = true
            soundManager?.playWinFanfare()
        },
        label = "wheelRotation"
    )

    fun startSpin() {
        if (!isSpinning) {
            isSpinning = true
            soundManager?.playClick()
            val extraSpins = 5 + Random.nextInt(4)
            val randomDegrees = Random.nextFloat() * 360f
            currentAngle += extraSpins * 360f + randomDegrees
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ArcadeBackground,
        topBar = {
            ArcadeTopHeader(
                title = "Lucky Wheel",
                subtitle = "Daily Fortune",
                coins = stats.coins,
                gems = 120,
                onBack = onBack
            )
        },
        bottomBar = {
            ArcadeBottomNavigationBar(
                currentRoute = "spin_wheel",
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
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Free Spin Ready Alert / Balance Status Ribbon
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerHigh)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(ArcadeSecondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Daily Fortune",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ArcadeOnSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ArcadeSecondaryFixed)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "FREE READY",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = ArcadeSecondary
                                        )
                                    }
                                }
                                Text(
                                    text = "Daily reset in 14h 22m",
                                    fontSize = 11.sp,
                                    color = ArcadeOnSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = { },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(ArcadeSurfaceContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Rules",
                                tint = ArcadeOnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // 2. Daily Streak & Multiplier Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadeInverseSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = ArcadeSecondaryFixed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "5-Day Streak Active!",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ArcadeSecondaryFixed
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ArcadeTertiaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "2.5x MULTIPLIER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }

                        Text(
                            text = "Log in consecutive days to supercharge all wheel prizes.",
                            fontSize = 12.sp,
                            color = ArcadeInverseOnSurface.copy(alpha = 0.8f)
                        )

                        // 7-Day Dot Timeline
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (day in 1..7) {
                                val isPast = day < 5
                                val isToday = day == 5
                                val isChest = day == 7

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isToday -> ArcadePrimaryContainer
                                                    isPast -> ArcadeSecondaryContainer
                                                    isChest -> ArcadeTertiary
                                                    else -> ArcadeSurfaceContainerHigh
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        when {
                                            isPast -> Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            isChest -> Icon(
                                                imageVector = Icons.Default.Redeem,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            isToday -> Text(
                                                text = "⚡",
                                                fontSize = 16.sp
                                            )
                                            else -> Text(
                                                text = "${day}x",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ArcadeOnSurface
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isToday) "TODAY" else if (isChest) "CHEST" else "D$day",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isToday) ArcadePrimaryFixed else ArcadeInverseOnSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Centerpiece: Rotating Lucky Wheel Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .clip(CircleShape)
                            .shadow(8.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer rim with metallic pegs
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val rimRadius = size.width / 2f
                            drawCircle(color = ArcadeInverseSurface)
                            drawCircle(
                                color = ArcadeSurfaceContainer,
                                radius = rimRadius - 4.dp.toPx(),
                                style = Stroke(width = 8.dp.toPx())
                            )
                            // 16 golden perimeter pegs
                            val pegDistance = rimRadius - 4.dp.toPx()
                            for (p in 0 until 16) {
                                val angleDeg = p * (360f / 16f)
                                val angleRad = Math.toRadians(angleDeg.toDouble())
                                val px = center.x + (pegDistance * cos(angleRad)).toFloat()
                                val py = center.y + (pegDistance * sin(angleRad)).toFloat()
                                drawCircle(
                                    color = TournamentGold,
                                    radius = 3.5.dp.toPx(),
                                    center = Offset(px, py)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 1.2.dp.toPx(),
                                    center = Offset(px - 1f, py - 1f)
                                )
                            }
                        }

                        val textPaint = remember {
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                typeface = android.graphics.Typeface.DEFAULT_BOLD
                                textAlign = android.graphics.Paint.Align.RIGHT
                                setShadowLayer(4f, 1f, 1f, android.graphics.Color.argb(180, 0, 0, 0))
                            }
                        }
                        val iconPaint = remember {
                            android.graphics.Paint().apply {
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        }

                        // Rotating 8-slice wheel with numbers & icons
                        Canvas(
                            modifier = Modifier
                                .size(280.dp)
                                .rotate(animatedAngle)
                        ) {
                            val sliceAngle = 45f
                            val radius = size.width / 2f
                            textPaint.textSize = 14.sp.toPx()
                            iconPaint.textSize = 15.sp.toPx()

                            for (i in 0 until 8) {
                                val startA = i * sliceAngle
                                drawArc(
                                    color = prizes[i].color,
                                    startAngle = startA,
                                    sweepAngle = sliceAngle,
                                    useCenter = true
                                )
                                // Divider line
                                drawArc(
                                    color = Color.White.copy(alpha = 0.5f),
                                    startAngle = startA,
                                    sweepAngle = sliceAngle,
                                    useCenter = true,
                                    style = Stroke(width = 1.5.dp.toPx())
                                )
                            }

                            // Render amount and icon text on each slice
                            drawIntoCanvas { canvas ->
                                val native = canvas.nativeCanvas

                                for (i in 0 until 8) {
                                    val midAngle = i * sliceAngle + 22.5f
                                    native.save()
                                    native.rotate(midAngle, center.x, center.y)

                                    val amountX = center.x + radius - 14.dp.toPx()
                                    val textY = center.y + 5.dp.toPx()
                                    native.drawText(prizes[i].displayAmount, amountX, textY, textPaint)

                                    val iconX = center.x + radius * 0.44f
                                    native.drawText(prizes[i].displayIcon, iconX, textY, iconPaint)

                                    native.restore()
                                }
                            }
                        }

                        // Center Hub Button
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(ArcadeSurfaceContainerLowest)
                                .clickable(enabled = !isSpinning) { startSpin() }
                                .shadow(6.dp, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(ArcadePrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Casino,
                                    contentDescription = "Spin",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    // Authentic downward-pointing Casino Pointer Needle
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .size(width = 30.dp, height = 34.dp)
                            .shadow(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val needlePath = Path().apply {
                                moveTo(size.width / 2f, size.height) // sharp tip at bottom
                                lineTo(size.width, 6.dp.toPx())
                                lineTo(size.width * 0.8f, 0f)
                                lineTo(size.width * 0.2f, 0f)
                                lineTo(0f, 6.dp.toPx())
                                close()
                            }
                            drawPath(needlePath, color = RedPenStrike)
                            drawPath(needlePath, color = TournamentGold, style = Stroke(width = 2.dp.toPx()))
                            drawCircle(
                                color = TournamentGold,
                                radius = 5.dp.toPx(),
                                center = Offset(size.width / 2f, 9.dp.toPx())
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = Offset(size.width / 2f, 9.dp.toPx())
                            )
                        }
                    }
                }

                // 4. Spin Controls Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { startSpin() },
                        enabled = !isSpinning,
                        colors = ButtonDefaults.buttonColors(containerColor = ArcadePrimaryContainer),
                        shape = RoundedCornerShape(26.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(4.dp, RoundedCornerShape(26.dp))
                            .testTag("spin_now_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSpinning) "SPINNING..." else "SPIN NOW! (FREE)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { startSpin() },
                            enabled = !isSpinning,
                            colors = ButtonDefaults.buttonColors(containerColor = ArcadeSurfaceContainerHigh),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Diamond,
                                contentDescription = null,
                                tint = ArcadeTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Super Spin (20 💎)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ArcadeOnSurface)
                        }

                        Button(
                            onClick = { startSpin() },
                            enabled = !isSpinning,
                            colors = ButtonDefaults.buttonColors(containerColor = ArcadeSurfaceContainerHigh),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = ArcadePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Watch Ad (+1 Spin)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ArcadeOnSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal: Win Reward
    if (showWinDialog && wonPrize != null) {
        Dialog(onDismissRequest = { showWinDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(22.dp),
                color = ArcadeSurfaceContainerLowest,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(TournamentGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Celebration,
                            contentDescription = null,
                            tint = TournamentGold,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "PRIZE CLAIMED!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = ArcadeSecondary
                    )

                    Text(
                        text = "You won ${wonPrize?.label}! Multiplier x2.5 added to your bank.",
                        fontSize = 14.sp,
                        color = ArcadeOnSurface,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = { showWinDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = ArcadePrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Awesome!", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
