package com.example.brockapp.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import androidx.fragment.app.Fragment
import com.example.brockapp.R
import com.example.brockapp.activity.HomeActivity
import com.example.brockapp.activity.NewUserActivity
import com.example.brockapp.database.DbHelper

class StillFragment : Fragment(R.layout.start_stop_activity_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

        // Imposta lo stato iniziale dei pulsanti
        buttonStart.isEnabled = true
        buttonStop.isEnabled = false
    }






}
