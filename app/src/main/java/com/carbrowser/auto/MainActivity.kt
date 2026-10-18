package com.carbrowser.auto

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var etInitialUrl: EditText
    private lateinit var btnSaveUrl: Button
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("car_browser_prefs", Context.MODE_PRIVATE)

        etInitialUrl = findViewById(R.id.etInitialUrl)
        btnSaveUrl = findViewById(R.id.btnSaveUrl)

        val savedUrl = prefs.getString("initial_url", "https://www.youtube.com")
        etInitialUrl.setText(savedUrl)

        btnSaveUrl.setOnClickListener {
            val url = etInitialUrl.text.toString().trim()
            if (url.isNotEmpty()) {
                val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    "https://$url"
                } else {
                    url
                }
                prefs.edit().putString("initial_url", formattedUrl).apply()
                Toast.makeText(this, "URL guardada: $formattedUrl", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Helper method to open Android Auto Settings on the phone if user wants direct access
     */
    fun openAndroidAutoSettings() {
        val intents = listOf(
            Intent().setComponent(
                ComponentName(
                    "com.google.android.projection.gearhead",
                    "com.google.android.projection.gearhead.companion.settings.DefaultSettingsActivity"
                )
            ),
            Intent("com.google.android.gms.car.SETTINGS"),
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", "com.google.android.projection.gearhead", null)
            }
        )

        for (intent in intents) {
            try {
                startActivity(intent)
                return
            } catch (_: Exception) { }
        }

        Toast.makeText(this, "Abre Ajustes de tu teléfono y busca 'Android Auto'", Toast.LENGTH_LONG).show()
    }
}
