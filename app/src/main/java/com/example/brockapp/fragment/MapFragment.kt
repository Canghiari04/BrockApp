import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.brockapp.R
import com.example.brockapp.database.BrockDB
import com.example.brockapp.database.GeofenceAreaEntry
import com.example.brockapp.singleton.MyGeofence
import com.example.brockapp.viewmodel.GeofenceViewModel
import com.example.brockapp.viewmodel.GeofenceViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class MapFragment: Fragment(R.layout.map_fragment), OnMapReadyCallback {
    private lateinit var db: BrockDB
    private lateinit var map: GoogleMap
    private lateinit var geofence: MyGeofence
    private lateinit var viewModel: GeofenceViewModel
    private lateinit var mapFragment: SupportMapFragment

    private var mapMarker = mutableMapOf<String, LatLng>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        geofence = MyGeofence.getInstance()

        mapFragment = childFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        db = BrockDB.getInstance(requireContext())
        val factoryViewModel = GeofenceViewModelFactory(db)

        viewModel = ViewModelProvider(this, factoryViewModel)[GeofenceViewModel::class.java]

        observeGeofenceAreas()

        val input = view.findViewById<AutoCompleteTextView>(R.id.text_new_area)

        input.addTextChangedListener {
            val userInput = input.text.toString()
            if (userInput.isNotEmpty() && userInput.length >= 3) {
                showSuggestions(userInput)
            }
        }

        input.setOnItemClickListener { parent, _, position, _ ->
            val selectedLocation = parent.getItemAtPosition(position) as String
            val (address, location) = getAddressAndLocation(selectedLocation)

            if (address != null && location != null) {
                val geofenceArea = GeofenceAreaEntry(
                    longitude = location.longitude,
                    latitude = location.latitude,
                    name = address.featureName
                )

                viewModel.insertGeofenceArea(geofenceArea)
                addNewMarker(geofenceArea)

                // Svuota l'input e chiudi il dropdown
                input.setText("")
            } else {
                Toast.makeText(requireContext(), R.string.toast_error_map, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showSuggestions(query: String) {

        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {


            // Implementazione del GeocodeListener
            val geocodeListener = Geocoder.GeocodeListener { addresses ->
                val suggestions: MutableList<String> = mutableListOf()

                addresses?.let {
                    for (address in it) {
                        suggestions.add(address.getAddressLine(0))
                    }
                }

                // Aggiorna l'interfaccia utente sul thread principale
                CoroutineScope(Dispatchers.Main).launch {
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suggestions)
                    val input = view?.findViewById<AutoCompleteTextView>(R.id.text_new_area)
                    input?.setAdapter(adapter)
                    input?.showDropDown()
                }
            }

            // Avvio della ricerca asincrona
            geocoder.getFromLocationName(query, 3, geocodeListener)

        } else {
            // Gestione per versioni di Android inferiori a 13 (API 33)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val addresses = geocoder.getFromLocationName(query, 3)
                    val suggestions: MutableList<String> = mutableListOf()

                    addresses?.let {
                        for (address in it) {
                            suggestions.add(address.getAddressLine(0))
                        }
                    }

                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suggestions)
                    val input = view?.findViewById<AutoCompleteTextView>(R.id.text_new_area)
                    input?.setAdapter(adapter)
                    input?.showDropDown()

                } catch (e: Exception) {
                    Log.e("MAP_FRAGMENT", e.toString())
                }
            }
        }
    }



    private fun addNewMarker(geofenceArea: GeofenceAreaEntry) {
        val marker = LatLng(geofenceArea.latitude, geofenceArea.longitude)

        map.addMarker(
            MarkerOptions()
                .position(marker)
                .title(geofenceArea.name)
        )

        map.addCircle(
            CircleOptions()
                .center(marker)
                .radius(geofence.radius.toDouble())
                .strokeColor(R.color.uni_red)
                .strokeWidth(2.5f)
        )

        // Centra la mappa sul nuovo marker
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker, 15f))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val startingPoint = LatLng(41.8719, 12.5674)
        val position = CameraPosition.Builder()
            .target(startingPoint)
            .zoom(5.5f)
            .build()

        map.moveCamera(
            CameraUpdateFactory.newCameraPosition(position)
        )

        populateMapOfMarker()
    }

    private fun observeGeofenceAreas() {
        viewModel.areas.observe(viewLifecycleOwner) { areas ->
            if (areas.isNotEmpty()) {
                for (area in areas) {
                    val marker = LatLng(area.latitude, area.longitude)
                    mapMarker[area.name] = marker
                }
            } else {
                Log.d("MAP_FRAGMENT", "No geofence areas")
            }
        }
    }

    private fun populateMapOfMarker() {
        mapMarker.forEach { (key, value) ->
            map.addMarker(
                MarkerOptions()
                    .position(value)
                    .title(key)
            )

            map.addCircle(
                CircleOptions()
                    .center(value)
                    .radius(geofence.radius.toDouble())
                    .strokeColor(R.color.uni_red)
                    .strokeWidth(2.5f)
            )
        }
    }

    private fun getAddressAndLocation(item: String): Pair<Address?, LatLng?> {
        var address: Address? = null
        var location: LatLng? = null

        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        try {
            val addresses = geocoder.getFromLocationName(item, 1)

            if (addresses != null && addresses.isNotEmpty()) {
                address = addresses[0]
                location = LatLng(address.latitude, address.longitude)
            } else {
                return Pair(null, null)
            }
        } catch (e: Exception) {
            Log.e("MAP_FRAGMENT", e.toString())
        }

        return Pair(address, location)
    }
}
