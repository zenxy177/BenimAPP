package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.*
import com.example.ui.MainViewModel
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class SchoolSubTab {
    HOMEWORKS,
    EXAMS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val homeworks by viewModel.allHomeworks.collectAsState()
    val exams by viewModel.allExams.collectAsState()
    val homeworkFilter by viewModel.homeworkTypeFilter.collectAsState()

    var activeSubTab by remember { mutableStateOf(SchoolSubTab.HOMEWORKS) }
    var showCompletedHomeworks by remember { mutableStateOf(false) }

    var showAddHomeworkDialog by remember { mutableStateOf(false) }
    var showAddExamDialog by remember { mutableStateOf(false) }

    val filteredHomeworks = remember(homeworks, homeworkFilter, showCompletedHomeworks) {
        homeworks.filter { hw ->
            val matchesType = homeworkFilter == null || hw.type == homeworkFilter
            val matchesStatus = showCompletedHomeworks || !hw.isCompleted
            matchesType && matchesStatus
        }
    }

    val pendingCount = homeworks.count { !it.isCompleted }
    val examCount = exams.size

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (activeSubTab == SchoolSubTab.HOMEWORKS) {
                        showAddHomeworkDialog = true
                    } else {
                        showAddExamDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("school_fab_add")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (activeSubTab == SchoolSubTab.HOMEWORKS) "Ödev Ekle" else "Sınav Ekle"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Okul & Akademik Takip",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Ödevler, performans, projeler ve sınav takvimi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Sub Tab Segmented Bar
            PrimaryTabRow(
                selectedTabIndex = if (activeSubTab == SchoolSubTab.HOMEWORKS) 0 else 1,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeSubTab == SchoolSubTab.HOMEWORKS,
                    onClick = { activeSubTab = SchoolSubTab.HOMEWORKS },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ödev & Projeler ($pendingCount)")
                        }
                    }
                )
                Tab(
                    selected = activeSubTab == SchoolSubTab.EXAMS,
                    onClick = { activeSubTab = SchoolSubTab.EXAMS },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sınav Günleri ($examCount)")
                        }
                    }
                )
            }

            if (activeSubTab == SchoolSubTab.HOMEWORKS) {
                // Homework filter chips
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = homeworkFilter == null,
                            onClick = { viewModel.setHomeworkFilter(null) },
                            label = { Text("Tümü") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = homeworkFilter == HomeworkType.NORMAL,
                            onClick = { viewModel.setHomeworkFilter(HomeworkType.NORMAL) },
                            label = { Text("Normal Ödev") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = homeworkFilter == HomeworkType.PERFORMANS,
                            onClick = { viewModel.setHomeworkFilter(HomeworkType.PERFORMANS) },
                            label = { Text("Performans") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = homeworkFilter == HomeworkType.PROJE,
                            onClick = { viewModel.setHomeworkFilter(HomeworkType.PROJE) },
                            label = { Text("Proje") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = showCompletedHomeworks,
                            onClick = { showCompletedHomeworks = !showCompletedHomeworks },
                            label = { Text("Tamamlananları Göster") },
                            leadingIcon = {
                                if (showCompletedHomeworks) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        )
                    }
                }

                // Homework list
                if (filteredHomeworks.isEmpty()) {
                    EmptyStateCard(
                        icon = Icons.Outlined.AssignmentTurnedIn,
                        title = "Görev Bulunmuyor",
                        subtitle = "Tebrikler! Bekleyen ödeviniz yok veya filtreye uygun ödev bulunamadı.",
                        actionButtonText = "+ Yeni Ödev Ekle",
                        onActionClick = { showAddHomeworkDialog = true }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("homeworks_list"),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        items(filteredHomeworks, key = { it.id }) { hw ->
                            HomeworkItemCard(
                                homework = hw,
                                onToggle = { viewModel.toggleHomework(hw) },
                                onDelete = { viewModel.deleteHomework(hw) },
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            } else {
                // Exams subtab
                if (exams.isEmpty()) {
                    EmptyStateCard(
                        icon = Icons.Outlined.EventNote,
                        title = "Kayıtlı Sınav Yok",
                        subtitle = "Yaklaşan sınavlarınızı, yazılı tarihlerinizi ve hedef notlarınızı ekleyebilirsiniz.",
                        actionButtonText = "+ Yeni Sınav Ekle",
                        onActionClick = { showAddExamDialog = true }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("exams_list"),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        items(exams, key = { it.id }) { exam ->
                            ExamDetailCard(
                                exam = exam,
                                onUpdateGrade = { newGrade -> viewModel.updateExamGrade(exam, newGrade) },
                                onDelete = { viewModel.deleteExam(exam) },
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }

    if (showAddHomeworkDialog) {
        AddHomeworkDialog(
            onDismiss = { showAddHomeworkDialog = false },
            onConfirm = { title, course, type, desc, dueDate, priority ->
                viewModel.addHomework(title, course, type, desc, dueDate, priority)
                showAddHomeworkDialog = false
            }
        )
    }

    if (showAddExamDialog) {
        AddExamDialog(
            onDismiss = { showAddExamDialog = false },
            onConfirm = { course, title, dateMillis, topics, targetGrade ->
                viewModel.addExam(course, title, dateMillis, topics, targetGrade)
                showAddExamDialog = false
            }
        )
    }
}

@Composable
fun HomeworkItemCard(
    homework: HomeworkEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (typeTitle, typeColor) = when (homework.type) {
        HomeworkType.NORMAL -> Pair("Normal Ödev", ColorOdevNormal)
        HomeworkType.PERFORMANS -> Pair("Performans Görevi", ColorOdevPerformans)
        HomeworkType.PROJE -> Pair("Proje Ödevi", ColorOdevProje)
    }

    val (priorityTitle, priorityColor) = when (homework.priority) {
        Priority.DUSUK -> Pair("Düşük", Color(0xFF10B981))
        Priority.ORTA -> Pair("Orta", Color(0xFFF59E0B))
        Priority.YUKSEK -> Pair("Yüksek", Color(0xFFEF4444))
    }

    val now = System.currentTimeMillis()
    val isOverdue = homework.dueDateMillis < now && !homework.isCompleted
    val diffDays = ((homework.dueDateMillis - now) / (24 * 60 * 60 * 1000)).toInt()

    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy, EEEE", Locale("tr")) }
    val dueDateStr = remember(homework.dueDateMillis) { dateFormat.format(Date(homework.dueDateMillis)) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("homework_item_${homework.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (homework.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (homework.isCompleted) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggle,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (homework.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                            contentDescription = "Tamamla",
                            tint = if (homework.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = typeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = typeTitle,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = typeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = priorityColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = priorityTitle,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = priorityColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Sil",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Column(modifier = Modifier.padding(start = 38.dp)) {
                Text(
                    text = homework.course,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = homework.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (homework.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (homework.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )

                if (homework.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = homework.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when {
                            homework.isCompleted -> "Tamamlandı ($dueDateStr)"
                            isOverdue -> "Gecikmiş! ($dueDateStr)"
                            diffDays == 0 -> "Bugün teslim!"
                            diffDays == 1 -> "Yarın teslim!"
                            else -> "$dueDateStr ($diffDays gün kaldı)"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ExamDetailCard(
    exam: ExamEntity,
    onUpdateGrade: (Int?) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val diffMillis = exam.examDateMillis - now
    val daysRemaining = (diffMillis / (24 * 60 * 60 * 1000)).toInt()

    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy - HH:mm", Locale("tr")) }
    val examDateStr = remember(exam.examDateMillis) { dateFormat.format(Date(exam.examDateMillis)) }

    var showGradeDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("exam_item_${exam.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ColorSinav.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = exam.courseName,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = ColorSinav,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (daysRemaining >= 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = when {
                                daysRemaining < 0 -> "Sınav Bitti"
                                daysRemaining == 0 -> "Bugün Sınav!"
                                daysRemaining == 1 -> "Yarın!"
                                else -> "$daysRemaining gün kaldı"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (daysRemaining >= 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Sil",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = exam.examTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = examDateStr,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (exam.topics.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Konular: ${exam.topics}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Target and Actual Grade
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (exam.targetGrade != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "🎯 Hedef: ${exam.targetGrade}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (exam.actualGrade != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "🏆 Alınan: ${exam.actualGrade}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF047857),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                TextButton(
                    onClick = { showGradeDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (exam.actualGrade == null) "Not Gir" else "Notu Güncelle",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }

    if (showGradeDialog) {
        var gradeInput by remember { mutableStateOf(exam.actualGrade?.toString() ?: "") }
        Dialog(onDismissRequest = { showGradeDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "${exam.courseName} Sınav Notu",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = gradeInput,
                        onValueChange = { if (it.length <= 3 && it.all { ch -> ch.isDigit() }) gradeInput = it },
                        label = { Text("Aldığınız Not (0-100)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showGradeDialog = false }) { Text("İptal") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val parsed = gradeInput.toIntOrNull()
                                onUpdateGrade(parsed)
                                showGradeDialog = false
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Kaydet")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddHomeworkDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, course: String, type: HomeworkType, desc: String, dueDate: Long, priority: Priority) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(HomeworkType.NORMAL) }
    var priority by remember { mutableStateOf(Priority.ORTA) }
    var description by remember { mutableStateOf("") }

    val calendar = remember { Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 2) } }
    var selectedDueDateMillis by remember { mutableStateOf(calendar.timeInMillis) }

    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy", Locale("tr")) }
    var dateButtonText by remember { mutableStateOf(dateFormat.format(Date(selectedDueDateMillis))) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Yeni Ödev / Görev Ekle",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Type selector
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    HomeworkType.values().forEach { t ->
                        val isSelected = type == t
                        val label = when (t) {
                            HomeworkType.NORMAL -> "Ödev"
                            HomeworkType.PERFORMANS -> "Performans"
                            HomeworkType.PROJE -> "Proje"
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { type = t },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = course,
                    onValueChange = { course = it },
                    label = { Text("Ders Adı (Örn: Matematik, Fizik)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("hw_input_course")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Ödev Başlığı / Görev") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("hw_input_title")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Açıklama / Sayfalar (Opsiyonel)") },
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Date Picker trigger button
                OutlinedButton(
                    onClick = {
                        val c = Calendar.getInstance().apply { timeInMillis = selectedDueDateMillis }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                c.set(Calendar.YEAR, year)
                                c.set(Calendar.MONTH, month)
                                c.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                selectedDueDateMillis = c.timeInMillis
                                dateButtonText = dateFormat.format(Date(selectedDueDateMillis))
                            },
                            c.get(Calendar.YEAR),
                            c.get(Calendar.MONTH),
                            c.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Teslim Tarihi: $dateButtonText")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Priority
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Öncelik:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Priority.values().forEach { p ->
                            FilterChip(
                                selected = priority == p,
                                onClick = { priority = p },
                                label = {
                                    Text(
                                        when (p) {
                                            Priority.DUSUK -> "Düşük"
                                            Priority.ORTA -> "Orta"
                                            Priority.YUKSEK -> "Yüksek"
                                        },
                                        fontSize = 11.sp
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("İptal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && course.isNotBlank()) {
                                onConfirm(title, course, type, description, selectedDueDateMillis, priority)
                            }
                        },
                        enabled = title.isNotBlank() && course.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("hw_confirm_button")
                    ) {
                        Text("Kaydet")
                    }
                }
            }
        }
    }
}

@Composable
fun AddExamDialog(
    onDismiss: () -> Unit,
    onConfirm: (course: String, title: String, dateMillis: Long, topics: String, targetGrade: Int?) -> Unit
) {
    val context = LocalContext.current
    var course by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var topics by remember { mutableStateOf("") }
    var targetGradeText by remember { mutableStateOf("") }

    val calendar = remember { Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) } }
    var selectedDateMillis by remember { mutableStateOf(calendar.timeInMillis) }

    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy - HH:mm", Locale("tr")) }
    var dateButtonText by remember { mutableStateOf(dateFormat.format(Date(selectedDateMillis))) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Yeni Sınav Ekle",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = course,
                    onValueChange = { course = it },
                    label = { Text("Ders Adı (Örn: Kimya, Biyoloji)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("exam_input_course")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Sınav Başlığı (Örn: 1. Yazılı Sınavı)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("exam_input_title")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = topics,
                    onValueChange = { topics = it },
                    label = { Text("Dahil Konular / Notlar") },
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = targetGradeText,
                    onValueChange = { if (it.length <= 3 && it.all { ch -> ch.isDigit() }) targetGradeText = it },
                    label = { Text("Hedef Not (0-100, opsiyonel)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Date & Time picker
                OutlinedButton(
                    onClick = {
                        val c = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                c.set(Calendar.YEAR, year)
                                c.set(Calendar.MONTH, month)
                                c.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        c.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                        c.set(Calendar.MINUTE, minute)
                                        selectedDateMillis = c.timeInMillis
                                        dateButtonText = dateFormat.format(Date(selectedDateMillis))
                                    },
                                    c.get(Calendar.HOUR_OF_DAY),
                                    c.get(Calendar.MINUTE),
                                    true
                                ).show()
                            },
                            c.get(Calendar.YEAR),
                            c.get(Calendar.MONTH),
                            c.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(dateButtonText, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("İptal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (course.isNotBlank() && title.isNotBlank()) {
                                onConfirm(course, title, selectedDateMillis, topics, targetGradeText.toIntOrNull())
                            }
                        },
                        enabled = course.isNotBlank() && title.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("exam_confirm_button")
                    ) {
                        Text("Kaydet")
                    }
                }
            }
        }
    }
}
