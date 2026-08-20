package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.AddTransactionDialog
import com.example.ui.DashboardScreen
import com.example.ui.SettingsScreen
import com.example.ui.OthersScreen
import com.example.ui.UpdateScreen
import com.example.ui.AboutScreen
import com.example.ui.FullScreenUpdateScreen
import com.example.ui.InstallPermissionDialog
import com.example.ui.ReleaseNotesScreen
import com.example.ui.TimelineScreen
import com.example.ui.theme.FinanceTrackerTheme
import com.example.viewmodel.FinanceViewModel
import com.example.data.UpdateInstaller
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import com.example.ui.PinScreen

import androidx.core.view.WindowCompat
import com.example.ui.FloatingCapsuleNavigationBar
import com.google.firebase.FirebaseApp

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : androidx.fragment.app.FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            val viewModel: FinanceViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val appTheme by viewModel.appTheme.collectAsState()
            val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val effectiveDarkTheme = when (themeMode) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemDark
            }

            // Sync system status bar appearance with active theme mode
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !effectiveDarkTheme
                }
            }

            val isAppLocked by viewModel.isAppLocked.collectAsState()
            val isOfflineGuest by viewModel.isOfflineGuest.collectAsState()
            val isUserSignedIn by viewModel.isUserSignedInFlow.collectAsState()
            val isEmailVerified by viewModel.isEmailVerifiedFlow.collectAsState()
            val isOnboardingComplete by viewModel.isOnboardingComplete.collectAsState()
            val showUpdateDialog by viewModel.showUpdateDialog.collectAsState()
            val availableUpdate by viewModel.availableUpdate.collectAsState()

            val showAuthScreen = !isUserSignedIn && !isOfflineGuest
            
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_STOP) {
                        viewModel.lockApp()
                    } else if (event == Lifecycle.Event.ON_RESUME) {
                        viewModel.triggerFetchFromCloud()
                        UpdateInstaller.onResumeCheck(this@MainActivity)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            val installPermissionRequest by UpdateInstaller.permissionRequest.collectAsState()

            FinanceTrackerTheme(darkTheme = effectiveDarkTheme, themeName = appTheme) {
                val rootNavController = rememberNavController()

                val startDest = remember(showAuthScreen, isUserSignedIn, isEmailVerified, isOnboardingComplete) {
                    if (showAuthScreen) "welcome_auth"
                    else if (isUserSignedIn && !isEmailVerified) "verification"
                    else if (!isOnboardingComplete) "main"
                    else "main"
                }

                LaunchedEffect(showAuthScreen) {
                    if (showAuthScreen) {
                        rootNavController.navigate("welcome_auth") {
                            popUpTo(rootNavController.graph.id) { inclusive = true }
                        }
                    }
                }

                NavHost(navController = rootNavController, startDestination = startDest) {
                    composable("welcome_auth") {
                        com.example.ui.WelcomeAuthScreen(
                            viewModel = viewModel,
                            onLoginSuccess = { 
                                if (!viewModel.isEmailVerifiedFlow.value) {
                                    rootNavController.navigate("verification") { popUpTo("welcome_auth") { inclusive = true } }
                                } else if (!viewModel.isOnboardingComplete.value) {
                                    viewModel.completeOnboarding() // Skip onboarding step
                                    rootNavController.navigate("main") { popUpTo("welcome_auth") { inclusive = true } }
                                } else {
                                    rootNavController.navigate("main") { popUpTo("welcome_auth") { inclusive = true } }
                                }
                            },
                            onBypass = { 
                                if (!viewModel.isOnboardingComplete.value) {
                                    viewModel.completeOnboarding() // Skip onboarding step
                                    rootNavController.navigate("main") { popUpTo("welcome_auth") { inclusive = true } }
                                } else {
                                    rootNavController.navigate("main") { popUpTo("welcome_auth") { inclusive = true } }
                                }
                            }
                        )
                    }
                    composable("verification") {
                        com.example.ui.EmailVerificationScreen(viewModel = viewModel, navController = rootNavController)
                    }
                    // composable("onboarding_balance") intentionally disabled
                    /* composable("onboarding_balance") {
                        com.example.ui.OnboardingBalanceScreen(
                            viewModel = viewModel,
                            onComplete = { 
                                viewModel.completeOnboarding()
                                rootNavController.navigate("main") { popUpTo("onboarding_balance") { inclusive = true } }
                            }
                        )
                    } */
                    composable("main") {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val navController = rememberNavController()
                    
                    // Track modal quick add state
                    var showAddDialog by remember { mutableStateOf(false) }

                // Retrieve active navigation route
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"
                
                val timelineScrollState = rememberLazyListState()
                val isFabVisible by remember {
                    derivedStateOf {
                        !timelineScrollState.isScrollInProgress ||
                        timelineScrollState.firstVisibleItemScrollOffset == 0
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize().testTag("app_scaffold"),
                    topBar = {
                        if (currentRoute == "dashboard" || currentRoute == "timeline") {
                            val selectedCalendar by viewModel.selectedCalendar.collectAsState()
                            val monthNameFormatter = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
                            val formattedMonth = monthNameFormatter.format(selectedCalendar.time)
                            
                            CenterAlignedTopAppBar(
                                title = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.wrapContentSize().testTag("unified_month_selector")
                                    ) {
                                        IconButton(
                                            onClick = { viewModel.previousMonth() },
                                            modifier = Modifier.testTag("prev_month_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Previous Month",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Text(
                                            text = formattedMonth.uppercase(),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier
                                                .padding(horizontal = 8.dp)
                                                .testTag("selected_month_label")
                                        )

                                        IconButton(
                                            onClick = { viewModel.nextMonth() },
                                            modifier = Modifier.testTag("next_month_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = "Next Month",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                },
                                actions = {
                                    IconButton(
                                        onClick = { 
                                            val nextMode = if (effectiveDarkTheme) "Light" else "Dark"
                                            viewModel.setThemeMode(nextMode)
                                        },
                                        modifier = Modifier.testTag("theme_toggle_btn")
                                    ) {
                                        Icon(
                                            imageVector = if (effectiveDarkTheme) Icons.Default.WbSunny else Icons.Default.ModeNight, 
                                            contentDescription = "Toggle Theme", 
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = "dashboard",
                            modifier = Modifier.fillMaxSize(),
                            enterTransition = { 
                                androidx.compose.animation.slideInHorizontally(animationSpec = androidx.compose.animation.core.tween(150)) + androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(150))
                            },
                            exitTransition = { 
                                androidx.compose.animation.slideOutHorizontally(animationSpec = androidx.compose.animation.core.tween(150)) + androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(150))
                            },
                            popEnterTransition = { 
                                androidx.compose.animation.slideInHorizontally(animationSpec = androidx.compose.animation.core.tween(150)) + androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(150))
                            },
                            popExitTransition = { 
                                androidx.compose.animation.slideOutHorizontally(animationSpec = androidx.compose.animation.core.tween(150)) + androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(150))
                            }
                        ) {
                            composable("dashboard") {
                                DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToSettings = {
                                        navController.navigate("settings") {
                                            popUpTo("dashboard") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                            composable("timeline") {
                                TimelineScreen(viewModel = viewModel, lazyListState = timelineScrollState)
                            }
                            composable("yearly") {
                                com.example.ui.AnalyticsScreen(viewModel = viewModel)
                            }
                            composable("settings") {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onNavigateToProfile = { navController.navigate("profile") },
                                    onNavigateToAuth = { rootNavController.navigate("welcome_auth") },
                                    onNavigateToOthers = { navController.navigate("others") },
                                    onNavigateToDataManagement = { navController.navigate("data_management") },
                                    onNavigateToTheme = { navController.navigate("theme") }
                                )
                            }
                            composable("theme") {
                                com.example.ui.ThemeScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("data_management") {
                                com.example.ui.DataManagementScreen(
                                    viewModel = viewModel, 
                                    onBack = { navController.popBackStack() },
                                    onNavigateToSummary = { navController.navigate("summary") }
                                )
                            }
                            composable("summary") {
                                com.example.ui.YearlySummaryScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("others") {
                                OthersScreen(viewModel = viewModel, onBack = { navController.popBackStack() }, onNavigateToUpdate = { navController.navigate("update") })
                            }
                            composable("update") {
                                UpdateScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() },
                                    onNavigateToAbout = { navController.navigate("about") },
                                    onNavigateToReleaseNotes = { navController.navigate("release_notes") }
                                )
                            }
                            composable("release_notes") {
                                ReleaseNotesScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                            }
                            composable("about") {
                                AboutScreen(onBack = { navController.popBackStack() })
                            }
                            composable("profile") {
                                AccountSettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                            }
                        }

                        // Floating Capsule Navigation Bar with quick add circular FAB
                        val isMainTab = currentRoute in listOf("dashboard", "timeline", "yearly", "settings")
                        if (isMainTab) {
                            FloatingCapsuleNavigationBar(
                                currentRoute = currentRoute,
                                isDarkTheme = effectiveDarkTheme,
                                onNavigate = { route ->
                                    if (currentRoute != route) {
                                        navController.navigate(route) {
                                            popUpTo("dashboard") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                onQuickAddClick = { showAddDialog = true },
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }

                        // Render quick-add transaction dialogue
                        if (showAddDialog) {
                            AddTransactionDialog(
                                onDismiss = { showAddDialog = false },
                                viewModel = viewModel
                            )
                        }

                        if (isAppLocked) {
                            PinScreen(onVerify = { pin -> viewModel.verifyPin(pin) }, onUnlocked = { viewModel.unlockApp() })
                        }
                    } // End of Scaffold content Box
                } // End of Scaffold
            } // End of Box(Modifier.fillMaxSize()) in composable("main")
        } // End of composable("main")
    } // End of root NavHost

    if (showUpdateDialog && availableUpdate != null) {
        FullScreenUpdateScreen(
            updateInfo = availableUpdate!!,
            onDismiss = {
                viewModel.dismissUpdateDialog(availableUpdate!!.version)
            },
            onUpdate = {
                viewModel.dismissUpdateDialog(availableUpdate!!.version)
            }
        )
    }

    if (installPermissionRequest != null) {
        InstallPermissionDialog(
            request = installPermissionRequest!!,
            onAllow = {
                UpdateInstaller.onPermissionAllow(this@MainActivity)
            },
            onCancel = { dontAskAgain ->
                UpdateInstaller.onPermissionCancel(this@MainActivity, dontAskAgain)
            }
        )
    }
} // End of Theme
} // End of setContent
} // End of onCreate
} // End of MainActivity class
