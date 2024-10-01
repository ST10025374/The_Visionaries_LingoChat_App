package com.example.lingov3

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.example.lingov3.databinding.ChatScreenLayoutBinding

class MainChatActivity : AppCompatActivity() {
    private lateinit var binding: ChatScreenLayoutBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var chatAdapter: ChatItemAdapter
    private val userList = mutableListOf<User>()
    private var userListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ChatScreenLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeFirebase()
        setupViews()
        setupBottomNavigation()
        setupSearch()
        fetchUsers()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_language_settings -> {
                startActivity(Intent(this, LanguageSettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initializeFirebase() {
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    private fun setupViews() {
        chatAdapter = ChatItemAdapter(userList)
        binding.rvChats.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@MainChatActivity)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_chats -> true
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
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

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterUsers(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun fetchUsers() {
        showProgress(true)
        userListener = firestore.collection("users")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("MainChatActivity", "Listen failed.", e)
                    showProgress(false)
                    showError("Error fetching users: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val newUserList = mutableListOf<User>()
                    for (document in snapshot.documents) {
                        val userId = document.id
                        val fullname = document.getString("Fullname") ?: document.getString("fullName") ?: "Unknown User"
                        val profilePictureUrl = document.getString("profilePictureUrl")
                        val lastMessage = document.getString("lastMessage") ?: ""
                        val lastMessageTimestamp = document.getLong("lastMessageTimestamp") ?: 0

                        Log.d("MainChatActivity", "Fetched user: id=$userId, fullname=$fullname, profilePic=$profilePictureUrl, lastMessage=$lastMessage")

                        val user = User(
                            id = userId,
                            fullname = fullname,
                            profilePictureUrl = profilePictureUrl,
                            lastMessage = lastMessage,
                            lastMessageTimestamp = lastMessageTimestamp
                        )
                        newUserList.add(user)
                    }
                    userList.clear()
                    userList.addAll(newUserList)
                    chatAdapter.updateUsers(userList)
                    showProgress(false)
                }
            }
    }

    private fun filterUsers(query: String) {
        val filteredList = userList.filter { user ->
            user.fullname.contains(query, ignoreCase = true)
        }
        chatAdapter.updateUsers(filteredList)
    }

    private fun showProgress(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        userListener?.remove()
    }
}