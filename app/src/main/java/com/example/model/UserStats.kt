package com.example.model

data class UserStats(
    val coins: Int = 500, // Initial bonus coins
    val level: Int = 1,
    val xp: Int = 0,
    val totalGamesPlayed: Int = 0,
    val totalWins: Int = 0,
    val totalLosses: Int = 0,
    val totalDaubs: Int = 0,
    val bestStreak: Int = 0,
    val currentStreak: Int = 0,
    val selectedThemeId: ThemeId = ThemeId.CLASSIC,
    val unlockedThemeIds: List<ThemeId> = listOf(ThemeId.CLASSIC, ThemeId.ROYAL_GOLD),
    val isSoundEnabled: Boolean = true,
    val isMusicEnabled: Boolean = true,
    val isDarkMode: Boolean = false,
    val isAutoDaubEnabled: Boolean = false,
    val lastDailyRewardTime: Long = 0L,
    val dailyRewardStreak: Int = 0,
    val isAdMobTestEnabled: Boolean = true
) {
    val xpForNextLevel: Int get() = level * 250

    fun addXp(addedXp: Int): UserStats {
        var newXp = xp + addedXp
        var newLevel = level
        while (newXp >= newLevel * 250) {
            newXp -= newLevel * 250
            newLevel++
        }
        return copy(xp = newXp, level = newLevel)
    }
}
