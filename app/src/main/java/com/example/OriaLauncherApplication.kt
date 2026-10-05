package com.example

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.WindowManager

class OriaLauncherApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Register global activity lifecycle callbacks to enforce hardware acceleration across all activities and windows
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                try {
                    // Enforce hardware acceleration globally across all activity windows for maximum frame rates & smooth UI transitions
                    activity.window.setFlags(
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}
