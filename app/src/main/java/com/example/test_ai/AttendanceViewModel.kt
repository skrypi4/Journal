package com.example.test_ai

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AttendanceViewModel : ViewModel() {
    private val _groups = MutableStateFlow<Map<String, List<Student>>>(
        mapOf(
            "З-24ИВТ(б)" to listOf(
                Student(1, "Савченко Екатерина"),
                Student(2, "Скрыпников Никита"),
                Student(3, "Тимербаев Марат"),
                Student(4, "Трубалетов Никита")
            ).sortedBy { it.name }
        )
    )
    val groups: StateFlow<Map<String, List<Student>>> = _groups.asStateFlow()

    private val _currentGroupName = MutableStateFlow("З-24ИВТ(б)")
    val currentGroupName: StateFlow<String> = _currentGroupName.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun selectGroup(name: String) {
        _currentGroupName.value = name
    }

    fun addGroup(name: String) {
        if (name.isBlank() || _groups.value.containsKey(name)) return
        _groups.update { it + (name to emptyList()) }
        _currentGroupName.value = name
    }
    
    fun deleteGroup(name: String) {
        if (_groups.value.size <= 1) return
        _groups.update { it - name }
        if (_currentGroupName.value == name) {
            _currentGroupName.value = _groups.value.keys.first()
        }
    }

    fun updateAttendanceStatus(studentId: Int, date: LocalDate, status: AttendanceStatus) {
        val groupName = _currentGroupName.value
        _groups.update { allGroups ->
            val updatedList = allGroups[groupName]?.map { student ->
                if (student.id == studentId) {
                    student.copy(
                        attendanceHistory = student.attendanceHistory + (date to status)
                    )
                } else {
                    student
                }
            } ?: emptyList()
            allGroups + (groupName to updatedList)
        }
    }

    fun addStudent(name: String) {
        if (name.isBlank()) return
        val groupName = _currentGroupName.value
        _groups.update { allGroups ->
            val currentList = allGroups[groupName] ?: emptyList()
            val newId = (allGroups.values.flatten().maxOfOrNull { it.id } ?: 0) + 1
            val updatedList = (currentList + Student(newId, name)).sortedBy { it.name }
            allGroups + (groupName to updatedList)
        }
    }

    fun removeStudent(studentId: Int) {
        val groupName = _currentGroupName.value
        _groups.update { allGroups ->
            val updatedList = allGroups[groupName]?.filter { it.id != studentId } ?: emptyList()
            allGroups + (groupName to updatedList)
        }
    }

    fun generateReport(date: LocalDate): String {
        val groupName = _currentGroupName.value
        val studentList = _groups.value[groupName] ?: emptyList()
        val dateStr = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        val presentCount = studentList.count { it.getStatusOn(date) == AttendanceStatus.PRESENT }
        
        val sb = StringBuilder()
        sb.append("Отчет о посещаемости ОГТИ\n")
        sb.append("Группа: $groupName\n")
        sb.append("Дата: $dateStr\n")
        sb.append("Присутствует: $presentCount из ${studentList.size}\n\n")
        
        studentList.forEachIndexed { index, student ->
            val status = student.getStatusOn(date)
            val statusIcon = if (status == AttendanceStatus.PRESENT) "[+]" else "[-]"
            val reason = if (status != AttendanceStatus.PRESENT) " (${status.label})" else ""
            sb.append("${index + 1}. $statusIcon ${student.name}$reason\n")
        }
        
        return sb.toString()
    }
}
