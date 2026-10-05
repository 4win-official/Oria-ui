package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.app.NotificationCompat

class TopTouchInterceptorService : Service() {

    private var windowManager: WindowManager? = null
    private var interceptorView: View? = null

    companion object {
        private const val CHANNEL_ID = "TopInterceptorChannel"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        addInterceptorOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun startForegroundServiceNotification() {
        try {
            val notificationManager = getSystemService(NotificationManager::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Touch Interceptor Service",
                    NotificationManager.IMPORTANCE_MIN
                ).apply {
                    description = "Keeps status bar interceptor active"
                }
                notificationManager?.createNotificationChannel(channel)
            }

            val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Oria Launcher")
                .setContentText("Touch interceptor active")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build()

            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addInterceptorOverlay() {
        try {
            if (interceptorView != null) return

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                140, // Expanded height in pixels to fully cover and suppress top edge / status bar pull-down
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }

            val view = FrameLayout(this).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                var startY = 0f
                var isDragging = false

                setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            startY = event.rawY
                            isDragging = false
                            true // Intercept touch event to fully suppress system status bar pull-down
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val deltaY = event.rawY - startY
                            if (deltaY > 12f && !isDragging) {
                                isDragging = true
                                val intent = Intent("com.example.OPEN_CONTROL_CENTER")
                                sendBroadcast(intent)
                            }
                            true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            isDragging = false
                            true
                        }
                        else -> false
                    }
                }
            }

            windowManager?.addView(view, params)
            interceptorView = view
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (interceptorView != null) {
                windowManager?.removeView(interceptorView)
                interceptorView = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
