package com.example.lingov3

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.text.TextRange
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {



    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_in_screen)

        auth = FirebaseAuth.getInstance()

        val tvGoogleSignIn = findViewById<TextView>(R.id.tvGoogleSignIn)
        tvGoogleSignIn.setOnClickListener {
            startActivity(Intent(this, GoogleSignInActivity::class.java))
        }

        val tvFingerPrintLogin = findViewById <TextView>(R.id.tvFingerPrintLogin)
        tvFingerPrintLogin.setOnClickListener {
            startActivity(Intent(this, FingerprintAuthActivity::class.java))
        }
    }

}