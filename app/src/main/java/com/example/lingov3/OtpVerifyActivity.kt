package com.example.lingov3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.android.material.snackbar.Snackbar

class OtpVerifyActivity : AppCompatActivity() {

    private var storeVerificationId: String? = ""
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verify)

        val otpPinEditText = findViewById<EditText>(R.id.etC6)
        val btnVerify = findViewById<Button>(R.id.btnVerify)

        auth = FirebaseAuth.getInstance()

        storeVerificationId = intent.getStringExtra("storedVerificationId")

        btnVerify.setOnClickListener {
            val otpCode = otpPinEditText.text.toString()
            if (otpCode.isNotEmpty()) {
                verifyPhoneNumberWithCode(storeVerificationId, otpCode)
            } else {
                Snackbar.make(it, "Please enter the OTP", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("success", "Phone signInWithCredential:success")


                    val intent = Intent(this@OtpVerifyActivity, SignInActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    Log.w("failed", "Phone signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Log.e("error", "Invalid verification code.")
                    }
                }
            }
    }
}
