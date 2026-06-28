package com.example.test_ai

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromAttendanceHistory(value: Map<LocalDate, AttendanceStatus>): String {
        val map = value.mapKeys { it.key.toString() }
        return gson.toJson(map)
    }

    @TypeConverter
    fun toAttendanceHistory(value: String): Map<LocalDate, AttendanceStatus> {
        val type = object : TypeToken<Map<String, AttendanceStatus>>() {}.type
        val map = gson.fromJson<Map<String, AttendanceStatus>>(value, type) ?: emptyMap()
        return map.mapKeys { LocalDate.parse(it.key) }
    }
}
