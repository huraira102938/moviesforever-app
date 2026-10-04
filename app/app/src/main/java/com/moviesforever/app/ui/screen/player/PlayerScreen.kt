package com.moviesforever.app.ui.screen.player

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class PlayerActivity : Activity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Keep screen awake during playback
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 2. Fullscreen Immersive Mode
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // 3. Root FrameLayout to hold both WebView and UI controls
        val rootLayout = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.BLACK)
        }

        // 4. Initialize WebView
        webView = WebView(this).apply {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            setBackgroundColor(Color.BLACK)

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                mediaPlaybackRequiresUserGesture = false
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                userAgentString = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36"

                // --- AD BLOCKING SETTINGS ---
                setSupportMultipleWindows(false)
                javaScriptCanOpenWindowsAutomatically = false
            }

            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                    Log.d("PlayerActivity", "JS: ${consoleMessage?.message()}")
                    return true
                }
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url?.toString() ?: return false

                    // ALLOWED DOMAINS: Only allow the embed player and recognized media CDN domains
                    val allowedDomains = listOf("vidsrc", "vidplay", "mcloud", "megacloud", "cloudstream")
                    val isAllowed = allowedDomains.any { domain -> url.contains(domain, ignoreCase = true) }

                    if (!isAllowed) {
                        Log.d("PlayerActivity", "🛡️ Blocked Ad Redirect: $url")
                        return true // Intercept & block external ad redirects
                    }

                    return false // Allow legitimate stream requests
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    Log.d("PlayerActivity", "✅ Page Finished: $url")
                }
            }
        }

        rootLayout.addView(webView)

        // 5. Floating Back Button (Top-Left Overlay)
        val density = resources.displayMetrics.density
        val buttonSize = (42 * density).toInt()
        val margin = (16 * density).toInt()

        val backButton = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setColorFilter(Color.WHITE)
            setBackgroundColor(Color.parseColor("#80000000")) // Semi-transparent black circle/box

            layoutParams = FrameLayout.LayoutParams(buttonSize, buttonSize).apply {
                gravity = Gravity.TOP or Gravity.START
                topMargin = margin
                leftMargin = margin
            }

            setOnClickListener {
                finish() // Closes PlayerActivity cleanly and returns to the app
            }
        }

        rootLayout.addView(backButton)
        setContentView(rootLayout)

        // 6. Load Video Stream URL
        val videoUrl = intent.getStringExtra("EXTRA_VIDEO_URL")
        if (!videoUrl.isNullOrEmpty()) {
            Log.d("PlayerActivity", "🚀 Loading URL: $videoUrl")
            webView.loadUrl(videoUrl)
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        webView.stopLoading()
        webView.loadUrl("about:blank")
        webView.clearHistory()
        webView.removeAllViews()
        webView.destroy()
        super.onDestroy()
    }
}