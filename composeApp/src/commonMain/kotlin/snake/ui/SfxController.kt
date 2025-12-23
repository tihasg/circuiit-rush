package snake.ui

import androidx.compose.runtime.Composable

interface SfxController {
    fun playZap()
    fun playGameOver()
    fun vibrateTick()
    fun vibrateError()
}

@Composable
expect fun rememberSfxController(): SfxController