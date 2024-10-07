package com.example.brockapp.dialog

import com.example.brockapp.R
import com.example.brockapp.viewmodel.GeofenceViewModel

import java.util.Locale
import android.os.Bundle
import android.view.View
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import android.location.Geocoder
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

        val textViewLocality = view.findViewById<TextView>(R.id.text_view_locality_marker)
        val textViewAddress = view.findViewById<TextView>(R.id.text_view_address_marker)

        dialog?.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setGravity(Gravity.CENTER)
        dialog?.setCanceledOnTouchOutside(true)

        textViewLocality.setText(name)
        textViewAddress.setText(getAddress(latitude, longitude))

        view.findViewById<TextView>(R.id.delete_marker_button).setOnClickListener {
            viewModel.deleteGeofenceArea(name, longitude, latitude)
            marker.remove()
            dismiss()
        }
    }

    private fun getAddress(latitude: Double, longitude: Double): String? {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val address = geocoder.getFromLocation(latitude, longitude, 1)

        return if (!address.isNullOrEmpty()) {
            "${address[0].thoroughfare}, ${address[0].locality}"
        } else {
            null
        }
    }
}