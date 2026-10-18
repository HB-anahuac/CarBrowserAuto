package com.carbrowser.auto

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session

class CarBrowserSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return CarBrowserScreen(carContext)
    }
}
