package snake.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import snake.model.Direction
import kotlin.random.Random

sealed interface SnakeFxEvent {
    data object EnergyCollected : SnakeFxEvent
    data object GameOver : SnakeFxEvent
}

class SnakeViewModel : ViewModel() {

    private var gameJob: Job? = null

    private val _uiState = mutableStateOf(SnakeUiState())
    val uiState: State<SnakeUiState> get() = _uiState

    // Eventos de FX (som/choque/flash)
    private val _fxEvents = MutableSharedFlow<SnakeFxEvent>(extraBufferCapacity = 8)
    val fxEvents = _fxEvents.asSharedFlow()

    init {
        startGameLoop()
    }

    private fun startGameLoop() {
        gameJob = viewModelScope.launch {
            while (!uiState.value.isFinished) {
                delay(300)
                if (!uiState.value.isPaused) {
                    moveSnake()
                    checkCollisions()
                    updateGrid()
                }
            }
        }

        gameJob?.invokeOnCompletion {
            if (uiState.value.score > uiState.value.highScore) {
                _uiState.value = uiState.value.copy(highScore = uiState.value.score)
            }
        }
    }

    fun onDirectionChanged(newDirection: Direction) {
        if (!newDirection.isOpposite(uiState.value.direction)) {
            _uiState.value = uiState.value.copy(direction = newDirection)
        }
    }

    private suspend fun moveSnake() {
        with(_uiState.value) {
            val head = snake.first()
            val newHead = when (direction) {
                Direction.UP -> Pair(head.first - 1, head.second)
                Direction.DOWN -> Pair(head.first + 1, head.second)
                Direction.LEFT -> Pair(head.first, head.second - 1)
                Direction.RIGHT -> Pair(head.first, head.second + 1)
            }

            if (newHead.first in 0 until GRID_SIZE && newHead.second in 0 until GRID_SIZE) {
                snake.add(0, newHead)

                if (newHead == apple) {
                    // FX: energia coletada
                    _fxEvents.tryEmit(SnakeFxEvent.EnergyCollected)

                    _uiState.value = _uiState.value.copy(
                        apple = generateRandomApple(),
                        score = _uiState.value.score + 1,
                        highScore = maxOf(_uiState.value.score + 1, _uiState.value.highScore)
                    )
                } else {
                    snake.removeLast()
                }
            } else {
                delay(500)
                _fxEvents.tryEmit(SnakeFxEvent.GameOver)
                _uiState.value = _uiState.value.copy(isFinished = true)
            }
        }
    }

    private suspend fun checkCollisions() {
        val head = _uiState.value.snake.first()
        if (_uiState.value.snake.drop(1).contains(head)) {
            delay(500)
            _fxEvents.tryEmit(SnakeFxEvent.GameOver)
            _uiState.value = _uiState.value.copy(isFinished = true)
        }
    }

    private fun updateGrid() {
        val newGrid = MutableList(GRID_SIZE) { MutableList(GRID_SIZE) { false } }

        for (segment in _uiState.value.snake) {
            newGrid[segment.first][segment.second] = true
        }
        newGrid[_uiState.value.apple.first][_uiState.value.apple.second] = true

        _uiState.value = _uiState.value.copy(grid = newGrid)
    }

    private fun generateRandomApple(): Pair<Int, Int> {
        var position: Pair<Int, Int>
        do {
            position = Pair(Random.nextInt(GRID_SIZE), Random.nextInt(GRID_SIZE))
        } while (uiState.value.snake.contains(position))
        return position
    }

    private fun setPauseState(isPaused: Boolean) {
        if (_uiState.value.isPaused != isPaused) {
            _uiState.value = _uiState.value.copy(isPaused = isPaused)
        }
    }

    fun togglePause() = setPauseState(!uiState.value.isPaused)

    fun pauseGame() = setPauseState(true)

    fun restartGame() {
        _uiState.value = SnakeUiState().copy(highScore = uiState.value.highScore)
        gameJob?.cancel()
        startGameLoop()
    }

    companion object {
        const val GRID_SIZE = 20
    }
}
