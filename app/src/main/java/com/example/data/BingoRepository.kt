package com.example.data

import com.example.model.Achievement
import com.example.model.ThemeId
import com.example.model.UserStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BingoRepository(private val dao: BingoDao) {

    val userStats: Flow<UserStats> = dao.getUserStatsFlow().map { entity ->
        entity?.toDomain() ?: UserStats()
    }

    val achievements: Flow<List<Achievement>> = dao.getAllAchievementsFlow().map { list ->
        if (list.isEmpty()) {
            Achievement.DEFAULT_ACHIEVEMENTS
        } else {
            list.map { it.toDomain() }
        }
    }

    suspend fun ensureInitialized() {
        val statsEntity = dao.getUserStatsDirect()
        if (statsEntity == null) {
            dao.insertOrUpdateUserStats(UserStatsEntity.fromDomain(UserStats()))
        }
        val currentAchievements = dao.getAllAchievementsFlow()
        // We can check and populate default achievements
        val defaultList = Achievement.DEFAULT_ACHIEVEMENTS.map { AchievementEntity.fromDomain(it) }
        dao.insertAchievements(defaultList)
    }

    suspend fun saveUserStats(stats: UserStats) {
        dao.insertOrUpdateUserStats(UserStatsEntity.fromDomain(stats))
    }

    suspend fun addCoins(amount: Int) {
        val current = getCurrentStats()
        saveUserStats(current.copy(coins = (current.coins + amount).coerceAtLeast(0)))
    }

    suspend fun recordDailySpin(amount: Int) {
        val current = getCurrentStats()
        saveUserStats(
            current.copy(
                coins = (current.coins + amount).coerceAtLeast(0),
                lastDailyRewardTime = System.currentTimeMillis()
            )
        )
    }

    suspend fun addXpAndCoins(xpAmount: Int, coinsAmount: Int) {
        val current = getCurrentStats()
        val updatedWithXp = current.addXp(xpAmount)
        saveUserStats(updatedWithXp.copy(coins = (updatedWithXp.coins + coinsAmount).coerceAtLeast(0)))
    }

    suspend fun recordGameFinished(isWin: Boolean, daubsMade: Int, coinsEarned: Int) {
        val current = getCurrentStats()
        val wins = if (isWin) current.totalWins + 1 else current.totalWins
        val losses = if (!isWin) current.totalLosses + 1 else current.totalLosses
        val currentStreak = if (isWin) current.currentStreak + 1 else 0
        val bestStreak = maxOf(current.bestStreak, currentStreak)
        val games = current.totalGamesPlayed + 1
        val daubs = current.totalDaubs + daubsMade

        val updated = current.copy(
            totalGamesPlayed = games,
            totalWins = wins,
            totalLosses = losses,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            totalDaubs = daubs
        ).addXp(if (isWin) 100 else 25)

        saveUserStats(updated.copy(coins = updated.coins + coinsEarned))

        // Update achievements progress
        updateAchievement("first_win", wins)
        updateAchievement("bingo_master_5", wins)
        updateAchievement("bingo_master_20", wins)
        updateAchievement("daub_50", daubs)
    }

    suspend fun selectTheme(themeId: ThemeId) {
        val current = getCurrentStats()
        if (current.unlockedThemeIds.contains(themeId)) {
            saveUserStats(current.copy(selectedThemeId = themeId))
        }
    }

    suspend fun unlockTheme(themeId: ThemeId, costCoins: Int): Boolean {
        val current = getCurrentStats()
        if (current.coins >= costCoins && !current.unlockedThemeIds.contains(themeId)) {
            val updatedUnlocked = current.unlockedThemeIds + themeId
            saveUserStats(
                current.copy(
                    coins = current.coins - costCoins,
                    unlockedThemeIds = updatedUnlocked,
                    selectedThemeId = themeId
                )
            )
            return true
        }
        return false
    }

    suspend fun updateAchievement(id: String, count: Int) {
        dao.updateAchievementProgress(id, count)
    }

    suspend fun claimAchievementReward(id: String, rewardCoins: Int, rewardXp: Int) {
        dao.claimAchievement(id)
        addXpAndCoins(rewardXp, rewardCoins)
    }

    suspend fun resetProgress() {
        val fresh = UserStats()
        saveUserStats(fresh)
        val defaultList = Achievement.DEFAULT_ACHIEVEMENTS.map { AchievementEntity.fromDomain(it) }
        dao.insertAchievements(defaultList)
    }

    suspend fun updateUserProfile(name: String, provider: String, avatar: String) {
        val current = getCurrentStats()
        saveUserStats(
            current.copy(
                userName = name,
                loginProvider = provider,
                userAvatar = avatar
            )
        )
    }

    suspend fun getCurrentStats(): UserStats {
        return dao.getUserStatsDirect()?.toDomain() ?: UserStats()
    }
}
