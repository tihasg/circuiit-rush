package snake.theme

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Modifier.neonBorder(): Modifier = this.then(
    Modifier
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    NeonCyan.copy(alpha = 0.70f),
                    NeonBlue.copy(alpha = 0.45f),
                    ElectricYellow.copy(alpha = 0.35f)
                )
            ),
            shape = RoundedCornerShape(18.dp)
        )
        .border(
            width = 2.dp,
            color = NeonCyan.copy(alpha = 0.14f),
            shape = RoundedCornerShape(18.dp)
        )
)

fun Modifier.circuitBackground(): Modifier = this.drawBehind {
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(MatteBlack, SurfaceDark, MatteBlack),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height)
        )
    )

    fun pulse(center: Offset, radius: Float, color: Color, alpha: Float) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = alpha), Color.Transparent),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )
    }

    pulse(
        Offset(size.width * 0.18f, size.height * 0.20f),
        size.minDimension * 0.45f,
        NeonCyan,
        0.14f
    )
    pulse(
        Offset(size.width * 0.90f, size.height * 0.35f),
        size.minDimension * 0.55f,
        NeonBlue,
        0.10f
    )
    pulse(
        Offset(size.width * 0.65f, size.height * 0.90f),
        size.minDimension * 0.60f,
        ElectricYellow,
        0.06f
    )
}
