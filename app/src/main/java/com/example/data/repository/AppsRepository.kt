package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.example.data.db.PinnedAppDao
import com.example.data.db.PinnedAppEntity
import com.example.data.model.AppCategory
import com.example.data.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class AppsRepository(
    private val context: Context,
    private val pinnedAppDao: PinnedAppDao
) {
    private val packageManager: PackageManager = context.packageManager
    private val iconCache = ConcurrentHashMap<String, Bitmap>()

    suspend fun loadInstalledApps(): List<AppItem> = withContext(Dispatchers.IO) {
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = try {
            packageManager.queryIntentActivities(launcherIntent, 0)
        } catch (e: Exception) {
            emptyList()
        }

        val myPackage = context.packageName
        val items = mutableListOf<AppItem>()

        for (resolveInfo in resolveInfos) {
            val activityInfo = resolveInfo.activityInfo ?: continue
            val pkg = activityInfo.packageName
            if (pkg == myPackage) continue // Don't list ourselves as an app to launch inside our own launcher

            val label = try {
                resolveInfo.loadLabel(packageManager)?.toString()?.trim() ?: pkg
            } catch (e: Exception) {
                pkg
            }

            val bitmap = iconCache.getOrPut(pkg) {
                try {
                    val drawable = resolveInfo.loadIcon(packageManager)
                    drawableToBitmap(drawable)
                } catch (e: Exception) {
                    createFallbackBitmap(label)
                }
            }

            val category = detectCategory(pkg, label)
            val badgeColor = getDeterministicColor(pkg)

            items.add(
                AppItem(
                    id = pkg,
                    label = label,
                    packageName = pkg,
                    activityName = activityInfo.name,
                    iconBitmap = bitmap,
                    category = category,
                    badgeColor = badgeColor
                )
            )
        }

        // If very few apps exist (e.g. minimal emulator environment), provide high-fidelity default shortcuts
        if (items.size < 6) {
            val defaults = getEssentialDefaultApps()
            for (def in defaults) {
                if (items.none { it.packageName == def.packageName }) {
                    items.add(def)
                }
            }
        }

        // Sort alphabetically
        items.sortedBy { it.label.lowercase(Locale.getDefault()) }
    }

    fun getAllPinnedApps() = pinnedAppDao.getAllPinnedApps()

    suspend fun setPinnedToHome(packageName: String, label: String, isPinned: Boolean) = withContext(Dispatchers.IO) {
        pinnedAppDao.insertOrUpdate(
            PinnedAppEntity(
                packageName = packageName,
                label = label,
                isPinnedToHome = isPinned,
                isPinnedToDock = false
            )
        )
    }

    suspend fun setPinnedToDock(packageName: String, label: String, isDocked: Boolean) = withContext(Dispatchers.IO) {
        pinnedAppDao.insertOrUpdate(
            PinnedAppEntity(
                packageName = packageName,
                label = label,
                isPinnedToHome = false,
                isPinnedToDock = isDocked
            )
        )
    }

    private fun detectCategory(pkg: String, label: String): AppCategory {
        val lowerPkg = pkg.lowercase(Locale.ROOT)
        val lowerLabel = label.lowercase(Locale.ROOT)

        return when {
            lowerPkg.contains("dialer") || lowerPkg.contains("phone") || lowerPkg.contains("contact") ||
                    lowerPkg.contains("message") || lowerPkg.contains("sms") || lowerPkg.contains("chat") ||
                    lowerPkg.contains("telegram") || lowerPkg.contains("whatsapp") || lowerPkg.contains("mail") ||
                    lowerLabel.contains("phone") || lowerLabel.contains("call") || lowerLabel.contains("message") -> AppCategory.COMMUNICATION

            lowerPkg.contains("camera") || lowerPkg.contains("gallery") || lowerPkg.contains("photo") ||
                    lowerPkg.contains("music") || lowerPkg.contains("video") || lowerPkg.contains("audio") ||
                    lowerPkg.contains("youtube") || lowerPkg.contains("player") || lowerLabel.contains("camera") ||
                    lowerLabel.contains("music") || lowerLabel.contains("photos") -> AppCategory.MEDIA

            lowerPkg.contains("game") || lowerPkg.contains("play") && !lowerPkg.contains("store") -> AppCategory.GAMES

            lowerPkg.contains("calculator") || lowerPkg.contains("clock") || lowerPkg.contains("file") ||
                    lowerPkg.contains("setting") || lowerPkg.contains("tool") || lowerPkg.contains("browser") ||
                    lowerPkg.contains("chrome") || lowerLabel.contains("calculator") || lowerLabel.contains("settings") -> AppCategory.TOOLS

            else -> AppCategory.ALL
        }
    }

    private fun getDeterministicColor(pkg: String): Long {
        val palette = listOf(
            0xFF00E5FF, 0xFF7C4DFF, 0xFFFF5252, 0xFFFFD600,
            0xFF00E676, 0xFFFF4081, 0xFF651FFF, 0xFF00B0FF,
            0xFFFF9100, 0xFF1DE9B6
        )
        val index = Math.abs(pkg.hashCode()) % palette.size
        return palette[index]
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            val b = drawable.bitmap
            if (b.width > 160 || b.height > 160) {
                return Bitmap.createScaledBitmap(b, 128, 128, true)
            }
            return b
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth.coerceIn(72, 144) else 108
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight.coerceIn(72, 144) else 108
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun createFallbackBitmap(label: String): Bitmap {
        val size = 96
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#1E293B")
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 42f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val initial = label.firstOrNull()?.uppercaseChar()?.toString() ?: "A"
        val yPos = (size / 2f - (textPaint.descent() + textPaint.ascent()) / 2)
        canvas.drawText(initial, size / 2f, yPos, textPaint)
        return bitmap
    }

    private fun getEssentialDefaultApps(): List<AppItem> {
        return listOf(
            AppItem(
                id = "com.google.android.dialer",
                label = "Phone",
                packageName = "com.google.android.dialer",
                category = AppCategory.COMMUNICATION,
                badgeColor = 0xFF30D158,
                isPinnedToDock = true
            ),
            AppItem(
                id = "com.google.android.apps.messaging",
                label = "Messages",
                packageName = "com.google.android.apps.messaging",
                category = AppCategory.COMMUNICATION,
                badgeColor = 0xFF30D158,
                isPinnedToDock = true
            ),
            AppItem(
                id = "com.android.chrome",
                label = "Safari",
                packageName = "com.android.chrome",
                category = AppCategory.TOOLS,
                badgeColor = 0xFF0A84FF,
                isPinnedToDock = true
            ),
            AppItem(
                id = "com.google.android.GoogleCamera",
                label = "Camera",
                packageName = "com.google.android.GoogleCamera",
                category = AppCategory.MEDIA,
                badgeColor = 0xFF8E8E93,
                isPinnedToDock = true
            ),
            AppItem(
                id = "com.android.settings",
                label = "Settings",
                packageName = "com.android.settings",
                category = AppCategory.TOOLS,
                badgeColor = 0xFF8E8E93,
                isPinnedToHome = true
            ),
            AppItem(
                id = "com.google.android.calculator",
                label = "Calculator",
                packageName = "com.google.android.calculator",
                category = AppCategory.TOOLS,
                badgeColor = 0xFFFF9F0A,
                isPinnedToHome = true
            ),
            AppItem(
                id = "com.google.android.deskclock",
                label = "Clock",
                packageName = "com.google.android.deskclock",
                category = AppCategory.TOOLS,
                badgeColor = 0xFFFF9F0A,
                isPinnedToHome = true
            ),
            AppItem(
                id = "com.google.android.apps.photos",
                label = "Photos",
                packageName = "com.google.android.apps.photos",
                category = AppCategory.MEDIA,
                badgeColor = 0xFFFF375F,
                isPinnedToHome = true
            )
        )
    }
}
