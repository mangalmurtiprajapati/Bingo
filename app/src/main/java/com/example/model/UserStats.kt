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

    fun canSpinWheelToday(): Boolean {
        if (lastDailyRewardTime == 0L) return true
        val lastCal = java.util.Calendar.getInstance().apply { timeInMillis = lastDailyRewardTime }
        val nowCal = java.util.Calendar.getInstance()
        val isSameDay = lastCal.get(java.util.Calendar.YEAR) == nowCal.get(java.util.Calendar.YEAR) &&
                lastCal.get(java.util.Calendar.DAY_OF_YEAR) == nowCal.get(java.util.Calendar.DAY_OF_YEAR)
        return !isSameDay
    }

    fun getRemainingTimeUntilNextSpinMs(): Long {
        if (lastDailyRewardTime == 0L) return 0L
        val nextSpinCal = java.util.Calendar.getInstance().apply {
            timeInMillis = lastDailyRewardTime
            add(java.util.Calendar.DAY_OF_YEAR, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val diff = nextSpinCal.timeInMillis - System.currentTimeMillis()
        return diff.coerceAtLeast(0L)
    }

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
