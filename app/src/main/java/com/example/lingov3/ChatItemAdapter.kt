package com.example.lingov3

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatItemAdapter(private var users: List<User>) : RecyclerView.Adapter<ChatItemAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ChatActivity::class.java).apply {
                putExtra("userId", user.id)
                putExtra("userName", user.fullname)
                putExtra("userProfilePic", user.profilePictureUrl)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = users.size

    fun updateUsers(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }

    fun updateUser(updatedUser: User) {
        val index = users.indexOfFirst { it.id == updatedUser.id }
        if (index != -1) {
            users = users.toMutableList().apply { this[index] = updatedUser }
            notifyItemChanged(index)
        }
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProfilePic: CircleImageView = itemView.findViewById(R.id.ivProfilePic)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)

        fun bind(user: User) {
            tvName.text = user.fullname
            tvLastMessage.text = user.lastMessage
            tvTimestamp.text = formatTimestamp(user.lastMessageTimestamp)

            // Load profile picture using Glide with improved error handling and debugging
            Glide.with(itemView.context)
                .load(user.profilePictureUrl)
                .apply(RequestOptions()
                    .placeholder(R.drawable.default_profile_pic)
                    .error(R.drawable.default_profile_pic)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                )
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(ivProfilePic)

            // Log the profile picture URL for debugging
            Log.d("ChatItemAdapter", "Loading profile picture for ${user.fullname}: ${user.profilePictureUrl}")

            // Fetch the latest message for this chat
            fetchLatestMessage(user)
        }

        private fun fetchLatestMessage(user: User) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            val chatRoomId = if (currentUserId!! < user.id) {
                "$currentUserId${user.id}"
            } else {
                "${user.id}$currentUserId"
            }

            FirebaseDatabase.getInstance().reference
                .child("chats")
                .child(chatRoomId)
                .child("messages")
                .orderByKey()
                .limitToLast(1)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (messageSnapshot in snapshot.children) {
                                val message = messageSnapshot.getValue(ChatMessage::class.java)
                                message?.let {
                                    user.lastMessage = it.message ?: ""
                                    user.lastMessageTimestamp = it.timestamp
                                    tvLastMessage.text = user.lastMessage
                                    tvTimestamp.text = formatTimestamp(user.lastMessageTimestamp)
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ChatItemAdapter", "Error fetching latest message: ${error.message}", error.toException())
                    }
                })
        }

        private fun formatTimestamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}