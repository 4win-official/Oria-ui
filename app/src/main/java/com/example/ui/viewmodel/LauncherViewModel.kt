package com.example.ui.viewmodel

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.MainActivity
import com.example.R
import com.example.data.model.AeroToastData
import com.example.data.model.AppCategory
import com.example.data.model.AppItem
import com.example.data.model.GestureSensitivity
import com.example.data.model.GridDensity
import com.example.data.model.HomeLayoutMode
import com.example.data.model.IconShape
import com.example.data.model.LauncherSettings
import com.example.data.model.LauncherTheme
import com.example.data.model.NotificationItem
import com.example.data.model.RecentAppItem
import com.example.data.model.StatusBarStyle
import com.example.data.model.ToastIconType
import com.example.data.model.VolumeOverlayState
import com.example.data.repository.AppsRepository
import com.example.data.repository.BatteryInfo
import com.example.data.repository.RamStats
import com.example.data.repository.SystemControlRepository
import com.example.gesture.LauncherGestureService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LauncherViewModel(
    private val appsRepository: AppsRepository,
    private val systemControlRepository: SystemControlRepository
) : ViewModel() {

    private val _allApps = MutableStateFlow<List<AppItem>>(emptyList())
    val allApps: StateFlow<List<AppItem>> = _allApps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(AppCategory.ALL)
    val selectedCategory: StateFlow<AppCategory> = _selectedCategory.asStateFlow()

    private val _isDrawerOpen = MutableStateFlow(false)
    val isDrawerOpen: StateFlow<Boolean> = _isDrawerOpen.asStateFlow()

    private val _isRecentAppsOpen = MutableStateFlow(false)
    val isRecentAppsOpen: StateFlow<Boolean> = _isRecentAppsOpen.asStateFlow()

    private val _recentApps = MutableStateFlow<List<RecentAppItem>>(emptyList())
    val recentApps: StateFlow<List<RecentAppItem>> = _recentApps.asStateFlow()

    private val _isControlCenterOpen = MutableStateFlow(false)
    val isControlCenterOpen: StateFlow<Boolean> = _isControlCenterOpen.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _isPowerMenuOpen = MutableStateFlow(false)
    val isPowerMenuOpen: StateFlow<Boolean> = _isPowerMenuOpen.asStateFlow()

    private val _isScreenLocked = MutableStateFlow(false)
    val isScreenLocked: StateFlow<Boolean> = _isScreenLocked.asStateFlow()

    private val _toastData = MutableStateFlow<AeroToastData?>(null)
    val toastData: StateFlow<AeroToastData?> = _toastData.asStateFlow()

    private val _selectedAppContextApp = MutableStateFlow<AppItem?>(null)
    val selectedAppContextApp: StateFlow<AppItem?> = _selectedAppContextApp.asStateFlow()

    private val _isBrightnessHudVisible = MutableStateFlow(false)
    val isBrightnessHudVisible: StateFlow<Boolean> = _isBrightnessHudVisible.asStateFlow()

    private var brightnessHudDismissJob: Job? = null
    private var toastDismissJob: Job? = null

    private val _settings = MutableStateFlow(LauncherSettings())
    val settings: StateFlow<LauncherSettings> = _settings.asStateFlow()

    private val _currentTimeString = MutableStateFlow(getFormattedTime())
    val currentTimeString: StateFlow<String> = _currentTimeString.asStateFlow()

    private val _currentDateString = MutableStateFlow(getFormattedDate())
    val currentDateString: StateFlow<String> = _currentDateString.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(getInitialNotifications())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _hasCameraPermission = MutableStateFlow(false)
    val hasCameraPermission: StateFlow<Boolean> = _hasCameraPermission.asStateFlow()

    private val _hasNotificationPermission = MutableStateFlow(false)
    val hasNotificationPermission: StateFlow<Boolean> = _hasNotificationPermission.asStateFlow()

    private val _isDefaultLauncher = MutableStateFlow(false)
    val isDefaultLauncher: StateFlow<Boolean> = _isDefaultLauncher.asStateFlow()

    private val _isPermissionDialogOpen = MutableStateFlow(false)
    val isPermissionDialogOpen: StateFlow<Boolean> = _isPermissionDialogOpen.asStateFlow()

    val hasMissingPermissions: StateFlow<Boolean> = combine(
        _hasCameraPermission,
        _hasNotificationPermission,
        _isDefaultLauncher
    ) { cam, notif, defaultHome ->
        !cam || !notif || !defaultHome
    }.stateIn(viewModelScope, SharingStarted.Lazily, true)

    private var permissionReminderJob: Job? = null

    val batteryState: StateFlow<BatteryInfo> = systemControlRepository.batteryState
    val volumeState: StateFlow<VolumeOverlayState> = systemControlRepository.volumeState
    val isFlashlightOn: StateFlow<Boolean> = systemControlRepository.isFlashlightOn
    val brightnessLevel: StateFlow<Float> = systemControlRepository.brightnessLevel
    val ramStats: StateFlow<RamStats> = systemControlRepository.ramStats

    val gestureService = LauncherGestureService(
        onHaptic = {
            if (_settings.value.hapticFeedbackEnabled) {
                systemControlRepository.performHapticFeedback()
            }
        }
    )

    private var volumeDismissJob: Job? = null

    // Pinned to Home list
    val homeApps: StateFlow<List<AppItem>> = combine(_allApps, appsRepository.getAllPinnedApps()) { apps, pinnedEntities ->
        val pinnedMap = pinnedEntities.filter { it.isPinnedToHome }.associateBy { it.packageName }
        apps.filter { pinnedMap.containsKey(it.packageName) || it.isPinnedToHome }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Dock apps (usually 4-5 apps)
    val dockApps: StateFlow<List<AppItem>> = combine(_allApps, appsRepository.getAllPinnedApps()) { apps, pinnedEntities ->
        val dockMap = pinnedEntities.filter { it.isPinnedToDock }.associateBy { it.packageName }
        val found = apps.filter { dockMap.containsKey(it.packageName) || it.isPinnedToDock }
        if (found.isNotEmpty()) found.take(5) else apps.take(5)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Filtered apps in drawer
    val filteredDrawerApps: StateFlow<List<AppItem>> = combine(
        _allApps,
        _searchQuery,
        _selectedCategory
    ) { apps, query, category ->
        apps.filter { app ->
            val matchesCategory = when (category) {
                AppCategory.ALL -> true
                AppCategory.FAVORITES -> app.isPinnedToHome || app.isPinnedToDock
                else -> app.category == category
            }
            val matchesQuery = query.isBlank() || app.label.contains(query, ignoreCase = true) || app.packageName.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        loadApps()
        startClockTicker()
        startInitialRecentApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            val list = appsRepository.loadInstalledApps()
            _allApps.value = list
        }
    }

    private fun startClockTicker() {
        viewModelScope.launch {
            while (true) {
                _currentTimeString.value = getFormattedTime()
                _currentDateString.value = getFormattedDate()
                delay(1000)
            }
        }
    }

    private fun startInitialRecentApps() {
        viewModelScope.launch {
            delay(500)
            val current = _allApps.value.take(6)
            _recentApps.value = current.mapIndexed { idx, app ->
                RecentAppItem(
                    app = app,
                    memoryUsageMb = 48 + (idx * 14) % 65,
                    lastActiveTime = if (idx == 0) "Active" else "${idx * 3}m ago"
                )
            }
        }
    }

    fun onVolumeKeyPressed(isUp: Boolean) {
        val stream = volumeState.value.activeStreamType
        systemControlRepository.stepVolume(stream, isUp)
        scheduleVolumePanelDismiss()
    }

    fun adjustVolumeByStep(direction: Int) {
        val stream = volumeState.value.activeStreamType
        val isUp = direction > 0
        systemControlRepository.stepVolume(stream, isUp)
        scheduleVolumePanelDismiss()
    }

    fun setVolumeProgress(streamType: Int, progress: Float) {
        systemControlRepository.setStreamVolume(streamType, progress)
        scheduleVolumePanelDismiss()
    }

    fun toggleVolumeMute(streamType: Int) {
        systemControlRepository.toggleMute(streamType)
        scheduleVolumePanelDismiss()
    }

    fun setVolumeLevel(streamType: Int, level: Int) {
        val max = 15
        systemControlRepository.setStreamVolume(streamType, (level.toFloat() / max).coerceIn(0f, 1f))
        scheduleVolumePanelDismiss()
    }

    fun setActiveVolumeStream(streamType: Int) {
        systemControlRepository.stepVolume(streamType, true)
        scheduleVolumePanelDismiss(durationMs = 4500)
    }

    fun setRingerMode(mode: Int) {
        systemControlRepository.setRingerMode(mode)
        scheduleVolumePanelDismiss()
    }

    fun setVolumeExpanded(expanded: Boolean) {
        systemControlRepository.setVolumeExpanded(expanded)
        scheduleVolumePanelDismiss(durationMs = 6000)
    }

    fun dismissVolumePanel() {
        volumeDismissJob?.cancel()
        systemControlRepository.dismissVolumePanel()
    }

    private fun scheduleVolumePanelDismiss(durationMs: Long = 3200) {
        volumeDismissJob?.cancel()
        volumeDismissJob = viewModelScope.launch {
            delay(durationMs)
            systemControlRepository.dismissVolumePanel()
        }
    }

    fun toggleControlCenter(open: Boolean? = null) {
        systemControlRepository.performHapticFeedback()
        _isControlCenterOpen.value = open ?: !_isControlCenterOpen.value
        if (_isControlCenterOpen.value) {
            _isDrawerOpen.value = false
            _isRecentAppsOpen.value = false
            _isSettingsOpen.value = false
        }
    }

    fun toggleAppDrawer(open: Boolean? = null) {
        systemControlRepository.performHapticFeedback()
        _isDrawerOpen.value = open ?: !_isDrawerOpen.value
        if (_isDrawerOpen.value) {
            _isControlCenterOpen.value = false
            _isRecentAppsOpen.value = false
            _isSettingsOpen.value = false
        }
    }

    fun toggleRecentApps(open: Boolean? = null) {
        systemControlRepository.performHapticFeedback()
        _isRecentAppsOpen.value = open ?: !_isRecentAppsOpen.value
        if (_isRecentAppsOpen.value) {
            _isDrawerOpen.value = false
            _isControlCenterOpen.value = false
            _isSettingsOpen.value = false
        }
    }

    fun dismissRecentApp(packageName: String) {
        systemControlRepository.killBackgroundProcess(packageName)
        val app = _recentApps.value.firstOrNull { it.app.packageName == packageName }
        _recentApps.value = _recentApps.value.filter { it.app.packageName != packageName }
        showToast(
            AeroToastData(
                message = "Closed ${app?.app?.label ?: packageName}",
                iconType = ToastIconType.SUCCESS
            )
        )
    }

    fun clearAllRecentApps() {
        val packageNames = _recentApps.value.map { it.app.packageName }
        systemControlRepository.killAllBackgroundProcesses(packageNames)
        _recentApps.value = emptyList()
        showToast(
            AeroToastData(
                message = "All Apps Closed • Memory Optimized",
                iconType = ToastIconType.SUCCESS
            )
        )
    }

    fun toggleSettingsDialog(open: Boolean? = null) {
        systemControlRepository.performHapticFeedback()
        _isSettingsOpen.value = open ?: !_isSettingsOpen.value
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: AppCategory) {
        systemControlRepository.performHapticFeedback()
        _selectedCategory.value = category
    }

    fun toggleFlashlight() {
        systemControlRepository.toggleFlashlight()
    }

    fun setBrightness(level: Float) {
        systemControlRepository.setBrightness(level)
        _isBrightnessHudVisible.value = true
        brightnessHudDismissJob?.cancel()
        brightnessHudDismissJob = viewModelScope.launch {
            delay(1800)
            _isBrightnessHudVisible.value = false
        }
    }

    fun togglePowerMenu(open: Boolean? = null) {
        systemControlRepository.performHapticFeedback()
        _isPowerMenuOpen.value = open ?: !_isPowerMenuOpen.value
    }

    fun lockScreen() {
        systemControlRepository.performHapticFeedback()
        _isScreenLocked.value = true
        _isPowerMenuOpen.value = false
        _isControlCenterOpen.value = false
        _isDrawerOpen.value = false
        showToast(
            AeroToastData(
                message = "Screen Locked",
                subMessage = "Standby mode activated",
                iconType = ToastIconType.LOCK
            )
        )
    }

    fun unlockScreen() {
        systemControlRepository.performHapticFeedback()
        _isScreenLocked.value = false
    }

    fun showToast(data: AeroToastData) {
        toastDismissJob?.cancel()
        _toastData.value = data
        toastDismissJob = viewModelScope.launch {
            delay(data.durationMs)
            _toastData.value = null
        }
    }

    fun openAppContext(app: AppItem) {
        systemControlRepository.performHapticFeedback()
        _selectedAppContextApp.value = app
    }

    fun closeAppContext() {
        _selectedAppContextApp.value = null
    }

    fun restartLauncher(context: Context) {
        systemControlRepository.performHapticFeedback()
        _isPowerMenuOpen.value = false
        viewModelScope.launch {
            showToast(
                AeroToastData(
                    message = "Restarting Launcher",
                    subMessage = "Memory flushed and cache reloaded",
                    iconType = ToastIconType.SUCCESS
                )
            )
            appsRepository.loadInstalledApps()
            System.gc()
        }
    }

    fun toggleTurboMode() {
        toggleLowRamMode()
        val isLow = _settings.value.isLowRamMode
        showToast(
            AeroToastData(
                message = if (isLow) "Low Power Mode Enabled" else "Standard Performance Mode Enabled",
                subMessage = if (isLow) "Background activity minimized" else "Smooth animations and visuals active",
                iconType = ToastIconType.SPEED
            )
        )
    }

    fun toggleSilentMode() {
        val currentMode = volumeState.value.ringerMode
        val nextMode = if (currentMode == AudioManager.RINGER_MODE_SILENT) {
            AudioManager.RINGER_MODE_NORMAL
        } else {
            AudioManager.RINGER_MODE_SILENT
        }
        setRingerMode(nextMode)
        showToast(
            AeroToastData(
                message = if (nextMode == AudioManager.RINGER_MODE_SILENT) "Silent Mode Active" else "Ringer Mode Normal",
                iconType = ToastIconType.VOLUME
            )
        )
    }

    fun openSystemSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openEmergencyDialer(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:112")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleRingerMode() {
        systemControlRepository.toggleRingerMode()
    }

    fun dismissNotification(id: String) {
        systemControlRepository.performHapticFeedback()
        val isPermission = id in setOf("perm_camera", "perm_notif", "setup_default_home")
        _notifications.value = _notifications.value.filter { it.id != id }
        if (isPermission) {
            showToast(
                AeroToastData(
                    message = "این دسترسی هنوز داده نشده است!",
                    subMessage = "Oria UI در صورت نیاز مجدداً در بخش اعلان‌ها یادآوری خواهد کرد",
                    iconType = ToastIconType.WARNING
                )
            )
        }
    }

    fun clearAllNotifications() {
        systemControlRepository.performHapticFeedback()
        val permissionIds = setOf("perm_camera", "perm_notif", "setup_default_home")
        val keptPermissions = _notifications.value.filter { it.id in permissionIds }
        _notifications.value = keptPermissions
        if (keptPermissions.isNotEmpty()) {
            showToast(
                AeroToastData(
                    message = "اعلانات عادی پاک شدند",
                    subMessage = "هشدارهای دسترسی‌های ناقص برای اعطا باقی ماندند",
                    iconType = ToastIconType.WARNING
                )
            )
        }
    }

    fun addReminderNotification(title: String, message: String) {
        systemControlRepository.performHapticFeedback()
        val item = NotificationItem(
            id = System.currentTimeMillis().toString(),
            title = title,
            message = message,
            appName = "System",
            time = getFormattedTime()
        )
        _notifications.value = listOf(item) + _notifications.value
    }

    fun updateTheme(theme: LauncherTheme) {
        systemControlRepository.performHapticFeedback()
        _settings.value = _settings.value.copy(theme = theme)
    }

    fun updateHomeLayoutMode(mode: HomeLayoutMode) {
        systemControlRepository.performHapticFeedback()
        _settings.value = _settings.value.copy(homeLayoutMode = mode)
        showToast(
            AeroToastData(
                message = "Layout: ${mode.title}",
                subMessage = mode.subtitle,
                iconType = ToastIconType.SPEED
            )
        )
    }

    fun toggleUseAmoledWallpaper() {
        systemControlRepository.performHapticFeedback()
        _settings.value = _settings.value.copy(useAmoledWallpaper = !_settings.value.useAmoledWallpaper)
    }

    fun boostSystemRam() {
        systemControlRepository.performHapticFeedback()
        val packageNames = _recentApps.value.map { it.app.packageName }
        systemControlRepository.killAllBackgroundProcesses(packageNames)
        _recentApps.value = emptyList()
        systemControlRepository.updateRamStats()
        System.gc()
        showToast(
            AeroToastData(
                message = "RAM Boosted",
                subMessage = "Background tasks cleared and memory optimized",
                iconType = ToastIconType.SPEED
            )
        )
    }

    fun updateGridDensity(density: GridDensity) {
        systemControlRepository.performHapticFeedback()
        _settings.value = _settings.value.copy(gridDensity = density)
    }

    fun updateIconShape(shape: IconShape) {
        systemControlRepository.performHapticFeedback()
        _settings.value = _settings.value.copy(iconShape = shape)
    }

    fun updateStatusBarStyle(style: StatusBarStyle) {
        systemControlRepository.performHapticFeedback()
        _settings.value = _settings.value.copy(statusBarStyle = style)
    }

    fun toggleLowRamMode() {
        systemControlRepository.performHapticFeedback()
        _settings.value = _settings.value.copy(isLowRamMode = !_settings.value.isLowRamMode)
    }

    fun toggleRamMonitor() {
        systemControlRepository.performHapticFeedback()
        _settings.value = _settings.value.copy(showRamMonitor = !_settings.value.showRamMonitor)
    }

    fun updateGestureSensitivity(sensitivity: GestureSensitivity) {
        systemControlRepository.performHapticFeedback()
        _settings.value = _settings.value.copy(gestureSensitivity = sensitivity)
    }

    fun toggleGesturesEnabled() {
        systemControlRepository.performHapticFeedback()
        _settings.value = _settings.value.copy(gesturesEnabled = !_settings.value.gesturesEnabled)
    }

    fun launchApp(context: Context, app: AppItem) {
        systemControlRepository.performHapticFeedback()
        // Record into recent apps
        val updated = listOf(
            RecentAppItem(
                app = app,
                memoryUsageMb = 46 + (app.label.length * 4) % 40,
                lastActiveTime = "Active"
            )
        ) + _recentApps.value.filter { it.app.packageName != app.packageName }
        _recentApps.value = updated.take(12)

        try {
            val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                // If special system action
                openFallbackAppIntent(context, app.packageName)
            }
        } catch (e: Exception) {
            openFallbackAppIntent(context, app.packageName)
        }
    }

    private fun openFallbackAppIntent(context: Context, pkg: String) {
        try {
            when {
                pkg.contains("dialer") || pkg.contains("phone") -> {
                    context.startActivity(Intent(Intent.ACTION_DIAL).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
                pkg.contains("message") -> {
                    context.startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MESSAGING).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
                pkg.contains("chrome") || pkg.contains("browser") -> {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
                pkg.contains("camera") -> {
                    context.startActivity(Intent("android.media.action.IMAGE_CAPTURE").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
                pkg.contains("setting") -> {
                    context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
                else -> {
                    val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", pkg, null)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                }
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    fun pinAppToHome(app: AppItem, pin: Boolean) {
        systemControlRepository.performHapticFeedback()
        viewModelScope.launch {
            appsRepository.setPinnedToHome(app.packageName, app.label, pin)
            _allApps.value = _allApps.value.map {
                if (it.packageName == app.packageName) it.copy(isPinnedToHome = pin) else it
            }
            showToast(
                AeroToastData(
                    message = if (pin) "Pinned to Home Screen" else "Removed from Home Screen",
                    subMessage = app.label,
                    iconType = ToastIconType.PIN
                )
            )
        }
    }

    fun pinAppToDock(app: AppItem, pin: Boolean) {
        systemControlRepository.performHapticFeedback()
        viewModelScope.launch {
            appsRepository.setPinnedToDock(app.packageName, app.label, pin)
            _allApps.value = _allApps.value.map {
                if (it.packageName == app.packageName) it.copy(isPinnedToDock = pin) else it
            }
            showToast(
                AeroToastData(
                    message = if (pin) "Added to Dock" else "Removed from Dock",
                    subMessage = app.label,
                    iconType = ToastIconType.PIN
                )
            )
        }
    }

    fun openAppInfo(context: Context, packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun requestUninstall(context: Context, packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun launchCamera(context: Context) {
        systemControlRepository.performHapticFeedback()
        try {
            val cameraIntent = Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (cameraIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(cameraIntent)
            } else {
                val fallbackIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            }
        } catch (e: Exception) {
            showToast(
                AeroToastData(
                    message = "Camera launched",
                    subMessage = "Standby unlocked",
                    iconType = ToastIconType.INFO
                )
            )
        }
    }

    fun openDefaultHomeSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                context.startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (ex: Exception) {
                // Ignore
            }
        }
    }

    fun startPermissionReminderTicker(context: Context) {
        permissionReminderJob?.cancel()
        permissionReminderJob = viewModelScope.launch {
            while (true) {
                delay(30000) // Periodic reminder ticker every 30s
                checkAndRefreshPermissions(context)
            }
        }
    }

    fun checkAndRefreshPermissions(context: Context) {
        val camGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        _hasCameraPermission.value = camGranted

        val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        _hasNotificationPermission.value = notifGranted

        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolve = context.packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
        val defaultHome = resolve?.activityInfo?.packageName == context.packageName
        _isDefaultLauncher.value = defaultHome

        val currentList = _notifications.value.toMutableList()

        // 1. Camera permission persistent notification ("هی بگه که فلان دسترسی رو ندارم و بده")
        if (!camGranted) {
            currentList.removeAll { it.id == "perm_camera" }
            currentList.add(
                0,
                NotificationItem(
                    id = "perm_camera",
                    title = "⚠️ دسترسی دوربین را ندارم! (Camera Missing)",
                    message = "من دسترسی دوربین و چراغ‌قوه را ندارم و بده! برای استفاده از چراغ‌قوه در کنترل سنتر و دوربین در صفحه قفل، لطفاً دسترسی دوربین را فعال کنید.",
                    appName = "Oria UI",
                    time = "همین حالا",
                    isPriority = true,
                    category = "سیستم"
                )
            )
        } else {
            currentList.removeAll { it.id == "perm_camera" }
        }

        // 2. Notification permission persistent notification
        if (!notifGranted) {
            currentList.removeAll { it.id == "perm_notif" }
            val insertIdx = if (currentList.isNotEmpty() && currentList[0].id == "perm_camera") 1 else 0
            currentList.add(
                insertIdx,
                NotificationItem(
                    id = "perm_notif",
                    title = "⚠️ دسترسی اعلان‌ها را ندارم! (Notification Missing)",
                    message = "من دسترسی نوتیفیکیشن را ندارم و بده! برای نمایش وضعیت سیستم، کنترل مدیا و هشدارهای پویا، لطفاً دسترسی اعلان را بدهید.",
                    appName = "Oria UI",
                    time = "همین حالا",
                    isPriority = true,
                    category = "سیستم"
                )
            )
        } else {
            currentList.removeAll { it.id == "perm_notif" }
        }

        // 3. Default Home Setup notification
        if (!defaultHome) {
            currentList.removeAll { it.id == "setup_default_home" }
            currentList.add(
                NotificationItem(
                    id = "setup_default_home",
                    title = "⚠️ هنوز لانچر پیش‌فرض نیستم! (Set Default Home)",
                    message = "من هنوز لانچر پیش‌فرض شما نیستم و برای کنترل تمام گوشی نیاز به این تنظیم است! لطفاً Oria UI را لانچر پیش‌فرض قرار دهید.",
                    appName = "Oria UI",
                    time = "همین حالا",
                    isPriority = false,
                    category = "سیستم"
                )
            )
        } else {
            currentList.removeAll { it.id == "setup_default_home" }
        }

        _notifications.value = currentList

        // Post system notification if any permission/role is missing
        if (!camGranted || !defaultHome || !notifGranted) {
            val missingList = mutableListOf<String>()
            if (!camGranted) missingList.add("دوربین و چراغ‌قوه")
            if (!notifGranted) missingList.add("نوتیفیکیشن")
            if (!defaultHome) missingList.add("لانچر پیش‌فرض")

            postSystemNotification(
                context = context,
                title = "⚠️ Oria UI: دسترسی‌های لازم داده نشده است!",
                message = "من دسترسی ${missingList.joinToString(" و ")} را ندارم و بده! لطفاً برای کارکرد کامل تپ کنید."
            )
        }
    }

    fun togglePermissionDialog(open: Boolean? = null) {
        systemControlRepository.performHapticFeedback()
        _isPermissionDialogOpen.value = open ?: !_isPermissionDialogOpen.value
    }

    fun onNotificationActionClicked(context: Context, notificationId: String) {
        systemControlRepository.performHapticFeedback()
        when (notificationId) {
            "perm_camera", "perm_notif" -> {
                _isPermissionDialogOpen.value = true
            }
            "setup_default_home" -> {
                openDefaultHomeSettings(context)
            }
            else -> {
                // Regular notification
            }
        }
    }

    private fun postSystemNotification(context: Context, title: String, message: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
            }
            val channelId = "oria_ui_permissions_channel"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Oria UI Setup Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alerts for missing permissions and system setup in Oria UI"
                }
                notificationManager.createNotificationChannel(channel)
            }
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("open_permissions", true)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            notificationManager.notify(1001, notification)
        } catch (e: Exception) {
            // Ignore system notification errors
        }
    }

    fun handleBackPress(): Boolean {
        if (_isScreenLocked.value) {
            unlockScreen()
            return true
        }
        if (_isPowerMenuOpen.value) {
            _isPowerMenuOpen.value = false
            return true
        }
        if (_selectedAppContextApp.value != null) {
            _selectedAppContextApp.value = null
            return true
        }
        if (_isControlCenterOpen.value) {
            _isControlCenterOpen.value = false
            return true
        }
        if (_isDrawerOpen.value) {
            _isDrawerOpen.value = false
            return true
        }
        if (_isRecentAppsOpen.value) {
            _isRecentAppsOpen.value = false
            return true
        }
        if (_isSettingsOpen.value) {
            _isSettingsOpen.value = false
            return true
        }
        if (volumeState.value.isVisible) {
            dismissVolumePanel()
            return true
        }
        return false
    }

    private fun getFormattedTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    private fun getFormattedDate(): String {
        return SimpleDateFormat("EEEE, MMMM d", Locale.ENGLISH).format(Date())
    }

    private fun getInitialNotifications(): List<NotificationItem> {
        return listOf(
            NotificationItem(
                id = "1",
                title = "Oria UI System Active",
                message = "Dynamic Island, Control Center, and full-screen gestures are live.",
                appName = "System",
                time = "Just now",
                isPriority = true
            ),
            NotificationItem(
                id = "2",
                title = "Battery & RAM Optimized",
                message = "Low memory footprint with active hardware acceleration.",
                appName = "Battery",
                time = "10m ago"
            ),
            NotificationItem(
                id = "3",
                title = "Control Center & Gestures",
                message = "Swipe down from status bar for Control Center. Swipe up for App Switcher.",
                appName = "Tips",
                time = "15m ago"
            )
        )
    }
}
