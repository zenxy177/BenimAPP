package com.example.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.DailyGoalEntity
import com.example.data.local.entity.GoalCategory
import com.example.data.local.entity.ReminderEntity
import com.example.ui.MainViewModel
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class GoalsSubTab {
    GOALS,
    REMINDERS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsAndRemindersScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val todayGoals by viewModel.todayGoals.collectAsState()
    val allReminders by viewModel.allReminders.collectAsState()

    var activeSubTab by remember { mutableStateOf(GoalsSubTab.GOALS) }
    var selectedCategoryFilter by remember { mutableStateOf<GoalCategory?>(null) }

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddReminderDialog by remember { mutableStateOf(false) }

    val filteredGoals = remember(todayGoals, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) todayGoals else todayGoals.filter { it.category == selectedCategoryFilter }
    }

    val completedGoalsCount = todayGoals.count { it.isCompleted }
    val totalGoalsCount = todayGoals.size
    val progressFraction = if (totalGoalsCount > 0) completedGoalsCount.toFloat() / totalGoalsCount else 0f

    val activeRemindersCount = allReminders.count { it.isEnabled }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (activeSubTab == GoalsSubTab.GOALS) {
                        showAddGoalDialog = true
                    } else {
                        showAddReminderDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("goals_add_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (activeSubTab == GoalsSubTab.GOALS) "Hedef Ekle" else "Hatırlatıcı Ekle"
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Hedefler & Hatırlatıcılar",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Günlük alışkanlıkların ve vaktinde hatırlanacak işlerin",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Sub Tab Bar
            PrimaryTabRow(
                selectedTabIndex = if (activeSubTab == GoalsSubTab.GOALS) 0 else 1,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeSubTab == GoalsSubTab.GOALS,
                    onClick = { activeSubTab = GoalsSubTab.GOALS },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TaskAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Günlük Hedefler ($completedGoalsCount/$totalGoalsCount)")
                        }
                    }
                )
                Tab(
                    selected = activeSubTab == GoalsSubTab.REMINDERS,
                    onClick = { activeSubTab = GoalsSubTab.REMINDERS },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hatırlatıcılar ($activeRemindersCount)")
                        }
                    }
                )
            }

            if (activeSubTab == GoalsSubTab.GOALS) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("goals_list"),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    // Progress Banner Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Bugünün İlerlemesi",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = when {
                                            progressFraction >= 1.0f -> "Tebrikler! Bugünün tüm hedeflerini tamamladın! 🎉"
                                            progressFraction >= 0.5f -> "Harika gidiyorsun! Hedeflerinin yarısından fazlası bitti. 🌟"
                                            totalGoalsCount > 0 -> "Adım adım hedeflerine doğru ilerle! 🚀"
                                            else -> "Bugün için yeni hedefler belirle."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    LinearProgressIndicator(
                                        progress = { progressFraction },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surface
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "%${(progressFraction * 100).toInt()}",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Category filters
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedCategoryFilter == null,
                                    onClick = { selectedCategoryFilter = null },
                                    label = { Text("Tümü (${todayGoals.size})") }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedCategoryFilter == GoalCategory.AKADEMIK,
                                    onClick = { selectedCategoryFilter = GoalCategory.AKADEMIK },
                                    label = { Text("📚 Akademik") }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedCategoryFilter == GoalCategory.MANEVIYAT,
                                    onClick = { selectedCategoryFilter = GoalCategory.MANEVIYAT },
                                    label = { Text("🕌 Maneviyat") }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedCategoryFilter == GoalCategory.KISISISIL_GELISIM,
                                    onClick = { selectedCategoryFilter = GoalCategory.KISISISIL_GELISIM },
                                    label = { Text("🌱 Gelişim") }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (filteredGoals.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Outlined.CheckCircle,
                                title = "Kayıtlı Hedef Yok",
                                subtitle = "Günlük ders çalışma, soru çözme, kitap okuma veya manevi hedefler ekleyin.",
                                actionButtonText = "+ Yeni Hedef Ekle",
                                onActionClick = { showAddGoalDialog = true }
                            )
                        }
                    } else {
                        items(filteredGoals, key = { it.id }) { goal ->
                            DailyGoalItemCard(
                                goal = goal,
                                onToggleCompleted = { viewModel.toggleGoalCompleted(goal) },
                                onIncrement = { delta -> viewModel.incrementGoal(goal, delta) },
                                onDelete = { viewModel.deleteGoal(goal) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            } else {
                // Reminders subtab
                if (allReminders.isEmpty()) {
                    EmptyStateCard(
                        icon = Icons.Outlined.Alarm,
                        title = "Hatırlatıcı Yok",
                        subtitle = "Ders tekrarı, ödev teslimi veya kişisel hatırlatıcılar kurarak bildirim alabilirsiniz.",
                        actionButtonText = "+ Yeni Hatırlatıcı Kur",
                        onActionClick = { showAddReminderDialog = true }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("reminders_list"),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        items(allReminders, key = { it.id }) { reminder ->
                            ReminderItemCard(
                                reminder = reminder,
                                onToggle = { viewModel.toggleReminder(reminder) },
                                onDelete = { viewModel.deleteReminder(reminder) },
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
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { title, category, target, unit ->
                viewModel.addGoal(title, category, target, unit)
                showAddGoalDialog = false
            }
        )
    }

    if (showAddReminderDialog) {
        AddReminderDialog(
            onDismiss = { showAddReminderDialog = false },
            onConfirm = { title, note, triggerMillis ->
                viewModel.addReminder(title, note, triggerMillis)
                showAddReminderDialog = false
            }
        )
    }
}

@Composable
fun DailyGoalItemCard(
    goal: DailyGoalEntity,
    onToggleCompleted: () -> Unit,
    onIncrement: (Int) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (categoryTitle, categoryColor) = when (goal.category) {
        GoalCategory.AKADEMIK -> Pair("Akademik", ColorOdevNormal)
        GoalCategory.MANEVIYAT -> Pair("Maneviyat", ColorAyet)
        GoalCategory.KISISISIL_GELISIM -> Pair("Gelişim", ColorHikmet)
    }

    val progress = if (goal.targetCount > 0) {
        (goal.currentCount.toFloat() / goal.targetCount).coerceIn(0f, 1f)
    } else 0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("goal_item_${goal.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (goal.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (goal.isCompleted) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleCompleted,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (goal.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                            contentDescription = "Tamamla",
                            tint = if (goal.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = categoryColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = categoryTitle,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = categoryColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Sil",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Column(modifier = Modifier.padding(start = 38.dp)) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (goal.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (goal.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Progress count & bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${goal.currentCount} / ${goal.targetCount} ${goal.unit}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Quick increment pills
                    if (goal.targetCount > 1 && !goal.isCompleted) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            AssistChip(
                                onClick = { onIncrement(1) },
                                label = { Text("+1", fontSize = 11.sp) },
                                modifier = Modifier.height(28.dp)
                            )
                            if (goal.targetCount >= 10) {
                                AssistChip(
                                    onClick = { onIncrement(10) },
                                    label = { Text("+10", fontSize = 11.sp) },
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                        }
                    }
                }

                if (goal.targetCount > 1) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = categoryColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ReminderItemCard(
    reminder: ReminderEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("tr")) }
    val formattedDate = remember(reminder.triggerTimeMillis) {
        dateFormat.format(Date(reminder.triggerTimeMillis))
    }

    val isPast = reminder.triggerTimeMillis < System.currentTimeMillis()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("reminder_item_${reminder.id}"),
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
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (reminder.isEnabled && !isPast) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = if (reminder.isEnabled && !isPast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (reminder.note.isNotBlank()) {
                        Text(
                            text = reminder.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = if (isPast) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPast) "Zamanı geçti ($formattedDate)" else formattedDate,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPast) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.testTag("reminder_switch_${reminder.id}")
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Sil",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: GoalCategory, target: Int, unit: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(GoalCategory.AKADEMIK) }
    var targetText by remember { mutableStateOf("50") }
    var unit by remember { mutableStateOf("soru") }

    val presetGoals = listOf(
        Pair("50 Soru Çöz", Pair(50, "soru")),
        Pair("Kuran / Meal Oku (1 Cüz/Sure)", Pair(10, "sayfa")),
        Pair("Kitap Oku (30 Sayfa)", Pair(30, "sayfa")),
        Pair("Ders Tekrarı Yap", Pair(30, "dk")),
        Pair("Günün Hikmetli Sözünü Tefekkür Et", Pair(1, "kez"))
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
                    text = "Yeni Günlük Hedef Ekle",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Category selector
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    GoalCategory.values().forEach { cat ->
                        val isSelected = category == cat
                        val label = when (cat) {
                            GoalCategory.AKADEMIK -> "Akademik"
                            GoalCategory.MANEVIYAT -> "Maneviyat"
                            GoalCategory.KISISISIL_GELISIM -> "Gelişim"
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { category = cat },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Fast presets
                Text("Hızlı Seçenekler:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(presetGoals) { (presetTitle, pair) ->
                        AssistChip(
                            onClick = {
                                title = presetTitle
                                targetText = pair.first.toString()
                                unit = pair.second
                            },
                            label = { Text(presetTitle, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Hedef Başlığı (Örn: 50 Matematik Sorusu)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("goal_input_title")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) targetText = it },
                        label = { Text("Hedef Miktar") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Birim (soru, sayfa, dk)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("İptal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val count = targetText.toIntOrNull() ?: 1
                            if (title.isNotBlank()) {
                                onConfirm(title, category, count, unit)
                            }
                        },
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("goal_confirm_button")
                    ) {
                        Text("Kaydet")
                    }
                }
            }
        }
    }
}

@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, note: String, triggerMillis: Long) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val calendar = remember {
        Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 2)
            set(Calendar.MINUTE, 0)
        }
    }
    var triggerMillis by remember { mutableStateOf(calendar.timeInMillis) }

    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy - HH:mm", Locale("tr")) }
    var dateButtonText by remember { mutableStateOf(dateFormat.format(Date(triggerMillis))) }

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
                    text = "Yeni Hatırlatıcı Kur",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Hatırlatıcı Başlığı (Örn: Fizik Ödevini Kontrol Et)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("reminder_input_title")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Açıklama / Not (Opsiyonel)") },
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        val c = Calendar.getInstance().apply { timeInMillis = triggerMillis }
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
                                        triggerMillis = c.timeInMillis
                                        dateButtonText = dateFormat.format(Date(triggerMillis))
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
                    Icon(imageVector = Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Alarm Saati: $dateButtonText", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("İptal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onConfirm(title, note, triggerMillis)
                            }
                        },
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("reminder_confirm_button")
                    ) {
                        Text("Alarm Kur")
                    }
                }
            }
        }
    }
}
