package com.example.brockapp.fragment


import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.brockapp.R
import com.example.brockapp.WALK_ACTIVITY_TYPE
import com.example.brockapp.adapter.HomeAdapter
import com.example.brockapp.data.UserActivity
import com.example.brockapp.database.BrockDB
import com.example.brockapp.singleton.User
import com.example.brockapp.viewmodel.ActivitiesViewModel
import com.example.brockapp.viewmodel.ActivitiesViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomeFragment : Fragment(R.layout.home_fragment) {
    private lateinit var user: User
    private lateinit var db: BrockDB
    private lateinit var viewModel: ActivitiesViewModel

    private lateinit var stepsProgressBar: ProgressBar
    private lateinit var stepsCountText: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.home_recycler_view)

        db = BrockDB.getInstance(requireContext())
        val factoryViewModelActivities = ActivitiesViewModelFactory(db)

        viewModel = ViewModelProvider(this, factoryViewModelActivities)[ActivitiesViewModel::class.java]

        user = User.getInstance()
        viewModel.getWeekUserActivities(LocalDate.now(), user)

        viewModel.sortedWeekActivitiesDayList.observe(viewLifecycleOwner) { item ->
            if (item.isNotEmpty()) {
                populateHomeRecyclerView(recyclerView, item)

                CoroutineScope(Dispatchers.IO).launch {
                    populateStepProgressBar(view)

                }
            }
        }

        viewModel.sortedDayExitActivitiesList.observe(viewLifecycleOwner) { item ->

            val userWalkActivities = item.filter { it.type == WALK_ACTIVITY_TYPE}

            val steps = userWalkActivities.parallelStream().mapToInt { it.info.toInt() }.sum()
            CoroutineScope(Dispatchers.IO).launch {
                updateSteps(steps)
            }

        }
    }

    private suspend fun populateStepProgressBar(view: View) {
        stepsProgressBar = view.findViewById(R.id.steps_progress_bar)
        stepsCountText = view.findViewById(R.id.steps_count_text)

        val steps = db.UserWalkActivityDao().getEndingWalkActivitiesByUserIdAndPeriod(user.id,
            LocalDate.now().atTime(0,0,0).toString(), LocalDate.now().atTime(23,59,59).toString()).parallelStream().mapToInt { it.stepNumber.toInt() }.sum()

        updateSteps(steps)
    }

    private fun populateHomeRecyclerView(homeList: RecyclerView, activities: List<UserActivity>) {
        val adapterHome = HomeAdapter(activities)
        val layoutManager = LinearLayoutManager(requireContext())

        homeList.adapter = adapterHome
        homeList.layoutManager = layoutManager
    }

    private fun updateSteps(steps: Int) {
        stepsProgressBar.progress = steps
        stepsCountText.text = "$steps/10000 passi"
    }
}