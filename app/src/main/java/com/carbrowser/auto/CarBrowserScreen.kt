package com.carbrowser.auto

import android.content.Context
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarColor
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate

class CarBrowserScreen(carContext: CarContext) : Screen(carContext) {

    private var virtualDisplay: VirtualDisplay? = null
    private var carPresentation: CarPresentation? = null
    private var surfaceWidth: Int = 1280
    private var surfaceHeight: Int = 720
    private var surfaceDpi: Int = 160

    private val surfaceCallback = object : SurfaceCallback {
        override fun onSurfaceAvailable(surfaceContainer: SurfaceContainer) {
            val surface = surfaceContainer.surface ?: return
            surfaceWidth = if (surfaceContainer.width > 0) surfaceContainer.width else 1280
            surfaceHeight = if (surfaceContainer.height > 0) surfaceContainer.height else 720
            surfaceDpi = if (surfaceContainer.dpi > 0) surfaceContainer.dpi else 160

            Log.d("CarBrowser", "Surface available: ${surfaceWidth}x${surfaceHeight} @ ${surfaceDpi}dpi")

            val displayManager = carContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            try {
                virtualDisplay?.release()
                carPresentation?.dismiss()

                virtualDisplay = displayManager.createVirtualDisplay(
                    "CarBrowserVirtualDisplay",
                    surfaceWidth,
                    surfaceHeight,
                    surfaceDpi,
                    surface,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION
                )

                virtualDisplay?.display?.let { display ->
                    val prefs = carContext.getSharedPreferences("car_browser_prefs", Context.MODE_PRIVATE)
                    val initialUrl = prefs.getString("initial_url", "https://www.youtube.com") ?: "https://www.youtube.com"

                    carPresentation = CarPresentation(carContext, display, initialUrl)
                    carPresentation?.show()
                }
            } catch (e: Exception) {
                Log.e("CarBrowser", "Error initializing VirtualDisplay/Presentation", e)
            }
        }

        override fun onVisibleAreaChanged(visibleArea: Rect) {
            Log.d("CarBrowser", "Visible area changed: $visibleArea")
        }

        override fun onStableAreaChanged(stableArea: Rect) {
            Log.d("CarBrowser", "Stable area changed: $stableArea")
        }

        override fun onSurfaceDestroyed(surfaceContainer: SurfaceContainer) {
            Log.d("CarBrowser", "Surface destroyed")
            try {
                carPresentation?.dismiss()
                carPresentation = null
                virtualDisplay?.release()
                virtualDisplay = null
            } catch (e: Exception) {
                Log.e("CarBrowser", "Error releasing VirtualDisplay", e)
            }
        }

        override fun onClick(x: Float, y: Float) {
            // Forward click event to the presentation window
            carPresentation?.window?.decorView?.let { decorView ->
                val downTime = SystemClock.uptimeMillis()
                val downEvent = MotionEvent.obtain(
                    downTime, downTime, MotionEvent.ACTION_DOWN, x, y, 0
                )
                val upEvent = MotionEvent.obtain(
                    downTime, downTime + 50, MotionEvent.ACTION_UP, x, y, 0
                )
                decorView.dispatchTouchEvent(downEvent)
                decorView.dispatchTouchEvent(upEvent)
                downEvent.recycle()
                upEvent.recycle()
            }
        }

        override fun onScroll(distanceX: Float, distanceY: Float) {
            // Scroll handling if needed
        }

        override fun onFling(velocityX: Float, velocityY: Float) {
            // Fling handling if needed
        }

        override fun onScale(focusX: Float, focusY: Float, scaleFactor: Float) {
            // Scale handling if needed
        }
    }

    init {
        // Register the SurfaceCallback with CarContext AppManager
        val appManager = carContext.getCarService(AppManager::class.java)
        appManager.setSurfaceCallback(surfaceCallback)
    }

    override fun onGetTemplate(): Template {
        // Build minimal NavigationTemplate so car screen surface is available
        val actionStrip = ActionStrip.Builder()
            .addAction(
                Action.Builder()
                    .setTitle("YouTube")
                    .setBackgroundColor(CarColor.RED)
                    .setOnClickListener {
                        val prefs = carContext.getSharedPreferences("car_browser_prefs", Context.MODE_PRIVATE)
                        val url = prefs.getString("initial_url", "https://www.youtube.com") ?: "https://www.youtube.com"
                        carPresentation?.findViewById<android.webkit.WebView>(R.id.carWebView)?.loadUrl(url)
                    }
                    .build()
            )
            .build()

        return NavigationTemplate.Builder()
            .setActionStrip(actionStrip)
            .build()
    }
}
