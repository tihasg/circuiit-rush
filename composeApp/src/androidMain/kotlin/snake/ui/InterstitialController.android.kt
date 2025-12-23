package snake.ui

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

private const val PREFS_NAME = "interstitial_prefs"
private const val KEY_FINISHED_RUNS = "finished_runs"
private const val KEY_LAST_AD_WAS_SHOWN = "last_ad_was_shown"
private const val SHOW_THRESHOLD = 5
private const val TEST_AD_UNIT_ID = "ca-app-pub-9197090603849099/3835986341" //  "ca-app-pub-3940256099942544/1033173712"

class AndroidInterstitialController(private val context: Context) : InterstitialController {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private var interstitialAd: InterstitialAd? = null
    private var activityRef: WeakReference<Activity?> = WeakReference(null)

    private val scope = MainScope()

    init {
        preload()
    }

    fun setActivity(activity: Activity?) {
        activityRef = WeakReference(activity)
    }

    override fun preload() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, TEST_AD_UNIT_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                // keep interstitialAd null; next calls will simply continue
                interstitialAd = null
            }
        })
    }

    override fun showIfReady(onComplete: () -> Unit) {
        // increment finished runs only when this is called from GameOver path
        val finishedRuns = prefs.getInt(KEY_FINISHED_RUNS, 0) + 1
        val lastWasShown = prefs.getBoolean(KEY_LAST_AD_WAS_SHOWN, false)

        prefs.edit().putInt(KEY_FINISHED_RUNS, finishedRuns).apply()

        // If already shown last run, flip the flag and continue
        if (lastWasShown) {
            prefs.edit().putBoolean(KEY_LAST_AD_WAS_SHOWN, false).apply()
            onComplete()
            return
        }

        // Check threshold
        if (finishedRuns >= SHOW_THRESHOLD && interstitialAd != null) {
            val activity = activityRef.get()
            if (activity == null) {
                // No valid activity — do not block
                onComplete()
                return
            }

            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Reset state and preload next
                    prefs.edit().putInt(KEY_FINISHED_RUNS, 0).putBoolean(KEY_LAST_AD_WAS_SHOWN, true).apply()
                    interstitialAd = null
                    preload()
                    onComplete()
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    // treat as not shown
                    preload()
                    onComplete()
                }

                override fun onAdShowedFullScreenContent() {
                    // no-op
                }
            }

            // Show ad
            try {
                interstitialAd?.show(activity)
            } catch (e: Exception) {
                // If any error, continue immediately
                onComplete()
            }
        } else {
            // Not ready or not reached threshold — continue without showing
            onComplete()
        }
    }
}

@Composable
actual fun rememberInterstitialController(): InterstitialController {
    val context = LocalContext.current
    val controller = remember { AndroidInterstitialController(context.applicationContext) }

    DisposableEffect(context) {
        val activity = context as? Activity
        controller.setActivity(activity)
        onDispose { controller.setActivity(null) }
    }

    LaunchedEffect(Unit) { controller.preload() }

    return controller
}

