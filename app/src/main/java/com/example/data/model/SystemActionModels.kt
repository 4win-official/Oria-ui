package com.example.data.model

/**
 * Toast feedback message displayed via custom Aero dynamic heads-up banner,
 * completely replacing stock Android system toasts.
 */
data class AeroToastData(
    val message: String,
    val subMessage: String? = null,
    val iconType: ToastIconType = ToastIconType.INFO,
    val durationMs: Long = 2400
)

enum class ToastIconType {
    INFO,
    SUCCESS,
    WARNING,
    BATTERY,
    VOLUME,
    SPEED,
    LOCK,
    PIN
}
