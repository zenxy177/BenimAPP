package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.*
import com.example.ui.MainTab
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val TR_LOCALE: Locale = Locale.forLanguageTag("tr")

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateTab: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val featuredQuote by viewModel.featuredQuote.collectAsStateWithLifecycle()
    val todayLessons by viewModel.todayLessons.collectAsStateWithLifecycle()
    val upcomingHomeworks by viewModel.upcomingHomeworks.collectAsStateWithLifecycle()
    val upcomingExams by viewModel.upcomingExams.collectAsStateWithLifecycle()
    val goalsSummary by viewModel.goalsProgressSummary.collectAsStateWithLifecycle()
    val pendingTasks by viewModel.pendingTasks.collectAsStateWithLifecycle()
    val urgentOrOverdueTask by viewModel.urgentOrOverdueTask.collectAsStateWithLifecycle()
    val motivationState by viewModel.motivationState.collectAsStateWithLifecycle()
    val celebrationEvent by viewModel.celebrationEvent.collectAsStateWithLifecycle()

    val pomodoroSelectedMinutes by viewModel.pomodoroSelectedMinutes.collectAsStateWithLifecycle()
    val pomodoroRemainingSeconds by viewModel.pomodoroRemainingSeconds.collectAsStateWithLifecycle()
    val pomodoroIsRunning by viewModel.pomodoroIsRunning.collectAsStateWithLifecycle()
    val pomodoroCourse by viewModel.pomodoroCourse.collectAsStateWithLifecycle()
    val todayStudyMinutes by viewModel.todayStudyMinutes.collectAsStateWithLifecycle()

    val todayFormatted = remember {
        SimpleDateFormat("EEEE, d MMMM yyyy", TR_LOCALE).format(Date())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_scroll"),
        contentPadding = PaddingValues(bottom = 84.dp)
    ) {
        // --- TOP GREETING HEADER (Compact for 720p 16:9) ---
        item(key = "header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Selamün Aleyküm 🌿",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = todayFormatted,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier
                            .testTag("dark_mode_toggle_button")
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkMode) "Açık Moda Geç" else "Siyah Moda Geç",
                            tint = if (isDarkMode) GoldSecondaryDark else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { viewModel.triggerTestNotification() },
                        modifier = Modifier
                            .testTag("home_quick_notify_button")
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Hızlı Bildirim Testi",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // --- MOTIVATION & STREAK SUMMARY CARD ---
        item(key = "home_motivation_banner") {
            MotivationStreakBanner(
                motivationState = motivationState,
                urgentOrOverdueTask = urgentOrOverdueTask,
                onStartFocusTask = { task -> viewModel.startFocusOnTask(task, 25) },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onNavigateTab(MainTab.GOALS) }
            )
        }

        // --- FEATURED QUOTE HERO CARD (Flat GPU-Friendly Card) ---
        item(key = "featured_quote") {
            featuredQuote?.let { quote ->
                FeaturedQuoteHeroCard(
                    quote = quote,
                    onToggleFavorite = { viewModel.toggleFavoriteQuote(quote) },
                    onRefresh = { viewModel.refreshFeaturedQuote() },
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Rehberim Söz", "\"${quote.text}\" — ${quote.source}"))
                        Toast.makeText(context, "Söz panoya kopyalandı", Toast.LENGTH_SHORT).show()
                    },
                    onShare = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, "\"${quote.text}\"\n\n— ${quote.source}\n(Rehberim Uygulaması)")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Sözü Paylaş"))
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        // --- POMODORO FOCUS TIMER CARD (Flat GPU-Friendly Card) ---
        item(key = "pomodoro_focus_card") {
            PomodoroFocusCard(
                selectedMinutes = pomodoroSelectedMinutes,
                remainingSeconds = pomodoroRemainingSeconds,
                isRunning = pomodoroIsRunning,
                selectedCourse = pomodoroCourse,
                todayStudyMinutes = todayStudyMinutes,
                onSelectDuration = { viewModel.setPomodoroDuration(it) },
                onSelectCourse = { viewModel.setPomodoroCourse(it) },
                onStartPause = { viewModel.startPausePomodoro() },
                onReset = { viewModel.resetPomodoro() },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // --- PRIORITY TASKS GLANCE ---
        item(key = "tasks_section_header") {
            SectionHeader(
                title = "Öncelikli Görevlerin",
                badgeText = "${pendingTasks.size} Bekleyen",
                actionText = "Tüm Görevler",
                onActionClick = { onNavigateTab(MainTab.GOALS) }
            )
        }

        if (pendingTasks.isEmpty()) {
            item(key = "no_pending_tasks") {
                EmptyStateCard(
                    icon = Icons.Default.TaskAlt,
                    title = "Tüm Görevler Tamamlandı!",
                    subtitle = "Yeni bir görev planlayarak günlük serini ve motivasyon puanını artırabilirsin.",
                    actionButtonText = "+ Görev Planla",
                    onActionClick = { onNavigateTab(MainTab.GOALS) }
                )
            }
        } else {
            items(pendingTasks.take(2), key = { "home_task_${it.id}" }) { task ->
                TaskItemCard(
                    task = task,
                    onToggleCompleted = { viewModel.toggleTaskCompleted(task) },
                    onToggleSubtask = { subIdx -> viewModel.toggleSubtask(task, subIdx) },
                    onStartFocus = { viewModel.startFocusOnTask(task, 25) },
                    onDelete = { viewModel.deleteTask(task) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        // --- QUICK STATS / GOALS GLANCE ---
        item(key = "goals_glance") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onNavigateTab(MainTab.GOALS) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bugünün Hedefleri",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (goalsSummary.totalCount > 0) {
                                "${goalsSummary.completedCount} / ${goalsSummary.totalCount} tamamlandı (%${goalsSummary.progressPercent})"
                            } else {
                                "Henüz hedef belirlenmedi"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { goalsSummary.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .height(6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Detay",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // --- TODAY'S TIMETABLE LESSONS ---
        item(key = "timetable_header") {
            SectionHeader(
                title = "Bugünkü Derslerin",
                badgeText = "${todayLessons.size} Ders",
                actionText = "Tüm Program",
                onActionClick = { onNavigateTab(MainTab.TIMETABLE) }
            )
        }

        if (todayLessons.isEmpty()) {
            item(key = "no_lessons") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Weekend,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Bugün programda kayıtlı ders yok",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "İstirahat edebilir veya hedeflerine odaklanabilirsin.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            item(key = "today_lessons_row") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(todayLessons, key = { it.id }) { lesson ->
                        LessonQuickCard(lesson = lesson)
                    }
                }
            }
        }

        // --- UPCOMING HOMEWORKS & EXAMS ---
        item(key = "tasks_header") {
            Spacer(modifier = Modifier.height(6.dp))
            SectionHeader(
                title = "Yaklaşan Ödev & Sınavlar",
                actionText = "Tümünü Gör",
                onActionClick = { onNavigateTab(MainTab.SCHOOL) }
            )
        }

        if (upcomingExams.isNotEmpty()) {
            items(upcomingExams, key = { "exam_${it.id}" }) { exam ->
                ExamCountdownCard(
                    exam = exam,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        if (upcomingHomeworks.isNotEmpty()) {
            items(upcomingHomeworks, key = { "hw_${it.id}" }) { hw ->
                HomeworkHomeCard(
                    homework = hw,
                    onToggle = { viewModel.toggleHomework(hw) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        if (upcomingExams.isEmpty() && upcomingHomeworks.isEmpty()) {
            item(key = "empty_tasks") {
                EmptyStateCard(
                    icon = Icons.Outlined.CheckCircle,
                    title = "Bekleyen Ödev veya Sınav Yok",
                    subtitle = "Tüm görevlerini tamamladın ya da yeni görev ekleyebilirsin.",
                    actionButtonText = "+ Ödev / Sınav Ekle",
                    onActionClick = { onNavigateTab(MainTab.SCHOOL) }
                )
            }
        }
    }

    if (celebrationEvent != null) {
        TaskCelebrationDialog(
            event = celebrationEvent!!,
            onDismiss = { viewModel.dismissCelebrationEvent() }
        )
    }
}

@Composable
fun PomodoroFocusCard(
    selectedMinutes: Int,
    remainingSeconds: Int,
    isRunning: Boolean,
    selectedCourse: String,
    todayStudyMinutes: Int,
    onSelectDuration: (Int) -> Unit,
    onSelectCourse: (String) -> Unit,
    onStartPause: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val minutesPart = remainingSeconds / 60
    val secondsPart = remainingSeconds % 60
    val formattedTime = remember(minutesPart, secondsPart) {
        String.format(Locale.US, "%02d:%02d", minutesPart, secondsPart)
    }

    val totalSeconds = (selectedMinutes * 60).coerceAtLeast(1)
    val progress = remember(remainingSeconds, totalSeconds) {
        (1f - (remainingSeconds.toFloat() / totalSeconds)).coerceIn(0f, 1f)
    }

    val courses = remember {
        listOf("Genel Çalışma", "Matematik", "Fizik", "Türkçe", "Tarih", "Kimya")
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pomodoro_focus_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
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
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pomodoro Odaklanma Sayacı",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Bugün: $todayStudyMinutes dk",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Course selection chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(courses) { course ->
                    FilterChip(
                        selected = selectedCourse == course,
                        onClick = { onSelectCourse(course) },
                        label = { Text(course, fontSize = 11.sp) },
                        modifier = Modifier.height(28.dp)
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
                        text = formattedTime,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("pomodoro_timer_text")
                    )
                    Text(
                        text = if (isRunning) "$selectedCourse odaklanılıyor..." else "$selectedCourse • Süre seçip başlatın",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onStartPause,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("pomodoro_start_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Duraklat" else "Başlat",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isRunning) "Duraklat" else "Başlat", fontSize = 13.sp)
                    }

                    OutlinedIconButton(
                        onClick = onReset,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("pomodoro_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sıfırla",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Duration quick selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Süre:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                listOf(15, 25, 45).forEach { mins ->
                    FilterChip(
                        selected = selectedMinutes == mins,
                        onClick = { onSelectDuration(mins) },
                        label = { Text("$mins dk", fontSize = 11.sp) },
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("pomodoro_duration_${mins}")
                    )
                }
            }
        }
    }
}

@Composable
fun FeaturedQuoteHeroCard(
    quote: QuoteEntity,
    onToggleFavorite: () -> Unit,
    onRefresh: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (categoryTitle, categoryColor, categoryIcon) = when (quote.category) {
        QuoteCategory.AYET -> Triple("Âyet-i Kerîme", ColorAyet, Icons.AutoMirrored.Filled.MenuBook)
        QuoteCategory.HADIS -> Triple("Hadîs-i Şerîf", ColorHadis, Icons.Default.AutoStories)
        QuoteCategory.HIKMET -> Triple("Hikmetli Söz", ColorHikmet, Icons.Default.FormatQuote)
    }

    // Flat solid surface + 1dp border: zero GPU gradient/shadow overdraw on Mali-T720 MP2
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("featured_quote_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = categoryTitle,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = categoryColor
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Farklı Söz Getir",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (quote.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favoriye Ekle",
                            tint = if (quote.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "“${quote.text}”",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    lineHeight = 21.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "— ${quote.source}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Italic
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                Row {
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Kopyala",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Paylaş",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LessonQuickCard(lesson: ScheduleLessonEntity) {
    Card(
        modifier = Modifier
            .width(142.dp)
            .height(86.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
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
                        text = "${lesson.periodIndex}. Ders",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = lesson.startTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column {
                Text(
                    text = lesson.lessonName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (lesson.teacherOrRoom.isNotBlank()) {
                    Text(
                        text = lesson.teacherOrRoom,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun ExamCountdownCard(
    exam: ExamEntity,
    modifier: Modifier = Modifier
) {
    val daysRemaining = remember(exam.examDateMillis) {
        val diffMillis = exam.examDateMillis - System.currentTimeMillis()
        (diffMillis / (24L * 60L * 60L * 1000L)).coerceAtLeast(0L)
    }

    val examDateStr = remember(exam.examDateMillis) {
        SimpleDateFormat("d MMM, HH:mm", TR_LOCALE).format(Date(exam.examDateMillis))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, ColorSinav)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = ColorSinav,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "${exam.courseName} - ${exam.examTitle}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Tarih: $examDateStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ColorSinav
            ) {
                Text(
                    text = when {
                        daysRemaining == 0L -> "Bugün!"
                        daysRemaining == 1L -> "Yarın!"
                        else -> "$daysRemaining Gün"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun HomeworkHomeCard(
    homework: HomeworkEntity,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (typeTitle, typeColor) = when (homework.type) {
        HomeworkType.NORMAL -> Pair("Normal Ödev", ColorOdevNormal)
        HomeworkType.PERFORMANS -> Pair("Performans", ColorOdevPerformans)
        HomeworkType.PROJE -> Pair("Proje Ödevi", ColorOdevProje)
    }

    val isOverdue = remember(homework.dueDateMillis, homework.isCompleted) {
        homework.dueDateMillis < System.currentTimeMillis() && !homework.isCompleted
    }

    val dueDateStr = remember(homework.dueDateMillis) {
        SimpleDateFormat("d MMMM", TR_LOCALE).format(Date(homework.dueDateMillis))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggle, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = if (homework.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = "Tamamla",
                    tint = if (homework.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "[$typeTitle]",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = typeColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = homework.course,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = homework.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = if (isOverdue) "Gecikti ($dueDateStr)" else dueDateStr,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
