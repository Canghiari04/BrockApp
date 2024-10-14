package com.example.brockapp.page

import com.example.brockapp.R
import com.example.brockapp.database.BrockDB
import com.example.brockapp.adapter.FriendAdapter
import com.example.brockapp.activity.FriendActivity
import com.example.brockapp.viewmodel.UserViewModel
import com.example.brockapp.viewmodel.GroupViewModel
import com.example.brockapp.viewmodel.NetworkViewModel
import com.example.brockapp.singleton.MyS3ClientProvider
import com.example.brockapp.viewmodel.UserViewModelFactory
import com.example.brockapp.viewmodel.GroupViewModelFactory

import java.io.File
import android.view.View
import android.os.Bundle
import android.content.Intent
import androidx.fragment.app.Fragment
import android.widget.AutoCompleteTextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager

class FriendPage: Fragment(R.layout.fragment_friends) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModelUser: UserViewModel
    private lateinit var viewModelGroup: GroupViewModel
    private lateinit var viewModelNetwork: NetworkViewModel
    private lateinit var searchTextView: AutoCompleteTextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view_page_friend)

        searchTextView = view.findViewById(R.id.auto_complete_text_view_friend_page)
        setUpSearchTextView()

        viewModelNetwork = ViewModelProvider(requireActivity())[NetworkViewModel::class.java]

        val db: BrockDB = BrockDB.getInstance(requireContext())
        val s3Client = MyS3ClientProvider.getInstance(requireContext())
        val file = File(requireContext().filesDir, "user_data.json")

        val viewModelFactoryGroup = GroupViewModelFactory(s3Client, db)
        viewModelGroup =
            ViewModelProvider(requireActivity(), viewModelFactoryGroup)[GroupViewModel::class.java]

        val viewModelFactoryUser = UserViewModelFactory(db, s3Client, file)
        viewModelUser =
            ViewModelProvider(requireActivity(), viewModelFactoryUser)[UserViewModel::class.java]

        observeUsers()
        observeNetwork()
        observeSuggestion()
    }

    private fun setUpSearchTextView() {
        searchTextView.addTextChangedListener {
            val user = searchTextView.text.toString()
            viewModelGroup.getSuggestions(user)
        }
    }

    private fun observeUsers() {
        viewModelGroup.users.observe(viewLifecycleOwner) { items ->
            if (items.isNotEmpty()) {
                populateRecyclerView(items.take(27))
            }
        }
    }

    private fun populateRecyclerView(usernames: List<String>) {
        val adapter = FriendAdapter(usernames) { username -> showFriend(username) }
        val layoutManager = LinearLayoutManager(context)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager
    }

    private fun showFriend(username: String) {
        val intent = Intent(requireContext(), FriendActivity::class.java).putExtra("USERNAME_USER", username)
        startActivity(intent)
        requireActivity().finish()
    }

    // Able or disable the feature in base of the state of the network
    private fun observeNetwork() {
        viewModelNetwork.currentNetwork.observe(viewLifecycleOwner) { item ->
            if (item) {
                viewModelGroup.getAllUsers()
            }

            searchTextView.isEnabled = item
        }
    }

    private fun observeSuggestion() {
        viewModelGroup.suggestions.observe(viewLifecycleOwner) { items ->
            if (items.isNotEmpty()) {
                populateRecyclerView(items)
            }
        }
    }

//    private fun observeAddedFriend() {
//        viewModelGroup.errorAddFriend.observe(viewLifecycleOwner) { errorAddFriend ->
//            if (errorAddFriend) {
//                Log.d("FRIEND_PAGE", "Friend added to the list")
//            } else {
//                toastUtil.showBasicToast(
//                    "User is already a friend",
//                    requireContext()
//                )
//            }
//        }
//    }
}