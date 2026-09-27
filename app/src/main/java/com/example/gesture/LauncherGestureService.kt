package com.example.gesture

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * Gestures supported by Aero Launcher.
 */
enum class LauncherGesture(val description: String) {
    SWIPE_UP("Swipe Up (App Library)"),
    SWIPE_DOWN("Swipe Down (Control Center & Notifications)"),
    DOUBLE_TAP("Double Tap"),
    LONG_PRESS("Long Press")
}

/**
 * Custom Gesture Detection Service for Aero Launcher.
 *
 * Responsibilities:
 * 1. Accumulates multi-frame touch coordinates to distinguish intentional swipes from accidental micro-movements.
 * 2. Compares vertical vs horizontal drag ratios to avoid conflicting with horizontal swipes.
 * 3. Enforces single-trigger execution per swipe gesture session (no rapid-fire re-triggers).
 * 4. Integrates optional haptic feedback on gesture recognition.
 * 5. Exposes reactive StateFlow of the latest detected gesture for UI or telemetry.
 */
class LauncherGestureService(
    private val onHaptic: (() -> Unit)? = null
) {
    private var accumulatedX: Float = 0f
    private var accumulatedY: Float = 0f
    private var isTriggered: Boolean = false

    private val _lastDetectedGesture = MutableStateFlow<LauncherGesture?>(null)
    val lastDetectedGesture: StateFlow<LauncherGesture?> = _lastDetectedGesture.asStateFlow()

    fun onDragStart() {
        accumulatedX = 0f
        accumulatedY = 0f
        isTriggered = false
    }

    fun onDragEnd() {
        accumulatedX = 0f
        accumulatedY = 0f
        isTriggered = false
    }

    fun onDragCancel() {
        accumulatedX = 0f
        accumulatedY = 0f
        isTriggered = false
    }

    /**
     * Processes drag deltas across frames.
     *
     * @param deltaX delta X for the frame
     * @param deltaY delta Y for the frame
     * @param thresholdPx threshold distance in pixels to trigger gesture
     * @return [LauncherGesture.SWIPE_UP] or [LauncherGesture.SWIPE_DOWN] if recognized, else null.
     */
    fun processVerticalDrag(deltaX: Float, deltaY: Float, thresholdPx: Float): LauncherGesture? {
        if (isTriggered) return null

        accumulatedX += deltaX
        accumulatedY += deltaY

        // Ensure vertical drag is dominant over horizontal movement (more forgiving ratio)
        val isVerticalDominant = abs(accumulatedY) > abs(accumulatedX) * 0.85f

        if (isVerticalDominant) {
            if (accumulatedY <= -thresholdPx) {
                isTriggered = true
                _lastDetectedGesture.value = LauncherGesture.SWIPE_UP
                onHaptic?.invoke()
                return LauncherGesture.SWIPE_UP
            } else if (accumulatedY >= thresholdPx) {
                isTriggered = true
                _lastDetectedGesture.value = LauncherGesture.SWIPE_DOWN
                onHaptic?.invoke()
                return LauncherGesture.SWIPE_DOWN
            }
        }
        return null
    }

    fun triggerDoubleTap(): LauncherGesture {
        _lastDetectedGesture.value = LauncherGesture.DOUBLE_TAP
        onHaptic?.invoke()
        return LauncherGesture.DOUBLE_TAP
    }

    fun triggerLongPress(): LauncherGesture {
        _lastDetectedGesture.value = LauncherGesture.LONG_PRESS
        onHaptic?.invoke()
        return LauncherGesture.LONG_PRESS
    }

    fun isGestureTriggered(): Boolean = isTriggered
    fun getAccumulatedY(): Float = accumulatedY
    fun getAccumulatedX(): Float = accumulatedX
}

/**
 * Compose modifier extension to bind custom gesture detection service to any UI element.
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.launcherGestures(
    gestureService: LauncherGestureService,
    thresholdDp: Float = 45f,
    enabled: Boolean = true,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
    onDoubleTap: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null
): Modifier = composed {
    if (!enabled) return@composed this

    val density = LocalDensity.current
    val thresholdPx = with(density) { thresholdDp.dp.toPx() }

    this
        .pointerInput(thresholdPx) {
            awaitPointerEventScope {
                while (true) {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    gestureService.onDragStart()
                    val currentPointer = down.id
                    var triggered = false

                    try {
                        while (true) {
                            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                            val change = event.changes.firstOrNull { it.id == currentPointer } ?: break
                            if (!change.pressed) {
                                gestureService.onDragEnd()
                                break
                            }

                            val dragDelta = change.position - change.previousPosition
                            if (dragDelta != androidx.compose.ui.geometry.Offset.Zero) {
                                val gesture = gestureService.processVerticalDrag(
                                    deltaX = dragDelta.x,
                                    deltaY = dragDelta.y,
                                    thresholdPx = thresholdPx
                                )
                                if (gesture != null && !triggered) {
                                    triggered = true
                                    change.consume()
                                    when (gesture) {
                                        LauncherGesture.SWIPE_UP -> onSwipeUp()
                                        LauncherGesture.SWIPE_DOWN -> onSwipeDown()
                                        else -> {}
                                    }
                                    break
                                }
                            }
                        }
                    } catch (e: Exception) {
                        gestureService.onDragCancel()
                    }
                }
            }
        }
        .combinedClickable(
            onClick = {},
            onDoubleClick = onDoubleTap?.let { action ->
                {
                    gestureService.triggerDoubleTap()
                    action()
                }
            },
            onLongClick = onLongPress?.let { action ->
                {
                    gestureService.triggerLongPress()
                    action()
                }
            }
        )
}
