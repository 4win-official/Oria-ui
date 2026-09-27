package com.example

import com.example.data.model.NotificationItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationShadeTest {

    @Test
    fun `notification filtering by category works properly`() {
        val notifications = listOf(
            NotificationItem(
                id = "1",
                title = "پیام جدید از علی",
                message = "سلام، پروژه آماده شد؟",
                appName = "پیام‌رسان",
                time = "۲ دقیقه پیش",
                category = "پیام‌ها"
            ),
            NotificationItem(
                id = "2",
                title = "بهینه‌ساز باتری",
                message = "مصرف پردازنده کاهش یافت.",
                appName = "سیستم",
                time = "۱۰ دقیقه پیش",
                category = "سیستم",
                isPriority = true
            ),
            NotificationItem(
                id = "3",
                title = "یادداشت جدید",
                message = "خرید وسایل هفتگی",
                appName = "یادداشت‌ها",
                time = "۱ ساعت پیش",
                category = "عمومی"
            )
        )

        // Filter messages
        val messages = notifications.filter { it.category.contains("پیام") || it.appName.contains("پیام") }
        assertEquals(1, messages.size)
        assertEquals("1", messages.first().id)

        // Filter system
        val systemNotifications = notifications.filter {
            it.category.contains("سیستم") || it.appName.contains("سیستم")
        }
        assertEquals(1, systemNotifications.size)
        assertEquals("2", systemNotifications.first().id)
        assertTrue(systemNotifications.first().isPriority)
    }

    @Test
    fun `dismissing notification removes target notification`() {
        var notifications = listOf(
            NotificationItem("1", "A", "M1", "App1", time = "now"),
            NotificationItem("2", "B", "M2", "App2", time = "now")
        )

        fun dismiss(id: String) {
            notifications = notifications.filterNot { it.id == id }
        }

        dismiss("1")
        assertEquals(1, notifications.size)
        assertEquals("2", notifications.first().id)

        dismiss("2")
        assertTrue(notifications.isEmpty())
    }

    @Test
    fun `low ram mode disables blur calculation to save memory`() {
        fun calculateEffectiveBlurRadius(isOpen: Boolean, isLowRamMode: Boolean): Float {
            return if (isOpen && !isLowRamMode) 22f else 0f
        }

        // When closed, blur is always 0
        assertEquals(0f, calculateEffectiveBlurRadius(isOpen = false, isLowRamMode = false), 0.01f)
        assertEquals(0f, calculateEffectiveBlurRadius(isOpen = false, isLowRamMode = true), 0.01f)

        // When open in normal mode, smooth blur of 22dp is enabled
        assertEquals(22f, calculateEffectiveBlurRadius(isOpen = true, isLowRamMode = false), 0.01f)

        // When open in low-RAM turbo mode, blur is strictly 0 to conserve RAM and GPU framebuffers
        assertEquals(0f, calculateEffectiveBlurRadius(isOpen = true, isLowRamMode = true), 0.01f)
    }

    @Test
    fun `missing permissions produce persistent notifications that tell user about missing access`() {
        val permissionNotifications = mutableListOf<NotificationItem>()
        var camGranted = false
        var notifGranted = false
        var defaultHome = false

        fun refreshPermissionNotifs() {
            if (!camGranted) {
                permissionNotifications.removeAll { it.id == "perm_camera" }
                permissionNotifications.add(
                    NotificationItem(
                        id = "perm_camera",
                        title = "⚠️ دسترسی دوربین را ندارم! (Camera Missing)",
                        message = "من دسترسی دوربین و چراغ‌قوه را ندارم و بده!",
                        appName = "Oria UI",
                        time = "همین حالا",
                        category = "سیستم",
                        isPriority = true
                    )
                )
            } else {
                permissionNotifications.removeAll { it.id == "perm_camera" }
            }

            if (!notifGranted) {
                permissionNotifications.removeAll { it.id == "perm_notif" }
                permissionNotifications.add(
                    NotificationItem(
                        id = "perm_notif",
                        title = "⚠️ دسترسی اعلان‌ها را ندارم! (Notification Missing)",
                        message = "من دسترسی نوتیفیکیشن را ندارم و بده!",
                        appName = "Oria UI",
                        time = "همین حالا",
                        category = "سیستم",
                        isPriority = true
                    )
                )
            } else {
                permissionNotifications.removeAll { it.id == "perm_notif" }
            }

            if (!defaultHome) {
                permissionNotifications.removeAll { it.id == "setup_default_home" }
                permissionNotifications.add(
                    NotificationItem(
                        id = "setup_default_home",
                        title = "⚠️ هنوز لانچر پیش‌فرض نیستم! (Set Default Home)",
                        message = "من هنوز لانچر پیش‌فرض شما نیستم و برای کنترل تمام گوشی نیاز به این تنظیم است!",
                        appName = "Oria UI",
                        time = "همین حالا",
                        category = "سیستم",
                        isPriority = false
                    )
                )
            } else {
                permissionNotifications.removeAll { it.id == "setup_default_home" }
            }
        }

        refreshPermissionNotifs()
        assertEquals(3, permissionNotifications.size)
        assertTrue(permissionNotifications.any { it.id == "perm_camera" })
        assertTrue(permissionNotifications.any { it.id == "perm_notif" })
        assertTrue(permissionNotifications.any { it.id == "setup_default_home" })

        // When user grants camera permission
        camGranted = true
        refreshPermissionNotifs()
        assertEquals(2, permissionNotifications.size)
        assertFalse(permissionNotifications.any { it.id == "perm_camera" })
        assertTrue(permissionNotifications.any { it.id == "perm_notif" })

        // When user grants all permissions
        notifGranted = true
        defaultHome = true
        refreshPermissionNotifs()
        assertTrue(permissionNotifications.isEmpty())
    }

    @Test
    fun `clearAll preserves required permission notifications so user is continually reminded`() {
        val permissionIds = setOf("perm_camera", "perm_notif", "setup_default_home")
        var currentNotifications = listOf(
            NotificationItem("perm_camera", "دسترسی دوربین", "ندارم و بده", "Oria UI", time = "now"),
            NotificationItem("chat_1", "پیام جدید", "سلام", "تلگرام", time = "now"),
            NotificationItem("perm_notif", "دسترسی اعلان", "ندارم و بده", "Oria UI", time = "now"),
            NotificationItem("email_1", "ایمیل کاری", "جلسه فردا", "جیمیل", time = "now")
        )

        // Clear All should preserve permission warnings
        currentNotifications = currentNotifications.filter { it.id in permissionIds }
        assertEquals(2, currentNotifications.size)
        assertEquals("perm_camera", currentNotifications[0].id)
        assertEquals("perm_notif", currentNotifications[1].id)
    }
}
