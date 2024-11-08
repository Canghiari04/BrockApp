package com.example.brockapp.page

import com.example.brockapp.R
import com.example.brockapp.room.BrockDB
import com.example.brockapp.data.TransitionAverage
import com.example.brockapp.adapter.GeofenceAdapter
import com.example.brockapp.viewmodel.GroupViewModel
import com.example.brockapp.interfaces.PeriodRangeImpl
import com.example.brockapp.viewmodel.GeofenceViewModel
import com.example.brockapp.singleton.MyS3ClientProvider
import com.example.brockapp.room.GeofenceTransitionsEntity
import com.example.brockapp.interfaces.ShowCustomToastImpl
import com.example.brockapp.viewmodel.GroupViewModelFactory
import com.example.brockapp.viewmodel.GeofenceViewModelFactory

import android.os.Bundle
import android.view.View
import java.time.LocalDate
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import kotlin.time.Duration.Companion.milliseconds
import androidx.recyclerview.widget.LinearLayoutManager

abstract class GeofencePage: Fragment(R.layout.page_geofence) {
    private val rangeUtil = PeriodRangeImpl()

    private lateinit var spinnerName: Spinner
    private lateinit var spinnerMonth: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var period: Pair<String, String>

    protected val toastUtil = ShowCustomToastImpl()

    protected lateinit var buttonUser: Button
    protected lateinit var viewModelGroup: GroupViewModel
    protected lateinit var cardViewYouGeofencePage: CardView
    protected lateinit var cardViewUserGeofencePage: CardView
    protected lateinit var viewModelGeofence: GeofenceViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.text_view_welcome_geofence).text =
            ("Geofence Area Overview: Here you'll find all the geofenced areas you've accessed")

        cardViewYouGeofencePage = view.findViewById(R.id.card_view_welcome_you_geofence_page)
        cardViewUserGeofencePage = view.findViewById(R.id.card_view_welcome_user_geofence_page)

        buttonUser = view.findViewById(R.id.button_user_geofence_page)

        spinnerMonth = view.findViewById(R.id.spinner_transitions_temporal)
        spinnerName = view.findViewById(R.id.spinner_transitions_names)

        recyclerView = view.findViewById(R.id.recycler_view_page_geofence)

        val db = BrockDB.getInstance(requireContext())
        val s3Client = MyS3ClientProvider.getInstance(requireContext())

        val factoryGeofenceViewModel = GeofenceViewModelFactory(db)
        viewModelGeofence = ViewModelProvider(this, factoryGeofenceViewModel)[GeofenceViewModel::class.java]

        val factoryGroupViewModel = GroupViewModelFactory(s3Client, db)
        viewModelGroup = ViewModelProvider(this, factoryGroupViewModel)[GroupViewModel::class.java]

        setUpCardView()

        observeGeofenceTransitions()

        populateSpinnerTemporal()
    }

    protected abstract fun setUpCardView()

    protected abstract fun observeGeofenceTransitions()

    private fun populateSpinnerTemporal() {
        val spinnerItems = resources.getStringArray(R.array.spinner_temporal_items)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonth.adapter = adapter

        spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val item = spinnerItems[position]

                when (item) {
                    "Day" -> {
                        period = rangeUtil.getDayRange(LocalDate.now())
                        loadGeofenceTransitions(
                            period.first,
                            period.second
                        )
                    }

                    "Week" -> {
                        period = rangeUtil.getWeekRange(LocalDate.now())
                        loadGeofenceTransitions(
                            period.first,
                            period.second
                        )
                    }

                    "Month" -> {
                        period = rangeUtil.getMonthRange(LocalDate.now())
                        loadGeofenceTransitions(
                            period.first,
                            period.second
                        )
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                period = rangeUtil.getDayRange(LocalDate.now())
                loadGeofenceTransitions(
                    period.first,
                    period.second
                )
            }
        }
    }

    protected abstract fun loadGeofenceTransitions(startOfPeriod: String, endOfPeriod: String)

    protected fun getGroupedTransitions(items: List<GeofenceTransitionsEntity>): List<TransitionAverage> {
        val groupedByLocation = items.groupBy { it.nameLocation }

        return groupedByLocation.map { (locationName, locationList) ->
            val firstLocation = locationList.first()

            val totalDurationMillis = locationList.fold(0L) { acc, location ->
                val timeSpent = location.exitTime - location.arrivalTime
                acc + timeSpent
            }

            val averageDurationMillis = totalDurationMillis / locationList.size
            val averageDuration = averageDurationMillis.milliseconds.toComponents { hours, minutes, seconds, _ ->
                "%01dh %01dm %01ds".format(hours, minutes, seconds)
            }

            TransitionAverage(
                nameLocation = locationName,
                longitude = firstLocation.longitude,
                latitude = firstLocation.latitude,
                averageTime = averageDuration,
                count = locationList.size
            )
        }
    }

    protected fun populateSpinnerNames(transitions: List<TransitionAverage>) {
        val spinnerItems = transitions.map {
            it.nameLocation
        }.toMutableList()

        spinnerItems.add(0, "All transitions")

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerName.adapter = adapter

        spinnerName.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                populateRecyclerView(
                    mutableListOf<TransitionAverage>().run {
                        if (spinnerItems[position] == "All transitions") {
                            transitions
                        } else {
                            mutableListOf<TransitionAverage>().also {
                                it.add(transitions[position])
                            }
                        }
                    }
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }
    }

    protected fun populateRecyclerView(transitions: List<TransitionAverage>) {
        val adapter = GeofenceAdapter(transitions)
        val layoutManager = LinearLayoutManager(requireContext())

        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager
    }
}