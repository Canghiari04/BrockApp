package com.example.brockapp.fragment

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.brockapp.R

class PageLoaderActivityFragment : Fragment(R.layout.page_loader_activity_fragment) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_still).setOnClickListener {
            replaceFragments(StillFragment())
        }

        view.findViewById<Button>(R.id.button_walk).setOnClickListener {
            replaceFragments(WalkFragment())
        }

        view.findViewById<Button>(R.id.button_vehicle).setOnClickListener {
            replaceFragments(VehicleFragment())
        }
    }

    private fun replaceFragments(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.detect_fragment, fragment)
            addToBackStack(null)
            commit()
        }
    }
}