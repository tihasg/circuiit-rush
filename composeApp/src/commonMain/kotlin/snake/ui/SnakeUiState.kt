package snake.ui

import snake.model.Direction


data class SnakeUiState(
    val snake: List<Pair<Int, Int>> = listOf(Pair(7, 6), Pair(7, 7)),
    val apple: Pair<Int, Int> = Pair(4, 4),
    val score: Int = 0,
    val highScore: Int = 0,
    val isFinished: Boolean = false,
    val isPaused: Boolean = false,
    val direction: Direction = Direction.RIGHT
)