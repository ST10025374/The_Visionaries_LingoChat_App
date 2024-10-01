package com.example.lingov3

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import java.util.*

class MyApplication : Application() {
    override fun attachBaseContext(base: Context) {
        val prefs = base.getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
        val languageCode = prefs.getString("appLanguage", "en") ?: "en"
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(base.createConfigurationContext(config))
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize any app-wide configurations here
    }
}