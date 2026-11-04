package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import com.example.data.local.entity.QuoteCategory
import com.example.data.local.entity.QuoteEntity
import com.example.ui.MainViewModel
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val quotes by viewModel.allQuotes.collectAsState()
    val selectedCategory by viewModel.selectedQuoteCategory.collectAsState()
    val searchQuery by viewModel.quoteSearchQuery.collectAsState()
    val showFavoritesOnly by viewModel.showOnlyFavorites.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val notificationInterval by viewModel.notificationIntervalHours.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val filteredQuotes = remember(quotes, selectedCategory, searchQuery, showFavoritesOnly) {
        quotes.filter { quote ->
            val matchesCategory = selectedCategory == null || quote.category == selectedCategory
            val matchesFavorite = !showFavoritesOnly || quote.isFavorite
            val matchesSearch = searchQuery.isBlank() ||
                    quote.text.contains(searchQuery, ignoreCase = true) ||
                    quote.source.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesFavorite && matchesSearch
        }
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
                    .testTag("add_quote_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Yeni Hikmet Ekle")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("quotes_list"),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Header with Notification Controls Trigger
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Âyet, Hadîs & Hikmetler",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Gönlü ferahlatan ve yol gösteren sözler",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Notification quick alert banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
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
                                    modifier = Modifier.size(20.dp)
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
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("test_notification_btn")
                            ) {
                                Text("Test Bildirimi", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search text field
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
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quote_search_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category filter chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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

            // Quotes Cards List
            if (filteredQuotes.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Outlined.AutoStories,
                        title = "Eşleşen Söz Bulunamadı",
                        subtitle = "Arama kriterlerinizi değiştirebilir veya yeni bir hikmetli söz ekleyebilirsiniz.",
                        actionButtonText = "+ Yeni Söz Ekle",
                        onActionClick = { showAddDialog = true }
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
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }

    // Add Quote Dialog
    if (showAddDialog) {
        AddQuoteDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { text, source, category ->
                viewModel.addQuote(text, source, category)
                showAddDialog = false
            }
        )
    }

    // Notification Settings Dialog
    if (showSettingsDialog) {
        QuoteNotificationSettingsDialog(
            isEnabled = notificationsEnabled,
            intervalHours = notificationInterval,
            onToggle = { viewModel.toggleNotifications(it) },
            onSelectInterval = { viewModel.setNotificationInterval(it) },
            onTestNotification = { viewModel.triggerTestNotification() },
            onDismiss = { showSettingsDialog = false }
        )
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    color = categoryColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = categoryTitle,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = categoryColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

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

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "“${quote.text}”",
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Normal
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
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Yeni Hikmetli Söz Ekle",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Kategori Seçin:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Söz veya Meal Metni") },
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_quote_text_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("Kaynak / Söyleyen (Örn: Hz. Ali, Bakara 152)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_quote_source_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                        shape = RoundedCornerShape(12.dp),
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
    onToggle: (Boolean) -> Unit,
    onSelectInterval: (Int) -> Unit,
    onTestNotification: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bildirim Tercihleri",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Bildirim Gönderme Sıklığı:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val intervals = listOf(
                        Pair(2, "Her 2 saatte"),
                        Pair(4, "Her 4 saatte"),
                        Pair(8, "Günde 2-3 kez"),
                        Pair(24, "Günde 1 kez")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        intervals.forEach { (hours, label) ->
                            val isSelected = intervalHours == hours
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectInterval(hours) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
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

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onTestNotification()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
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
