package com.example.lingov3

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var tvTranslationLanguage: TextView
    private lateinit var switchEnableTranslation: SwitchMaterial
    private lateinit var switchFingerprint: SwitchMaterial
    private lateinit var switchOfflineMode: SwitchMaterial
    private lateinit var switchPushNotifications: SwitchMaterial
    private lateinit var switchSingleSignOn: SwitchMaterial
    private lateinit var tvChangeAppLanguage: TextView
    private lateinit var tvAfrikaans: TextView
    private lateinit var tvEnglish: TextView
    private lateinit var bottomNavigation: BottomNavigationView

    companion object {
        private const val LANGUAGE_SELECTION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initializeViews()
        loadSavedSettings()
        setupListeners()
        updateLanguageButtons()
    }

    private fun initializeViews() {
        tvTranslationLanguage = findViewById(R.id.tvTranslationLanguage)
        switchEnableTranslation = findViewById(R.id.switchEnableTranslation)
        switchFingerprint = findViewById(R.id.switchFingerprint)
        switchOfflineMode = findViewById(R.id.switchOfflineMode)
        switchPushNotifications = findViewById(R.id.switchPushNotifications)
        switchSingleSignOn = findViewById(R.id.switchSingleSignOn)
        tvChangeAppLanguage = findViewById(R.id.tvChangeAppLanguage)
        tvAfrikaans = findViewById(R.id.tvAfrikaans)
        tvEnglish = findViewById(R.id.tvEnglish)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }

    private fun loadSavedSettings() {
        val prefs = getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
        switchEnableTranslation.isChecked = prefs.getBoolean("enableTranslation", true)
        switchFingerprint.isChecked = prefs.getBoolean("fingerprint", false)
        switchOfflineMode.isChecked = prefs.getBoolean("offlineMode", true)
        switchPushNotifications.isChecked = prefs.getBoolean("pushNotifications", true)
        switchSingleSignOn.isChecked = prefs.getBoolean("singleSignOn", true)
    }

    private fun setupListeners() {
        tvTranslationLanguage.setOnClickListener {
            val intent = Intent(this, LanguageSettingsActivity::class.java)
            startActivityForResult(intent, LANGUAGE_SELECTION_REQUEST_CODE)
        }
        tvAfrikaans.setOnClickListener { changeLanguage("af") }
        tvEnglish.setOnClickListener { changeLanguage("en") }
        setupBottomNavigation()
        setupSwitchListeners()
    }

    private fun setupSwitchListeners() {
        val switches = mapOf(
            "enableTranslation" to switchEnableTranslation,
            "fingerprint" to switchFingerprint,
            "offlineMode" to switchOfflineMode,
            "pushNotifications" to switchPushNotifications,
            "singleSignOn" to switchSingleSignOn
        )

        switches.forEach { (key, switch) ->
            switch.setOnCheckedChangeListener { _, isChecked ->
                saveBooleanSetting(key, isChecked)
            }
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_chats -> {
                    startActivity(Intent(this, MainChatActivity::class.java))
                    true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.navigation_settings -> true
                else -> false
            }
        }
    }

    private fun changeLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        saveStringSetting("appLanguage", languageCode)
        updateLanguageButtons()
        recreate() // Recreate the activity to apply the new language
        showLanguageChangedToast(languageCode)
    }

    private fun updateLanguageButtons() {
        val currentLanguage = Locale.getDefault().language
        tvAfrikaans.setBackgroundResource(if (currentLanguage == "af") R.color.selected_language else R.color.unselected_language)
        tvEnglish.setBackgroundResource(if (currentLanguage == "en") R.color.selected_language else R.color.unselected_language)
    }

    private fun showLanguageChangedToast(languageCode: String) {
        val languageName = if (languageCode == "af") getString(R.string.afrikaans) else getString(R.string.english)
        Toast.makeText(this, getString(R.string.language_changed, languageName), Toast.LENGTH_SHORT).show()
    }

    private fun saveStringSetting(key: String, value: String) {
        val prefs = getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString(key, value).apply()
    }

    private fun saveBooleanSetting(key: String, value: Boolean) {
        val prefs = getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean(key, value).apply()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LANGUAGE_SELECTION_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.getStringExtra("SELECTED_LANGUAGE")?.let { languageCode ->
                changeLanguage(languageCode)
            }
        }
    }
}