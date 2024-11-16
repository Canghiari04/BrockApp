package com.example.brockapp.page.userlist

import com.example.brockapp.page.UserListPage

class FriendsPage: UserListPage() {
    override fun loadUsers() {
        viewModelGroup.getAllFriends()
    }

    override fun observeUsers() {
        viewModelGroup.friends.observe(viewLifecycleOwner) { items ->
            if (!items.isNullOrEmpty()) {
                populateRecyclerView(items.filterNotNull())
            }
        }
    }
}