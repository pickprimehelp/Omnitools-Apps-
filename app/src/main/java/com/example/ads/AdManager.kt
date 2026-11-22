package com.example.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aistudio.omnitools.xkrvp.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

object AdManager {

    private const val TAG = "OmniToolsAds"

    const val APP_ID = "ca-app-pub-7584087725069099~5449667520"

    private const val PROD_BANNER_ID = "ca-app-pub-7584087725069099/8487278849"
    private const val PROD_INTERSTITIAL_ID = "ca-app-pub-7584087725069099/7969559042"
    private const val PROD_REWARDED_ID = "ca-app-pub-7584087725069099/3640752672"

    // Google official AdMob test IDs used in DEBUG builds so ads always fill on emulators/dev devices
    private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    val BANNER_ID: String
        get() = if (BuildConfig.DEBUG) TEST_BANNER_ID else PROD_BANNER_ID

    val INTERSTITIAL_ID: String
        get() = if (BuildConfig.DEBUG) TEST_INTERSTITIAL_ID else PROD_INTERSTITIAL_ID

    val REWARDED_ID: String
        get() = if (BuildConfig.DEBUG) TEST_REWARDED_ID else PROD_REWARDED_ID

    private val handler = Handler(Looper.getMainLooper())
    private val initScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var appContext: Context? = null
    private val _isInitialized = MutableStateFlow(false)
    val isInitializedFlow: StateFlow<Boolean> = _isInitialized.asStateFlow()

    @Volatile
    private var initializing = false

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    private var interstitialLoading = false
    private var rewardedLoading = false

    private var interstitialRetries = 0
    private var rewardedRetries = 0

    private var lastInterstitialTime = 0L
    private var screenChangeCount = 0

    private const val INTERSTITIAL_COOLDOWN = 45_000L
    private const val FORCED_COOLDOWN = 15_000L
    private const val SCREENS_BETWEEN_ADS = 2

    private fun runOnMain(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            handler.post(action)
        }
    }

    fun initialize(context: Context) {
        val safeContext = context.applicationContext
        appContext = safeContext

        if (_isInitialized.value || initializing) return
        initializing = true

        initScope.launch {
            try {
                MobileAds.initialize(safeContext) { initStatus ->
                    runOnMain {
                        initializing = false
                        _isInitialized.value = true
                        Log.d(TAG, "AdMob initialized (debug=${BuildConfig.DEBUG}): ${initStatus.adapterStatusMap.keys}")
                        preloadInterstitial(safeContext)
                        preloadRewarded(safeContext)
                    }
                }
            } catch (t: Throwable) {
                runOnMain {
                    initializing = false
                    Log.e(TAG, "MobileAds initialization error", t)
                }
            }
        }
    }

    fun preloadInterstitial(context: Context) {
        val safeContext = context.applicationContext
        appContext = safeContext

        runOnMain {
            if (!_isInitialized.value || interstitialAd != null || interstitialLoading) {
                return@runOnMain
            }
            interstitialLoading = true
            try {
                InterstitialAd.load(
                    safeContext,
                    INTERSTITIAL_ID,
                    AdRequest.Builder().build(),
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(ad: InterstitialAd) {
                            interstitialAd = ad
                            interstitialLoading = false
                            interstitialRetries = 0
                            Log.d(TAG, "Interstitial loaded")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            interstitialAd = null
                            interstitialLoading = false
                            Log.e(TAG, "Interstitial failed (${error.code}): ${error.message}")
                            retryInterstitial()
                        }
                    }
                )
            } catch (t: Throwable) {
                interstitialLoading = false
                Log.e(TAG, "Interstitial load exception", t)
                retryInterstitial()
            }
        }
    }

    private fun retryInterstitial() {
        if (interstitialRetries >= 4) return
        val delays = longArrayOf(6_000L, 15_000L, 35_000L, 60_000L)
        val delay = delays[interstitialRetries++]
        handler.postDelayed({
            appContext?.let { preloadInterstitial(it) }
        }, delay)
    }

    fun preloadRewarded(context: Context) {
        val safeContext = context.applicationContext
        appContext = safeContext

        runOnMain {
            if (!_isInitialized.value || rewardedAd != null || rewardedLoading) {
                return@runOnMain
            }
            rewardedLoading = true
            try {
                RewardedAd.load(
                    safeContext,
                    REWARDED_ID,
                    AdRequest.Builder().build(),
                    object : RewardedAdLoadCallback() {
                        override fun onAdLoaded(ad: RewardedAd) {
                            rewardedAd = ad
                            rewardedLoading = false
                            rewardedRetries = 0
                            Log.d(TAG, "Rewarded ad loaded")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            rewardedAd = null
                            rewardedLoading = false
                            Log.e(TAG, "Rewarded failed (${error.code}): ${error.message}")
                            retryRewarded()
                        }
                    }
                )
            } catch (t: Throwable) {
                rewardedLoading = false
                Log.e(TAG, "Rewarded load exception", t)
                retryRewarded()
            }
        }
    }

    private fun retryRewarded() {
        if (rewardedRetries >= 4) return
        val delays = longArrayOf(6_000L, 15_000L, 35_000L, 60_000L)
        val delay = delays[rewardedRetries++]
        handler.postDelayed({
            appContext?.let { preloadRewarded(it) }
        }, delay)
    }

    fun showInterstitialIfReady(
        activity: Activity,
        forceShow: Boolean = false,
        onDismissed: () -> Unit
    ) {
        runOnMain {
            if (!_isInitialized.value) {
                initialize(activity)
                onDismissed()
                return@runOnMain
            }

            screenChangeCount++
            val now = System.currentTimeMillis()
            val elapsed = now - lastInterstitialTime

            val shouldShow = if (forceShow) {
                lastInterstitialTime == 0L || elapsed >= FORCED_COOLDOWN
            } else {
                (lastInterstitialTime == 0L || elapsed >= INTERSTITIAL_COOLDOWN) &&
                    screenChangeCount >= SCREENS_BETWEEN_ADS
            }

            val ad = interstitialAd
            if (ad == null || !shouldShow || activity.isFinishing || activity.isDestroyed) {
                appContext?.let { preloadInterstitial(it) }
                onDismissed()
                return@runOnMain
            }

            interstitialAd = null
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    lastInterstitialTime = System.currentTimeMillis()
                    screenChangeCount = 0
                    appContext?.let { preloadInterstitial(it) }
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "Interstitial show error: ${error.message}")
                    appContext?.let { preloadInterstitial(it) }
                    onDismissed()
                }
            }

            try {
                ad.show(activity)
            } catch (t: Throwable) {
                Log.e(TAG, "Interstitial show exception", t)
                appContext?.let { preloadInterstitial(it) }
                onDismissed()
            }
        }
    }

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onDismissed: () -> Unit
    ) {
        runOnMain {
            val ad = rewardedAd
            if (ad == null || activity.isFinishing || activity.isDestroyed) {
                appContext?.let { preloadRewarded(it) }
                // Grant reward gracefully if ad isn't cached yet so user isn't blocked
                onRewardEarned()
                onDismissed()
                return@runOnMain
            }

            rewardedAd = null
            var rewardEarned = false

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appContext?.let { preloadRewarded(it) }
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "Rewarded show error: ${error.message}")
                    appContext?.let { preloadRewarded(it) }
                    onDismissed()
                }
            }

            try {
                ad.show(activity) {
                    if (!rewardEarned) {
                        rewardEarned = true
                        onRewardEarned()
                    }
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Rewarded show exception", t)
                appContext?.let { preloadRewarded(it) }
                onDismissed()
            }
        }
    }
}

