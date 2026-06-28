package com.example.test_ai

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test_ai.ui.theme.Test_aiTheme
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Test_aiTheme {
                AppNavigator()
            }
        }
    }
}

@Composable
fun AppNavigator() {
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000)
        showSplash = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AttendanceScreen()
        
        AnimatedVisibility(
            visible = showSplash,
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            SplashScreen()
        }
    }
}

@Composable
fun SplashScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.School,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "ОГТИ",
            style = MaterialTheme.typography.displayMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Журнал посещаемости",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(48.dp))
        CircularProgressIndicator(color = Color.White)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(viewModel: AttendanceViewModel = viewModel()) {
    val groups by viewModel.groups.collectAsState()
    val currentGroupName by viewModel.currentGroupName.collectAsState()
    val students by viewModel.students.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    
    var newStudentName by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showGroupDialog by remember { mutableStateOf(false) }
    var showAddGroupDialog by remember { mutableStateOf(false) }
    var showEasterEgg by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }
    var statsMonth by remember { mutableIntStateOf(selectedDate.monthValue) }
    var statsYear by remember { mutableIntStateOf(selectedDate.year) }
    
    val presentCount = students.count { it.getStatusOn(selectedDate) == AttendanceStatus.PRESENT }
    
    // Пасхалка: если все присутствуют и их больше нуля
    LaunchedEffect(presentCount, students.size) {
        if (students.isNotEmpty() && presentCount == students.size) {
            showEasterEgg = true
        }
    }
    
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { showGroupDialog = true }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(currentGroupName, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        Text(
                            text = selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showGroupDialog = true }) {
                        Icon(Icons.Default.Groups, contentDescription = "Группы")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        statsMonth = selectedDate.monthValue
                        statsYear = selectedDate.year
                        showStatsDialog = true 
                    }) {
                        Icon(Icons.Default.BarChart, contentDescription = "Статистика")
                    }
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.Event, contentDescription = "Выбрать дату")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val report = viewModel.generateReport(selectedDate)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Отчет ОГТИ - $currentGroupName")
                        putExtra(Intent.EXTRA_TEXT, report)
                    }
                    context.startActivity(Intent.createChooser(intent, "Отправить отчет"))
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Share, contentDescription = "Поделиться")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Поле добавления студента
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newStudentName,
                        onValueChange = { newStudentName = it },
                        placeholder = { Text("ФИО студента") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (newStudentName.isNotBlank()) {
                                viewModel.addStudent(newStudentName)
                                newStudentName = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить")
                    }
                }
            }

            // Статистика
            val presentCount = students.count { it.getStatusOn(selectedDate) == AttendanceStatus.PRESENT }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Студенты",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "$presentCount / ${students.size}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 15.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(students) { student ->
                    StudentItem(
                        student = student,
                        date = selectedDate,
                        onStatusChange = { status -> 
                            viewModel.updateAttendanceStatus(student.id, selectedDate, status) 
                        },
                        onDelete = { viewModel.removeStudent(student.id) }
                    )
                }
            }
        }

        // Подпись разработчика
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "Разработал Никита Скрыпников",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }

        // Диалоги
        if (showDatePicker) {
            DatePickerModal(
                onDateSelected = { date ->
                    date?.let { viewModel.selectDate(it) }
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }

        if (showGroupDialog) {
            AlertDialog(
                onDismissRequest = { showGroupDialog = false },
                title = { Text("Выберите группу") },
                text = {
                    LazyColumn {
                        items(groups) { groupName ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectGroup(groupName)
                                        showGroupDialog = false
                                    }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    groupName,
                                    fontWeight = if (groupName == currentGroupName) FontWeight.Bold else FontWeight.Normal,
                                    color = if (groupName == currentGroupName) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (groups.size > 1) {
                                    IconButton(onClick = { viewModel.deleteGroup(groupName) }) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(25.dp))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAddGroupDialog = true }) {
                        Text("Добавить группу")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGroupDialog = false }) {
                        Text("Закрыть")
                    }
                }
            )
        }

        if (showAddGroupDialog) {
            var groupNameInput by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAddGroupDialog = false },
                title = { Text("Новая группа") },
                text = {
                    OutlinedTextField(
                        value = groupNameInput,
                        onValueChange = { groupNameInput = it },
                        label = { Text("Название группы") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.addGroup(groupNameInput)
                        showAddGroupDialog = false
                        showGroupDialog = false
                    }) {
                        Text("Создать")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddGroupDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }

        if (showEasterEgg) {
            AlertDialog(
                onDismissRequest = { showEasterEgg = false },
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎉 ", fontSize = 24.sp)
                        Text("Идеально!")
                    }
                },
                text = {
                    Text("Невероятно! Вся группа З-24ИВТ(б) в сборе. Это поистине исторический момент!")
                },
                confirmButton = {
                    Button(onClick = { showEasterEgg = false }) {
                        Text("Ура!")
                    }
                }
            )
        }

        if (showStatsDialog) {
            val stats = viewModel.getMonthlyStats(currentGroupName, java.time.Month.of(statsMonth), statsYear)
            AlertDialog(
                onDismissRequest = { showStatsDialog = false },
                title = { 
                    Column {
                        Text("Посещаемость", style = MaterialTheme.typography.titleLarge)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                if (statsMonth == 1) {
                                    statsMonth = 12
                                    statsYear--
                                } else statsMonth--
                            }) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = null)
                            }
                            
                            Text(
                                text = "${java.time.Month.of(statsMonth).getDisplayName(java.time.format.TextStyle.FULL_STANDALONE, java.util.Locale("ru")).replaceFirstChar { it.uppercase() }} $statsYear",
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            IconButton(onClick = {
                                if (statsMonth == 12) {
                                    statsMonth = 1
                                    statsYear++
                                } else statsMonth++
                            }) {
                                Icon(Icons.Default.ChevronRight, contentDescription = null)
                            }
                        }
                    }
                },
                text = {
                    if (stats.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            Text("Нет данных за этот месяц", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                            items(stats.toList()) { (name, percentage) ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(name, style = MaterialTheme.typography.bodyMedium)
                                        Text("$percentage%", fontWeight = FontWeight.Bold)
                                    }
                                    LinearProgressIndicator(
                                        progress = { percentage / 100f },
                                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                        color = if (percentage > 80) Color(0xFF4CAF50) else if (percentage > 50) Color(0xFFFFC107) else Color(0xFFF44336)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showStatsDialog = false }) {
                        Text("Закрыть")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (LocalDate?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val selectedDate = datePickerState.selectedDateMillis?.let {
                    java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                }
                onDateSelected(selectedDate)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun StudentItem(
    student: Student,
    date: LocalDate,
    onStatusChange: (AttendanceStatus) -> Unit,
    onDelete: () -> Unit
) {
    val currentStatus = student.getStatusOn(date)
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (currentStatus == AttendanceStatus.PRESENT) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        onStatusChange(if (currentStatus == AttendanceStatus.PRESENT) AttendanceStatus.UNEXCUSED else AttendanceStatus.PRESENT)
                    }
                ) {
                    Icon(
                        if (currentStatus == AttendanceStatus.PRESENT) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (currentStatus == AttendanceStatus.PRESENT) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
                Text(
                    text = student.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (currentStatus == AttendanceStatus.PRESENT) FontWeight.Bold else FontWeight.Normal
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                }
            }

            if (currentStatus != AttendanceStatus.PRESENT) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currentStatus.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Box {
                        AssistChip(
                            onClick = { showMenu = true },
                            label = { Text("Причина", fontSize = 10.sp) },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp)) }
                        )
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            AttendanceStatus.entries.filter { it != AttendanceStatus.PRESENT }.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status.label) },
                                    onClick = {
                                        onStatusChange(status)
                                        showMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
