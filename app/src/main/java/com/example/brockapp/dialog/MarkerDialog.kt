package com.example.brockapp.dialog

import com.example.brockapp.R
import com.example.brockapp.database.BrockDB
import com.example.brockapp.viewmodel.GeofenceViewModel

import java.util.Locale
import android.os.Bundle
import android.view.View
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import android.location.Address
import android.location.Geocoder
import kotlinx.coroutines.launch
import android.view.LayoutInflater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.model.Marker

class MarkerDialog(private val marker: Marker, private val viewModel: GeofenceViewModel): DialogFragment() {
    private lateinit var db: BrockDB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_map_marker_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = BrockDB.getInstance(requireContext())

        val name = marker.title
        val latitude = marker.position.latitude
        val longitude = marker.position.longitude

        val address = getAddress(name, latitude, longitude)

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setGravity(Gravity.CENTER)
        dialog?.setCanceledOnTouchOutside(true)

        setUpDialog(name, address)

        view.findViewById<TextView>(R.id.button_delete_marker).setOnClickListener {
            viewModel.deleteGeofenceArea(name, longitude, latitude)
            marker.remove()
            dismiss()
        }
    }

    private fun getAddress(name: String?, latitude: Double, longitude: Double): Address {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 10)

        if (!addresses.isNullOrEmpty()) {
            for (address in addresses) {
                if (address.featureName.equals(name)) return address
            }
        }

        // If the geocoder don't find the marker name inside the addresses list it will return the first
        return addresses!![0]
    }

    private fun setUpDialog(markerName: String?, address: Address) {
        requireView().findViewById<TextView>(R.id.text_view_feature_name).also {
            it.text = markerName
        }

        requireView().findViewById<TextView>(R.id.text_view_city).also {
            it.text = "${address.locality}, ${address.countryName}"
        }

        requireView().findViewById<TextView>(R.id.text_view_address).also {
            it.text = address.thoroughfare
        }
    }
}