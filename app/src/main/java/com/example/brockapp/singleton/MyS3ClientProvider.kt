package com.example.brockapp.singleton

import com.example.brockapp.BuildConfig

import android.content.Context
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.auth.CognitoCachingCredentialsProvider

class MyS3ClientProvider private constructor() {

    companion object {
        @Volatile
        private var s3Client: AmazonS3Client? = null

        fun getInstance(context: Context): AmazonS3Client {
            synchronized(this) {
                if (s3Client == null) {
                    s3Client = createS3Client(context)
                }
            }

            return s3Client!!
        }

        private fun createS3Client(context: Context): AmazonS3Client {
            val credentialsProvider = CognitoCachingCredentialsProvider(
                context,
                BuildConfig.IDENTITY_POOL_ID,
                Regions.EU_NORTH_1
            )

            return AmazonS3Client(credentialsProvider)
        }
    }
}
