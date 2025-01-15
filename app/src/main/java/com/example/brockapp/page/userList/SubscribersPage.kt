package com.example.brockapp.page.userList

import com.example.brockapp.page.UserListPage

class SubscribersPage: UserListPage() {

    override fun loadUsers() {
        viewModelGroup.getAllSubscribers()
    }

    override fun observeUsers() {
        viewModelGroup.subscribers.observe(viewLifecycleOwner) { items ->
            populateRecyclerView(items.filterNotNull())
        }
    }
}