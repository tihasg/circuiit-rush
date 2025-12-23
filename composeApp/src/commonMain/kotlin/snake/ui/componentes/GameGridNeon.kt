package snake.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
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
    val gridSize = uiState.grid.size

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(18.dp))
            .neonBorder()
            .background(SurfaceDark.copy(alpha = 0.92f))
            .padding(8.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(gridSize),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(uiState.grid.flatten().size) { index ->
                val row = index / gridSize
                val column = index % gridSize
                val cell = Pair(row, column)

                val snakeSize = uiState.snake.size
                val headPosition = uiState.snake.first()

                val minAlphaValue = 0.28f
                val idxInSnake = uiState.snake.indexOf(cell)
                val alpha = if (idxInSnake >= 0) {
                    val current = (snakeSize - idxInSnake).toFloat() / snakeSize
                    maxOf(current, minAlphaValue)
                } else 0f

                val isHead = cell == headPosition
                val isBody = idxInSnake >= 0
                val isEnergy = uiState.apple == cell

                val cellColor = when {
                    isHead -> NeonCyan
                    isBody -> NeonBlue.copy(alpha = alpha)
                    isEnergy -> ElectricYellow
                    else -> MatteBlack
                }

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(cellColor.copy(alpha = if (isBody || isHead || isEnergy) 1f else 0.95f))
                        .drawWithCache {
                            val trace = Brush.linearGradient(
                                colors = listOf(
                                    GridLine.copy(alpha = 0.15f),
                                    Color.Transparent,
                                    GridLine.copy(alpha = 0.10f)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, size.height)
                            )

                            val glowColor = when {
                                isEnergy -> ElectricYellow
                                isHead -> NeonCyan
                                isBody -> NeonBlue
                                else -> Color.Transparent
                            }

                            onDrawBehind {
                                drawRect(
                                    brush = trace,
                                    alpha = if (isBody || isHead || isEnergy) 0.22f else 0.30f
                                )

                                if (glowColor != Color.Transparent) {
                                    drawRoundRect(
                                        color = glowColor.copy(alpha = if (isEnergy) 0.42f else 0.28f),
                                        cornerRadius = CornerRadius(18f, 18f),
                                        size = Size(size.width, size.height),
                                        style = Fill
                                    )
                                }
                            }
                        }
                )
            }
        }
    }
}