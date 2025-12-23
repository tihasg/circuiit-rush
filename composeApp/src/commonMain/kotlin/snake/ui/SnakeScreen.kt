package snake.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.sin
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snake.composeapp.generated.resources.Res
import snake.composeapp.generated.resources.ic_down
import snake.composeapp.generated.resources.ic_left
import snake.composeapp.generated.resources.ic_pause
import snake.composeapp.generated.resources.ic_play
import snake.composeapp.generated.resources.ic_right
import snake.composeapp.generated.resources.ic_up
import snake.composeapp.generated.resources.*

// ========================
// Paleta "Circuit Rush"
// ========================
private val MatteBlack = Color(0xFF070A10)
private val SurfaceDark = Color(0xFF0B1020)
private val NeonCyan = Color(0xFF00E5FF)
private val NeonBlue = Color(0xFF2D6BFF)
private val ElectricYellow = Color(0xFFFFD400)
private val GridLine = Color(0xFF13213A)
private val TextPrimary = Color(0xFFEAF2FF)
private val TextSecondary = Color(0xFF9FB3D1)

// ========================
// Screen
// ========================
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
                GameOverNeon(
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

// ========================
// HUD
// ========================
@Composable
private fun HudHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(
                width = 1.dp,
                color = NeonCyan.copy(alpha = 0.35f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = stringResource(Res.string.title_game),
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(Res.string.subtitle_game),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = TextSecondary,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun HudScoreRow(score: Int, highScore: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.035f))
            .border(1.dp, NeonBlue.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(Res.string.label_energy, score),
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = stringResource(Res.string.label_max, highScore),
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

// ========================
// Grid neon
// ========================
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
                                drawRect(brush = trace, alpha = if (isBody || isHead || isEnergy) 0.22f else 0.30f)

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

// ========================
// Controls neon
// ========================
@Composable
fun DirectionButtonsNeon(viewModel: SnakeViewModel) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        NeonIconButton(
            resId = Res.drawable.ic_up,
            contentDescription = stringResource(Res.string.cd_up),
            onClick = { viewModel.onDirectionChanged(Direction.UP) }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NeonIconButton(
                resId = Res.drawable.ic_left,
                contentDescription = stringResource(Res.string.cd_left),
                onClick = { viewModel.onDirectionChanged(Direction.LEFT) }
            )

            NeonIconButton(
                resId = if (viewModel.uiState.value.isPaused) Res.drawable.ic_play else Res.drawable.ic_pause,
                contentDescription = stringResource(Res.string.cd_play_pause),
                highlight = true,
                onClick = { viewModel.togglePause() }
            )

            NeonIconButton(
                resId = Res.drawable.ic_right,
                contentDescription = stringResource(Res.string.cd_right),
                onClick = { viewModel.onDirectionChanged(Direction.RIGHT) }
            )
        }

        NeonIconButton(
            resId = Res.drawable.ic_down,
            contentDescription = stringResource(Res.string.cd_down),
            onClick = { viewModel.onDirectionChanged(Direction.DOWN) }
        )
    }
}

@Composable
private fun NeonIconButton(
    resId: Any,
    contentDescription: String,
    highlight: Boolean = false,
    onClick: () -> Unit
) {
    val stroke = if (highlight) ElectricYellow else NeonCyan
    val fill = if (highlight) ElectricYellow.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.06f)

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

// ========================
// Game Over neon
// ========================
@Composable
fun GameOverNeon(score: Int, highScore: Int, onRetryClicked: () -> Unit) {
    val text = if (score == highScore)
        stringResource(Res.string.game_over_new_high, score)
    else
        stringResource(Res.string.game_over_score, score)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .neonBorder()
            .padding(18.dp)
    ) {
        Text(
            text = stringResource(Res.string.game_over_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(text, fontSize = 16.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRetryClicked,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricYellow.copy(alpha = 0.16f))
        ) {
            Text(stringResource(Res.string.btn_restart), color = ElectricYellow, fontWeight = FontWeight.Bold)
        }
    }
}

// ========================
// Modifiers
// ========================
private fun Modifier.circuitBackground(): Modifier = this.drawBehind {
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

    pulse(Offset(size.width * 0.18f, size.height * 0.20f), size.minDimension * 0.45f, NeonCyan, 0.14f)
    pulse(Offset(size.width * 0.90f, size.height * 0.35f), size.minDimension * 0.55f, NeonBlue, 0.10f)
    pulse(Offset(size.width * 0.65f, size.height * 0.90f), size.minDimension * 0.60f, ElectricYellow, 0.06f)
}

private fun Modifier.neonBorder(): Modifier = this.then(
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
