package com.example.brockapp.fragment

import com.example.brockapp.R
import com.example.brockapp.room.BrockDB
import com.example.brockapp.data.Activity
import com.example.brockapp.util.ScheduleWorkerUtil
import com.example.brockapp.dialog.DatePickerDialog
import com.example.brockapp.interfaces.PeriodRangeImpl
import com.example.brockapp.viewModel.ActivitiesViewModel
import com.example.brockapp.adapter.ActivitiesSummaryAdapter
import com.example.brockapp.viewModel.ActivitiesViewModelFactory

import android.os.Bundle
import android.view.View
import java.time.LocalDate
import android.widget.Spinner
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.compose.material.icons.Icons
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.RecyclerView
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.filled.DateRange
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.compose.material3.FloatingActionButtonDefaults

class ActivitiesSummaryFragment: Fragment(R.layout.fragment_activities_summary) {

    private val rangeUtil = PeriodRangeImpl()

    private lateinit var type: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: ActivitiesViewModel
    private lateinit var listActivities: List<Activity>
    private lateinit var adapter: ActivitiesSummaryAdapter
    private lateinit var scheduleWorkerUtil: ScheduleWorkerUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = BrockDB.getInstance(requireContext())

        val activitiesFactoryViewModel = ActivitiesViewModelFactory(db)
        viewModel = ViewModelProvider(this, activitiesFactoryViewModel)[ActivitiesViewModel::class.java]

        type = "All"
        scheduleWorkerUtil = ScheduleWorkerUtil(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinner = view.findViewById<Spinner>(R.id.spinner_type_activity)
        setUpActivityTypeSpinner(spinner)

        val pickButton = view.findViewById<ComposeView>(R.id.button_pick_date)
        pickButton.setContent {
            DefineFloatingButton(
                icon = Icons.Filled.DateRange
            ) {
                val datePickerFragment = DatePickerDialog(viewModel)
                datePickerFragment.show(requireActivity().supportFragmentManager, "DatePicker")
            }
        }

        recyclerView = view.findViewById(R.id.recycler_view_activities_summary)

        observeActivities()

        val range = rangeUtil.getDayRange(LocalDate.now())
        viewModel.getAllActivities(range.first, range.second)
    }

    private fun setUpActivityTypeSpinner(spinner: Spinner) {
        val spinnerItems = resources.getStringArray(R.array.spinner_all_activities)

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
                type = spinnerItems[position]
                populateRecyclerView(scheduleWorkerUtil, listActivities, viewModel)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }
    }

    @Composable
    private fun DefineFloatingButton(icon: ImageVector, onClick: () -> Unit) {
        FloatingActionButton(
            onClick = {
                onClick()
            },
            containerColor = colorResource(id = R.color.uni_red),
            elevation = FloatingActionButtonDefaults.elevation(4.dp),
            shape = CircleShape
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Calendar",
                tint = Color.White
            )
        }
    }

    private fun observeActivities() {
        viewModel.listActivities.observe(viewLifecycleOwner) { list ->
            listActivities = list
            populateRecyclerView(scheduleWorkerUtil, listActivities, viewModel)
        }
    }

    private fun populateRecyclerView(scheduleWorkerUtil: ScheduleWorkerUtil, list: List<Activity>, viewModel: ActivitiesViewModel) {
        val listFiltered = list.filter {
            it.type.contains(type) || type == "All"
        }

        adapter = ActivitiesSummaryAdapter(scheduleWorkerUtil, listFiltered, viewModel)
        val layoutManager = LinearLayoutManager(requireContext())

        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager
    }
}