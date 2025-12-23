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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp
import snake.theme.ElectricYellow
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
            .clip(RoundedCornerShape(14.dp))
            .neonBorder()
            .background(SurfaceDark.copy(alpha = 0.92f))
            .padding(6.dp)
            .drawBehind {
                // Simpler drawing per cell to reduce allocations and GPU work
                val total = size.minDimension
                val spacing = 2.dp.toPx()
                val cellSize = (total - spacing * (gridSize - 1)) / gridSize
                val corner = CornerRadius(cellSize * 0.12f, cellSize * 0.12f)

                fun cellTopLeft(row: Int, col: Int): Offset {
                    val x = col * (cellSize + spacing)
                    val y = row * (cellSize + spacing)
                    return Offset(x, y)
                }

                for (row in 0 until gridSize) {
                    for (col in 0 until gridSize) {
                        val cell = Pair(row, col)
                        val isEnergy = uiState.apple == cell
                        val idxInSnake = uiState.snake.indexOf(cell)
                        val isBody = idxInSnake >= 0
                        val isHead = uiState.snake.firstOrNull() == cell

                        val snakeSize = uiState.snake.size
                        val alpha = if (isBody) {
                            val current = (snakeSize - idxInSnake).toFloat() / snakeSize
                            kotlin.math.max(current, 0.28f)
                        } else 0f

                        val cellColor = when {
                            isHead -> NeonCyan
                            isBody -> NeonBlue.copy(alpha = alpha)
                            isEnergy -> ElectricYellow
                            else -> MatteBlack
                        }

                        val topLeft = cellTopLeft(row, col)
                        val rectSize = Size(cellSize, cellSize)

                        // Single draw per cell (fast path)
                        drawRoundRect(
                            color = cellColor,
                            topLeft = topLeft,
                            size = rectSize,
                            cornerRadius = corner,
                            style = Fill
                        )

                        // subtle inner highlight for head/energy to give small glow effect
                        if (isHead || isEnergy) {
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.06f),
                                topLeft = Offset(topLeft.x + rectSize.width * 0.08f, topLeft.y + rectSize.height * 0.08f),
                                size = Size(rectSize.width * 0.84f, rectSize.height * 0.84f),
                                cornerRadius = CornerRadius(corner.x * 0.8f, corner.y * 0.8f),
                                style = Fill
                            )
                        }
                    }
                }
            }
    ) {}
}

private const val SnakeViewModelGridSizeFallback = 20
