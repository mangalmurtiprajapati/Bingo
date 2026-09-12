package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Achievement

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val targetCount: Int,
    val currentCount: Int,
    val rewardCoins: Int,
    val rewardXp: Int,
    val isClaimed: Boolean
) {
    fun toDomain(): Achievement {
        return Achievement(
            id = id,
            title = title,
            description = description,
            iconName = iconName,
            targetCount = targetCount,
            currentCount = currentCount,
            rewardCoins = rewardCoins,
            rewardXp = rewardXp,
            isClaimed = isClaimed
        )
    }

    companion object {
        fun fromDomain(a: Achievement): AchievementEntity {
            return AchievementEntity(
                id = a.id,
                title = a.title,
                description = a.description,
                iconName = a.iconName,
                targetCount = a.targetCount,
                currentCount = a.currentCount,
                rewardCoins = a.rewardCoins,
                rewardXp = a.rewardXp,
                isClaimed = a.isClaimed
            )
        }
    }
}
