package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.*
import com.example.ui.GradeAverageSummary
import com.example.ui.MainViewModel
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val SCHOOL_TR_LOCALE: Locale = Locale.forLanguageTag("tr")

enum class SchoolSubTab {
    HOMEWORKS,
    EXAMS,
    NOTES_AND_AVERAGE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val homeworks by viewModel.allHomeworks.collectAsStateWithLifecycle()
    val exams by viewModel.allExams.collectAsStateWithLifecycle()
    val studyNotes by viewModel.allStudyNotes.collectAsStateWithLifecycle()
    val gradeSummary by viewModel.gradeAverageSummary.collectAsStateWithLifecycle()
    val homeworkFilter by viewModel.homeworkTypeFilter.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf(SchoolSubTab.HOMEWORKS) }
    var showCompletedHomeworks by remember { mutableStateOf(false) }

    var showAddHomeworkDialog by remember { mutableStateOf(false) }
    var showAddExamDialog by remember { mutableStateOf(false) }
    var showAddStudyNoteDialog by remember { mutableStateOf(false) }

    val filteredHomeworks = remember(homeworks, homeworkFilter, showCompletedHomeworks) {
        homeworks.filter { hw ->
            val matchesType = homeworkFilter == null || hw.type == homeworkFilter
            val matchesStatus = showCompletedHomeworks || !hw.isCompleted
            matchesType && matchesStatus
        }
    }

    val pendingCount = remember(homeworks) { homeworks.count { !it.isCompleted } }
    val examCount = exams.size

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (activeSubTab) {
                        SchoolSubTab.HOMEWORKS -> showAddHomeworkDialog = true
                        SchoolSubTab.EXAMS -> showAddExamDialog = true
                        SchoolSubTab.NOTES_AND_AVERAGE -> showAddStudyNoteDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(14.dp),
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                modifier = Modifier
                    .padding(bottom = 64.dp)
                    .testTag("school_fab_add")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = when (activeSubTab) {
                        SchoolSubTab.HOMEWORKS -> "Ödev Ekle"
                        SchoolSubTab.EXAMS -> "Sınav Ekle"
                        SchoolSubTab.NOTES_AND_AVERAGE -> "Ezber Kartı Ekle"
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Okul & Akademik Takip",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Ödevler, sınavlar, takdir hesaplama ve ezber kartları",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            PrimaryTabRow(
                selectedTabIndex = when (activeSubTab) {
                    SchoolSubTab.HOMEWORKS -> 0
                    SchoolSubTab.EXAMS -> 1
                    SchoolSubTab.NOTES_AND_AVERAGE -> 2
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeSubTab == SchoolSubTab.HOMEWORKS,
                    onClick = { activeSubTab = SchoolSubTab.HOMEWORKS },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Assignment,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ödevler ($pendingCount)", fontSize = 12.sp)
                        }
                    },
                    modifier = Modifier.testTag("tab_school_homeworks")
                )
                Tab(
                    selected = activeSubTab == SchoolSubTab.EXAMS,
                    onClick = { activeSubTab = SchoolSubTab.EXAMS },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sınavlar ($examCount)", fontSize = 12.sp)
                        }
                    },
                    modifier = Modifier.testTag("tab_school_exams")
                )
                Tab(
                    selected = activeSubTab == SchoolSubTab.NOTES_AND_AVERAGE,
                    onClick = { activeSubTab = SchoolSubTab.NOTES_AND_AVERAGE },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Not & Kart (${studyNotes.size})", fontSize = 12.sp)
                        }
                    },
                    modifier = Modifier.testTag("tab_school_notes_average")
                )
            }

            when (activeSubTab) {
                SchoolSubTab.HOMEWORKS -> {
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                                label = { Text("Tamamlananlar") },
                                leadingIcon = {
                                    if (showCompletedHomeworks) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }

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
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
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
                                Spacer(modifier = Modifier.height(76.dp))
                            }
                        }
                    }
                }

                SchoolSubTab.EXAMS -> {
                    if (exams.isEmpty()) {
                        EmptyStateCard(
                            icon = Icons.AutoMirrored.Outlined.EventNote,
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
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            items(exams, key = { it.id }) { exam ->
                                ExamDetailCard(
                                    exam = exam,
                                    onUpdateGrade = { newGrade -> viewModel.updateExamGrade(exam, newGrade) },
                                    onDelete = { viewModel.deleteExam(exam) },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(76.dp))
                            }
                        }
                    }
                }

                SchoolSubTab.NOTES_AND_AVERAGE -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("notes_and_average_list"),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        item(key = "grade_average_card") {
                            GradeAverageSummaryCard(
                                summary = gradeSummary,
                                onNavigateToExams = { activeSubTab = SchoolSubTab.EXAMS }
                            )
                        }

                        item(key = "flashcards_header") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp, bottom = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Ezber Kartları & Ders Notları",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "Cevabı görmek veya gizlemek için karta dokunun",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (studyNotes.isEmpty()) {
                            item(key = "empty_study_notes") {
                                EmptyStateCard(
                                    icon = Icons.Outlined.Style,
                                    title = "Henüz Ezber Kartı Yok",
                                    subtitle = "Formüller, önemli tarihler veya ders özetleri için hızlı ezber kartları oluşturabilirsiniz.",
                                    actionButtonText = "+ Ezber Kartı Ekle",
                                    onActionClick = { showAddStudyNoteDialog = true }
                                )
                            }
                        } else {
                            items(studyNotes, key = { it.id }) { note ->
                                StudyNoteFlashcardItem(
                                    note = note,
                                    onDelete = { viewModel.deleteStudyNote(note) },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(76.dp))
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

    if (showAddStudyNoteDialog) {
        AddStudyNoteDialog(
            onDismiss = { showAddStudyNoteDialog = false },
            onConfirm = { course, title, content ->
                viewModel.addStudyNote(course, title, content)
                showAddStudyNoteDialog = false
            }
        )
    }
}

@Composable
fun GradeAverageSummaryCard(
    summary: GradeAverageSummary,
    onNavigateToExams: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedAverage = remember(summary.averageScore) {
        String.format(Locale.US, "%.1f", summary.averageScore)
    }
    val progress = (summary.averageScore / 100f).coerceIn(0f, 1f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("grade_average_summary_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Takdir / Teşekkür Hesaplayıcı",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = summary.certificateStatus,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Genel Sınav & Hedef Ortalaması",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formattedAverage,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Notu Girilen Sınav: ${summary.gradedExamsCount}/${summary.totalExamsCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (summary.averageScore >= 85f) {
                            "Takdir barajını geçtiniz! 🌟"
                        } else if (summary.averageScore >= 70f) {
                            "Takdir için +${String.format(Locale.US, "%.1f", summary.pointsToNextTier)} puan lazım"
                        } else {
                            "Teşekkür için +${String.format(Locale.US, "%.1f", (70f - summary.averageScore).coerceAtLeast(0f))} puan lazım"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Barajlar: 50 Geçer • 70 Teşekkür • 85 Takdir",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = onNavigateToExams,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Notları Düzenle", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun StudyNoteFlashcardItem(
    note: StudyNoteEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isAnswerVisible by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { isAnswerVisible = !isAnswerVisible }
            .testTag("study_note_item_${note.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            1.dp,
            if (isAnswerVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = note.courseName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isAnswerVisible) "Cevabı Gizle" else "Cevabı Gör",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Sil",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = note.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (isAnswerVisible) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddStudyNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (course: String, title: String, content: String) -> Unit
) {
    var course by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Yeni Ezber Kartı / Not Ekle",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = course,
                    onValueChange = { course = it },
                    label = { Text("Ders Adı (Örn: Fizik, Tarih)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_input_course")
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Soru veya Kavram Başlığı") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_input_title")
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Cevap / Formül / Özet Açıklama") },
                    minLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_input_content")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("İptal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (course.isNotBlank() && title.isNotBlank() && content.isNotBlank()) {
                                onConfirm(course, title, content)
                            }
                        },
                        enabled = course.isNotBlank() && title.isNotBlank() && content.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("note_confirm_button")
                    ) {
                        Text("Kaydet")
                    }
                }
            }
        }
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
        Priority.DUSUK -> Pair("Düşük", Color(0xFF059669))
        Priority.ORTA -> Pair("Orta", Color(0xFFD97706))
        Priority.YUKSEK -> Pair("Yüksek", Color(0xFFDC2626))
    }

    val now = System.currentTimeMillis()
    val isOverdue = homework.dueDateMillis < now && !homework.isCompleted
    val diffDays = ((homework.dueDateMillis - now) / (24L * 60L * 60L * 1000L)).toInt()

    val dueDateStr = remember(homework.dueDateMillis) {
        SimpleDateFormat("d MMMM yyyy, EEEE", SCHOOL_TR_LOCALE).format(Date(homework.dueDateMillis))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("homework_item_${homework.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (homework.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
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

                    Text(
                        text = typeTitle,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = typeColor
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "• $priorityTitle",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = priorityColor
                    )
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

            Spacer(modifier = Modifier.height(4.dp))

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
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = homework.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
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
    val daysRemaining = remember(exam.examDateMillis) {
        val diffMillis = exam.examDateMillis - System.currentTimeMillis()
        (diffMillis / (24L * 60L * 60L * 1000L)).toInt()
    }

    val examDateStr = remember(exam.examDateMillis) {
        SimpleDateFormat("d MMMM yyyy - HH:mm", SCHOOL_TR_LOCALE).format(Date(exam.examDateMillis))
    }

    var showGradeDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("exam_item_${exam.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = exam.courseName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = ColorSinav
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
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
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = exam.examTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = examDateStr,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (exam.topics.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Konular: ${exam.topics}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (exam.targetGrade != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
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
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "🏆 Alınan: ${exam.actualGrade}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
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
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${exam.courseName} Sınav Notu",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = gradeInput,
                        onValueChange = { if (it.length <= 3 && it.all { ch -> ch.isDigit() }) gradeInput = it },
                        label = { Text("Aldığınız Not (0-100)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showGradeDialog = false }) { Text("İptal") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val parsed = gradeInput.toIntOrNull()
                                onUpdateGrade(parsed)
                                showGradeDialog = false
                            },
                            shape = RoundedCornerShape(8.dp)
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

    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy", SCHOOL_TR_LOCALE) }
    var dateButtonText by remember { mutableStateOf(dateFormat.format(Date(selectedDueDateMillis))) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth().padding(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Yeni Ödev / Görev Ekle",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

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

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = course,
                    onValueChange = { course = it },
                    label = { Text("Ders Adı (Örn: Matematik, Fizik)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("hw_input_course")
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Ödev Başlığı / Görev") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("hw_input_title")
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Açıklama / Sayfalar (Opsiyonel)") },
                    minLines = 2,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

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
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Teslim Tarihi: $dateButtonText")
                }

                Spacer(modifier = Modifier.height(6.dp))

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

                Spacer(modifier = Modifier.height(12.dp))

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
                        shape = RoundedCornerShape(10.dp),
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

    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy - HH:mm", SCHOOL_TR_LOCALE) }
    var dateButtonText by remember { mutableStateOf(dateFormat.format(Date(selectedDateMillis))) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth().padding(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Yeni Sınav Ekle",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = course,
                    onValueChange = { course = it },
                    label = { Text("Ders Adı (Örn: Kimya, Biyoloji)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("exam_input_course")
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Sınav Başlığı (Örn: 1. Yazılı Sınavı)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("exam_input_title")
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = topics,
                    onValueChange = { topics = it },
                    label = { Text("Dahil Konular / Notlar") },
                    minLines = 2,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = targetGradeText,
                    onValueChange = { if (it.length <= 3 && it.all { ch -> ch.isDigit() }) targetGradeText = it },
                    label = { Text("Hedef Not (0-100, opsiyonel)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

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
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(dateButtonText, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("exam_confirm_button")
                    ) {
                        Text("Kaydet")
                    }
                }
            }
        }
    }
}
