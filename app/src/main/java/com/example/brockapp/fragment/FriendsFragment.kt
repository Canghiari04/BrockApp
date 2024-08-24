package com.example.brockapp.fragment

import com.example.brockapp.R

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FriendsFragment: Fragment(R.layout.fragment_friends) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observer
    }

    private fun populateRecyclerView() {
        // NECESSARIO UN VIEW MODEL IN CUI APPENA ACCETTA IL PERMESSO VA A POPOLARE UN ARRAY NEL
        // VIEW MODEL, USATO POI PER LA RECYCLER VIEW
    }
}