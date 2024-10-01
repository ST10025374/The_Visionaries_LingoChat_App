package com.example.lingov3

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LanguageSelectionActivity : AppCompatActivity() {

    private lateinit var spinnerLanguage: Spinner
    private lateinit var confirmButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selection)

        spinnerLanguage = findViewById(R.id.spinner_language)
        confirmButton = findViewById(R.id.confirm_button)
        progressBar = findViewById(R.id.progress_bar)
        prefs = getSharedPreferences("app_preferences", MODE_PRIVATE)

        progressBar.visibility = View.GONE

        // Load saved preferred language and set the spinner
        val savedLanguage = prefs.getString("preferred_language", "en")
        setSpinnerSelection(savedLanguage)

        // Listen to language selection changes
        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // No need to do anything here now
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Confirm button click listener
        confirmButton.setOnClickListener {
            val selectedLanguage = getLanguageCodeFromSpinner()
            savePreferredLanguage(selectedLanguage)
            updateChatLanguage(selectedLanguage)
            Toast.makeText(this, "Language updated to $selectedLanguage", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getLanguageCodeFromSpinner(): String {
        val selectedLanguage = spinnerLanguage.selectedItem as String
        return when (selectedLanguage) {
            "Indonesia" -> "id"
            "Inggris" -> "en"
            "Jawa" -> "jv"
            "Sunda" -> "su"
            "Afrikaans" -> "af"
            "Albania" -> "sq"
            "Amharic" -> "am"
            "Arab" -> "ar"
            "Armenian" -> "hy"
            "Bengali" -> "bn"
            "Bulgarian" -> "bg"
            "Chinese (Simplified)" -> "zh-Hans"
            "Chinese (Traditional)" -> "zh-Hant"
            "French" -> "fr"
            "German" -> "de"
            "Japanese" -> "ja"
            else -> "en"
        }
    }

    private fun setSpinnerSelection(languageCode: String?) {
        val languagesArray = resources.getStringArray(R.array.language_array)
        val index = when (languageCode) {
            "id" -> languagesArray.indexOf("Indonesia")
            "en" -> languagesArray.indexOf("Inggris")
            "jv" -> languagesArray.indexOf("Jawa")
            "su" -> languagesArray.indexOf("Sunda")
            "af" -> languagesArray.indexOf("Afrikaans")
            "sq" -> languagesArray.indexOf("Albania")
            "am" -> languagesArray.indexOf("Amharic")
            "ar" -> languagesArray.indexOf("Arab")
            "hy" -> languagesArray.indexOf("Armenian")
            "bn" -> languagesArray.indexOf("Bengali")
            "bg" -> languagesArray.indexOf("Bulgarian")
            "zh-Hans" -> languagesArray.indexOf("Chinese (Simplified)")
            "zh-Hant" -> languagesArray.indexOf("Chinese (Traditional)")
            "fr" -> languagesArray.indexOf("French")
            "de" -> languagesArray.indexOf("German")
            "ja" -> languagesArray.indexOf("Japanese")
            else -> languagesArray.indexOf("Inggris")
        }
        spinnerLanguage.setSelection(index)
    }

    private fun savePreferredLanguage(languageCode: String) {
        val editor = prefs.edit()
        editor.putString("preferred_language", languageCode)
        editor.apply()
    }

    private fun updateChatLanguage(languageCode: String) {
        // Notify the ChatActivity to reload messages
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("languageCode", languageCode)
        startActivity(intent)
    }
}
