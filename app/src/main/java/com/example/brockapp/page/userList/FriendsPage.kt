package com.example.brockapp.page.userList

import com.example.brockapp.page.UserListPage

class FriendsPage: UserListPage() {

    override fun loadUsers() {
        viewModelGroup.getAllFriends()
    }

    override fun observeUsers() {
        viewModelGroup.friends.observe(viewLifecycleOwner) { items ->
            populateRecyclerView(items.filterNotNull())
        }
    }
}