private class BannerAdHolder(context: Context) {

    val container = FrameLayout(context)
    private val adView = AdView(context)
    private val handler = Handler(Looper.getMainLooper())

    var hasLoaded = false
    var isLoading = false
    var isDisposed = false

    private var retryCount = 0
    private var pendingRetry: Runnable? = null

    init {
        val density = context.resources.displayMetrics.density.takeIf { it > 0f } ?: 1f
        val widthDp = (context.resources.displayMetrics.widthPixels / density).toInt().coerceAtLeast(320)

        val size = try {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
        } catch (e: Exception) {
            Log.e("TopBannerAd", "Adaptive banner size fallback", e)
            AdSize.BANNER
        }

        val minHeightPx = size.getHeightInPixels(context).coerceAtLeast((50f * density).toInt())
        container.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        container.minimumHeight = minHeightPx

        adView.adUnitId = AdManager.BANNER_ID
        adView.setAdSize(size)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                hasLoaded = true
                isLoading = false
                retryCount = 0
                pendingRetry?.let { handler.removeCallbacks(it) }
                pendingRetry = null
                container.visibility = View.VISIBLE
                Log.d("TopBannerAd", "Banner loaded")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                isLoading = false
                Log.e("TopBannerAd", "Banner failed (${error.code}): ${error.message}")
                if (isDisposed || hasLoaded) return

                if (retryCount < 4) {
                    val delays = longArrayOf(3_000L, 7_000L, 15_000L, 30_000L)
                    val delay = delays[retryCount++]
                    val retry = Runnable {
                        pendingRetry = null
                        loadIfNeeded()
                    }
                    pendingRetry = retry
                    handler.postDelayed(retry, delay)
                }
            }
        }

        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        )
        container.addView(adView, params)
    }

    fun loadIfNeeded() {
        if (isDisposed || hasLoaded || isLoading) return
        isLoading = true
        try {
            adView.loadAd(AdRequest.Builder().build())
        } catch (t: Throwable) {
            isLoading = false
            Log.e("TopBannerAd", "Banner load exception", t)
        }
    }

    fun resume() {
        if (!isDisposed) {
            try {
                adView.resume()
            } catch (_: Throwable) {
            }
        }
    }

    fun pause() {
        if (!isDisposed) {
            try {
                adView.pause()
            } catch (_: Throwable) {
            }
        }
    }

    fun dispose() {
        if (isDisposed) return
        isDisposed = true
        pendingRetry?.let { handler.removeCallbacks(it) }
        pendingRetry = null
        handler.removeCallbacksAndMessages(null)
        try {
            container.removeAllViews()
            adView.pause()
            adView.destroy()
        } catch (_: Throwable) {
        }
    }
}

@Composable
fun TopBannerAd(modifier: Modifier = Modifier) {
    val holderState = remember { mutableStateOf<BannerAdHolder?>(null) }
    val isInitialized by AdManager.isInitializedFlow.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(isInitialized) {
        if (isInitialized) {
            holderState.value?.loadIfNeeded()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> holderState.value?.resume()
                Lifecycle.Event.ON_PAUSE -> holderState.value?.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            holderState.value?.dispose()
            holderState.value = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
            .heightIn(min = 50.dp)
            .testTag("banner_ad_container"),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp),
            factory = { context ->
                AdManager.initialize(context)
                val banner = BannerAdHolder(context)
                holderState.value = banner
                if (isInitialized) {
                    banner.loadIfNeeded()
                }
                banner.container
            },
            update = {
                holderState.value?.let { banner ->
                    if (isInitialized && !banner.hasLoaded && !banner.isLoading) {
                        banner.loadIfNeeded()
                    }
                }
            }
        )
    }
}
