package com.example.brockapp.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.brockapp.R
import com.example.brockapp.detect.UserActivityTransitionManager
import com.google.android.gms.location.ActivityTransition

class StillFragment() : Fragment(R.layout.start_stop_activity_fragment) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val handlerTransition = UserActivityTransitionManager(context)

        view.findViewById<Button>(R.id.button_start).setOnClickListener {
            Log.d("DETECT", "Iniziamo a fare il detect!")
        }

        view.findViewById<Button>(R.id.button_stop).setOnClickListener {
            Log.d("DETECT", "Terminiamo il detect!")
        }
    }
}
