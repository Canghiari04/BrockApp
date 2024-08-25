import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.example.brockapp.R
import com.example.brockapp.adapter.FriendViewHolder
import com.example.brockapp.data.Friend
import com.example.brockapp.fragment.FriendsFragment
import com.example.brockapp.viewmodel.FriendsViewModel

class FriendsAdapter(
    private val friends: List<String>,
    private val friendsViewModel: FriendsViewModel,
    private val onFriendClick: (String) -> Unit
) : RecyclerView.Adapter<FriendViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_cell, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.usernameTextView.text = friend

        holder.viewFriendActivityButton.setOnClickListener {
            onFriendClick(friend)
        }
    }

    override fun getItemCount(): Int = friends.size
}
