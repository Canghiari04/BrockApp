package com.example.brockapp.activity

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.database.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.viewmodel.UserViewModel
import com.example.brockapp.viewmodel.FriendsViewModel
import com.example.brockapp.singleton.MyS3ClientProvider
import com.example.brockapp.viewmodel.UserViewModelFactory
import com.example.brockapp.extraObject.MySharedPreferences
import com.example.brockapp.viewmodel.FriendsViewModelFactory
import com.example.brockapp.util.AccountActivityPermissionUtil

import java.io.File
import android.util.Log
import android.os.Bundle
import android.view.MenuItem
import android.content.Intent
import android.widget.TextView
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.appcompat.app.AppCompatActivity

class AccountActivity: AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var deleteTextView: TextView
    private lateinit var logoutTextView: TextView
    private lateinit var contentFirstColumn: TextView
    private lateinit var userViewModel: UserViewModel
    private lateinit var friendsViewModel: FriendsViewModel
    private lateinit var permissionUtil: AccountActivityPermissionUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_account)
        setSupportActionBar(toolbar)

        toolbar.run {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        contentFirstColumn = findViewById(R.id.text_view_content_first_column)

        imageView = findViewById(R.id.image_view_account)
        deleteTextView = findViewById(R.id.text_view_delete_account)
        logoutTextView = findViewById(R.id.text_view_logout_account)

        setUpView()

        findViewById<TextView>(R.id.text_view_username_account).text =
            (MyUser.username)

        findViewById<TextView>(R.id.text_view_danger_zone).text =
            ("You are entering a danger area. Please proceed with caution or exit immediately")

        // Inside there is the callback to start the new intent
        permissionUtil = AccountActivityPermissionUtil(this) { pickImage() }

        val db = BrockDB.getInstance(this)
        val file = File(this.filesDir, "user_data.json")
        val s3Client = MyS3ClientProvider.getInstance(this)

        val friendViewModelFactory = FriendsViewModelFactory(s3Client, db, file)
        friendsViewModel = ViewModelProvider(this, friendViewModelFactory)[FriendsViewModel::class.java]

        val userViewModelFactory = UserViewModelFactory(db, s3Client, file)
        userViewModel = ViewModelProvider(this, userViewModelFactory)[UserViewModel::class.java]

        observeAccountDeleted()
        observeNumberOfFollower()

        friendsViewModel.getCurrentFriends(MyUser.id)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, PageLoaderActivity::class.java).putExtra(
                    "FRAGMENT_TO_SHOW",
                    "You"
                )
                startActivity(intent)
                finish()
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
                false
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == REQUEST_CODE_PICKING_IMAGE) {
            val uri = data?.data
            imageView.setImageURI(uri)
        }
    }

    private fun setUpView() {
        imageView.setOnClickListener {
            permissionUtil.requestReadStoragePermission()
        }

        deleteTextView.setOnClickListener {
            showDangerousDialog()
        }

        logoutTextView.setOnClickListener {
            MySharedPreferences.logout(this)
            goToAuthenticator()
        }
    }

    private fun pickImage() {
        val intent = Intent()
        intent.run {
            type = "image/*"
            action = Intent.ACTION_PICK
        }

        startActivityForResult(
            intent,
            REQUEST_CODE_PICKING_IMAGE
        )
    }

    private fun showDangerousDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dangerous_dialog_title)
            .setMessage(R.string.dangerous_dialog_message)
            .setPositiveButton(R.string.dangerous_positive_button) { dialog, _ ->
                dialog.dismiss()
                MySharedPreferences.deleteAll(this)
                userViewModel.deleteUser(MyUser.username, MyUser.password)
            }
            .setNegativeButton(R.string.dangerous_negative_button) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun observeAccountDeleted() {
        userViewModel.currentUser.observe(this) {
            goToAuthenticator()
        }
    }

    private fun goToAuthenticator() {
        startActivity(Intent(this, AuthenticatorActivity::class.java))
        finish()
    }

    private fun observeNumberOfFollower() {
        friendsViewModel.friends.observe(this) {
            if (it.isNotEmpty()) {
                contentFirstColumn.text = it.size.toString()
            } else {
                Log.d("ACCOUNT_ACTIVITY", "No one friend retrieved")
            }
        }
    }
}