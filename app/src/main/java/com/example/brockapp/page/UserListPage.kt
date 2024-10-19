package com.example.brockapp.page

import com.example.brockapp.R
import com.example.brockapp.data.User
import com.example.brockapp.database.BrockDB
import com.example.brockapp.activity.FriendActivity
import com.example.brockapp.viewmodel.GroupViewModel
import com.example.brockapp.adapter.SubscriberAdapter
import com.example.brockapp.viewmodel.NetworkViewModel
import com.example.brockapp.singleton.MyS3ClientProvider
import com.example.brockapp.interfaces.ShowCustomToastImpl
import com.example.brockapp.viewmodel.GroupViewModelFactory


import android.os.Bundle
import android.view.View
import android.content.Intent
import androidx.fragment.app.Fragment
import android.widget.AutoCompleteTextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager

abstract class UserListPage: Fragment(R.layout.page_user_list) {
    private val toastUtil = ShowCustomToastImpl()

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModelNetwork: NetworkViewModel
    private lateinit var searchTextView: AutoCompleteTextView

    protected lateinit var viewModelGroup: GroupViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view_user_list_page)

        searchTextView = view.findViewById(R.id.auto_complete_text_view_user_list_page)
        setUpSearchTextView()

        viewModelNetwork = ViewModelProvider(requireActivity())[NetworkViewModel::class.java]

        val db: BrockDB = BrockDB.getInstance(requireContext())
        val s3Client = MyS3ClientProvider.getInstance(requireContext())

        val viewModelFactoryGroup = GroupViewModelFactory(s3Client, db)
        viewModelGroup =
            ViewModelProvider(requireActivity(), viewModelFactoryGroup)[GroupViewModel::class.java]

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
            if (item) loadUsers()
            searchTextView.isEnabled = item
        }
    }

    protected abstract fun loadUsers()

    protected abstract fun observeUsers()

    private fun observeSuggestion() {
        viewModelGroup.suggestions.observe(viewLifecycleOwner) { items ->
            if (items.isNotEmpty()) {
                populateRecyclerView(items.filterNotNull())
            } else {
                showToastUtil()
            }
        }
    }

    protected fun showToastUtil() {
        toastUtil.showWarningToast(
            "Username not found",
            requireContext()
        )
    }

    // Refactor the names
    protected fun populateRecyclerView(subscribers: List<User>) {
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
}