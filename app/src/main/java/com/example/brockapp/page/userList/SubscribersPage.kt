package com.example.brockapp.page.userlist

import com.example.brockapp.page.UserListPage

class SubscribersPage: UserListPage() {
    override fun loadUsers() {
        viewModelGroup.getAllSubscribers()
    }

    override fun observeUsers() {
        viewModelGroup.subscribers.observe(viewLifecycleOwner) { items ->
            if (!items.isNullOrEmpty()) {
                populateRecyclerView(items.filterNotNull())
            }
        }
    }
}