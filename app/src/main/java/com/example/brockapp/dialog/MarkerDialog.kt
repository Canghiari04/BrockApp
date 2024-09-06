package com.example.brockapp.dialog

import com.example.brockapp.R
import com.example.brockapp.viewmodel.GeofenceViewModel

import android.view.View
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.view.ViewGroup
import android.widget.TextView
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.model.Marker

class MarkerDialog(private val marker: Marker, private val viewModel: GeofenceViewModel): DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_map_marker_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = marker.title
        val longitude = marker.position.longitude
        val latitude = marker.position.latitude

        val textViewLocality = view.findViewById<TextView>(R.id.text_locality_marker)
        val textViewLongitude = view.findViewById<TextView>(R.id.text_longitude_marker)
        val textViewLatitude = view.findViewById<TextView>(R.id.text_latitude_marker)

        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setGravity(Gravity.CENTER)
        dialog?.setCanceledOnTouchOutside(true)

        textViewLocality.setText(name)
        textViewLongitude.setText(longitude.toString())
        textViewLatitude.setText(latitude.toString())

        view.findViewById<Button>(R.id.delete_marker_button).setOnClickListener {
            viewModel.deleteGeofenceArea(name, longitude, latitude)
            marker.remove()
            dismiss()
        }

        view.findViewById<Button>(R.id.dismiss_dialog_button).setOnClickListener {
            dismiss()
        }
    }
}