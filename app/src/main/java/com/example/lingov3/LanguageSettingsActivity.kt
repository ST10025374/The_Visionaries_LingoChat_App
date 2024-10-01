package com.example.lingov3

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class LanguageSettingsActivity : AppCompatActivity() {
    private lateinit var languageSpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_settings)

        languageSpinner = findViewById(R.id.languageSpinner)
        saveButton = findViewById(R.id.saveButton)
        prefs = getSharedPreferences("LingvoPreferences", MODE_PRIVATE)

        val languages = arrayOf("English", "Spanish", "French", "German", "Italian")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter

        val currentLanguage = prefs.getString("preferred_language", "en") ?: "en"
        val index = languages.indexOf(getLanguageName(currentLanguage))
        if (index != -1) {
            languageSpinner.setSelection(index)
        }

        saveButton.setOnClickListener {
            val selectedLanguage = getLanguageCode(languageSpinner.selectedItem.toString())
            prefs.edit().putString("preferred_language", selectedLanguage).apply()
            finish()
        }
    }

    private fun getLanguageCode(languageName: String): String {
        return when (languageName) {
            "English" -> "en"
            "Spanish" -> "es"
            "French" -> "fr"
            "German" -> "de"
            "Italian" -> "it"
            else -> "en"
        }
    }

    private fun getLanguageName(languageCode: String): String {
        return when (languageCode) {
            "en" -> "English"
            "es" -> "Spanish"
            "fr" -> "French"
            "de" -> "German"
            "it" -> "Italian"
            else -> "English"
        }
    }
}