package snake.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp
import snake.theme.ElectricYellow
import snake.theme.GridLine
import snake.theme.MatteBlack
import snake.theme.NeonBlue
import snake.theme.NeonCyan
import snake.theme.SurfaceDark
import snake.theme.neonBorder
import snake.ui.SnakeUiState


@Composable
fun GameGridNeon(uiState: SnakeUiState) {
    val gridSize = SnakeViewModelGridSizeFallback

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(18.dp))
            .neonBorder()
            .background(SurfaceDark.copy(alpha = 0.92f))
            .padding(8.dp)
            .drawBehind {
                // Draw grid cells procedurally on the canvas for performance
                val total = size.minDimension
                val spacing = 2.dp.toPx()
                val cellSize = (total - spacing * (gridSize - 1)) / gridSize

                fun cellTopLeft(row: Int, col: Int): Offset {
                    val x = col * (cellSize + spacing)
                    val y = row * (cellSize + spacing)
                    return Offset(x, y)
                }

                // background for each cell and glow if snake/energy
                for (row in 0 until gridSize) {
                    for (col in 0 until gridSize) {
                        val cell = Pair(row, col)
                        val isEnergy = uiState.apple == cell
                        val idxInSnake = uiState.snake.indexOf(cell)
                        val isBody = idxInSnake >= 0
                        val isHead = uiState.snake.firstOrNull() == cell

                        val snakeSize = uiState.snake.size
                        val minAlphaValue = 0.28f
                        val alpha = if (isBody) {
                            val current = (snakeSize - idxInSnake).toFloat() / snakeSize
                            maxOf(current, minAlphaValue)
                        } else 0f

                        val cellColor = when {
                            isHead -> NeonCyan
                            isBody -> NeonBlue.copy(alpha = alpha)
                            isEnergy -> ElectricYellow
                            else -> MatteBlack
                        }

                        val topLeft = cellTopLeft(row, col)
                        val rectSize = Size(cellSize, cellSize)

                        // draw base rect
                        drawRoundRect(
                            color = cellColor.copy(alpha = if (isBody || isHead || isEnergy) 1f else 0.95f),
                            topLeft = topLeft,
                            size = rectSize,
                            cornerRadius = CornerRadius(10f, 10f),
                            style = Fill
                        )

                        // draw subtle trace overlay
                        val traceBrush = Brush.linearGradient(
                            colors = listOf(
                                GridLine.copy(alpha = 0.15f),
                                Color.Transparent,
                                GridLine.copy(alpha = 0.10f)
                            ),
                            start = topLeft,
                            end = Offset(topLeft.x + rectSize.width, topLeft.y + rectSize.height)
                        )

                        drawRoundRect(
                            brush = traceBrush,
                            topLeft = topLeft,
                            size = rectSize,
                            cornerRadius = CornerRadius(10f, 10f),
                            alpha = if (isBody || isHead || isEnergy) 0.22f else 0.30f,
                            style = Fill
                        )

                        // glow
                        val glowColor = when {
                            isEnergy -> ElectricYellow
                            isHead -> NeonCyan
                            isBody -> NeonBlue
                            else -> Color.Transparent
                        }

                        if (glowColor != Color.Transparent) {
                            drawRoundRect(
                                color = glowColor.copy(alpha = if (isEnergy) 0.42f else 0.28f),
                                topLeft = topLeft,
                                size = rectSize,
                                cornerRadius = CornerRadius(10f, 10f),
                                style = Fill
                            )
                        }
                    }
                }
            }
    ) {}
}

private const val SnakeViewModelGridSizeFallback = 20
