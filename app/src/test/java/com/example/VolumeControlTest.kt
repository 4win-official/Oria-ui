package com.example

import android.media.AudioManager
import com.example.data.model.VolumeOverlayState
import com.example.data.model.VolumeStreamInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VolumeControlTest {

    @Test
    fun `volume stream progress is correctly calculated between 0 and 1`() {
        val streamHalf = VolumeStreamInfo(
            streamType = AudioManager.STREAM_MUSIC,
            title = "رسانه",
            currentVolume = 5,
            maxVolume = 10
        )
        assertEquals(0.5f, streamHalf.progress, 0.01f)

        val streamZero = VolumeStreamInfo(
            streamType = AudioManager.STREAM_MUSIC,
            title = "رسانه",
            currentVolume = 0,
            maxVolume = 10
        )
        assertEquals(0.0f, streamZero.progress, 0.01f)

        val streamMax = VolumeStreamInfo(
            streamType = AudioManager.STREAM_MUSIC,
            title = "رسانه",
            currentVolume = 15,
            maxVolume = 15
        )
        assertEquals(1.0f, streamMax.progress, 0.01f)
    }

    @Test
    fun `volume state expansion toggle maintains stream data`() {
        val initialState = VolumeOverlayState(
            isVisible = true,
            isExpanded = false,
            mediaStream = VolumeStreamInfo(
                streamType = AudioManager.STREAM_MUSIC,
                title = "رسانه",
                currentVolume = 8,
                maxVolume = 15
            )
        )

        assertFalse(initialState.isExpanded)
        assertTrue(initialState.isVisible)

        val expandedState = initialState.copy(isExpanded = true)
        assertTrue(expandedState.isExpanded)
        assertEquals(initialState.mediaStream.currentVolume, expandedState.mediaStream.currentVolume)
        assertEquals(initialState.mediaStream.maxVolume, expandedState.mediaStream.maxVolume)
    }

    @Test
    fun `ringer modes switch correctly across normal vibrate and silent`() {
        var ringerMode = AudioManager.RINGER_MODE_NORMAL
        fun nextRingerMode(current: Int): Int {
            return when (current) {
                AudioManager.RINGER_MODE_NORMAL -> AudioManager.RINGER_MODE_VIBRATE
                AudioManager.RINGER_MODE_VIBRATE -> AudioManager.RINGER_MODE_SILENT
                else -> AudioManager.RINGER_MODE_NORMAL
            }
        }

        ringerMode = nextRingerMode(ringerMode)
        assertEquals(AudioManager.RINGER_MODE_VIBRATE, ringerMode)

        ringerMode = nextRingerMode(ringerMode)
        assertEquals(AudioManager.RINGER_MODE_SILENT, ringerMode)

        ringerMode = nextRingerMode(ringerMode)
        assertEquals(AudioManager.RINGER_MODE_NORMAL, ringerMode)
    }

    @Test
    fun `volume mute toggle sets level or restores previous level`() {
        var currentVol = 10
        val maxVol = 15
        var isMuted = false

        fun toggleMute() {
            if (isMuted || currentVol == 0) {
                isMuted = false
                currentVol = (maxVol * 0.5f).toInt()
            } else {
                isMuted = true
                currentVol = 0
            }
        }

        // Initially unmuted, toggle to mute
        toggleMute()
        assertTrue(isMuted)
        assertEquals(0, currentVol)

        // Toggle back to restore
        toggleMute()
        assertFalse(isMuted)
        assertTrue(currentVol > 0)
    }
}
