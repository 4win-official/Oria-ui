package com.example.data.model

data class VolumeStreamInfo(
    val streamType: Int,
    val title: String,
    val currentVolume: Int,
    val maxVolume: Int,
    val isMuted: Boolean = false
) {
    val progress: Float
        get() = if (maxVolume > 0) (currentVolume.toFloat() / maxVolume.toFloat()).coerceIn(0f, 1f) else 0f
}

data class VolumeOverlayState(
    val isVisible: Boolean = false,
    val isExpanded: Boolean = false,
    val activeStreamType: Int = 3, // AudioManager.STREAM_MUSIC
    val mediaStream: VolumeStreamInfo = VolumeStreamInfo(3, "Media", 8, 15),
    val ringStream: VolumeStreamInfo = VolumeStreamInfo(2, "Ringtone", 5, 7),
    val alarmStream: VolumeStreamInfo = VolumeStreamInfo(4, "Alarm", 6, 7),
    val notificationStream: VolumeStreamInfo = VolumeStreamInfo(5, "Notifications", 5, 7),
    val ringerMode: Int = 2, // AudioManager.RINGER_MODE_NORMAL
    val outputDevice: String = "Speaker"
)
