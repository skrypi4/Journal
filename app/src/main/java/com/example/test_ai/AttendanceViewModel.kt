package com.example.test_ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AttendanceDatabase.getDatabase(application).attendanceDao()

    val groups = dao.getAllGroups()
        .map { list -> list.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentGroupName = MutableStateFlow("З-24ИВТ(б)")
    val currentGroupName: StateFlow<String> = _currentGroupName.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val students = _currentGroupName.flatMapLatest { groupName ->
        dao.getStudentsByGroup(groupName).map { list -> 
            list.map { it.toDomain() }.sortedBy { it.name } 
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    init {
        viewModelScope.launch {
            dao.insertGroup(GroupEntity("З-24ИВТ(б)"))
            // Начальные студенты, если группа пуста
            val currentStudents = dao.getStudentsByGroup("З-24ИВТ(б)").first()
            if (currentStudents.isEmpty()) {
                val initialStudents = listOf(
                    Student(0, "Савченко Екатерина"),
                    Student(0, "Скрыпников Никита"),
                    Student(0, "Тимербаев Марат"),
                    Student(0, "Трубалетов Никита")
                )
                initialStudents.forEach { 
                    dao.insertStudent(it.toEntity("З-24ИВТ(б)")) 
                }
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun selectGroup(name: String) {
        _currentGroupName.value = name
    }

    fun addGroup(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            dao.insertGroup(GroupEntity(name))
            _currentGroupName.value = name
        }
    }
    
    fun deleteGroup(name: String) {
        viewModelScope.launch {
            dao.deleteGroup(name)
            dao.deleteStudentsByGroup(name)
            if (_currentGroupName.value == name) {
                val allGroups = dao.getAllGroups().first()
                if (allGroups.isNotEmpty()) {
                    _currentGroupName.value = allGroups.first().name
                }
            }
        }
    }

    fun updateAttendanceStatus(studentId: Int, date: LocalDate, status: AttendanceStatus) {
        val groupName = _currentGroupName.value
        viewModelScope.launch {
            val studentList = dao.getStudentsByGroup(groupName).first()
            val studentEntity = studentList.find { it.id == studentId }
            studentEntity?.let {
                val updatedHistory = it.attendanceHistory + (date to status)
                dao.updateStudent(it.copy(attendanceHistory = updatedHistory))
            }
        }
    }

    fun addStudent(name: String) {
        if (name.isBlank()) return
        val groupName = _currentGroupName.value
        viewModelScope.launch {
            dao.insertStudent(Student(0, name).toEntity(groupName))
        }
    }

    fun removeStudent(studentId: Int) {
        viewModelScope.launch {
            dao.deleteStudent(studentId)
        }
    }

    fun getMonthlyStats(groupName: String, month: java.time.Month, year: Int): Map<String, Int> {
        val studentList = if (groupName == _currentGroupName.value) students.value else emptyList()
        
        // Получаем общее количество рабочих дней (например, пн-пт) в месяце до сегодняшнего дня
        val today = LocalDate.now()
        val daysInMonth = java.time.YearMonth.of(year, month).lengthOfMonth()
        val totalDaysToCount = (1..daysInMonth).count { day ->
            val date = LocalDate.of(year, month, day)
            // Считаем только прошедшие или сегодняшние дни, исключая выходные (сб, вс)
            !date.isAfter(today) && 
            date.dayOfWeek != java.time.DayOfWeek.SATURDAY && 
            date.dayOfWeek != java.time.DayOfWeek.SUNDAY
        }.coerceAtLeast(1) // Чтобы не делить на ноль
        
        return studentList.associate { student ->
            val presentDays = student.attendanceHistory.count { (date, status) ->
                date.month == month && 
                date.year == year && 
                status == AttendanceStatus.PRESENT
            }
            
            val percentage = (presentDays * 100) / totalDaysToCount
            student.name to percentage.coerceAtMost(100)
        }
    }

    fun generateReport(date: LocalDate): String {
        val groupName = _currentGroupName.value
        val studentList = students.value
        val dateStr = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        val presentCount = studentList.count { it.getStatusOn(date) == AttendanceStatus.PRESENT }
        
        val sb = StringBuilder()
        sb.append("Отчет о посещаемости ОГТИ\n")
        sb.append("Группа: $groupName\n")
        sb.append("Дата: $dateStr\n")
        sb.append("Присутствует: $presentCount из ${studentList.size}\n\n")
        
        studentList.forEachIndexed { index, student ->
            val status = student.getStatusOn(date)
            val statusIcon = if (status == AttendanceStatus.PRESENT) "[+]" else "[-] "
            val reason = if (status != AttendanceStatus.PRESENT) " (${status.label})" else ""
            sb.append("${index + 1}. $statusIcon ${student.name}$reason\n")
        }
        
        return sb.toString()
    }
}
