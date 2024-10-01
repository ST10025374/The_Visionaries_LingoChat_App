package com.example.lingov3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import java.util.concurrent.TimeUnit

class OtpSendActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private lateinit var btnSend: Button
    private lateinit var etPhone: EditText
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        // Initialize views
        btnSend = findViewById(R.id.btnSend)
        etPhone = findViewById(R.id.etPhone)
        progressBar = findViewById(R.id.progressBar)

        auth = Firebase.auth

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted:$credential")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "onVerificationFailed", e)
                showLoading(false)
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> showToast("Invalid phone number format.")
                    is FirebaseTooManyRequestsException -> showToast("Too many requests. Try again later.")
                    is FirebaseAuthMissingActivityForRecaptchaException -> showToast("reCAPTCHA verification failed.")
                    else -> showToast("Verification failed. Please try again.")
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "onCodeSent:$verificationId")
                storedVerificationId = verificationId
                resendToken = token
                showLoading(false)
                showToast("OTP sent successfully!")
                navigateToOtpVerifyActivity()
            }
        }

        btnSend.setOnClickListener {
            val phoneNumber = etPhone.text.toString().trim()
            if (phoneNumber.isNotEmpty()) {
                showLoading(true)
                startPhoneNumberVerification(phoneNumber)
            } else {
                showToast("Please enter a phone number")
            }
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        val formattedNumber = "+27$phoneNumber" // Ensure the country code is included
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = task.result?.user
                    navigateToOtpVerifyActivity()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        showToast("Invalid verification code.")
                    }
                }
                showLoading(false)
            }
    }

    private fun navigateToOtpVerifyActivity() {
        val intent = Intent(this@OtpSendActivity, OtpVerifyActivity::class.java)
        intent.putExtra("storedVerificationId", storedVerificationId)
        startActivity(intent)
    }

    private fun showLoading(isLoading: Boolean) {
        runOnUiThread {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnSend.isEnabled = !isLoading
            etPhone.isEnabled = !isLoading
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@OtpSendActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TAG = "OtpSendActivity"
    }
}