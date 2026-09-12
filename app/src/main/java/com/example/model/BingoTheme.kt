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
                name = "Classic Blue",
                description = "Traditional casino bingo look",
                isUnlockedByDefault = true,
                primaryColor = Color(0xFF1E88E5),
                secondaryColor = Color(0xFF1565C0),
                accentColor = Color(0xFFFFD54F),
                cardBgColor = Color(0xFFFFFFFF),
                cellDefaultBg = Color(0xFFE3F2FD),
                cellMarkedBg = Color(0xFF1E88E5),
                textPrimary = Color(0xFF0D47A1),
                headerBgGradients = listOf(Color(0xFF1976D2), Color(0xFF0D47A1)),
                backgroundGradients = listOf(Color(0xFF0D47A1), Color(0xFF1565C0), Color(0xFF1E88E5))
            ),
            BingoTheme(
                id = ThemeId.ROYAL_GOLD,
                name = "Royal Gold",
                description = "Luxurious gold and deep purple theme",
                isUnlockedByDefault = true,
                primaryColor = Color(0xFFFFD700),
                secondaryColor = Color(0xFFDAA520),
                accentColor = Color(0xFFFF4081),
                cardBgColor = Color(0xFF2A004E),
                cellDefaultBg = Color(0xFF4A148C),
                cellMarkedBg = Color(0xFFFFD700),
                textPrimary = Color(0xFFFFF8E1),
                headerBgGradients = listOf(Color(0xFFFFD700), Color(0xFFB8860B)),
                backgroundGradients = listOf(Color(0xFF1A0033), Color(0xFF330066), Color(0xFF4C0099))
            ),
            BingoTheme(
                id = ThemeId.GALAXY,
                name = "Galaxy Deep",
                description = "Cosmic neon stars and deep space",
                unlockCostCoins = 500,
                primaryColor = Color(0xFF7C4DFF),
                secondaryColor = Color(0xFF651FFF),
                accentColor = Color(0xFF00E5FF),
                cardBgColor = Color(0xFF12123B),
                cellDefaultBg = Color(0xFF1F1F54),
                cellMarkedBg = Color(0xFF00E5FF),
                textPrimary = Color(0xFFE0F7FA),
                headerBgGradients = listOf(Color(0xFF651FFF), Color(0xFF311B92)),
                backgroundGradients = listOf(Color(0xFF0B0B26), Color(0xFF161642), Color(0xFF2E1B5B))
            ),
            BingoTheme(
                id = ThemeId.NEON,
                name = "Neon Cyber",
                description = "Vibrant electric neon night vibe",
                unlockCostCoins = 1000,
                primaryColor = Color(0xFF00FFCC),
                secondaryColor = Color(0xFFFF007F),
                accentColor = Color(0xFFFFE600),
                cardBgColor = Color(0xFF0F0F1A),
                cellDefaultBg = Color(0xFF1A1A2E),
                cellMarkedBg = Color(0xFFFF007F),
                textPrimary = Color(0xFFFFFFFF),
                headerBgGradients = listOf(Color(0xFFFF007F), Color(0xFF7900FF)),
                backgroundGradients = listOf(Color(0xFF05050D), Color(0xFF0F0F23), Color(0xFF1A0033))
            ),
            BingoTheme(
                id = ThemeId.DARK,
                name = "Midnight Stealth",
                description = "Sleek dark mode for eye comfort",
                unlockCostCoins = 1200,
                primaryColor = Color(0xFF90CAF9),
                secondaryColor = Color(0xFF64B5F6),
                accentColor = Color(0xFFFFB74D),
                cardBgColor = Color(0xFF1E1E1E),
                cellDefaultBg = Color(0xFF2C2C2C),
                cellMarkedBg = Color(0xFF1976D2),
                textPrimary = Color(0xFFEEEEEE),
                headerBgGradients = listOf(Color(0xFF212121), Color(0xFF000000)),
                backgroundGradients = listOf(Color(0xFF121212), Color(0xFF1E1E1E), Color(0xFF232323))
            ),
            BingoTheme(
                id = ThemeId.CANDY,
                name = "Candy Pop",
                description = "Sweet pastel shades and fun energy",
                unlockCostCoins = 1500,
                primaryColor = Color(0xFFFF80AB),
                secondaryColor = Color(0xFFFF4081),
                accentColor = Color(0xFF1DE9B6),
                cardBgColor = Color(0xFFFFF0F5),
                cellDefaultBg = Color(0xFFFFE4E1),
                cellMarkedBg = Color(0xFFFF4081),
                textPrimary = Color(0xFF880E4F),
                headerBgGradients = listOf(Color(0xFFFF80AB), Color(0xFFC51162)),
                backgroundGradients = listOf(Color(0xFF880E4F), Color(0xFFAD1457), Color(0xFFD81B60))
            ),
            BingoTheme(
                id = ThemeId.WOOD,
                name = "Rustic Mahogany",
                description = "Classic warm wooden tabletop style",
                unlockCostCoins = 2000,
                primaryColor = Color(0xFFFFB300),
                secondaryColor = Color(0xFFFF8F00),
                accentColor = Color(0xFF8D6E63),
                cardBgColor = Color(0xFF3E2723),
                cellDefaultBg = Color(0xFF4E342E),
                cellMarkedBg = Color(0xFFFF8F00),
                textPrimary = Color(0xFFFFECB3),
                headerBgGradients = listOf(Color(0xFF5D4037), Color(0xFF3E2723)),
                backgroundGradients = listOf(Color(0xFF1B0000), Color(0xFF2C1609), Color(0xFF3E2723))
            ),
            BingoTheme(
                id = ThemeId.PREMIUM,
                name = "Emerald Empire",
                description = "Ultra high-roller emerald VIP theme",
                unlockCostCoins = 3000,
                primaryColor = Color(0xFF00E676),
                secondaryColor = Color(0xFF00B0FF),
                accentColor = Color(0xFFFFD700),
                cardBgColor = Color(0xFF00221A),
                cellDefaultBg = Color(0xFF00382B),
                cellMarkedBg = Color(0xFF00E676),
                textPrimary = Color(0xFFE0F2F1),
                headerBgGradients = listOf(Color(0xFF00C853), Color(0xFF00796B)),
                backgroundGradients = listOf(Color(0xFF001A12), Color(0xFF00291D), Color(0xFF003324))
            )
        )

        fun getTheme(id: ThemeId): BingoTheme {
            return ALL_THEMES.find { it.id == id } ?: ALL_THEMES.first()
        }
    }
}
