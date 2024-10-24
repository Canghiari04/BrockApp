package com.example.brockapp.viewmodel

import com.example.brockapp.room.BrockDB

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amazonaws.services.s3.AmazonS3Client

class GroupViewModelFactory(private val s3Client: AmazonS3Client, private val db: BrockDB): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        return GroupViewModel(s3Client, db) as T
    }
}