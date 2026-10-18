package com.carbrowser.auto

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

class CarBrowserService : CarAppService() {

    override fun createHostValidator(): HostValidator {
        // Allows connection to both official Android Auto head units and development/debug hosts
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(): Session {
        return CarBrowserSession()
    }
}
