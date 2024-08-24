package com.example.brockapp.fragment

import FriendsAdapter
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
import com.example.brockapp.viewmodel.FriendsViewModelFactory
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
    private lateinit var friendsViewModel : FriendsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        user = User.getInstance()

        credentialsProvider = CognitoCachingCredentialsProvider(requireContext(), "eu-west-3:8fe18ff5-1fe5-429d-b11c-16e8401d3a00", Regions.EU_WEST_3)
        s3Client = AmazonS3Client(credentialsProvider)
        val db : BrockDB = BrockDB.getInstance(requireContext())
        val friendsViewModelFactory = FriendsViewModelFactory(s3Client, db, requireContext())
        friendsViewModel = ViewModelProvider(this, friendsViewModelFactory)[FriendsViewModel::class.java]

        friendsRecyclerView = view.findViewById(R.id.friends_recycler_view)

        /*
        1.chiamare metodo observe() per mettere in ascolto il fragment
        2. aggiornare dati view model
        3. La lista viene aggiornata e sveglia il viewModel che aggiorna la view
         */

        syncDataFriendsButton.setOnClickListener {

            friendsViewModel.uploadUserData()
            //viewModel.updateFriendsData()

            syncDataFriendsButton.setEnabled(false)

            android.os.Handler().postDelayed( {
                syncDataFriendsButton.setEnabled(true)
            }, 5000)

            //observeFriends()
        }
    }


    /**
     * Popolo le card view con i dati aggiornati che arrivano dal view model
     */
    private fun observeFriends() {
        friendsViewModel.friends.observe(viewLifecycleOwner) { friends ->
            if (friends.isNotEmpty()) {
                populateRecyclerView(friends)
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
