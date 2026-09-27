package com.example

import com.example.gesture.LauncherGesture
import com.example.gesture.LauncherGestureService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherGestureServiceTest {

    @Test
    fun `swipe up triggers SWIPE_UP when threshold is exceeded`() {
        var hapticCount = 0
        val service = LauncherGestureService(onHaptic = { hapticCount++ })

        service.onDragStart()
        val gesture = service.processVerticalDrag(deltaX = 0f, deltaY = -60f, thresholdPx = 45f)

        assertEquals(LauncherGesture.SWIPE_UP, gesture)
        assertEquals(LauncherGesture.SWIPE_UP, service.lastDetectedGesture.value)
        assertTrue(service.isGestureTriggered())
        assertEquals(1, hapticCount)

        // Ensure no re-triggering in same session
        val nextGesture = service.processVerticalDrag(deltaX = 0f, deltaY = -20f, thresholdPx = 45f)
        assertNull(nextGesture)
        assertEquals(1, hapticCount)
    }

    @Test
    fun `swipe down triggers SWIPE_DOWN for notification shade`() {
        var hapticCount = 0
        val service = LauncherGestureService(onHaptic = { hapticCount++ })

        service.onDragStart()
        val gesture = service.processVerticalDrag(deltaX = 2f, deltaY = 55f, thresholdPx = 45f)

        assertEquals(LauncherGesture.SWIPE_DOWN, gesture)
        assertEquals(LauncherGesture.SWIPE_DOWN, service.lastDetectedGesture.value)
        assertTrue(service.isGestureTriggered())
        assertEquals(1, hapticCount)
    }

    @Test
    fun `horizontal drag does not trigger vertical swipe gestures`() {
        val service = LauncherGestureService()

        service.onDragStart()
        // Strong horizontal swipe (deltaX 100f, deltaY 20f)
        val gesture = service.processVerticalDrag(deltaX = 100f, deltaY = 20f, thresholdPx = 45f)

        assertNull(gesture)
        assertFalse(service.isGestureTriggered())
        assertNull(service.lastDetectedGesture.value)
    }

    @Test
    fun `sub-threshold drag does not trigger gesture until threshold reached`() {
        val service = LauncherGestureService()

        service.onDragStart()
        // First small movement (15f < 45f)
        val g1 = service.processVerticalDrag(deltaX = 0f, deltaY = 15f, thresholdPx = 45f)
        assertNull(g1)
        assertFalse(service.isGestureTriggered())

        // Additional movement reaching 45f total
        val g2 = service.processVerticalDrag(deltaX = 0f, deltaY = 35f, thresholdPx = 45f)
        assertEquals(LauncherGesture.SWIPE_DOWN, g2)
        assertTrue(service.isGestureTriggered())
    }

    @Test
    fun `onDragEnd resets gesture state for future interactions`() {
        val service = LauncherGestureService()

        service.onDragStart()
        service.processVerticalDrag(deltaX = 0f, deltaY = -50f, thresholdPx = 40f)
        assertTrue(service.isGestureTriggered())

        service.onDragEnd()
        assertFalse(service.isGestureTriggered())
        assertEquals(0f, service.getAccumulatedY(), 0.001f)

        // Can now detect another gesture
        val secondGesture = service.processVerticalDrag(deltaX = 0f, deltaY = 50f, thresholdPx = 40f)
        assertEquals(LauncherGesture.SWIPE_DOWN, secondGesture)
    }

    @Test
    fun `double tap and long press trigger actions correctly`() {
        var hapticCount = 0
        val service = LauncherGestureService(onHaptic = { hapticCount++ })

        val dt = service.triggerDoubleTap()
        assertEquals(LauncherGesture.DOUBLE_TAP, dt)
        assertEquals(LauncherGesture.DOUBLE_TAP, service.lastDetectedGesture.value)
        assertEquals(1, hapticCount)

        val lp = service.triggerLongPress()
        assertEquals(LauncherGesture.LONG_PRESS, lp)
        assertEquals(LauncherGesture.LONG_PRESS, service.lastDetectedGesture.value)
        assertEquals(2, hapticCount)
    }
}
