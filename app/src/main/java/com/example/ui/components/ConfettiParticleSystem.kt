package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Color,
    var size: Float,
    var rotation: Float,
    var vRot: Float,
    var alpha: Float,
    var shape: ParticleShape
)

enum class ParticleShape {
    CIRCLE,
    RECTANGLE,
    STAR,
    COIN
}

@Composable
fun ConfettiParticleSystem(
    modifier: Modifier = Modifier,
    isTriggered: Boolean = true,
    particleCount: Int = 100
) {
    val particles = remember { mutableStateListOf<Particle>() }

    LaunchedEffect(isTriggered) {
        if (isTriggered) {
            particles.clear()
            val colors = listOf(
                Color(0xFFFFD700), // Gold
                Color(0xFFFF4081), // Pink
                Color(0xFF00E5FF), // Cyan
                Color(0xFF7C4DFF), // Purple
                Color(0xFF76FF03), // Lime
                Color(0xFFFF9100)  // Orange
            )
            for (i in 0 until particleCount) {
                particles.add(
                    Particle(
                        x = Random.nextFloat(), // 0.0 to 1.0 fraction of screen
                        y = -0.05f - Random.nextFloat() * 0.2f, // start above top
                        vx = (Random.nextFloat() - 0.5f) * 0.005f,
                        vy = 0.008f + Random.nextFloat() * 0.015f,
                        color = colors.random(),
                        size = 12f + Random.nextFloat() * 18f,
                        rotation = Random.nextFloat() * 360f,
                        vRot = (Random.nextFloat() - 0.5f) * 10f,
                        alpha = 1.0f,
                        shape = ParticleShape.entries.toTypedArray().random()
                    )
                )
            }

            while (particles.isNotEmpty()) {
                withFrameNanos {
                    for (p in particles) {
                        p.x += p.vx
                        p.y += p.vy
                        p.vy += 0.0001f // gravity
                        p.rotation += p.vRot
                        if (p.y > 0.8f) {
                            p.alpha -= 0.02f
                        }
                    }
                    particles.removeAll { it.y > 1.2f || it.alpha <= 0f }
                }
            }
        }
    }

    if (particles.isNotEmpty()) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            for (p in particles) {
                val px = p.x * width
                val py = p.y * height
                drawParticle(p, px, py)
            }
        }
    }
}

private fun DrawScope.drawParticle(p: Particle, px: Float, py: Float) {
    val color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f))
    when (p.shape) {
        ParticleShape.CIRCLE -> {
            drawCircle(color = color, radius = p.size / 2f, center = Offset(px, py))
        }
        ParticleShape.RECTANGLE -> {
            drawRect(
                color = color,
                topLeft = Offset(px - p.size / 2f, py - p.size / 2f),
                size = Size(p.size, p.size * 0.6f)
            )
        }
        ParticleShape.COIN -> {
            drawCircle(color = Color(0xFFFFD700).copy(alpha = p.alpha), radius = p.size / 1.8f, center = Offset(px, py))
            drawCircle(color = Color(0xFFDAA520).copy(alpha = p.alpha), radius = p.size / 2.8f, center = Offset(px, py))
        }
        ParticleShape.STAR -> {
            drawCircle(color = color, radius = p.size / 2.2f, center = Offset(px, py))
        }
    }
}
