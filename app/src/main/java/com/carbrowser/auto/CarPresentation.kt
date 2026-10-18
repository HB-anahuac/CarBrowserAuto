package com.carbrowser.auto

import android.app.Presentation
import android.content.Context
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.Display
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar

/**
 * Presentation rendered on the VirtualDisplay created from the Android Auto Surface.
 */
class CarPresentation(
    outerContext: Context,
    display: Display,
    private val initialUrl: String = "https://www.youtube.com"
) : Presentation(outerContext, display) {

    private lateinit var webView: WebView
    private lateinit var carToolbar: LinearLayout
    private lateinit var loadingBar: ProgressBar
    private lateinit var etCarUrl: EditText
    private lateinit var fullscreenContainer: FrameLayout
    private lateinit var btnShowToolbar: Button
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.presentation_browser)

        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        requestCarAudioFocus()

        initViews()
        setupWebView()
        setupListeners()

        webView.loadUrl(initialUrl)
    }

    private fun initViews() {
        webView = findViewById(R.id.carWebView)
        carToolbar = findViewById(R.id.carToolbar)
        loadingBar = findViewById(R.id.loadingBar)
        etCarUrl = findViewById(R.id.etCarUrl)
        fullscreenContainer = findViewById(R.id.fullscreenContainer)
        btnShowToolbar = findViewById(R.id.btnShowToolbar)
    }

    private fun setupWebView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false

        // Desktop User-Agent prevents YouTube from forcing redirect to YouTube app
        // and enables full HTML5 desktop/tablet video playback controls
        settings.userAgentString =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                // Prevent opening YouTube native app or external market links
                if (url.startsWith("vnd.youtube:") || url.startsWith("intent://")) {
                    return true
                }
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                loadingBar.visibility = View.VISIBLE
                if (url != null) {
                    etCarUrl.setText(url)
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                loadingBar.visibility = View.GONE
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                loadingBar.progress = newProgress
                if (newProgress >= 100) {
                    loadingBar.visibility = View.GONE
                }
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                // Handle Fullscreen HTML5 video (e.g. YouTube fullscreen button)
                if (customView != null) {
                    callback?.onCustomViewHidden()
                    return
                }
                customView = view
                customViewCallback = callback
                webView.visibility = View.GONE
                carToolbar.visibility = View.GONE
                fullscreenContainer.visibility = View.VISIBLE
                fullscreenContainer.addView(
                    view,
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            }

            override fun onHideCustomView() {
                if (customView == null) return
                fullscreenContainer.removeView(customView)
                fullscreenContainer.visibility = View.GONE
                customView = null
                customViewCallback?.onCustomViewHidden()
                webView.visibility = View.VISIBLE
                carToolbar.visibility = View.VISIBLE
            }
        }
    }

    private fun setupListeners() {
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            if (webView.canGoBack()) webView.goBack()
        }

        findViewById<Button>(R.id.btnForward).setOnClickListener {
            if (webView.canGoForward()) webView.goForward()
        }

        findViewById<Button>(R.id.btnReload).setOnClickListener {
            webView.reload()
        }

        findViewById<Button>(R.id.btnHome).setOnClickListener {
            webView.loadUrl("https://www.youtube.com")
        }

        findViewById<Button>(R.id.btnToggleToolbar).setOnClickListener {
            carToolbar.visibility = View.GONE
            btnShowToolbar.visibility = View.VISIBLE
        }

        btnShowToolbar.setOnClickListener {
            carToolbar.visibility = View.VISIBLE
            btnShowToolbar.visibility = View.GONE
        }

        etCarUrl.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                var url = etCarUrl.text.toString().trim()
                if (url.isNotEmpty()) {
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = if (url.contains(".") && !url.contains(" ")) {
                            "https://$url"
                        } else {
                            "https://www.google.com/search?q=$url"
                        }
                    }
                    webView.loadUrl(url)
                }
                true
            } else {
                false
            }
        }
    }

    private fun requestCarAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { /* handle car audio focus changes */ }
                .build()

            audioFocusRequest?.let { audioManager?.requestAudioFocus(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        webView.stopLoading()
        webView.destroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        }
    }
}
