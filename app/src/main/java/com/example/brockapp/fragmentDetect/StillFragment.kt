package com.example.brockapp.fragmentDetect

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.brockapp.NOTIFICATION_INTENT_FILTER
import com.example.brockapp.R
import com.example.brockapp.detect.UserActivityTransitionManager
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity

class StillFragment() : Fragment(R.layout.start_stop_activity_fragment) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val transitionManager = UserActivityTransitionManager(requireContext())
        val chronometer = view.findViewById<Chronometer>(R.id.chronometer)

        var running = false
        var pauseOffset: Long = 0

        setButtonListeners(view, running, chronometer, pauseOffset, transitionManager)

        setChronometerListener(chronometer)

        view.findViewById<Button>(R.id.button_start).isEnabled = true
        view.findViewById<Button>(R.id.button_stop).isEnabled = false
    }

    private fun setChronometerListener(chronometer: Chronometer) {
        var notificationSent = false
        chronometer.setOnChronometerTickListener {
            val elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
            val hours = (elapsedTime / 1000 ).toInt()
            if (hours == 10 && !notificationSent) {
                sendLazyUserNotification("Torna in attività!", "Sei fermo da più di un'ora ")
                notificationSent = true
            }
        }
    }

    private fun setButtonListeners(
        view: View,
        running: Boolean,
        chronometer: Chronometer,
        pauseOffset: Long,
        transitionManager: UserActivityTransitionManager
    ) {
        var running1 = running
        var pauseOffset1 = pauseOffset
        view.findViewById<Button>(R.id.button_start).setOnClickListener {
            if (!running1) {
                chronometer.base = SystemClock.elapsedRealtime() - pauseOffset1
                chronometer.start()
                running1 = true

                view.findViewById<Button>(R.id.button_start).isEnabled = false
                view.findViewById<Button>(R.id.button_stop).isEnabled = true

                registerTransition(
                    DetectedActivity.STILL,
                    ActivityTransition.ACTIVITY_TRANSITION_ENTER
                )
            }

            startDetection(transitionManager)
        }

        view.findViewById<Button>(R.id.button_stop).setOnClickListener {
            if (running1) {
                chronometer.stop()
                pauseOffset1 = SystemClock.elapsedRealtime() - chronometer.base
                running1 = false

                view.findViewById<Button>(R.id.button_start).isEnabled = true
                view.findViewById<Button>(R.id.button_stop).isEnabled = false

                chronometer.base = SystemClock.elapsedRealtime()

                registerTransition(
                    DetectedActivity.STILL,
                    ActivityTransition.ACTIVITY_TRANSITION_EXIT
                )
            }
        }
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

            //registerTransition(DetectedActivity.STILL, ActivityTransition.ACTIVITY_TRANSITION_ENTER)
        } else {
            Log.d("WTF", "WTF")
        }
    }

    private fun registerTransition(activityType: Int, transitionType: Int) {
        val intent = Intent("TRANSITIONS_RECEIVER_ACTION").apply {
            putExtra("activityType", activityType)
            putExtra("transitionType", transitionType)
        }

        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }

    private fun sendLazyUserNotification(title : String, content : String) {

        val intent = Intent(NOTIFICATION_INTENT_FILTER)
            .putExtra("title", title)
            .putExtra("content", content)
            .putExtra("type", "walk")


        //activity?.sendBroadcast(intent)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }
}