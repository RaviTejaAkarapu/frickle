package com.example.messenger.model

class ChatMessage(val id: String, val text: String, val fromId: String, val toId: String, timestamp: Long) {
    constructor() : this("", "", "", "", -1)
}