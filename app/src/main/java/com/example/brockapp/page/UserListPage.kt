package com.example.brockapp.page

import com.example.brockapp.R
import com.example.brockapp.data.User
import com.example.brockapp.room.BrockDB
import com.example.brockapp.adapter.UserAdapter
import com.example.brockapp.activity.UserActivity
import com.example.brockapp.viewModel.GroupViewModel
import com.example.brockapp.viewModel.NetworkViewModel
import com.example.brockapp.singleton.MyS3ClientProvider
import com.example.brockapp.viewModel.GroupViewModelFactory

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

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModelNetwork: NetworkViewModel
    private lateinit var searchTextView: AutoCompleteTextView

    protected lateinit var viewModelGroup: GroupViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view_user_list_page)
        searchTextView = view.findViewById(R.id.auto_complete_text_view_user_list_page)

        val db: BrockDB = BrockDB.getInstance(requireContext())
        val s3Client = MyS3ClientProvider.getInstance(requireContext())
        val viewModelFactoryGroup = GroupViewModelFactory(s3Client, db)

        viewModelNetwork = ViewModelProvider(requireActivity())[NetworkViewModel::class.java]
        viewModelGroup = ViewModelProvider(requireActivity(), viewModelFactoryGroup)[GroupViewModel::class.java]

        setUpSearchTextView()

        observeNetwork()
        observeUsers()
        observeSuggestion()
    }

    override fun onResume() {
        super.onResume()

        if (viewModelNetwork.currentNetwork.value == true) loadUsers()
    }

    private fun setUpSearchTextView() {
        searchTextView.addTextChangedListener {
            val user = searchTextView.text.toString()
            viewModelGroup.getSuggestions(user)
        }
    }

    private fun observeNetwork() {
        viewModelNetwork.currentNetwork.observe(viewLifecycleOwner) { item ->
            searchTextView.isEnabled = item
        }
    }

    protected abstract fun loadUsers()

    protected abstract fun observeUsers()

    private fun observeSuggestion() {
        viewModelGroup.suggestions.observe(viewLifecycleOwner) { items ->
            if (items.isNotEmpty()) {
                populateRecyclerView(items.filterNotNull())
            }
        }
    }

    protected fun populateRecyclerView(subscribers: List<User>) {
        val adapter = UserAdapter(subscribers) { username -> showSubscribe(username) }
        val layoutManager = LinearLayoutManager(context)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager
    }

    private fun showSubscribe(username: String) {
        val intent = Intent(requireContext(), UserActivity::class.java).putExtra("USERNAME_SUBSCRIBER", username)
        startActivity(intent)
    }
}