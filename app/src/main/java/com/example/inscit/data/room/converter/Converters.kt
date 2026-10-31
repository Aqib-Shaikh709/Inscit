package com.example.inscit.data.room.converter

import androidx.room.TypeConverter
import com.example.inscit.models.GoalType
import com.example.inscit.models.Lang
import com.example.inscit.models.ThemeMode
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {

    val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    // ---- GoalType <-> String ----
    @TypeConverter
    fun fromGoalType(type: GoalType): String = type.name

    @TypeConverter
    fun toGoalType(name: String): GoalType = GoalType.valueOf(name)

    // ---- Lang <-> String ----
    @TypeConverter
    fun fromLang(lang: Lang): String = lang.name

    @TypeConverter
    fun toLang(name: String): Lang = Lang.valueOf(name)

    // ---- ThemeMode <-> String ----
    @TypeConverter
    fun fromThemeMode(mode: ThemeMode): String = mode.name

    @TypeConverter
    fun toThemeMode(name: String): ThemeMode = ThemeMode.valueOf(name)

    // ---- Map<String, Int> <-> String (for dailyXp) ----
    @TypeConverter
    fun fromDailyXp(map: Map<String, Int>): String = json.encodeToString(map)

    @TypeConverter
    fun toDailyXp(jsonStr: String): Map<String, Int> =
        if (jsonStr.isNullOrEmpty()) emptyMap()
        else json.decodeFromString(jsonStr)

    // ---- Map<String, Float> <-> String (for domainScores) ----
    @TypeConverter
    fun fromDomainScores(map: Map<String, Float>): String = json.encodeToString(map)

    @TypeConverter
    fun toDomainScores(jsonStr: String): Map<String, Float> =
        if (jsonStr.isNullOrEmpty()) emptyMap()
        else json.decodeFromString(jsonStr)

    // ---- List<String> <-> String (for strengths/weaknesses) ----
    @TypeConverter
    fun fromStringList(list: List<String>): String = json.encodeToString(list)

    @TypeConverter
    fun toStringList(jsonStr: String): List<String> =
        if (jsonStr.isNullOrEmpty()) emptyList()
        else json.decodeFromString(jsonStr)
}
