package com.example.brockapp.dialog

import com.example.brockapp.R
import com.example.brockapp.room.BrockDB
import com.example.brockapp.viewmodel.GeofenceViewModel
import com.example.brockapp.interfaces.ScheduleWorkerImpl
import com.example.brockapp.interfaces.ReverseGeocodingImpl

import android.os.Bundle
import android.view.View
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import android.location.Address
import org.osmdroid.views.MapView
import android.view.LayoutInflater
import org.osmdroid.views.overlay.Marker
import androidx.fragment.app.DialogFragment

class MarkerDialog(private val marker: Marker, private val map: MapView, private val viewModel: GeofenceViewModel): DialogFragment() {
    private lateinit var db: BrockDB
    private lateinit var geocodeUtil: ReverseGeocodingImpl

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        geocodeUtil = ReverseGeocodingImpl(requireContext())
        return inflater.inflate(R.layout.dialog_map_marker_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = BrockDB.getInstance(requireContext())

        val name = marker.title ?: null
        val latitude = marker.position.latitude
        val longitude = marker.position.longitude

        val scheduleWorkerUtil = ScheduleWorkerImpl(requireContext())
        val address = geocodeUtil.getAddress(name, latitude, longitude)

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setGravity(Gravity.CENTER)
        dialog?.setCanceledOnTouchOutside(true)

        setUpDialog(name, address)

        view.findViewById<TextView>(R.id.button_delete_marker).setOnClickListener {
            scheduleWorkerUtil.scheduleDeleteGeofenceAreaWorker(latitude, longitude)
            viewModel.deleteGeofenceArea(longitude, latitude)
            map.overlays.remove(marker)
            dismiss()
        }
    }

    private fun setUpDialog(markerName: String?, address: Address) {
        requireView().findViewById<TextView>(R.id.text_view_feature_name).also {
            it.text = markerName ?: "Unknown"
        }

        requireView().findViewById<TextView>(R.id.text_view_city).also {
            if (address.locality.isNullOrBlank() || address.countryName.isNullOrBlank()) {
                it.text = "Unknown"
            } else {
                it.text = "${address.locality}, ${address.countryName}"
            }
        }

        requireView().findViewById<TextView>(R.id.text_view_address).also {
            if (address.thoroughfare.isNullOrBlank()) {
                it.text = "Unknown"
            } else {
                it.text = address.thoroughfare
            }
        }
    }
}