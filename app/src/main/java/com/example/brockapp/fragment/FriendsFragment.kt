package com.example.brockapp.fragment

import com.example.brockapp.R
import com.example.brockapp.singleton.User
import com.example.brockapp.database.BrockDB
import com.example.brockapp.adapter.FriendsAdapter
import com.example.brockapp.dialog.NewFriendDialog
import com.example.brockapp.viewmodel.UserViewModel
import com.example.brockapp.activity.FriendActivity
import com.example.brockapp.adapter.SuggestionsAdapter
import com.example.brockapp.singleton.S3ClientProvider
import com.example.brockapp.viewmodel.FriendsViewModel
import com.example.brockapp.viewmodel.NetworkViewModel
import com.example.brockapp.viewmodel.UserViewModelFactory
import com.example.brockapp.viewmodel.FriendsViewModelFactory

import java.io.File
import android.util.Log
import android.view.View
import android.os.Bundle
import android.widget.Toast
import android.content.Intent
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FriendsFragment: Fragment(R.layout.fragment_friends) {
    private lateinit var viewModelUser: UserViewModel
    private lateinit var syncButton: FloatingActionButton
    private lateinit var viewModelNetwork: NetworkViewModel
    private lateinit var viewModelFriends: FriendsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = User.getInstance()

        viewModelNetwork = ViewModelProvider(requireActivity())[NetworkViewModel::class.java]

        val db: BrockDB = BrockDB.getInstance(requireContext())
        val s3Client = S3ClientProvider.getInstance(requireContext())
        val file = File(requireContext().filesDir, "user_data.json")

        val viewModelFactoryFriends = FriendsViewModelFactory(s3Client, db, file)
        viewModelFriends = ViewModelProvider(
            requireActivity(),
            viewModelFactoryFriends
        )[FriendsViewModel::class.java]

        val viewModelFactoryUser = UserViewModelFactory(db, s3Client, file)
        viewModelUser = ViewModelProvider(
            requireActivity(),
            viewModelFactoryUser
        )[UserViewModel::class.java]

        observeNetwork()
        observeFriends()
        observeSuggestion()
        observeAddedFriend()

        viewModelFriends.getCurrentFriends(user.id)

        syncButton = view.findViewById(R.id.user_synchronized_button)

        syncButton.setOnClickListener {
            if (user.flag) {
                syncUserData(syncButton)
            } else {
                showShareDataDialog()
            }

            android.os.Handler().postDelayed({
                syncButton.isEnabled = true
            }, 5000)
        }

        val usernameTextView = view.findViewById<EditText>(R.id.search_user_text_area)

        usernameTextView.addTextChangedListener {
            val usernameToSearch = usernameTextView.text.toString()

            if (usernameToSearch.length > 2) {
                viewModelFriends.getSuggestions(usernameToSearch)
            } else {
                Log.d("FRIENDS_FRAGMENT", "Search with empty body not supported.")
            }
        }
    }
    
    private fun observeNetwork() {
        viewModelNetwork.currentNetwork.observe(viewLifecycleOwner) { currentNetwork ->
            if (!currentNetwork) {
                view?.findViewById<EditText>(R.id.search_user_text_area)?.isEnabled = false
                view?.findViewById<FloatingActionButton>(R.id.user_synchronized_button)?.hide()
            } else {
                view?.findViewById<EditText>(R.id.search_user_text_area)?.isEnabled = true
                view?.findViewById<FloatingActionButton>(R.id.user_synchronized_button)?.show()
            }
        }
    }

    private fun observeFriends() {
        viewModelFriends.friends.observe(viewLifecycleOwner) { friends ->
            if (!friends.isNullOrEmpty()) {
                val recyclerView = view?.findViewById<RecyclerView>(R.id.friends_recycler_view)
                populateFriendsRecyclerView(friends, recyclerView)
            }
        }
    }

    private fun observeSuggestion() {
        viewModelFriends.suggestions.observe(viewLifecycleOwner) { suggestions ->
            if (suggestions.isNotEmpty()) {
                val recyclerView = view?.findViewById<RecyclerView>(R.id.suggestions_recycler_view)
                populateSuggestionsRecyclerView(suggestions, recyclerView)
            }
        }
    }

    private fun observeAddedFriend() {
        viewModelFriends.errorAddFriend.observe(viewLifecycleOwner) { errorAddFriend ->
            if (errorAddFriend) {
                Log.d("FRIENDS_FRAGMENT", "Amico aggiunto alla lista.")
            } else {
                Toast.makeText(
                    context,
                    "Amico già presente nella lista",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showShareDataDialog() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.permission_title)
            .setMessage(R.string.permission_share_data)
            .setPositiveButton(R.string.permission_positive_button) { dialog, _ ->
                dialog.dismiss()
                User.flag = true
                viewModelUser.changeSharingDataFlag(User.username, User.password)
                syncUserData(syncButton)
            }
            .setNegativeButton(R.string.permission_negative_button) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun syncUserData(syncButton: FloatingActionButton) {
        viewModelFriends.uploadUserData()
        syncButton.isEnabled = false
        Toast.makeText(context, "Dati correttamente sincronizzati", Toast.LENGTH_SHORT).show()
    }

    private fun populateFriendsRecyclerView(friends: List<String>, friendsRecyclerView: RecyclerView?) {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val friendsAdapter = FriendsAdapter(friends) { friend ->
            val intent = Intent(
                context,
                FriendActivity::class.java
            ).putExtra("FRIEND_USERNAME", friend)
            startActivity(intent)
            activity?.finish()
        }

        friendsRecyclerView?.adapter = friendsAdapter
        friendsRecyclerView?.layoutManager = layoutManager
    }

    private fun populateSuggestionsRecyclerView(usernames: List<String>, suggestionsRecyclerView: RecyclerView?) {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val adapter = SuggestionsAdapter(usernames) { username ->
            showNewFriendDialog(username, viewModelFriends)
        }

        suggestionsRecyclerView?.layoutManager = layoutManager
        suggestionsRecyclerView?.adapter = adapter
    }

    private fun showNewFriendDialog(username: String, viewModel: FriendsViewModel) {
        activity?.let {
            NewFriendDialog(username, viewModel).show(
                it.supportFragmentManager,
                "CUSTOM_NEW_FRIEND_DIALOG"
            )
        }
    }
}