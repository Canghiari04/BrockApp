package com.example.brockapp.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.brockapp.R
import com.example.brockapp.database.DbHelper
import com.example.brockapp.detect.UserActivityTransitionManager
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition



class StillFragment() : Fragment(R.layout.start_stop_activity_fragment) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val transitionManager : UserActivityTransitionManager = UserActivityTransitionManager(requireContext())
        super.onViewCreated(view, savedInstanceState)

        val dbHelper = DbHelper(requireContext())

        val chronometer = view.findViewById<Chronometer>(R.id.chronometer)
        val buttonStart: Button = view.findViewById(R.id.button_start)
        val buttonStop: Button = view.findViewById(R.id.button_stop)

        var pauseOffset: Long = 0
        var running = false

        buttonStart.setOnClickListener {
            if (!running) {
                chronometer.base = SystemClock.elapsedRealtime() - pauseOffset
                chronometer.start()
                running = true

                buttonStart.isEnabled = false
                buttonStop.isEnabled = true
            }

            startDetection(transitionManager)
        }

        buttonStop.setOnClickListener {
            if (running) {
                chronometer.stop()
                pauseOffset = SystemClock.elapsedRealtime() - chronometer.base
                running = false

                buttonStart.isEnabled = true
                buttonStop.isEnabled = false
            }
        }


        buttonStart.isEnabled = true
        buttonStop.isEnabled = false
    }


    private fun startDetection(transitionManager : UserActivityTransitionManager) {
        val context = requireContext()

        val request = transitionManager.getRequest()
        val pendingIntent = transitionManager.getPendingIntent(context)

        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
            val task = ActivityRecognition.getClient(context)
                .requestActivityTransitionUpdates(request, pendingIntent)

            task.addOnSuccessListener {
                Log.d("DETECT", "Ti sei correttamente connesso all'api")

            }

            task.addOnFailureListener { e: Exception ->
                // Handle error
                Log.d("DETECT", "Errore nella connessione all'api")
            }
        } else {
            Log.d("WTF", "WTF")
        }
    }
}
