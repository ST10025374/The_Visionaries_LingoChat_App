package com.example.lingov3

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FingerprintAuthActivity : AppCompatActivity() {

    private lateinit var backArrow: ImageView
    private lateinit var titleText: TextView
    private lateinit var instructionText: TextView
    private lateinit var fingerprintIcon: ImageView
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fingerprint_auth)

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        backArrow = findViewById(R.id.backArrow)
        titleText = findViewById(R.id.titleText)
        instructionText = findViewById(R.id.instructionText)
        fingerprintIcon = findViewById(R.id.fingerprintIcon)
        statusText = findViewById(R.id.statusText)
    }

    private fun setupListeners() {
        backArrow.setOnClickListener {
            finish()  // Close this activity and return to the previous one
        }

        // Since we're not implementing actual fingerprint authentication,
        // let's simulate the process when the user taps on the fingerprint icon
        fingerprintIcon.setOnClickListener {
            simulateAuthentication()
        }
    }

    private fun simulateAuthentication() {
        statusText.text = "Authenticating..."

        // Simulate a delay for authentication process
        Handler(Looper.getMainLooper()).postDelayed({
            // Simulate successful authentication
            statusText.text = "Authentication successful!"

            // You might want to navigate to the next screen or finish this activity
            Handler(Looper.getMainLooper()).postDelayed({
                finish()
            }, 1000)  // Wait for 1 second before closing the activity
        }, 2000)  // Simulate 2 seconds for authentication process
    }
}