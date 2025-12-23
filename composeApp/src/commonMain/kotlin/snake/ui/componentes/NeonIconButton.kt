package snake.ui.componentes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import snake.theme.ElectricYellow
import snake.theme.NeonCyan

@Composable
fun NeonIconButton(
    resId: Any,
    contentDescription: String,
    highlight: Boolean = false,
    onClick: () -> Unit
) {
    val stroke = if (highlight) ElectricYellow else NeonCyan
    val fill =
        if (highlight) ElectricYellow.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.06f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(fill)
            .border(1.dp, stroke.copy(alpha = 0.60f), CircleShape)
            .drawBehind {
                drawCircle(
                    color = stroke.copy(alpha = 0.18f),
                    radius = size.minDimension * 0.62f,
                    center = center
                )
            }
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(resId as DrawableResource),
            contentDescription = contentDescription,
            modifier = Modifier.size(34.dp)
        )
    }
}
