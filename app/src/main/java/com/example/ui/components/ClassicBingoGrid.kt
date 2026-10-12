package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BingoTheme
import com.example.model.ClassicBingoBoard

@Composable
fun ClassicBingoGrid(
    board: ClassicBingoBoard,
    theme: BingoTheme,
    isSetupMode: Boolean,
    enabled: Boolean = true,
    lastCutNumber: Int? = null,
    selectedLineIndices: Set<Pair<Int, Int>>? = null,
    onCellClick: (row: Int, col: Int, number: Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, Color(0xFFFFD700).copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            .padding(2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (r in 0 until 5) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (c in 0 until 5) {
                        val number = board.cells.getOrNull(r)?.getOrNull(c)
                        val isCrossed = number != null && board.isNumberCrossed(number)
                        val isInSelectedLine = selectedLineIndices?.contains(Pair(r, c)) == true
                        val isInCompletedLine = isCrossed && (isInSelectedLine || (selectedLineIndices == null && board.isCellInCompletedLine(r, c)))
                        val isLastCut = number != null && number == lastCutNumber

                        val tileBg by animateColorAsState(
                            targetValue = when {
                                isInSelectedLine -> Color(0xFF00E676)
                                isInCompletedLine -> Color(0xFFFFD700)
                                isLastCut -> Color(0xFFFF4081)
                                isCrossed -> Color(0xFFFFEBEE)
                                number != null -> Color(0xFFFFFFFF)
                                else -> Color(0xFF334155) // Clean single slate tile for empty cell
                            },
                            label = "tileBg_$r$c"
                        )

                        val tileBorderColor = when {
                            isInSelectedLine -> Color(0xFF00C853)
                            isInCompletedLine -> Color(0xFFFFC107)
                            isLastCut -> Color(0xFFFF4081)
                            isCrossed -> Color(0xFFE53935)
                            number != null -> Color(0xFF38BDF8)
                            else -> Color(0xFF475569)
                        }

                        val textColor = when {
                            isInSelectedLine -> Color(0xFF003300)
                            isInCompletedLine -> Color(0xFF10002B)
                            isCrossed -> Color(0xFFB71C1C)
                            number != null -> Color(0xFF0F172A)
                            else -> Color(0xFF94A3B8)
                        }

                        // Clean single-surface tile (NO nested box, NO inner shadows)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(tileBg)
                                .border(
                                    width = if (isLastCut || isInCompletedLine) 2.5.dp else if (number != null) 2.dp else 1.2.dp,
                                    color = tileBorderColor,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable(enabled = enabled) {
                                    onCellClick(r, c, number)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (number != null) {
                                Text(
                                    text = number.toString(),
                                    color = textColor,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            } else if (isSetupMode) {
                                // Clean subtle slot indicator
                                Text(
                                    text = "${r * 5 + c + 1}",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Prominent Pen/Chalk Cross Mark (✕) across the box
                            if (isCrossed) {
                                Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                                    val strokeW = 3.dp.toPx()
                                    val crossColor = if (isInCompletedLine) Color(0xFFD50000) else Color(0xFFFF1744)

                                    // Line 1: Top-Left to Bottom-Right (\)
                                    drawLine(
                                        color = crossColor,
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = strokeW,
                                        cap = StrokeCap.Round
                                    )

                                    // Line 2: Top-Right to Bottom-Left (/)
                                    drawLine(
                                        color = crossColor,
                                        start = Offset(size.width, 0f),
                                        end = Offset(0f, size.height),
                                        strokeWidth = strokeW,
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
}
