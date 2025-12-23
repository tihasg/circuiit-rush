package snake.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                    // Run game tick logic on Default dispatcher to avoid blocking UI
                    withContext(Dispatchers.Default) {
                        performTick()
                    }
                }
            }
        }

        gameJob?.invokeOnCompletion {
            if (uiState.value.score > uiState.value.highScore) {
                _uiState.value = uiState.value.copy(highScore = uiState.value.score)
            }
        }
    }

    private fun performTick() {
        // Use local copy and immutable updates to keep state consistent
        val current = uiState.value
        val snake = current.snake.toMutableList()
        val head = snake.first()
        val newHead = when (current.direction) {
            Direction.UP -> Pair(head.first - 1, head.second)
            Direction.DOWN -> Pair(head.first + 1, head.second)
            Direction.LEFT -> Pair(head.first, head.second - 1)
            Direction.RIGHT -> Pair(head.first, head.second + 1)
        }

        if (newHead.first in 0 until GRID_SIZE && newHead.second in 0 until GRID_SIZE) {
            snake.add(0, newHead)

            if (newHead == current.apple) {
                // Emit FX event (non-blocking)
                _fxEvents.tryEmit(SnakeFxEvent.EnergyCollected)

                val newApple = generateRandomApple(snake)
                val newScore = current.score + 1
                val newHigh = maxOf(newScore, current.highScore)

                // Publish state on main thread via viewModelScope
                viewModelScope.launch {
                    _uiState.value = current.copy(
                        snake = snake.toList(),
                        apple = newApple,
                        score = newScore,
                        highScore = newHigh
                    )
                }
            } else {
                snake.removeLast()
                viewModelScope.launch {
                    _uiState.value = current.copy(snake = snake.toList())
                }
            }
        } else {
            // Hit wall -> game over
            viewModelScope.launch {
                delay(500)
                _fxEvents.tryEmit(SnakeFxEvent.GameOver)
                _uiState.value = _uiState.value.copy(isFinished = true)
            }
        }

        // Check self-collision (do on main thread update if detected)
        val headNow = snake.first()
        if (snake.drop(1).contains(headNow)) {
            viewModelScope.launch {
                delay(500)
                _fxEvents.tryEmit(SnakeFxEvent.GameOver)
                _uiState.value = _uiState.value.copy(isFinished = true)
            }
        }
    }

    fun onDirectionChanged(newDirection: Direction) {
        if (!newDirection.isOpposite(uiState.value.direction)) {
            _uiState.value = uiState.value.copy(direction = newDirection)
        }
    }

    private fun generateRandomApple(currentSnake: List<Pair<Int, Int>>): Pair<Int, Int> {
        var position: Pair<Int, Int>
        do {
            position = Pair(Random.nextInt(GRID_SIZE), Random.nextInt(GRID_SIZE))
        } while (currentSnake.contains(position))
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
