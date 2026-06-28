package com.example.test_ai

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupName: String,
    val name: String,
    val attendanceHistory: Map<LocalDate, AttendanceStatus>
)

fun StudentEntity.toDomain(): Student = Student(
    id = id,
    name = name,
    attendanceHistory = attendanceHistory
)

fun Student.toEntity(groupName: String): StudentEntity = StudentEntity(
    id = id,
    groupName = groupName,
    name = name,
    attendanceHistory = attendanceHistory
)
