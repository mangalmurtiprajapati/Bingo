package com.example.model

import androidx.compose.ui.graphics.Color

enum class ThemeId {
    CLASSIC,
    ROYAL_GOLD,
    GALAXY,
    NEON,
    DARK,
    CANDY,
    WOOD,
    PREMIUM
}

data class BingoTheme(
    val id: ThemeId,
    val name: String,
    val description: String,
    val isUnlockedByDefault: Boolean = false,
    val unlockCostCoins: Int = 0,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val cardBgColor: Color,
    val cellDefaultBg: Color,
    val cellMarkedBg: Color,
    val textPrimary: Color,
    val headerBgGradients: List<Color>,
    val backgroundGradients: List<Color>
) {
    companion object {
        val ALL_THEMES = listOf(
            BingoTheme(
                id = ThemeId.CLASSIC,
                name = "Sunny Playground",
                description = "Bright cheerful sky blue with sunshine yellow",
                isUnlockedByDefault = true,
                primaryColor = Color(0xFF0288D1),
                secondaryColor = Color(0xFF03A9F4),
                accentColor = Color(0xFFFF9800),
                cardBgColor = Color(0xFFFFFFFF),
                cellDefaultBg = Color(0xFFE1F5FE),
                cellMarkedBg = Color(0xFFFF7043),
                textPrimary = Color(0xFF0D47A1),
                headerBgGradients = listOf(Color(0xFF0288D1), Color(0xFF03A9F4)),
                backgroundGradients = listOf(Color(0xFFE1F5FE), Color(0xFFB3E5FC), Color(0xFF81D4FA))
            ),
            BingoTheme(
                id = ThemeId.CANDY,
                name = "Candy Pop",
                description = "Sweet pastel strawberry, cotton candy & balloons",
                isUnlockedByDefault = true,
                primaryColor = Color(0xFFEC407A),
                secondaryColor = Color(0xFFFF80AB),
                accentColor = Color(0xFF00E676),
                cardBgColor = Color(0xFFFFFFFF),
                cellDefaultBg = Color(0xFFFCE4EC),
                cellMarkedBg = Color(0xFFFF4081),
                textPrimary = Color(0xFF880E4F),
                headerBgGradients = listOf(Color(0xFFEC407A), Color(0xFFFF4081)),
                backgroundGradients = listOf(Color(0xFFFFF0F5), Color(0xFFFFE4E1), Color(0xFFFFCDD2))
            ),
            BingoTheme(
                id = ThemeId.ROYAL_GOLD,
                name = "Super Star",
                description = "Warm golden sunshine with rainbow stars",
                unlockCostCoins = 300,
                primaryColor = Color(0xFFFF8F00),
                secondaryColor = Color(0xFFFFB300),
                accentColor = Color(0xFF1E88E5),
                cardBgColor = Color(0xFFFFFFFF),
                cellDefaultBg = Color(0xFFFFF8E1),
                cellMarkedBg = Color(0xFFFFB300),
                textPrimary = Color(0xFFE65100),
                headerBgGradients = listOf(Color(0xFFFF8F00), Color(0xFFFFB300)),
                backgroundGradients = listOf(Color(0xFFFFF9C4), Color(0xFFFFF176), Color(0xFFFFE082))
            ),
            BingoTheme(
                id = ThemeId.GALAXY,
                name = "Space Rockets",
                description = "Friendly cartoon galaxy with twinkling stars",
                unlockCostCoins = 500,
                primaryColor = Color(0xFF7E57C2),
                secondaryColor = Color(0xFF9575CD),
                accentColor = Color(0xFFFFCA28),
                cardBgColor = Color(0xFFFFFFFF),
                cellDefaultBg = Color(0xFFEDE7F6),
                cellMarkedBg = Color(0xFF7E57C2),
                textPrimary = Color(0xFF4527A0),
                headerBgGradients = listOf(Color(0xFF5E35B1), Color(0xFF7E57C2)),
                backgroundGradients = listOf(Color(0xFFEDE7F6), Color(0xFFD1C4E9), Color(0xFFB39DDB))
            ),
            BingoTheme(
                id = ThemeId.NEON,
                name = "Happy Safari",
                description = "Lush green treehouse with cheerful animal friends",
                unlockCostCoins = 750,
                primaryColor = Color(0xFF43A047),
                secondaryColor = Color(0xFF66BB6A),
                accentColor = Color(0xFFFF7043),
                cardBgColor = Color(0xFFFFFFFF),
                cellDefaultBg = Color(0xFFE8F5E9),
                cellMarkedBg = Color(0xFF43A047),
                textPrimary = Color(0xFF1B5E20),
                headerBgGradients = listOf(Color(0xFF2E7D32), Color(0xFF43A047)),
                backgroundGradients = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFFA5D6A7))
            ),
            BingoTheme(
                id = ThemeId.DARK,
                name = "Ocean Splash",
                description = "Playful dolphin sea adventure with crystal water",
                unlockCostCoins = 1000,
                primaryColor = Color(0xFF00ACC1),
                secondaryColor = Color(0xFF26C6DA),
                accentColor = Color(0xFFFF5252),
                cardBgColor = Color(0xFFFFFFFF),
                cellDefaultBg = Color(0xFFE0F7FA),
                cellMarkedBg = Color(0xFF00ACC1),
                textPrimary = Color(0xFF006064),
                headerBgGradients = listOf(Color(0xFF00838F), Color(0xFF00ACC1)),
                backgroundGradients = listOf(Color(0xFFE0F7FA), Color(0xFFB2EBF2), Color(0xFF80DEEA))
            ),
            BingoTheme(
                id = ThemeId.WOOD,
                name = "Dino Explorer",
                description = "Friendly baby dinosaur prehistoric playground",
                unlockCostCoins = 1200,
                primaryColor = Color(0xFFFB8C00),
                secondaryColor = Color(0xFFFFA726),
                accentColor = Color(0xFF43A047),
                cardBgColor = Color(0xFFFFFFFF),
                cellDefaultBg = Color(0xFFFFF3E0),
                cellMarkedBg = Color(0xFFFB8C00),
                textPrimary = Color(0xFFE65100),
                headerBgGradients = listOf(Color(0xFFEF6C00), Color(0xFFFB8C00)),
                backgroundGradients = listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2), Color(0xFFFFCC80))
            ),
            BingoTheme(
                id = ThemeId.PREMIUM,
                name = "Magic Kingdom",
                description = "Enchanted fairytale castle with sparkles",
                unlockCostCoins = 1500,
                primaryColor = Color(0xFF8E24AA),
                secondaryColor = Color(0xFFAB47BC),
                accentColor = Color(0xFFFFD600),
                cardBgColor = Color(0xFFFFFFFF),
                cellDefaultBg = Color(0xFFF3E5F5),
                cellMarkedBg = Color(0xFF8E24AA),
                textPrimary = Color(0xFF4A148C),
                headerBgGradients = listOf(Color(0xFF6A1B9A), Color(0xFF8E24AA)),
                backgroundGradients = listOf(Color(0xFFF3E5F5), Color(0xFFE1BEE7), Color(0xFFCE93D8))
            )
        )

        fun getTheme(id: ThemeId): BingoTheme {
            return ALL_THEMES.find { it.id == id } ?: ALL_THEMES.first()
        }
    }
}
