package com.example.brockapp.page

import com.example.brockapp.R
import com.example.brockapp.data.Subscriber
import com.example.brockapp.database.BrockDB
import com.example.brockapp.activity.FriendActivity
import com.example.brockapp.viewmodel.UserViewModel
import com.example.brockapp.viewmodel.GroupViewModel
import com.example.brockapp.adapter.SubscriberAdapter
import com.example.brockapp.viewmodel.NetworkViewModel
import com.example.brockapp.singleton.MyS3ClientProvider
import com.example.brockapp.interfaces.ShowCustomToastImpl
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

class SubscribersPage: Fragment(R.layout.page_subscribers) {
    private val toastUtil = ShowCustomToastImpl()

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModelUser: UserViewModel
    private lateinit var viewModelGroup: GroupViewModel
    private lateinit var viewModelNetwork: NetworkViewModel
    private lateinit var searchTextView: AutoCompleteTextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view_subscribers_page)

        searchTextView = view.findViewById(R.id.auto_complete_text_view_subscribers_page)
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

        observeNetwork()
        observeUsers()
        observeSuggestion()
    }

    private fun setUpSearchTextView() {
        searchTextView.addTextChangedListener {
            val user = searchTextView.text.toString()
            viewModelGroup.getSuggestions(user)
        }
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

    private fun observeUsers() {
        viewModelGroup.subscribers.observe(viewLifecycleOwner) { items ->
            if (items.isNullOrEmpty()) {
                // Populate must be done after the friend's loading
                toastUtil.showBasicToast(
                    "Nobody found in Subscribers section",
                    requireContext()
                )
            } else {
                populateRecyclerView(items.filterNotNull())
            }
        }
    }

    // Refactor the names
    private fun populateRecyclerView(subscribers: List<Subscriber>) {
        val adapter = SubscriberAdapter(subscribers) { username -> showSubscribe(username) }
        val layoutManager = LinearLayoutManager(context)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager
    }

    private fun showSubscribe(username: String) {
        val intent = Intent(requireContext(), FriendActivity::class.java).putExtra("USERNAME_SUBSCRIBER", username)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun observeSuggestion() {
        viewModelGroup.suggestions.observe(viewLifecycleOwner) { items ->
            if (items.isNotEmpty()) {
                populateRecyclerView(items.filterNotNull())
            } else {
                toastUtil.showWarningToast(
                    "Username not found",
                    requireContext()
                )
            }
        }
    }
}