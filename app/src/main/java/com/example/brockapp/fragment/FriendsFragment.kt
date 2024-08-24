package com.example.brockapp.fragment

import FriendsAdapter
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.example.brockapp.R
import com.example.brockapp.data.Friend
import com.example.brockapp.database.BrockDB
import com.example.brockapp.singleton.User
import com.example.brockapp.viewmodel.FriendsViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FriendsFragment: Fragment(R.layout.friends_fragment) {
    private var flag_sharing = false

    private lateinit var user: User
    private lateinit var s3Client: AmazonS3Client
    private lateinit var friendsRecyclerView: RecyclerView
    private lateinit var credentialsProvider: CognitoCachingCredentialsProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        user = User.getInstance()

        credentialsProvider = CognitoCachingCredentialsProvider(requireContext(), "eu-west-3:8fe18ff5-1fe5-429d-b11c-16e8401d3a00", Regions.EU_WEST_3)
        s3Client = AmazonS3Client(credentialsProvider)

        friendsRecyclerView = view.findViewById(R.id.friends_recycler_view)

        CoroutineScope(Dispatchers.IO).launch {
            view.findViewById<FloatingActionButton>(R.id.friends_synchronized_button).setOnClickListener {
                val db : BrockDB = BrockDB.getInstance(requireContext())
                val viewModel = FriendsViewModel(s3Client, db, requireContext())
                viewModel.uploadUserData()
            }
        }
    }

    private fun showShareDataDialog() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.permission_title)
            .setMessage(R.string.permission_share_data)
            .setPositiveButton(R.string.permission_positive_button) { dialog, _ ->
                dialog.dismiss()
                flag_sharing = true
            }
            .setNegativeButton(R.string.permission_negative_button) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun populateRecyclerView(friends: List<Friend>) {
        val adapter = FriendsAdapter(friends)
        val layoutManager = LinearLayoutManager(context)

        friendsRecyclerView.adapter = adapter
        friendsRecyclerView.layoutManager = layoutManager
    }
}
