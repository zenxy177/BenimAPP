package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.DhikrEntity
import com.example.data.local.entity.QuoteCategory
import com.example.data.local.entity.QuoteEntity
import com.example.ui.MainViewModel
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.*

enum class SpiritualSubTab {
    QUOTES,
    DHIKR
}

private fun triggerHapticFeedback(context: Context, durationMs: Long = 20L) {
    try {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        }
    } catch (_: Exception) {
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val quotes by viewModel.allQuotes.collectAsStateWithLifecycle()
    val filteredQuotes by viewModel.filteredQuotes.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedQuoteCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.quoteSearchQuery.collectAsStateWithLifecycle()
    val showFavoritesOnly by viewModel.showOnlyFavorites.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val notificationInterval by viewModel.notificationIntervalHours.collectAsStateWithLifecycle()
    val allDhikrs by viewModel.allDhikrs.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf(SpiritualSubTab.QUOTES) }
    var selectedDhikrId by remember { mutableStateOf<Long?>(null) }

    var showAddQuoteDialog by remember { mutableStateOf(false) }
    var showAddDhikrDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val activeDhikr = remember(allDhikrs, selectedDhikrId) {
        allDhikrs.find { it.id == selectedDhikrId } ?: allDhikrs.firstOrNull()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (activeSubTab == SpiritualSubTab.QUOTES) {
                        showAddQuoteDialog = true
                    } else {
                        showAddDhikrDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(14.dp),
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                modifier = Modifier
                    .padding(bottom = 64.dp)
                    .testTag("add_quote_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (activeSubTab == SpiritualSubTab.QUOTES) "Yeni Hikmet Ekle" else "Yeni Tesbihat Ekle"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hikmetler & Tesbihat",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Âyet, hadis, hikmetli sözler ve günlük zikirmatik",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { showSettingsDialog = true },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .testTag("quote_notification_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Bildirim Ayarları",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            PrimaryTabRow(
                selectedTabIndex = if (activeSubTab == SpiritualSubTab.QUOTES) 0 else 1,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeSubTab == SpiritualSubTab.QUOTES,
                    onClick = { activeSubTab = SpiritualSubTab.QUOTES },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoStories,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sözler & Hikmetler (${quotes.size})", fontSize = 13.sp)
                        }
                    },
                    modifier = Modifier.testTag("tab_spiritual_quotes")
                )
                Tab(
                    selected = activeSubTab == SpiritualSubTab.DHIKR,
                    onClick = { activeSubTab = SpiritualSubTab.DHIKR },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Zikirmatik (${allDhikrs.size})", fontSize = 13.sp)
                        }
                    },
                    modifier = Modifier.testTag("tab_spiritual_dhikr")
                )
            }

            if (activeSubTab == SpiritualSubTab.QUOTES) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("quotes_list"),
                    contentPadding = PaddingValues(bottom = 84.dp)
                ) {
                    item(key = "quotes_filters") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            // Flat solid banner (no alpha overdraw)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (notificationsEnabled) {
                                                "Bildirimler Aktif (Her $notificationInterval saatte bir)"
                                            } else {
                                                "Bildirimler Kapalı"
                                            },
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Button(
                                        onClick = { viewModel.triggerTestNotification() },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("test_notification_btn")
                                    ) {
                                        Text("Test Bildirimi", fontSize = 12.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.setQuoteSearchQuery(it) },
                                placeholder = { Text("Sözlerde veya kaynakta ara...") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.setQuoteSearchQuery("") }) {
                                            Icon(imageVector = Icons.Default.Close, contentDescription = "Temizle")
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("quote_search_input")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedCategory == null && !showFavoritesOnly,
                                        onClick = {
                                            viewModel.setQuoteCategoryFilter(null)
                                            if (showFavoritesOnly) viewModel.toggleFavoritesFilter()
                                        },
                                        label = { Text("Tümü (${quotes.size})") }
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = selectedCategory == QuoteCategory.AYET && !showFavoritesOnly,
                                        onClick = {
                                            viewModel.setQuoteCategoryFilter(QuoteCategory.AYET)
                                            if (showFavoritesOnly) viewModel.toggleFavoritesFilter()
                                        },
                                        label = { Text("📖 Âyetler") }
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = selectedCategory == QuoteCategory.HADIS && !showFavoritesOnly,
                                        onClick = {
                                            viewModel.setQuoteCategoryFilter(QuoteCategory.HADIS)
                                            if (showFavoritesOnly) viewModel.toggleFavoritesFilter()
                                        },
                                        label = { Text("📜 Hadîsler") }
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = selectedCategory == QuoteCategory.HIKMET && !showFavoritesOnly,
                                        onClick = {
                                            viewModel.setQuoteCategoryFilter(QuoteCategory.HIKMET)
                                            if (showFavoritesOnly) viewModel.toggleFavoritesFilter()
                                        },
                                        label = { Text("💡 Hikmetler") }
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = showFavoritesOnly,
                                        onClick = { viewModel.toggleFavoritesFilter() },
                                        label = { Text("❤️ Favorilerim") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Filled.Favorite,
                                                contentDescription = null,
                                                tint = if (showFavoritesOnly) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (filteredQuotes.isEmpty()) {
                        item(key = "empty_quotes") {
                            EmptyStateCard(
                                icon = Icons.Outlined.AutoStories,
                                title = "Eşleşen Söz Bulunamadı",
                                subtitle = "Arama kriterlerinizi değiştirebilir veya yeni bir hikmetli söz ekleyebilirsiniz.",
                                actionButtonText = "+ Yeni Söz Ekle",
                                onActionClick = { showAddQuoteDialog = true }
                            )
                        }
                    } else {
                        items(filteredQuotes, key = { it.id }) { quote ->
                            QuoteCardItem(
                                quote = quote,
                                onToggleFavorite = { viewModel.toggleFavoriteQuote(quote) },
                                onDelete = if (quote.isUserAdded) {
                                    { viewModel.deleteQuote(quote) }
                                } else null,
                                onCopy = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Rehberim Söz", "\"${quote.text}\" — ${quote.source}"))
                                    Toast.makeText(context, "Söz kopyalandı", Toast.LENGTH_SHORT).show()
                                },
                                onShare = {
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        putExtra(Intent.EXTRA_TEXT, "\"${quote.text}\"\n\n— ${quote.source}\n(Rehberim)")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Sözü Paylaş"))
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            } else {
                // --- ZIKIRMATIK & TESBIHAT SUB-TAB ---
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("dhikr_list"),
                    contentPadding = PaddingValues(bottom = 84.dp)
                ) {
                    activeDhikr?.let { current ->
                        item(key = "active_dhikr_counter") {
                            ActiveDhikrCounterCard(
                                dhikr = current,
                                onIncrement = {
                                    val nextCount = current.currentCount + 1
                                    val hitTarget = current.targetCount > 0 && nextCount % current.targetCount == 0
                                    triggerHapticFeedback(context, if (hitTarget) 65L else 20L)
                                    viewModel.incrementDhikr(current)
                                },
                                onResetRound = {
                                    viewModel.resetDhikrRound(current)
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }

                    item(key = "dhikr_list_header") {
                        Text(
                            text = "Tesbihat Listesi (Seçmek için dokunun)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    if (allDhikrs.isEmpty()) {
                        item(key = "empty_dhikrs") {
                            EmptyStateCard(
                                icon = Icons.Outlined.TouchApp,
                                title = "Tesbihat Bulunamadı",
                                subtitle = "Günlük zikir ve tesbihatlarınızı ekleyerek sayacınızı takip edebilirsiniz.",
                                actionButtonText = "+ Tesbihat Ekle",
                                onActionClick = { showAddDhikrDialog = true }
                            )
                        }
                    } else {
                        items(allDhikrs, key = { it.id }) { item ->
                            DhikrListItemCard(
                                dhikr = item,
                                isSelected = activeDhikr?.id == item.id,
                                onSelect = { selectedDhikrId = item.id },
                                onQuickIncrement = {
                                    triggerHapticFeedback(context, 20L)
                                    selectedDhikrId = item.id
                                    viewModel.incrementDhikr(item)
                                },
                                onDelete = { viewModel.deleteDhikr(item) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddQuoteDialog) {
        AddQuoteDialog(
            onDismiss = { showAddQuoteDialog = false },
            onConfirm = { text, source, category ->
                viewModel.addQuote(text, source, category)
                showAddQuoteDialog = false
            }
        )
    }

    if (showAddDhikrDialog) {
        AddDhikrDialog(
            onDismiss = { showAddDhikrDialog = false },
            onConfirm = { title, meaning, target ->
                viewModel.addDhikr(title, meaning, target)
                showAddDhikrDialog = false
            }
        )
    }

    if (showSettingsDialog) {
        QuoteNotificationSettingsDialog(
            isEnabled = notificationsEnabled,
            intervalHours = notificationInterval,
            isDarkMode = isDarkMode,
            onToggleDarkMode = { viewModel.setDarkMode(it) },
            onToggle = { viewModel.toggleNotifications(it) },
            onSelectInterval = { viewModel.setNotificationInterval(it) },
            onTestNotification = { viewModel.triggerTestNotification() },
            onDismiss = { showSettingsDialog = false }
        )
    }
}

@Composable
fun ActiveDhikrCounterCard(
    dhikr: DhikrEntity,
    onIncrement: () -> Unit,
    onResetRound: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (dhikr.targetCount > 0) {
        ((dhikr.currentCount % dhikr.targetCount).let { rem ->
            if (rem == 0 && dhikr.currentCount > 0) dhikr.targetCount else rem
        }.toFloat() / dhikr.targetCount).coerceIn(0f, 1f)
    } else 0f

    val completedRounds = if (dhikr.targetCount > 0) dhikr.currentCount / dhikr.targetCount else 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("active_dhikr_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                    Text(
                        text = if (completedRounds > 0) "Hedef: ${dhikr.targetCount} • $completedRounds. Devir" else "Hedef: ${dhikr.targetCount}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "Toplam: ${dhikr.totalLifetimeCount}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = dhikr.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (dhikr.arabicOrMeaning.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dhikr.arabicOrMeaning,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${dhikr.currentCount} / ${dhikr.targetCount}",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("dhikr_counter_value")
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onResetRound,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("dhikr_reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Turu Sıfırla",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sıfırla")
                }

                Button(
                    onClick = onIncrement,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("dhikr_increment_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+1 ZİKİR ÇEK",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun DhikrListItemCard(
    dhikr: DhikrEntity,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onQuickIncrement: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("dhikr_item_${dhikr.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dhikr.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                if (dhikr.arabicOrMeaning.isNotBlank()) {
                    Text(
                        text = dhikr.arabicOrMeaning,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tur: ${dhikr.currentCount}/${dhikr.targetCount} • Toplam: ${dhikr.totalLifetimeCount}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledTonalButton(
                    onClick = onQuickIncrement,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("+1", fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Sil",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddDhikrDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, meaning: String, target: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var meaning by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("33") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Yeni Tesbihat / Zikir Ekle",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Zikir Okunuşu (Örn: Lâ ilâhe illallah)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dhikr_input_title")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = meaning,
                    onValueChange = { meaning = it },
                    label = { Text("Anlamı / Fazileti (Opsiyonel)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) targetText = it },
                    label = { Text("Tur Hedefi (Örn: 33, 99, 100)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("İptal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val target = (targetText.toIntOrNull() ?: 33).coerceAtLeast(1)
                                onConfirm(title, meaning, target)
                            }
                        },
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("confirm_add_dhikr_btn")
                    ) {
                        Text("Kaydet")
                    }
                }
            }
        }
    }
}

@Composable
fun QuoteCardItem(
    quote: QuoteEntity,
    onToggleFavorite: () -> Unit,
    onDelete: (() -> Unit)?,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (categoryTitle, categoryColor) = when (quote.category) {
        QuoteCategory.AYET -> Pair("Âyet-i Kerîme", ColorAyet)
        QuoteCategory.HADIS -> Pair("Hadîs-i Şerîf", ColorHadis)
        QuoteCategory.HIKMET -> Pair("Hikmetli Söz", ColorHikmet)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quote_item_${quote.id}"),
        shape = RoundedCornerShape(14.dp),
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
                Text(
                    text = categoryTitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = categoryColor
                )

                Row {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (quote.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favori",
                            tint = if (quote.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Sil",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "“${quote.text}”",
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "— ${quote.source}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.SemiBold
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
fun AddQuoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (text: String, source: String, category: QuoteCategory) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(QuoteCategory.HIKMET) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Yeni Hikmetli Söz Ekle",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Kategori Seçin:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    QuoteCategory.values().forEach { cat ->
                        val isSelected = category == cat
                        val label = when (cat) {
                            QuoteCategory.AYET -> "Âyet"
                            QuoteCategory.HADIS -> "Hadîs"
                            QuoteCategory.HIKMET -> "Hikmet"
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { category = cat },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Söz veya Meal Metni") },
                    minLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_quote_text_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("Kaynak / Söyleyen (Örn: Hz. Ali, Bakara 152)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_quote_source_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("İptal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (text.isNotBlank()) {
                                onConfirm(text, source, category)
                            }
                        },
                        enabled = text.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("confirm_add_quote_btn")
                    ) {
                        Text("Kaydet")
                    }
                }
            }
        }
    }
}

@Composable
fun QuoteNotificationSettingsDialog(
    isEnabled: Boolean,
    intervalHours: Int,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggle: (Boolean) -> Unit,
    onSelectInterval: (Int) -> Unit,
    onTestNotification: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
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
                    Text(
                        text = "Görünüm & Bildirim Ayarları",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Siyah Mod (True Black Mode) Switch Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Siyah Mod (Gece Görünümü)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tam siyah arka plan ile göz yormayan ve pil dostu görünüm",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = onToggleDarkMode,
                        modifier = Modifier.testTag("settings_dark_mode_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hikmet Bildirimleri",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Günün âyet ve hadisleri telefonunuza bildirim olarak gelsin",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = onToggle,
                        modifier = Modifier.testTag("quote_notification_toggle")
                    )
                }

                if (isEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Bildirim Gönderme Sıklığı:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val intervals = listOf(
                        Pair(2, "Her 2 saatte"),
                        Pair(4, "Her 4 saatte"),
                        Pair(8, "Günde 2-3 kez"),
                        Pair(24, "Günde 1 kez")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        intervals.forEach { (hours, label) ->
                            val isSelected = intervalHours == hours
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectInterval(hours) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { onSelectInterval(hours) }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onTestNotification,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hemen Bir Bildirim Gönder")
                }
            }
        }
    }
}
