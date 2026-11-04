package com.example.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.ScheduleLessonEntity
import com.example.ui.MainViewModel
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val allLessons by viewModel.allLessons.collectAsState()
    val selectedDay by viewModel.selectedScheduleDay.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val days = listOf(
        Pair(1, "Pazartesi"),
        Pair(2, "Salı"),
        Pair(3, "Çarşamba"),
        Pair(4, "Perşembe"),
        Pair(5, "Cuma"),
        Pair(6, "Cumartesi")
    )

    val currentDayLessons = remember(allLessons, selectedDay) {
        allLessons.filter { it.dayOfWeek == selectedDay }.sortedBy { it.periodIndex }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("timetable_add_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Ders Ekle")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Haftalık Okul Ders Programı",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Günlük derslerini, saatlerini ve sınıflarını takip et",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Days horizontal selector bar
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(days) { (dayNum, dayName) ->
                    val isSelected = selectedDay == dayNum
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectScheduleDay(dayNum) },
                        label = {
                            Text(
                                text = dayName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Lessons List
            if (currentDayLessons.isEmpty()) {
                val dayName = days.firstOrNull { it.first == selectedDay }?.second ?: "Seçili gün"
                EmptyStateCard(
                    icon = Icons.Outlined.CalendarMonth,
                    title = "$dayName İçin Ders Yok",
                    subtitle = "Bu gün için henüz ders eklemediniz. Aşağıdaki butondan ders ekleyebilirsiniz.",
                    actionButtonText = "+ Bu Güne Ders Ekle",
                    onActionClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("timetable_list"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(currentDayLessons, key = { it.id }) { lesson ->
                        LessonScheduleCard(
                            lesson = lesson,
                            onDelete = { viewModel.deleteLesson(lesson) },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddLessonDialog(
            initialDay = selectedDay,
            onDismiss = { showAddDialog = false },
            onConfirm = { day, period, name, teacher, start, end, colorHex ->
                viewModel.addLesson(day, period, name, teacher, start, end, colorHex)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun LessonScheduleCard(
    lesson: ScheduleLessonEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = remember(lesson.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(lesson.colorHex))
        } catch (e: Exception) {
            EmeraldPrimaryLight
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("lesson_item_${lesson.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Period Badge
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${lesson.periodIndex}.",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = cardColor
                        )
                        Text(
                            text = "Ders",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = cardColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = lesson.lessonName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (lesson.teacherOrRoom.isNotBlank()) {
                        Text(
                            text = lesson.teacherOrRoom,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (lesson.startTime.isNotBlank() || lesson.endTime.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${lesson.startTime} - ${lesson.endTime}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Dersi Kaldır",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AddLessonDialog(
    initialDay: Int,
    onDismiss: () -> Unit,
    onConfirm: (day: Int, period: Int, name: String, teacher: String, start: String, end: String, colorHex: String) -> Unit
) {
    val context = LocalContext.current
    var day by remember { mutableStateOf(initialDay) }
    var period by remember { mutableStateOf(1) }
    var name by remember { mutableStateOf("") }
    var teacherOrRoom by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("08:30") }
    var endTime by remember { mutableStateOf("09:10") }
    var selectedColorHex by remember { mutableStateOf("#0F5A4F") }

    val daysList = listOf(
        Pair(1, "Pzt"),
        Pair(2, "Sal"),
        Pair(3, "Çar"),
        Pair(4, "Per"),
        Pair(5, "Cum"),
        Pair(6, "Cmt")
    )

    val colorOptions = listOf(
        "#0F5A4F", // Emerald
        "#2C568E", // Indigo
        "#915D00", // Amber
        "#7C3AED", // Violet
        "#0284C7", // Cyan
        "#16A34A", // Green
        "#DC2626"  // Red
    )

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
                    text = "Ders Ekle",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Day selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysList.forEach { (dNum, dLabel) ->
                        FilterChip(
                            selected = day == dNum,
                            onClick = { day = dNum },
                            label = { Text(dLabel, fontSize = 11.sp) },
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Period selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Kaçıncı Ders?", style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (period > 1) period-- },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Azalt")
                        }
                        Text(
                            text = "$period. Ders",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(
                            onClick = { if (period < 12) period++ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Artır")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Ders Adı (Örn: Matematik, Fizik)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("lesson_input_name")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = teacherOrRoom,
                    onValueChange = { teacherOrRoom = it },
                    label = { Text("Öğretmen / Derslik (Opsiyonel)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Start & End Time Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val parts = startTime.split(":")
                            val h = parts.getOrNull(0)?.toIntOrNull() ?: 8
                            val m = parts.getOrNull(1)?.toIntOrNull() ?: 30
                            TimePickerDialog(context, { _, hourOfDay, minute ->
                                startTime = String.format("%02d:%02d", hourOfDay, minute)
                            }, h, m, true).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Başlangıç: $startTime", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val parts = endTime.split(":")
                            val h = parts.getOrNull(0)?.toIntOrNull() ?: 9
                            val m = parts.getOrNull(1)?.toIntOrNull() ?: 10
                            TimePickerDialog(context, { _, hourOfDay, minute ->
                                endTime = String.format("%02d:%02d", hourOfDay, minute)
                            }, h, m, true).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Bitiş: $endTime", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Color picker dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Renk:", style = MaterialTheme.typography.bodySmall)
                    colorOptions.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColorHex == hex) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("İptal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(day, period, name, teacherOrRoom, startTime, endTime, selectedColorHex)
                            }
                        },
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("lesson_confirm_button")
                    ) {
                        Text("Kaydet")
                    }
                }
            }
        }
    }
}
