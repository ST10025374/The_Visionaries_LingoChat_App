package com.example.lingov3

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageView
    private lateinit var backButton: ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var userProfileImageView: CircleImageView

    private lateinit var chatAdapter: ChatMessageAdapter
    private lateinit var messages: ArrayList<ChatMessage>

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var prefs: SharedPreferences

    private var receiverRoom: String? = null
    private var senderRoom: String? = null

    private val client = OkHttpClient()
    private val apiKey = "@string/azure_translation_key"
    private val endpoint = "https://api.cognitive.microsofttranslator.com"
    private val region = "southafricanorth"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainchatlayout)

        val receiverId = intent.getStringExtra("userId")
        val receiverName = intent.getStringExtra("userName")
        val receiverProfilePic = intent.getStringExtra("userProfilePic")

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference
        prefs = getSharedPreferences("LingvoPreferences", MODE_PRIVATE)

        senderRoom = receiverId + mAuth.currentUser?.uid
        receiverRoom = mAuth.currentUser?.uid + receiverId

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.send)
        backButton = findViewById(R.id.backArrow)
        userNameTextView = findViewById(R.id.userName)
        userProfileImageView = findViewById(R.id.profileImage)

        userNameTextView.text = receiverName
        Glide.with(this).load(receiverProfilePic).placeholder(R.drawable.default_profile_pic).into(userProfileImageView)

        messages = ArrayList()
        chatAdapter = ChatMessageAdapter(this, messages)

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = chatAdapter

        // Fetch and translate all messages
        mDbRef.child("chats").child(senderRoom!!).child("messages")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = snapshot.getValue(ChatMessage::class.java)
                    message?.let {
                        val selectedLanguage = prefs.getString("preferred_language", "en") ?: "en"
                        translateMessage(it.message ?: "", selectedLanguage) { translatedText ->
                            val translatedMessage = ChatMessage(translatedText, it.senderId, it.timestamp)
                            if (!messages.any { m -> m.timestamp == translatedMessage.timestamp }) {
                                messages.add(translatedMessage)
                                messages.sortBy { m -> m.timestamp }
                                chatAdapter.notifyItemInserted(messages.size - 1)
                                chatRecyclerView.scrollToPosition(messages.size - 1)
                            }
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })

        // Send and translate message before saving to DB
        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val selectedLanguage = prefs.getString("preferred_language", "en") ?: "en"
                val timestamp = System.currentTimeMillis()

                // Translate message before sending
                translateMessage(messageText, selectedLanguage) { translatedText ->
                    val messageObject = ChatMessage(translatedText, mAuth.currentUser?.uid, timestamp)

                    // Send message to database
                    mDbRef.child("chats").child(senderRoom!!).child("messages").push()
                        .setValue(messageObject).addOnSuccessListener {
                            mDbRef.child("chats").child(receiverRoom!!).child("messages").push()
                                .setValue(messageObject)
                        }
                    messageEditText.setText("")
                }
            }
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun translateMessage(text: String, targetLanguage: String, callback: (String) -> Unit) {
        val path = "/translate?api-version=3.0&to=$targetLanguage"
        val url = endpoint + path

        val requestBody = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            "[{\"Text\": \"$text\"}]"
        )

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Ocp-Apim-Subscription-Key", apiKey)
            .addHeader("Ocp-Apim-Subscription-Region", region)
            .addHeader("Content-type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ChatActivity", "Translation failed", e)
                runOnUiThread { callback(text) }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    responseBody?.let {
                        try {
                            val jsonArray = JSONArray(it)
                            val translatedText = jsonArray.getJSONObject(0)
                                .getJSONArray("translations")
                                .getJSONObject(0)
                                .getString("text")
                            runOnUiThread { callback(translatedText) }
                        } catch (e: Exception) {
                            Log.e("ChatActivity", "Error parsing translation response", e)
                            runOnUiThread { callback(text) }
                        }
                    } ?: run {
                        Log.e("ChatActivity", "Empty response body")
                        runOnUiThread { callback(text) }
                    }
                } else {
                    Log.e("ChatActivity", "Unsuccessful response: ${response.code}")
                    runOnUiThread { callback(text) }
                }
            }
        })
    }
}

data class ChatMessage(
    val message: String? = null,
    val senderId: String? = null,
    val timestamp: Long = 0
)

class ChatMessageAdapter(private val context: android.content.Context, private val messages: ArrayList<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_RECEIVE = 1
    private val ITEM_SENT = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == ITEM_RECEIVE) {
            val view: View = LayoutInflater.from(context).inflate(R.layout.receive, parent, false)
            ReceiveViewHolder(view)
        } else {
            val view: View = LayoutInflater.from(context).inflate(R.layout.sent, parent, false)
            SentViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messages[position]
        val formattedTime = getFormattedTime(currentMessage.timestamp)

        when (holder) {
            is SentViewHolder -> {
                holder.sentMessage.text = currentMessage.message
                holder.sentTime.text = formattedTime
            }
            is ReceiveViewHolder -> {
                holder.receiveMessage.text = currentMessage.message
                holder.receiveTime.text = formattedTime
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messages[position]
        return if(FirebaseAuth.getInstance().currentUser?.uid == currentMessage.senderId) {
            ITEM_SENT
        } else {
            ITEM_RECEIVE
        }
    }

    override fun getItemCount(): Int = messages.size

    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentMessage: TextView = itemView.findViewById(R.id.txt_sent_message)
        val sentTime: TextView = itemView.findViewById(R.id.txt_sent_time)
    }

    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiveMessage: TextView = itemView.findViewById(R.id.txt_receive_message)
        val receiveTime: TextView = itemView.findViewById(R.id.txt_receive_time)
    }

    private fun getFormattedTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
