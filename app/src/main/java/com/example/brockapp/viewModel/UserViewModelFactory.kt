package com.example.brockapp.viewModel

import com.example.brockapp.room.BrockDB

import java.io.File
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amazonaws.services.s3.AmazonS3Client

class UserViewModelFactory(private val brockDB: BrockDB, private val s3Client: AmazonS3Client, private val file: File): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UserViewModel(brockDB, s3Client, file) as T
    }
}