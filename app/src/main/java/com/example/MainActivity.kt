package com.example

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.example.service.TopTouchInterceptorService
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.db.LauncherDatabase
import com.example.data.model.AeroToastData
import com.example.data.model.ToastIconType
import com.example.data.repository.AppsRepository
import com.example.data.repository.SystemControlRepository
import com.example.ui.components.AeroLockScreen
import com.example.ui.components.AeroPowerMenu
import com.example.ui.components.AeroToast
import com.example.ui.components.AppDrawerSheet
import com.example.ui.components.AppContextSheet
import com.example.ui.components.CustomStatusBar
import com.example.ui.components.HomeScreenContent
import com.example.ui.components.LauncherSettingsDialog
import com.example.ui.components.OriaPermissionSetupDialog
import com.example.ui.components.OriaRecentAppsOverlay
import com.example.ui.shade.NotificationShadeSheet
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.LauncherViewModel
import com.example.ui.volume.MinimalBrightnessHUD
import com.example.ui.volume.MinimalVolumePanel

class MainActivity : ComponentActivity() {

    private lateinit var launcherViewModel: LauncherViewModel

    private val controlCenterReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.OPEN_CONTROL_CENTER") {
                if (::launcherViewModel.isInitialized) {
                    launcherViewModel.toggleControlCenter(true)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemStatusBar()

        try {
            val serviceIntent = Intent(this, TopTouchInterceptorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                androidx.core.content.ContextCompat.startForegroundService(this, serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (e: Exception) {
            // ignore
        }

        val filter = IntentFilter("com.example.OPEN_CONTROL_CENTER")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(controlCenterReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(controlCenterReceiver, filter)
        }

        val database = LauncherDatabase.getDatabase(applicationContext)
        val appsRepository = AppsRepository(applicationContext, database.pinnedAppDao())
        val systemControlRepository = SystemControlRepository(applicationContext)

        launcherViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LauncherViewModel(appsRepository, systemControlRepository) as T
                }
            }
        )[LauncherViewModel::class.java]

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!launcherViewModel.handleBackPress()) {
                    // Already at home root
                }
            }
        })

        setContent {
            val settings by launcherViewModel.settings.collectAsState()
            val timeString by launcherViewModel.currentTimeString.collectAsState()
            val dateString by launcherViewModel.currentDateString.collectAsState()
            val batteryInfo by launcherViewModel.batteryState.collectAsState()
            val ramStats by launcherViewModel.ramStats.collectAsState()
            val notifications by launcherViewModel.notifications.collectAsState()
            val isFlashlightOn by launcherViewModel.isFlashlightOn.collectAsState()
            val brightnessLevel by launcherViewModel.brightnessLevel.collectAsState()
            val volumeState by launcherViewModel.volumeState.collectAsState()
            val isDrawerOpen by launcherViewModel.isDrawerOpen.collectAsState()
            val isRecentAppsOpen by launcherViewModel.isRecentAppsOpen.collectAsState()
            val recentApps by launcherViewModel.recentApps.collectAsState()
            val isControlCenterOpen by launcherViewModel.isControlCenterOpen.collectAsState()
            val isSettingsOpen by launcherViewModel.isSettingsOpen.collectAsState()
            val homeApps by launcherViewModel.homeApps.collectAsState()
            val dockApps by launcherViewModel.dockApps.collectAsState()
            val filteredDrawerApps by launcherViewModel.filteredDrawerApps.collectAsState()
            val searchQuery by launcherViewModel.searchQuery.collectAsState()
            val selectedCategory by launcherViewModel.selectedCategory.collectAsState()
            val isPowerMenuOpen by launcherViewModel.isPowerMenuOpen.collectAsState()
            val isScreenLocked by launcherViewModel.isScreenLocked.collectAsState()
            val toastData by launcherViewModel.toastData.collectAsState()
            val selectedAppContextApp by launcherViewModel.selectedAppContextApp.collectAsState()
            val isBrightnessHudVisible by launcherViewModel.isBrightnessHudVisible.collectAsState()
            val hasCameraPermission by launcherViewModel.hasCameraPermission.collectAsState()
            val hasNotificationPermission by launcherViewModel.hasNotificationPermission.collectAsState()
            val isDefaultLauncher by launcherViewModel.isDefaultLauncher.collectAsState()
            val isPermissionDialogOpen by launcherViewModel.isPermissionDialogOpen.collectAsState()

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                launcherViewModel.checkAndRefreshPermissions(this@MainActivity)
                val camGranted = permissions[Manifest.permission.CAMERA] ?: false
                val notifGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: true
                if (camGranted) {
                    launcherViewModel.showToast(
                        AeroToastData(
                            message = "دسترسی دوربین فعال شد ✓",
                            subMessage = "چراغ‌قوه و شورتکات دوربین در دسترس است",
                            iconType = ToastIconType.SUCCESS
                        )
                    )
                }
                if (notifGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    launcherViewModel.showToast(
                        AeroToastData(
                            message = "دسترسی اعلان‌ها فعال شد ✓",
                            subMessage = "هشدارهای پویا و نوتیفیکیشن‌ها فعال شدند",
                            iconType = ToastIconType.SUCCESS
                        )
                    )
                }
            }

            LaunchedEffect(Unit) {
                launcherViewModel.checkAndRefreshPermissions(this@MainActivity)
                launcherViewModel.startPermissionReminderTicker(this@MainActivity)

                val hasMissing = !launcherViewModel.hasCameraPermission.value ||
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !launcherViewModel.hasNotificationPermission.value) ||
                        !launcherViewModel.isDefaultLauncher.value

                if (intent.getBooleanExtra("open_permissions", false) || hasMissing) {
                    launcherViewModel.togglePermissionDialog(true)
                    val permsToAsk = mutableListOf<String>()
                    if (!launcherViewModel.hasCameraPermission.value) permsToAsk.add(Manifest.permission.CAMERA)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !launcherViewModel.hasNotificationPermission.value) {
                        permsToAsk.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    if (permsToAsk.isNotEmpty()) {
                        permissionLauncher.launch(permsToAsk.toTypedArray())
                    }
                }
            }

            MyApplicationTheme {
                val backgroundColor = Color(settings.theme.backgroundColor)
                val secondaryColor = Color(settings.theme.secondaryColor)

                val bgBrush = if (settings.isLowRamMode) {
                    Brush.verticalGradient(listOf(backgroundColor, backgroundColor))
                } else {
                    Brush.radialGradient(
                        colors = listOf(
                            secondaryColor.copy(alpha = 0.12f),
                            backgroundColor
                        ),
                        radius = 1200f
                    )
                }

                val topSwipeInterceptor = Modifier.pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            if (down.position.y < 150f) {
                                val pointerId = down.id
                                var totalY = 0f
                                var triggered = false
                                try {
                                    while (true) {
                                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                        val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                                        if (!change.pressed) break
                                        val deltaY = change.position.y - change.previousPosition.y
                                        totalY += deltaY
                                        if (totalY > 12f && !triggered) {
                                            triggered = true
                                            change.consume()
                                            launcherViewModel.toggleControlCenter(true)
                                            break
                                        }
                                    }
                                } catch (e: Exception) {
                                    // ignore
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                        .then(topSwipeInterceptor)
                ) {
                    // iOS 18 Dark Wallpaper
                    if (settings.useAmoledWallpaper) {
                        Image(
                            painter = painterResource(id = R.drawable.img_ios_wallpaper),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Deep iOS Dark Scrim
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFF000000).copy(alpha = 0.35f),
                                            Color(0xFF05070B).copy(alpha = 0.25f),
                                            Color(0xFF000000).copy(alpha = 0.65f)
                                        )
                                    )
                                )
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(bgBrush)
                        )
                    }

                    // Main Launcher Scaffold
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                    ) {
                        // Integrated Custom Status Bar (Replaces Default Android System Status Bar)
                        CustomStatusBar(
                            timeString = timeString,
                            batteryInfo = batteryInfo,
                            notificationCount = notifications.size,
                            isLowRamMode = settings.isLowRamMode,
                            settings = settings,
                            onOpenControlCenter = { launcherViewModel.toggleControlCenter(true) }
                        )

                        // Revolutionary Oria Home Screen View
                        HomeScreenContent(
                            timeString = timeString,
                            dateString = dateString,
                            batteryInfo = batteryInfo,
                            ramStats = ramStats,
                            homeApps = homeApps,
                            dockApps = dockApps,
                            settings = settings,
                            gestureService = launcherViewModel.gestureService,
                            onLaunchApp = { launcherViewModel.launchApp(this@MainActivity, it) },
                            onLongPressApp = { launcherViewModel.openAppContext(it) },
                            onOpenDrawer = { launcherViewModel.toggleAppDrawer(true) },
                            onOpenRecentApps = { launcherViewModel.toggleRecentApps(true) },
                            onOpenNotificationShade = { launcherViewModel.toggleControlCenter(true) },
                            onOpenSettings = { launcherViewModel.toggleSettingsDialog(true) },
                            onOpenPowerMenu = { launcherViewModel.togglePowerMenu(true) },
                            onDoubleTapHome = { launcherViewModel.lockScreen() },
                            onUnpinFromHome = { launcherViewModel.pinAppToHome(it, false) },
                            onOpenAppInfo = { launcherViewModel.openAppInfo(this@MainActivity, it) },
                            onSelectLayoutMode = { launcherViewModel.updateHomeLayoutMode(it) },
                            onBoostRam = { launcherViewModel.boostSystemRam() },
                            onToggleFlashlight = {
                                if (!hasCameraPermission) {
                                    permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                                    launcherViewModel.showToast(
                                        AeroToastData(
                                            message = "دسترسی دوربین برای چراغ‌قوه لازم است!",
                                            subMessage = "لطفاً دسترسی را اعطا کنید",
                                            iconType = ToastIconType.WARNING
                                        )
                                    )
                                } else {
                                    launcherViewModel.toggleFlashlight()
                                }
                            },
                            isFlashlightOn = isFlashlightOn,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Fast Modular App Drawer Sheet
                    AppDrawerSheet(
                        isOpen = isDrawerOpen,
                        apps = filteredDrawerApps,
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        settings = settings,
                        onClose = { launcherViewModel.toggleAppDrawer(false) },
                        onSearchQueryChange = { launcherViewModel.updateSearchQuery(it) },
                        onSelectCategory = { launcherViewModel.selectCategory(it) },
                        onLaunchApp = { launcherViewModel.launchApp(this@MainActivity, it) },
                        onLongPressApp = { launcherViewModel.openAppContext(it) },
                        onPinToHome = { launcherViewModel.pinAppToHome(it, true) },
                        onPinToDock = { launcherViewModel.pinAppToDock(it, true) },
                        onOpenAppInfo = { launcherViewModel.openAppInfo(this@MainActivity, it) },
                        onRequestUninstall = { launcherViewModel.requestUninstall(this@MainActivity, it) },
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .navigationBarsPadding()
                    )

                    // Modular Lightweight Notification Shade
                    NotificationShadeSheet(
                        isOpen = isControlCenterOpen,
                        timeString = timeString,
                        dateString = dateString,
                        batteryInfo = batteryInfo,
                        ramStats = ramStats,
                        isFlashlightOn = isFlashlightOn,
                        isLowRamMode = settings.isLowRamMode,
                        brightnessLevel = brightnessLevel,
                        mediaVolumeProgress = volumeState.mediaStream.progress,
                        notifications = notifications,
                        settings = settings,
                        onClose = { launcherViewModel.toggleControlCenter(false) },
                        onToggleFlashlight = { launcherViewModel.toggleFlashlight() },
                        onToggleLowRamMode = { launcherViewModel.toggleLowRamMode() },
                        onToggleRingerMode = { launcherViewModel.toggleRingerMode() },
                        onSetBrightness = { launcherViewModel.setBrightness(it) },
                        onSetVolume = { launcherViewModel.setVolumeProgress(volumeState.mediaStream.streamType, it) },
                        onDismissNotification = { launcherViewModel.dismissNotification(it) },
                        onClearAllNotifications = { launcherViewModel.clearAllNotifications() },
                        onAddSampleNotification = { title, msg -> launcherViewModel.addReminderNotification(title, msg) },
                        onNotificationAction = { notifId ->
                            when (notifId) {
                                "perm_camera" -> {
                                    val perms = mutableListOf(Manifest.permission.CAMERA)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                        perms.add(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    permissionLauncher.launch(perms.toTypedArray())
                                }
                                "perm_notif" -> {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                                    }
                                }
                                "setup_default_home" -> {
                                    launcherViewModel.openDefaultHomeSettings(this@MainActivity)
                                }
                                else -> {
                                    launcherViewModel.onNotificationActionClicked(this@MainActivity, notifId)
                                }
                            }
                        },
                        onOpenSettings = {
                            launcherViewModel.toggleControlCenter(false)
                            launcherViewModel.toggleSettingsDialog(true)
                        },
                        onOpenPowerMenu = {
                            launcherViewModel.toggleControlCenter(false)
                            launcherViewModel.togglePowerMenu(true)
                        },
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .navigationBarsPadding()
                    )

                    // Lightweight Minimalist Volume Control Panel
                    MinimalVolumePanel(
                        volumeState = volumeState,
                        settings = settings,
                        onSetVolume = { streamType, progress ->
                            launcherViewModel.setVolumeProgress(streamType, progress)
                        },
                        onToggleMute = { streamType ->
                            launcherViewModel.toggleVolumeMute(streamType)
                        },
                        onSetRingerMode = { mode ->
                            launcherViewModel.setRingerMode(mode)
                        },
                        onSetExpanded = { expanded ->
                            launcherViewModel.setVolumeExpanded(expanded)
                        },
                        onDismiss = { launcherViewModel.dismissVolumePanel() },
                        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                    )

                    // Modular Personalization Settings Sheet
                    LauncherSettingsDialog(
                        isOpen = isSettingsOpen,
                        settings = settings,
                        onClose = { launcherViewModel.toggleSettingsDialog(false) },
                        onUpdateTheme = { launcherViewModel.updateTheme(it) },
                        onUpdateHomeLayoutMode = { launcherViewModel.updateHomeLayoutMode(it) },
                        onToggleAmoledWallpaper = { launcherViewModel.toggleUseAmoledWallpaper() },
                        onUpdateGridDensity = { launcherViewModel.updateGridDensity(it) },
                        onUpdateIconShape = { launcherViewModel.updateIconShape(it) },
                        onUpdateStatusBarStyle = { launcherViewModel.updateStatusBarStyle(it) },
                        onUpdateGestureSensitivity = { launcherViewModel.updateGestureSensitivity(it) },
                        onToggleGesturesEnabled = { launcherViewModel.toggleGesturesEnabled() },
                        onToggleLowRamMode = { launcherViewModel.toggleLowRamMode() },
                        onToggleRamMonitor = { launcherViewModel.toggleRamMonitor() },
                        onSetDefaultLauncher = { launcherViewModel.openDefaultHomeSettings(this@MainActivity) }
                    )

                    // App Context Sheet (Replaces Stock Dropdowns & Menus)
                    AppContextSheet(
                        app = selectedAppContextApp,
                        settings = settings,
                        isHomePinned = homeApps.any { it.packageName == selectedAppContextApp?.packageName },
                        isDockPinned = dockApps.any { it.packageName == selectedAppContextApp?.packageName },
                        onDismiss = { launcherViewModel.closeAppContext() },
                        onTogglePinHome = {
                            selectedAppContextApp?.let { app ->
                                val isPinned = homeApps.any { it.packageName == app.packageName }
                                launcherViewModel.pinAppToHome(app, !isPinned)
                            }
                        },
                        onTogglePinDock = {
                            selectedAppContextApp?.let { app ->
                                val isPinned = dockApps.any { it.packageName == app.packageName }
                                launcherViewModel.pinAppToDock(app, !isPinned)
                            }
                        },
                        onOpenAppInfo = {
                            selectedAppContextApp?.let { app ->
                                launcherViewModel.openAppInfo(this@MainActivity, app.packageName)
                            }
                        },
                        onRequestUninstall = {
                            selectedAppContextApp?.let { app ->
                                launcherViewModel.requestUninstall(this@MainActivity, app.packageName)
                            }
                        }
                    )

                    // Aero Power Menu (Replaces Default Android Power Dialog)
                    AeroPowerMenu(
                        isOpen = isPowerMenuOpen,
                        settings = settings,
                        onClose = { launcherViewModel.togglePowerMenu(false) },
                        onLockScreen = { launcherViewModel.lockScreen() },
                        onRestartLauncher = { launcherViewModel.restartLauncher(this@MainActivity) },
                        onToggleTurboMode = { launcherViewModel.toggleTurboMode() },
                        onToggleSilentMode = { launcherViewModel.toggleSilentMode() },
                        onOpenSystemSettings = { launcherViewModel.openSystemSettings(this@MainActivity) },
                        onOpenEmergencyDialer = { launcherViewModel.openEmergencyDialer(this@MainActivity) }
                    )

                    // Brightness HUD Feedback Overlay
                    MinimalBrightnessHUD(
                        isVisible = isBrightnessHudVisible,
                        brightness = brightnessLevel,
                        primaryColor = Color(settings.theme.primaryColor),
                        cardBg = Color(settings.theme.cardColor).copy(alpha = 0.94f)
                    )

                    // Oria Lock Screen Overlay (Replaces System Ambient Lock)
                    AeroLockScreen(
                        isLocked = isScreenLocked,
                        timeString = timeString,
                        dateString = dateString,
                        batteryInfo = batteryInfo,
                        primaryColor = Color(settings.theme.primaryColor),
                        onUnlock = { launcherViewModel.unlockScreen() },
                        isFlashlightOn = isFlashlightOn,
                        onToggleFlashlight = {
                            if (!hasCameraPermission) {
                                permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                                launcherViewModel.showToast(
                                    AeroToastData(
                                        message = "دسترسی دوربین برای چراغ‌قوه لازم است!",
                                        subMessage = "لطفاً دسترسی را اعطا کنید",
                                        iconType = ToastIconType.WARNING
                                    )
                                )
                            } else {
                                launcherViewModel.toggleFlashlight()
                            }
                        },
                        onLaunchCamera = {
                            if (!hasCameraPermission) {
                                permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                                launcherViewModel.showToast(
                                    AeroToastData(
                                        message = "دسترسی دوربین داده نشده است!",
                                        subMessage = "لطفاً دسترسی را اعطا کنید",
                                        iconType = ToastIconType.WARNING
                                    )
                                )
                            } else {
                                launcherViewModel.launchCamera(this@MainActivity)
                            }
                        }
                    )

                    // Oria Recent Apps Switcher (Replaces Android Stock Overview/Recents)
                    OriaRecentAppsOverlay(
                        isOpen = isRecentAppsOpen,
                        recentApps = recentApps,
                        ramStats = ramStats,
                        settings = settings,
                        onClose = { launcherViewModel.toggleRecentApps(false) },
                        onLaunchApp = { launcherViewModel.launchApp(this@MainActivity, it) },
                        onDismissApp = { launcherViewModel.dismissRecentApp(it) },
                        onClearAll = { launcherViewModel.clearAllRecentApps() }
                    )

                    // Aero Heads-Up Toast (Replaces Stock Android Gray Toasts)
                    AeroToast(
                        toast = toastData,
                        primaryColor = Color(settings.theme.primaryColor),
                        cardBg = Color(settings.theme.cardColor),
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    // Oria Permission Setup Dialog (Total Mobile Transformation Privileges)
                    OriaPermissionSetupDialog(
                        isOpen = isPermissionDialogOpen,
                        hasCameraPermission = hasCameraPermission,
                        hasNotificationPermission = hasNotificationPermission,
                        isDefaultLauncher = isDefaultLauncher,
                        onRequestPermissions = {
                            val perms = mutableListOf<String>()
                            if (!hasCameraPermission) perms.add(Manifest.permission.CAMERA)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                perms.add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            if (perms.isNotEmpty()) {
                                permissionLauncher.launch(perms.toTypedArray())
                            }
                        },
                        onSetDefaultLauncher = {
                            launcherViewModel.openDefaultHomeSettings(this@MainActivity)
                        },
                        onDismiss = {
                            launcherViewModel.togglePermissionDialog(false)
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemStatusBar()
        if (::launcherViewModel.isInitialized) {
            launcherViewModel.checkAndRefreshPermissions(this)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("open_permissions", false)) {
            launcherViewModel.togglePermissionDialog(true)
        }
        if (::launcherViewModel.isInitialized) {
            launcherViewModel.checkAndRefreshPermissions(this)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemStatusBar()
        }
    }

    private fun hideSystemStatusBar() {
        try {
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_TOUCH
            insetsController.hide(WindowInsetsCompat.Type.statusBars())
        } catch (e: Exception) {
            // Fallback
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(controlCenterReceiver)
        } catch (e: Exception) {
            // ignore
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            launcherViewModel.onVolumeKeyPressed(isUp = true)
            return true
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            launcherViewModel.onVolumeKeyPressed(isUp = false)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
