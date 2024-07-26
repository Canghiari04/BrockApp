package com.example.brockapp


import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment

class HomeActivity : Fragment(R.layout.fragment_home) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("INFO", "Sono dentro home!")
    }
}