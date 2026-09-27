package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.data.model.IconShape

@Composable
fun AppIconView(
    app: AppItem,
    iconShape: IconShape = IconShape.SQUIRCLE,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    val shape = when (iconShape) {
        IconShape.SQUIRCLE -> RoundedCornerShape(iconShape.cornerRadiusDp.dp)
        IconShape.ROUNDED_SQUARE -> RoundedCornerShape(iconShape.cornerRadiusDp.dp)
        IconShape.CIRCLE -> CircleShape
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape),
        contentAlignment = Alignment.Center
    ) {
        if (app.iconBitmap != null) {
            Image(
                bitmap = app.iconBitmap.asImageBitmap(),
                contentDescription = app.label,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Elegant geometric fallback icon
            val baseColor = Color(app.badgeColor)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                baseColor.copy(alpha = 0.85f),
                                baseColor.copy(alpha = 0.55f)
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.2f), shape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.label.firstOrNull()?.uppercaseChar()?.toString() ?: "A",
                    fontSize = (size.value * 0.42f).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
