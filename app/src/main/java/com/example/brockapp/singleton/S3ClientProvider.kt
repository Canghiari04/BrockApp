package com.example.brockapp.singleton

import android.content.Context
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client

class S3ClientProvider private constructor() {

    companion object {
        @Volatile
        private var s3Client: AmazonS3Client? = null

        fun getInstance(context: Context): AmazonS3Client {

            synchronized(this) {
                if(s3Client == null) {
                    s3Client = createS3Client(context)
                }
            }
            return s3Client!!

        }

        private fun createS3Client(context: Context): AmazonS3Client {
            val credentialsProvider = CognitoCachingCredentialsProvider(
                context,
                "eu-west-3:8fe18ff5-1fe5-429d-b11c-16e8401d3a00",
                Regions.EU_WEST_3
            )
            return AmazonS3Client(credentialsProvider)
        }
    }
}
