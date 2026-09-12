package com.example.model

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val targetCount: Int,
    val currentCount: Int,
    val rewardCoins: Int,
    val rewardXp: Int,
    val isClaimed: Boolean = false
) {
    val isCompleted: Boolean get() = currentCount >= targetCount

    companion object {
        val DEFAULT_ACHIEVEMENTS = listOf(
            Achievement(
                id = "first_win",
                title = "First Victory",
                description = "Win your first Bingo game",
                iconName = "trophy",
                targetCount = 1,
                currentCount = 0,
                rewardCoins = 100,
                rewardXp = 50
            ),
            Achievement(
                id = "bingo_master_5",
                title = "Bingo Apprentice",
                description = "Win 5 Bingo games",
                iconName = "star",
                targetCount = 5,
                currentCount = 0,
                rewardCoins = 250,
                rewardXp = 150
            ),
            Achievement(
                id = "bingo_master_20",
                title = "Bingo Champion",
                description = "Win 20 Bingo games",
                iconName = "crown",
                targetCount = 20,
                currentCount = 0,
                rewardCoins = 1000,
                rewardXp = 500
            ),
            Achievement(
                id = "daub_50",
                title = "Daub Enthusiast",
                description = "Mark 50 numbers on your cards",
                iconName = "touch",
                targetCount = 50,
                currentCount = 0,
                rewardCoins = 150,
                rewardXp = 75
            ),
            Achievement(
                id = "full_house",
                title = "Full House Legend",
                description = "Clear an entire card with Full House",
                iconName = "grid",
                targetCount = 1,
                currentCount = 0,
                rewardCoins = 500,
                rewardXp = 300
            ),
            Achievement(
                id = "spin_wheel_3",
                title = "Lucky Spinner",
                description = "Spin the Lucky Wheel 3 times",
                iconName = "wheel",
                targetCount = 3,
                currentCount = 0,
                rewardCoins = 200,
                rewardXp = 100
            )
        )
    }
}
