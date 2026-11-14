package com.example.inscit.xp

enum class Rank(val label: String, val iconAsset: String, val threshold: Int) {
    BEGINNER(label = "Beginner", iconAsset = "ranks/rank_ begginer.svg", threshold = 0),
    EXPLORER(label = "Explorer", iconAsset = "ranks/rank explorer.svg", threshold = 150),
    SCIENTIST(label = "Scientist", iconAsset = "ranks/rank _ scientist.svg", threshold = 300),
    GENIUS(label = "Genius", iconAsset = "ranks/rank_ genius.svg", threshold = 600),
    MASTER(label = "Master", iconAsset = "ranks/rank _ master.svg", threshold = 1000),
    CHAMP(label = "Champ", iconAsset = "ranks/rank_ champ.svg", threshold = 1500),
    MENACE(label = "Menace", iconAsset = "ranks/rank _ menace.svg", threshold = 2000);

    companion object {
        fun fromXp(xp: Int): Rank {
            return Rank.entries.findLast { xp >= it.threshold } ?: BEGINNER
        }
    }
}

object XpManager {
    fun calculateLevel(xp: Int): Int {
        return (xp / 100) + 1
    }
}
