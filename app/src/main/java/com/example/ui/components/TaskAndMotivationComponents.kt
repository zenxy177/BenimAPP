package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.*
import com.example.ui.MotivationState
import com.example.ui.TaskCelebrationEvent
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val TASK_TR_LOCALE: Locale = Locale.forLanguageTag("tr")

@Composable
fun MotivationStreakBanner(
    motivationState: MotivationState,
    urgentOrOverdueTask: TaskEntity?,
    onStartFocusTask: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("motivation_streak_banner"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔥 ${motivationState.currentStreakDays} Gün Seri",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFB45309)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = motivationState.levelTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${motivationState.totalXp} XP",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Seviye ${motivationState.levelNumber} İlerlemesi",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${motivationState.currentLevelXp} / ${motivationState.nextLevelXpTarget} XP • Bitirilen: ${motivationState.completedTasksCount}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { motivationState.levelProgressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            if (motivationState.badges.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(motivationState.badges, key = { it.id }) { badge ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (badge.isUnlocked) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            border = BorderStroke(
                                1.dp,
                                if (badge.isUnlocked) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (badge.isUnlocked) badge.emoji else "🔒",
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = badge.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (badge.isUnlocked) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (badge.isUnlocked) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Proactive encouragement for overdue or high-priority hard tasks
            if (urgentOrOverdueTask != null) {
                val now = System.currentTimeMillis()
                val isOverdue = urgentOrOverdueTask.dueDateMillis != null && urgentOrOverdueTask.dueDateMillis < now
                val isHard = urgentOrOverdueTask.priority == Priority.YUKSEK

                if (isOverdue || isHard) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isOverdue) {
                                        "💡 Geciken Görev İçin Teşvik"
                                    } else {
                                        "💪 Zor Görevleri Küçük Adımlarla Aş!"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "\"${urgentOrOverdueTask.title}\" için sadece 15 dk odaklanarak başla, gerisi kendiliğinden gelecek!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            FilledTonalButton(
                                onClick = { onStartFocusTask(urgentOrOverdueTask) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("encourage_focus_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Başla", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItemCard(
    task: TaskEntity,
    onToggleCompleted: () -> Unit,
    onToggleSubtask: (Int) -> Unit,
    onStartFocus: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (categoryTitle, categoryColor) = when (task.category) {
        TaskCategory.OKUL -> Pair("Okul", ColorOdevNormal)
        TaskCategory.MANEVIYAT -> Pair("Maneviyat", ColorAyet)
        TaskCategory.KISISEL -> Pair("Kişisel", ColorHikmet)
        TaskCategory.SAGLIK -> Pair("Sağlık", Color(0xFF059669))
        TaskCategory.GENEL -> Pair("Genel", ColorHadis)
    }

    val (priorityTitle, priorityColor, xpReward) = when (task.priority) {
        Priority.YUKSEK -> Triple("Yüksek / Zor", Color(0xFFDC2626), "+35 XP")
        Priority.ORTA -> Triple("Orta", Color(0xFFD97706), "+20 XP")
        Priority.DUSUK -> Triple("Düşük", Color(0xFF059669), "+10 XP")
    }

    val recurrenceLabel = when (task.recurrence) {
        TaskRecurrence.NONE -> null
        TaskRecurrence.DAILY -> "Her Gün"
        TaskRecurrence.WEEKLY -> "Her Hafta"
    }

    val subtasks = remember(task.subtasksRaw) { task.parseSubtasks() }
    val doneSubtasksCount = remember(subtasks) { subtasks.count { it.isDone } }
    val now = System.currentTimeMillis()
    val isOverdue = task.dueDateMillis != null && task.dueDateMillis < now && !task.isCompleted

    val dueText = remember(task.dueDateMillis) {
        task.dueDateMillis?.let {
            SimpleDateFormat("d MMM HH:mm", TASK_TR_LOCALE).format(Date(it))
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            1.dp,
            when {
                isOverdue -> MaterialTheme.colorScheme.error
                task.priority == Priority.YUKSEK && !task.isCompleted -> priorityColor
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = categoryColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = categoryTitle,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = categoryColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = priorityColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "$priorityTitle ($xpReward)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = priorityColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    if (recurrenceLabel != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "🔁 $recurrenceLabel",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Görevi Sil",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main title & checkbox row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleCompleted,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("task_toggle_${task.id}")
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                        contentDescription = "Görevi Tamamla",
                        tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else priorityColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (task.isCompleted) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )

                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Subtasks checklist if present
            if (subtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Alt Adımlar ($doneSubtasksCount/${subtasks.size})",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            val subProgress = (doneSubtasksCount.toFloat() / subtasks.size.toFloat()).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { subProgress },
                                modifier = Modifier
                                    .width(72.dp)
                                    .height(5.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surface
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        subtasks.forEachIndexed { idx, sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleSubtask(idx) }
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (sub.isDone) Icons.Filled.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                    contentDescription = sub.title,
                                    tint = if (sub.isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = sub.title,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        textDecoration = if (sub.isDone) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    color = if (sub.isDone) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Due date, tags, focused minutes & Focus Pomodoro button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (dueText != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (task.hasReminder) Icons.Default.AlarmOn else Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOverdue) "Gecikti: $dueText" else dueText,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (task.tags.isNotBlank() || task.focusedMinutes > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        val metaParts = mutableListOf<String>()
                        if (task.focusedMinutes > 0) metaParts.add("⏱️ ${task.focusedMinutes} dk odaklanıldı")
                        if (task.tags.isNotBlank()) metaParts.add("🏷️ ${task.tags}")
                        Text(
                            text = metaParts.joinToString(" • "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (!task.isCompleted) {
                    FilledTonalButton(
                        onClick = onStartFocus,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("task_focus_btn_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Odaklan",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Odaklan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCelebrationDialog(
    event: TaskCelebrationEvent,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .testTag("task_celebration_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🎉", fontSize = 28.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Görev Tamamlandı!",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = event.taskTitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "+${event.earnedXp} Azim Puanı (XP)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Text(
                            text = "🔥 ${event.streakDays} Gün Seri",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFB45309),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = event.encouragementNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Motivational Quote Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "✨ Günün Motivasyon & Hikmet Sözü",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "“${event.quoteText}”",
                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "— ${event.quoteSource}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("celebration_dismiss_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Elhamdülillah, Devam Et! 🚀", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        description: String,
        category: TaskCategory,
        priority: Priority,
        dueDateMillis: Long?,
        hasReminder: Boolean,
        recurrence: TaskRecurrence,
        subtasks: List<SubtaskItem>,
        tags: String
    ) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(TaskCategory.OKUL) }
    var priority by remember { mutableStateOf(Priority.ORTA) }
    var recurrence by remember { mutableStateOf(TaskRecurrence.NONE) }
    var tags by remember { mutableStateOf("") }

    // Subtasks dynamic input
    var subtaskDraft by remember { mutableStateOf("") }
    val subtaskList = remember { mutableStateListOf<SubtaskItem>() }

    // Optional alarm / due date
    var hasDueDateAndAlarm by remember { mutableStateOf(true) }
    val defaultCal = remember {
        Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 2)
            set(Calendar.MINUTE, 0)
        }
    }
    var selectedDueMillis by remember { mutableStateOf(defaultCal.timeInMillis) }
    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy - HH:mm", TASK_TR_LOCALE) }
    var dateText by remember { mutableStateOf(dateFormat.format(Date(selectedDueMillis))) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = "Yeni Görev & Motivasyon Planla",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Görev Başlığı (Örn: Fizik 30 Soru Çöz)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_input_title")
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Not / Motivasyon Amacı (Opsiyonel)") },
                    minLines = 2,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Kategori:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(TaskCategory.values()) { cat ->
                        val label = when (cat) {
                            TaskCategory.OKUL -> "Okul"
                            TaskCategory.KISISEL -> "Kişisel"
                            TaskCategory.MANEVIYAT -> "Maneviyat"
                            TaskCategory.SAGLIK -> "Sağlık"
                            TaskCategory.GENEL -> "Genel"
                        }
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Öncelik & Zorluk Derecesi:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Priority.values().forEach { prio ->
                        val label = when (prio) {
                            Priority.DUSUK -> "Düşük (+10 XP)"
                            Priority.ORTA -> "Orta (+20 XP)"
                            Priority.YUKSEK -> "Zor (+35 XP)"
                        }
                        FilterChip(
                            selected = priority == prio,
                            onClick = { priority = prio },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Subtasks builder
                Text(
                    text = "Alt Görevler (Adım Adım Kontrol Listesi):",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = subtaskDraft,
                        onValueChange = { subtaskDraft = it },
                        placeholder = { Text("Alt adım ekle...", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("subtask_input")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    FilledTonalIconButton(
                        onClick = {
                            if (subtaskDraft.isNotBlank()) {
                                subtaskList.add(SubtaskItem(subtaskDraft.trim(), false))
                                subtaskDraft = ""
                            }
                        },
                        modifier = Modifier.testTag("add_subtask_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Alt Adım Ekle")
                    }
                }

                if (subtaskList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    subtaskList.forEachIndexed { idx, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• ${item.title}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { subtaskList.removeAt(idx) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Kaldır",
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tekrarlama:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TaskRecurrence.values().forEach { rec ->
                        val label = when (rec) {
                            TaskRecurrence.NONE -> "Tek Seferlik"
                            TaskRecurrence.DAILY -> "Her Gün"
                            TaskRecurrence.WEEKLY -> "Her Hafta"
                        }
                        FilterChip(
                            selected = recurrence == rec,
                            onClick = { recurrence = rec },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Motivasyon Sözlü Alarm Hatırlatıcı",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = hasDueDateAndAlarm,
                        onCheckedChange = { hasDueDateAndAlarm = it }
                    )
                }

                if (hasDueDateAndAlarm) {
                    OutlinedButton(
                        onClick = {
                            val c = Calendar.getInstance().apply { timeInMillis = selectedDueMillis }
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
                                            selectedDueMillis = c.timeInMillis
                                            dateText = dateFormat.format(Date(selectedDueMillis))
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
                        Icon(imageVector = Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hatırlatma: $dateText", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Etiketler (Örn: Sınav, Acil, Kitap)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("İptal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val finalSubtasks = subtaskList.toList().let { list ->
                                    if (subtaskDraft.isNotBlank()) list + SubtaskItem(subtaskDraft.trim(), false)
                                    else list
                                }
                                onConfirm(
                                    title,
                                    description,
                                    category,
                                    priority,
                                    if (hasDueDateAndAlarm) selectedDueMillis else null,
                                    hasDueDateAndAlarm,
                                    recurrence,
                                    finalSubtasks,
                                    tags
                                )
                            }
                        },
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("task_confirm_button")
                    ) {
                        Text("Görevi Kaydet")
                    }
                }
            }
        }
    }
}
