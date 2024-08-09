package com.example.brockapp.fragment

import com.example.brockapp.R
import com.example.brockapp.detect.UserActivityTransitionManager

import android.view.View
import android.os.Bundle
import android.widget.Button
import android.os.SystemClock
import android.content.Intent
import android.widget.Chronometer
import androidx.fragment.app.Fragment
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.ActivityTransition
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class VehicleFragment() : Fragment(R.layout.start_stop_activity_fragment) {
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

            registerActivity(DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_ENTER, -1.0)
        }

        view.findViewById<Button>(R.id.button_stop).setOnClickListener {
            if (running) {
                chronometer.stop()
                pauseOffset = SystemClock.elapsedRealtime() - chronometer.base
                running = false

                view.findViewById<Button>(R.id.button_start).isEnabled = true
                view.findViewById<Button>(R.id.button_stop).isEnabled = false
            }
            //TODO implementare lettura distanza percorsa
            registerActivity(DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_EXIT, 1.0)
        }

        view.findViewById<Button>(R.id.button_start).isEnabled = true
        view.findViewById<Button>(R.id.button_stop).isEnabled = false
    }

//    private fun startDetection(transitionManager: UserActivityTransitionManager) {
//        val request = transitionManager.getRequest()
//        val myPendingIntentActivityRecognition = transitionManager.getPendingIntent(requireContext())
//
//        // Check richiesto obbligatoriamente prima di poter richiedere update su transitions activity.
//        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
//            val task = ActivityRecognition.getClient(requireContext()).requestActivityTransitionUpdates(request, myPendingIntentActivityRecognition)
//
//            task.addOnSuccessListener {
//                Log.d("DETECT", "Connesso all'API activity recognition")
//            }
//
//            task.addOnFailureListener {
//                Log.d("DETECT", "Errore di connessione con l'API activity recognition")
//            }
//
//            registerActivity(DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_ENTER)
//        } else {
//            Log.d("WTF", "WTF")
//        }
//    }

    private fun registerActivity(activityType: Int, transitionType: Int, distanceTravelled : Double) {
        // TODO --> PUT EXTRA ALL'INTENT PER DIVERSIFICARE LA TIPOLOGIA DI ACTIVITY RECOGNITION DA CONDURRE.

        val intent = Intent("TRANSITIONS_RECEIVER_ACTION").apply {
            putExtra("activityType", activityType)
            putExtra("transitionType", transitionType)
            putExtra("distanceTravelled", distanceTravelled)
        }

        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }
}