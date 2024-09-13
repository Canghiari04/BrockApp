package com.example.brockapp.interfaces

interface NotificationSender {
    fun sendNotification(title: String, content: String)
}