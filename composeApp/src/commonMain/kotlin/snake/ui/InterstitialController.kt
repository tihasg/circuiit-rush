package snake.ui

import androidx.compose.runtime.Composable

interface InterstitialController {
    fun preload()
    fun showIfReady(onComplete: () -> Unit)
}

@Composable
expect fun rememberInterstitialController(): InterstitialController
