package com.example.brockapp.activity

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.amazonaws.services.s3.model.Permission
import com.example.brockapp.ACTIVITY_RECOGNITION_INTENT_TYPE
import com.example.brockapp.R
import com.example.brockapp.receiver.ActivityRecognitionReceiver
import com.example.brockapp.recognition.Util
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity


// NON SI SPACCA PER IL DISPOSITIVO DA IMPLEMENTARE PER LE NOTIFICHE

class ActivityActivity: AppCompatActivity() {
    private lateinit var recognitionUtil: Util

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activity)

        recognitionUtil = Util()

        if(!checkVersion()) {
            // INIZIALIZZO L'ACTIVITY RECOGNITION
        } else {
            checkPermissionDuringRuntime()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == 27 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val receiver = ActivityRecognitionReceiver()

            val request = ActivityTransitionRequest(recognitionUtil.getActivitiesToTrack())

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val task =
                    ActivityRecognition.getClient(this).requestActivityTransitionUpdates(request,
                        receiver.getPendingIntent(this))

                task.run {
                    addOnSuccessListener {
                        Log.d("TransitionUpdate", "Well done, done in performance.")
                    }
                    addOnFailureListener {
                        Log.d("TransitionUpdate", "Bad implementation")
                    }
                }
            }
        } else {
            checkPermissionDuringRuntime()
        }
    }

    private fun checkPermissionDuringRuntime() {
        if (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)) {
            val receiver = ActivityRecognitionReceiver()

            val request = ActivityTransitionRequest(recognitionUtil.getActivitiesToTrack())
            val task =
                ActivityRecognition.getClient(this).requestActivityTransitionUpdates(request,
                    receiver.getPendingIntent(this))

            task.run {
                addOnSuccessListener {
                    Log.d("TransitionUpdate", "Well done, done in performance.")
                }
                addOnFailureListener {
                    Log.d("TransitionUpdate", "Bad implementation")
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                27
            )
        }
    }

    private fun checkVersion(): Boolean {
        val version: Boolean = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

        return version
    }
}