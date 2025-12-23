package snake.ui

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.chouaibmo.snake.R

@Composable
actual fun rememberSfxController(): SfxController {
    val context = LocalContext.current
    val controller = remember { AndroidSfxController(context) }

    DisposableEffect(Unit) {
        onDispose { controller.release() }
    }
    return controller
}

private class AndroidSfxController(context: Context) : SfxController {

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val zapId = soundPool.load(context, R.raw.zap, 1)
    private val gameOverId = soundPool.load(context, R.raw.game_over, 1)

    override fun playZap() {
        soundPool.play(zapId, 1f, 1f, 1, 0, 1f)
    }

    override fun playGameOver() {
        //soundPool.play(gameOverId, 1f, 1f, 1, 0, 1f)
    }

    override fun vibrateTick() = vibrateOneShot(25)

    override fun vibrateError() = vibrateOneShot(90)

    @SuppressLint("MissingPermission")
    private fun vibrateOneShot(ms: Long) {
        if (!vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(ms)
        }
    }

    fun release() {
        soundPool.release()
    }
}
