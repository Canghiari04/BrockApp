package com.example.brockapp.fragment

import com.example.brockapp.database.DbHelper

import android.Manifest
import android.util.Log
import android.view.View
import android.os.Bundle
import android.widget.Button
import com.example.brockapp.R
import android.os.SystemClock
import android.content.Intent
import android.widget.Chronometer
import androidx.fragment.app.Fragment
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityRecognition
import com.example.brockapp.detect.UserActivityTransitionManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class StillFragment() : Fragment(R.layout.start_stop_activity_fragment) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val transitionManager = UserActivityTransitionManager(requireContext())
        val chronometer = view.findViewById<Chronometer>(R.id.chronometer)

        var running = false
        var pauseOffset: Long = 0

        view.findViewById<Button>(R.id.button_start).setOnClickListener {
            if (!running) {
                chronometer.base = SystemClock.elapsedRealtime() - pauseOffset
                chronometer.start()
                running = true

                view.findViewById<Button>(R.id.button_start).isEnabled = false
                view.findViewById<Button>(R.id.button_stop).isEnabled = true
            }

            startDetection(transitionManager)
        }

        view.findViewById<Button>(R.id.button_stop).setOnClickListener {
            if (running) {
                chronometer.stop()
                pauseOffset = SystemClock.elapsedRealtime() - chronometer.base
                running = false

                view.findViewById<Button>(R.id.button_start).isEnabled = true
                view.findViewById<Button>(R.id.button_stop).isEnabled = false
            }

            stopDetection(DbHelper(requireContext()), transitionManager)
        }

        view.findViewById<Button>(R.id.button_start).isEnabled = true
        view.findViewById<Button>(R.id.button_stop).isEnabled = false
    }

    private fun startDetection(transitionManager: UserActivityTransitionManager) {
        val request = transitionManager.getRequest()
        val myPendingIntentActivityRecognition = transitionManager.getPendingIntent(requireContext())

        // Check richiesto obbligatoriamente prima di poter richiedere update su transitions activity.
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
            val task = ActivityRecognition.getClient(requireContext()).requestActivityTransitionUpdates(request, myPendingIntentActivityRecognition)

            task.addOnSuccessListener {
                Log.d("DETECT", "Connesso all'API activity recognition")
            }

            task.addOnFailureListener {
                Log.d("DETECT", "Errore di connessione con l'API activity recognition")
            }

            simulateStartActivity(DetectedActivity.STILL, ActivityTransition.ACTIVITY_TRANSITION_ENTER)
        } else {
            Log.d("WTF", "WTF")
        }
    }

    private fun simulateStartActivity(activityType: Int, transitionType: Int) {
        val intent = Intent("TRANSITIONS_RECEIVER_ACTION").apply {
            putExtra("activityType", activityType)
            putExtra("transitionType", transitionType)
        }
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }

    private fun stopDetection(dbHelper: DbHelper, transitionManager: UserActivityTransitionManager) {
        // TODO --> SAREBBE QUI DA MEMORIZZARE LA FINE DELL'ACTIVITY RECOGNITION
    }
}