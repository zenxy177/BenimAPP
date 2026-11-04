package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.notification.NotificationHelper
import com.example.ui.MainTab
import com.example.ui.MainViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channels
        NotificationHelper.createNotificationChannels(this)

        handleNavigationIntent(intent)

        setContent {
            MyApplicationTheme {
                MainAppScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNavigationIntent(intent)
    }

    private fun handleNavigationIntent(intent: Intent?) {
        when (intent?.getStringExtra("navigate_to")) {
            "quotes" -> viewModel.selectTab(MainTab.QUOTES)
            "reminders" -> viewModel.selectTab(MainTab.GOALS)
        }
    }
}

@Composable
fun MainAppScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Request notification permission on Android 13+ (API 33+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleNotifications(true)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        viewModel.uiMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Hardware / gesture back handling: return to Home tab first
    BackHandler(enabled = currentTab != MainTab.HOME) {
        viewModel.selectTab(MainTab.HOME)
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bottom_nav_bar"),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == MainTab.HOME,
                    onClick = { viewModel.selectTab(MainTab.HOME) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Bugün",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Bugün", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.HOME) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_item_home")
                )

                NavigationBarItem(
                    selected = currentTab == MainTab.QUOTES,
                    onClick = { viewModel.selectTab(MainTab.QUOTES) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainTab.QUOTES) Icons.Filled.AutoStories else Icons.Outlined.AutoStories,
                            contentDescription = "Hikmetler",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Hikmetler", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.QUOTES) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_item_quotes")
                )

                NavigationBarItem(
                    selected = currentTab == MainTab.SCHOOL,
                    onClick = { viewModel.selectTab(MainTab.SCHOOL) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainTab.SCHOOL) Icons.Filled.Assignment else Icons.Outlined.Assignment,
                            contentDescription = "Okul & Ödev",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Ödev & Sınav", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.SCHOOL) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_item_school")
                )

                NavigationBarItem(
                    selected = currentTab == MainTab.TIMETABLE,
                    onClick = { viewModel.selectTab(MainTab.TIMETABLE) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainTab.TIMETABLE) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                            contentDescription = "Ders Programı",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Ders Programı", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.TIMETABLE) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_item_timetable")
                )

                NavigationBarItem(
                    selected = currentTab == MainTab.GOALS,
                    onClick = { viewModel.selectTab(MainTab.GOALS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainTab.GOALS) Icons.Filled.TaskAlt else Icons.Outlined.TaskAlt,
                            contentDescription = "Hedefler",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Hedeflerim", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.GOALS) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_item_goals")
                )
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = currentTab,
            label = "tab_crossfade",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { tab ->
            when (tab) {
                MainTab.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateTab = { targetTab -> viewModel.selectTab(targetTab) }
                )
                MainTab.QUOTES -> QuotesScreen(
                    viewModel = viewModel
                )
                MainTab.SCHOOL -> SchoolScreen(
                    viewModel = viewModel
                )
                MainTab.TIMETABLE -> TimetableScreen(
                    viewModel = viewModel
                )
                MainTab.GOALS -> GoalsAndRemindersScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
