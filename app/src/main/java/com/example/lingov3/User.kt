package com.example.lingov3

data class User(
    val id: String = "",
    val fullname: String = "",
    val profilePictureUrl: String? = "",
        var lastMessage: String = "",
        var lastMessageTimestamp: Long = 0
)