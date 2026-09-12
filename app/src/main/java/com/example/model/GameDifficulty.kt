package com.example.model

enum class GameDifficulty(val displayName: String, val intervalMs: Long, val coinMultiplier: Double) {
    EASY("Easy (4s)", 4000L, 1.0),
    MEDIUM("Medium (2.5s)", 2500L, 1.5),
    HARD("Hard (1.5s)", 1500L, 2.0)
}
