package com.example.test_ai

import java.time.LocalDate

enum class AttendanceStatus(val label: String) {
    PRESENT("Присутствует"),
    EXCUSED("Уважительная"),
    UNEXCUSED("Не уважительная"),
    SICK("Больничный")
}

data class Student(
    val id: Int,
    val name: String,
    val attendanceHistory: Map<LocalDate, AttendanceStatus> = emptyMap()
) {
    fun getStatusOn(date: LocalDate): AttendanceStatus = attendanceHistory[date] ?: AttendanceStatus.UNEXCUSED
}
