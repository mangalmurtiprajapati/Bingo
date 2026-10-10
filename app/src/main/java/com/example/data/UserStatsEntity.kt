package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.ThemeId
import com.example.model.UserStats

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 500,
    val level: Int = 1,
    val xp: Int = 0,
    val totalGamesPlayed: Int = 0,
    val totalWins: Int = 0,
    val totalLosses: Int = 0,
    val totalDaubs: Int = 0,
    val bestStreak: Int = 0,
    val currentStreak: Int = 0,
    val selectedThemeId: String = ThemeId.CLASSIC.name,
    val unlockedThemeIds: String = "${ThemeId.CLASSIC.name},${ThemeId.ROYAL_GOLD.name}",
    val isSoundEnabled: Boolean = true,
    val isMusicEnabled: Boolean = true,
    val isDarkMode: Boolean = false,
    val isAutoDaubEnabled: Boolean = false,
    val lastDailyRewardTime: Long = 0L,
    val dailyRewardStreak: Int = 0,
    val isAdMobTestEnabled: Boolean = true,
    val userName: String = "MindPlayer_77",
    val loginProvider: String = "GUEST",
    val userAvatar: String = "🎯",
    val playerId: String = "MB-7429"
) {
    fun toDomain(): UserStats {
        val themesList = unlockedThemeIds.split(",")
            .mapNotNull { name -> runCatching { ThemeId.valueOf(name.trim()) }.getOrNull() }
            .ifEmpty { listOf(ThemeId.CLASSIC, ThemeId.ROYAL_GOLD) }

        return UserStats(
            coins = coins,
            level = level,
            xp = xp,
            totalGamesPlayed = totalGamesPlayed,
            totalWins = totalWins,
            totalLosses = totalLosses,
            totalDaubs = totalDaubs,
            bestStreak = bestStreak,
            currentStreak = currentStreak,
            selectedThemeId = runCatching { ThemeId.valueOf(selectedThemeId) }.getOrDefault(ThemeId.CLASSIC),
            unlockedThemeIds = themesList,
            isSoundEnabled = isSoundEnabled,
            isMusicEnabled = isMusicEnabled,
            isDarkMode = isDarkMode,
            isAutoDaubEnabled = isAutoDaubEnabled,
            lastDailyRewardTime = lastDailyRewardTime,
            dailyRewardStreak = dailyRewardStreak,
            isAdMobTestEnabled = isAdMobTestEnabled,
            userName = userName,
            loginProvider = loginProvider,
            userAvatar = userAvatar,
            playerId = playerId
        )
    }

    companion object {
        fun fromDomain(stats: UserStats): UserStatsEntity {
            return UserStatsEntity(
                id = 1,
                coins = stats.coins,
                level = stats.level,
                xp = stats.xp,
                totalGamesPlayed = stats.totalGamesPlayed,
                totalWins = stats.totalWins,
                totalLosses = stats.totalLosses,
                totalDaubs = stats.totalDaubs,
                bestStreak = stats.bestStreak,
                currentStreak = stats.currentStreak,
                selectedThemeId = stats.selectedThemeId.name,
                unlockedThemeIds = stats.unlockedThemeIds.joinToString(",") { it.name },
                isSoundEnabled = stats.isSoundEnabled,
                isMusicEnabled = stats.isMusicEnabled,
                isDarkMode = stats.isDarkMode,
                isAutoDaubEnabled = stats.isAutoDaubEnabled,
                lastDailyRewardTime = stats.lastDailyRewardTime,
                dailyRewardStreak = stats.dailyRewardStreak,
                isAdMobTestEnabled = stats.isAdMobTestEnabled,
                userName = stats.userName,
                loginProvider = stats.loginProvider,
                userAvatar = stats.userAvatar,
                playerId = stats.playerId
            )
        }
    }
}
