package com.example.lingov3

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.hdodenhof.circleimageview.CircleImageView
import android.webkit.MimeTypeMap
import com.google.firebase.firestore.SetOptions
import android.text.InputFilter

class ProfileActivity : AppCompatActivity() {

    private lateinit var ivProfilePicture: CircleImageView
    private lateinit var tvChangePhoto: TextView
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etCountryCode: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var btnUpdateProfile: Button
    private lateinit var btnLogout: Button
    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var selectedImageUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initializeViews()
        setupInputControls()

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        loadUserProfile()

        tvChangePhoto.setOnClickListener {
            openImageChooser()
        }

        btnUpdateProfile.setOnClickListener {
            updateProfile()
        }

        btnLogout.setOnClickListener {
            logout()
        }

        setupBottomNavigation()
    }

    private fun initializeViews() {
        ivProfilePicture = findViewById(R.id.ivProfilePicture)
        tvChangePhoto = findViewById(R.id.tvChangePhoto)
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etCountryCode = findViewById(R.id.etCountryCode)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile)
        btnLogout = findViewById(R.id.logout)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }

    private fun setupInputControls() {
        etCountryCode.filters = arrayOf(InputFilter.LengthFilter(3))
    }

    private fun loadUserProfile() {
        val user = auth.currentUser
        user?.let { firebaseUser ->
            val userId = firebaseUser.uid
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val fullName = document.getString("fullName")
                        val email = document.getString("email")
                        val phoneNumber = document.getString("phoneNumber")
                        val profilePictureUrl = document.getString("profilePictureUrl")

                        etFullName.setText(fullName)
                        etEmail.setText(email)

                        if (!phoneNumber.isNullOrEmpty()) {
                            val parts = phoneNumber.split(" ", limit = 2)
                            if (parts.size == 2) {
                                etCountryCode.setText(parts[0].removePrefix("+"))
                                etPhoneNumber.setText(parts[1])
                            } else {
                                etPhoneNumber.setText(phoneNumber)
                            }
                        }

                        if (!profilePictureUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(profilePictureUrl)
                                .placeholder(R.drawable.default_profile_pic)
                                .into(ivProfilePicture)
                        }
                    } else {
                        // If the document doesn't exist, populate fields with data from FirebaseUser
                        etFullName.setText(firebaseUser.displayName)
                        etEmail.setText(firebaseUser.email)

                        val phoneNumber = firebaseUser.phoneNumber
                        if (!phoneNumber.isNullOrEmpty()) {
                            val parts = phoneNumber.split(" ", limit = 2)
                            if (parts.size == 2) {
                                etCountryCode.setText(parts[0].removePrefix("+"))
                                etPhoneNumber.setText(parts[1])
                            } else {
                                etPhoneNumber.setText(phoneNumber)
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg", "image/gif")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            ivProfilePicture.setImageURI(selectedImageUri)
        }
    }

    private fun updateProfile() {
        val user = auth.currentUser
        user?.let { firebaseUser ->
            val userId = firebaseUser.uid
            val updatedFullName = etFullName.text.toString()
            val updatedEmail = etEmail.text.toString()
            val updatedCountryCode = etCountryCode.text.toString().trim()
            val updatedPhoneNumber = etPhoneNumber.text.toString().trim()

            val fullPhoneNumber = if (updatedCountryCode.isNotEmpty() && updatedPhoneNumber.isNotEmpty()) {
                "+$updatedCountryCode $updatedPhoneNumber"
            } else {
                null
            }

            val userUpdates = hashMapOf<String, Any>(
                "fullName" to updatedFullName,
                "email" to updatedEmail
            )

            if (fullPhoneNumber != null) {
                userUpdates["phoneNumber"] = fullPhoneNumber
            }

            if (selectedImageUri != null) {
                uploadProfilePicture(userId) { profilePictureUrl ->
                    userUpdates["profilePictureUrl"] = profilePictureUrl
                    updateUserData(userId, userUpdates)
                }
            } else {
                updateUserData(userId, userUpdates)
            }
        }
    }

    private fun uploadProfilePicture(userId: String, onSuccess: (String) -> Unit) {
        val fileExtension = getFileExtension(selectedImageUri)
        val profilePictureRef = storage.reference.child("profile_pictures/$userId.$fileExtension")
        profilePictureRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                profilePictureRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload profile picture", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getFileExtension(uri: Uri?): String {
        val contentResolver = contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri!!)) ?: "jpg"
    }

    private fun updateUserData(userId: String, updates: Map<String, Any>) {
        firestore.collection("users").document(userId)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                // Reload the user profile to reflect the changes
                loadUserProfile()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
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
                    // Already on profile, do nothing
                    true
                }
                R.id.navigation_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun logout() {
        auth.signOut()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()


        val intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}