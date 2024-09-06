package com.example.brockapp.viewmodel

import com.example.brockapp.database.BrockDB

import java.io.File
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amazonaws.services.s3.AmazonS3Client

class FriendsViewModelFactory(private val s3Client: AmazonS3Client, private val db: BrockDB, private val file: File): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        return FriendsViewModel(s3Client, db, file) as T
    }
}