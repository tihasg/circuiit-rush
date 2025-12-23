package snake.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import snake.theme.ElectricYellow
import snake.theme.NeonCyan
import snake.theme.circuitBackground
import snake.ui.SnakeFxEvent
import snake.ui.SnakeViewModel
import snake.ui.componentes.DirectionButtonsNeon
import snake.ui.componentes.GameGridNeon
import snake.ui.componentes.HudHeader
import snake.ui.componentes.HudScoreRow
import snake.ui.rememberSfxController
import kotlin.math.sin

@Composable
fun SnakeScreen(viewModel: SnakeViewModel = viewModel { SnakeViewModel() }) {

    val uiState = viewModel.uiState.value
    val lifecycleOwner = LocalLifecycleOwner.current

    // FX: flash + shake
    var flashAlpha by remember { mutableStateOf(0f) }
    var shake by remember { mutableStateOf(0f) }

    // SFX controller (KMP: expect/actual)
    val sfx = rememberSfxController()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.pauseGame()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Escutar eventos de FX do ViewModel
    LaunchedEffect(Unit) {
        viewModel.fxEvents.collectLatest { event ->
            when (event) {
                SnakeFxEvent.EnergyCollected -> {
                    sfx.playZap()
                    sfx.vibrateTick()

                    // Flash rápido
                    flashAlpha = 1f
                    repeat(8) {
                        flashAlpha *= 0.68f
                        delay(16)
                    }
                    flashAlpha = 0f

                    // Shake curto
                    shake = 1f
                    repeat(10) {
                        shake *= 0.70f
                        delay(16)
                    }
                    shake = 0f
                }

                SnakeFxEvent.GameOver -> {
                    sfx.playGameOver()
                    sfx.vibrateError()
                }
            }
        }
    }

    // Tremor determinístico leve (sem random)
    var frame by remember { mutableStateOf(0f) }

    LaunchedEffect(shake) {
        while (shake > 0f) {
            frame += 1f
            delay(16) // ~60fps
        }
    }

    val dx = (sin(frame * 0.6f) * 7f * shake)
    val dy = (sin(frame * 0.9f) * 5f * shake)



    Box(
        modifier = Modifier
            .fillMaxSize()
            .circuitBackground()
            .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(12.dp))
                HudHeader()
                Spacer(Modifier.height(12.dp))

                HudScoreRow(
                    score = uiState.score,
                    highScore = uiState.highScore
                )

                Spacer(Modifier.height(12.dp))
            }

            if (uiState.isFinished) {
                GameOverScreen(
                    score = uiState.score,
                    highScore = uiState.highScore,
                    onRetryClicked = { viewModel.restartGame() }
                )
            } else {
                // Shake aplicado no grid
                Box(
                    modifier = Modifier.graphicsLayer {
                        translationX = dx
                        translationY = dy
                    }
                ) {
                    GameGridNeon(uiState = uiState)
                }
            }

            DirectionButtonsNeon(viewModel = viewModel)
        }

        // Flash overlay por cima
        if (flashAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ElectricYellow.copy(alpha = flashAlpha * 0.18f))
                    .drawBehind {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    NeonCyan.copy(alpha = flashAlpha * 0.18f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = size.minDimension * 0.85f
                            )
                        )
                    }
            )
        }
    }
}



