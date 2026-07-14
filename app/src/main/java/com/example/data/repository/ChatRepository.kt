package com.example.data.repository

import com.example.data.db.ChatDao
import com.example.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val chatDao: ChatDao) {
    val allMessages: Flow<List<ChatMessage>> = chatDao.getAllMessages()

    suspend fun insertMessage(message: ChatMessage): Long {
        return chatDao.insertMessage(message)
    }

    suspend fun deleteMessage(message: ChatMessage) {
        chatDao.deleteMessage(message)
    }

    suspend fun clearAllMessages() {
        chatDao.clearAllMessages()
    }
}
