package com.example.ui.components

import android.view.View
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView

/**
 * A custom surface implementation that leverages View.setLayerType(LAYER_TYPE_HARDWARE, null)
 * for critical UI elements to ensure zero-lag animations and high-performance transitions.
 */
@Composable
fun HardwareAcceleratedSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            FrameLayout(context).apply {
                // Force hardware layer type to cache rendering on GPU
                setLayerType(View.LAYER_TYPE_HARDWARE, null)

                val composeView = ComposeView(context).apply {
                    setContent {
                        content()
                    }
                }
                addView(
                    composeView,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                )
            }
        },
        update = { view ->
            view.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }
    )
}
