package com.example.brockapp.page

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.room.BrockDB
import com.example.brockapp.data.GeofenceTransition
import com.example.brockapp.adapter.GeofenceAdapter
import com.example.brockapp.viewModel.GroupViewModel
import com.example.brockapp.interfaces.PeriodRangeImpl
import com.example.brockapp.viewModel.GeofenceViewModel
import com.example.brockapp.singleton.MyS3ClientProvider
import com.example.brockapp.room.GeofenceTransitionsEntity
import com.example.brockapp.interfaces.ShowCustomToastImpl
import com.example.brockapp.viewModel.GroupViewModelFactory
import com.example.brockapp.viewModel.GeofenceViewModelFactory

import java.util.Locale
import android.os.Bundle
import android.view.View
import java.time.LocalDate
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import java.time.LocalDateTime
import java.time.format.TextStyle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import java.time.format.DateTimeFormatter
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import kotlin.time.Duration.Companion.milliseconds
import androidx.recyclerview.widget.LinearLayoutManager

abstract class GeofencePage: Fragment(R.layout.page_geofence) {

    private val currentMonth = PeriodRangeImpl().getMonthRange(LocalDate.now())

    private lateinit var spinner: Spinner
    private lateinit var recyclerView: RecyclerView

    protected val toastUtil = ShowCustomToastImpl()

    protected lateinit var button: Button
    protected lateinit var viewModelGroup: GroupViewModel
    protected lateinit var cardViewYouGeofencePage: CardView
    protected lateinit var cardViewUserGeofencePage: CardView
    protected lateinit var viewModelGeofence: GeofenceViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.text_view_welcome_geofence).text = 
            resources.getText(R.string.text_view_welcome_geofence)

        cardViewYouGeofencePage = view.findViewById(R.id.card_view_welcome_you_geofence_page)
        cardViewUserGeofencePage = view.findViewById(R.id.card_view_welcome_user_geofence_page)

        button = view.findViewById(R.id.button_user_geofence_page)
        spinner = view.findViewById(R.id.spinner_transitions_names)
        recyclerView = view.findViewById(R.id.recycler_view_geofence)

        val db = BrockDB.getInstance(requireContext())
        val s3Client = MyS3ClientProvider.getInstance(requireContext())

        val factoryGeofenceViewModel = GeofenceViewModelFactory(db)
        viewModelGeofence = ViewModelProvider(this, factoryGeofenceViewModel)[GeofenceViewModel::class.java]

        val factoryGroupViewModel = GroupViewModelFactory(s3Client, db)
        viewModelGroup = ViewModelProvider(this, factoryGroupViewModel)[GroupViewModel::class.java]

        setUpCardView()

        observeGeofenceTransitions()

        loadGeofenceTransitions(currentMonth.first, currentMonth.second)
    }

    protected abstract fun setUpCardView()

    protected abstract fun observeGeofenceTransitions()

    protected abstract fun loadGeofenceTransitions(startOfPeriod: String, endOfPeriod: String)

    protected fun getGroupedTransitions(items: List<GeofenceTransitionsEntity>): List<GeofenceTransition> {
        val listGroupedBy = items.groupBy { it.nameLocation }

        return listGroupedBy.map { (locationName, list) ->
            val firstLocation = list.first()

            val timestamps = list.map {
                val item = LocalDateTime.parse(
                    it.timestamp,
                    DateTimeFormatter.ofPattern(ISO_DATE_FORMAT)
                )

                val date = "${item.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US)}, ${item.month.toString().lowercase().replaceFirstChar { it.uppercase() }} ${item.dayOfMonth}"
                val hour = "at ${item.hour}:${item.minute}"

                Pair(date, hour)
            }

            val totalDurationMillis = list.fold(0L) { acc, location ->
                val timeSpent = location.exitTime - location.arrivalTime
                acc + timeSpent
            }

            val averageDurationMillis = totalDurationMillis / list.size
            val averageDuration = averageDurationMillis.milliseconds.toComponents { hours, minutes, seconds, _ ->
                "%01dh %01dm %01ds".format(hours, minutes, seconds)
            }

            GeofenceTransition(
                timestamps = timestamps,
                nameLocation = locationName,
                longitude = firstLocation.longitude,
                latitude = firstLocation.latitude,
                averageTime = averageDuration,
                count = list.size
            )
        }
    }

    protected fun populateSpinnerNames(transitions: List<GeofenceTransition>) {
        val spinnerItems = transitions.map {
            it.nameLocation
        }.toMutableList()

        spinnerItems.add(0, "All transitions")

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val items = if (spinnerItems[position] == "All transitions") {
                    transitions
                } else {
                    listOf(transitions[position - 1])
                }

                populateRecyclerView(items)
            }

            override fun onNothingSelected(parent: AdapterView<*>?  ) { }
        }
    }

    protected fun populateRecyclerView(transitions: List<GeofenceTransition>) {
        val adapter = GeofenceAdapter(transitions)
        val layout = LinearLayoutManager(requireContext())

        recyclerView.adapter = adapter
        recyclerView.layoutManager = layout
    }
}