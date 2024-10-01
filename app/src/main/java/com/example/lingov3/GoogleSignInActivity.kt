package com.example.lingov3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class GoogleSignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_signin)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize Google Identity Services One Tap client
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        val signInButton = findViewById<Button>(R.id.signInButton)
        signInButton.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(this) { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, RC_SIGN_IN,
                        null, 0, 0, 0
                    )
                } catch (e: Exception) {
                    Toast.makeText(this, "Couldn't start One Tap sign-in: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener(this) { e ->
                Toast.makeText(this, "One Tap Sign-in Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            try {
                val credential: SignInCredential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    firebaseAuthWithGoogle(idToken, credential.displayName)
                } else {
                    Toast.makeText(this, "No ID token found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-in Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, displayName: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        checkAndUpdateUserData(it.uid, displayName ?: "Unknown")
                    }
                } else {
                    Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkAndUpdateUserData(userId: String, googleDisplayName: String) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // User data already exists, check if Fullname needs updating
                    val existingFullname = document.getString("Fullname")
                    if (existingFullname != googleDisplayName) {
                        // Update Fullname only if it's different
                        updateUserFullname(userId, googleDisplayName)
                    } else {
                        // No update needed, proceed to main activity
                        goToMainActivity()
                    }
                } else {
                    // User data doesn't exist, create new document
                    saveUserDataToFirestore(userId, googleDisplayName)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to check user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserFullname(userId: String, newFullname: String) {
        firestore.collection("users").document(userId)
            .update("Fullname", newFullname)
            .addOnSuccessListener {
                Toast.makeText(this, "User data updated successfully", Toast.LENGTH_SHORT).show()
                goToMainActivity()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserDataToFirestore(userId: String, fullName: String) {
        val userMap = hashMapOf(
            "Fullname" to fullName
        )

        firestore.collection("users").document(userId)
            .set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "User data saved successfully", Toast.LENGTH_SHORT).show()
                goToMainActivity()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainChatActivity::class.java))
        finish()
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}