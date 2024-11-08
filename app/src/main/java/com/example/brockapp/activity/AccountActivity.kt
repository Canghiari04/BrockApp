package com.example.brockapp.activity

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.room.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.extraObject.MyNetwork
import com.example.brockapp.service.SupabaseService
import com.example.brockapp.viewmodel.UserViewModel
import com.example.brockapp.viewmodel.GroupViewModel
import com.example.brockapp.viewmodel.NetworkViewModel
import com.example.brockapp.singleton.MyS3ClientProvider
import com.example.brockapp.receiver.ConnectivityReceiver
import com.example.brockapp.viewmodel.UserViewModelFactory
import com.example.brockapp.viewmodel.GroupViewModelFactory
import com.example.brockapp.interfaces.InternetAvailableImpl
import com.example.brockapp.util.AccountActivityPermissionUtil

import java.io.File
import android.util.Log
import android.os.Bundle
import android.view.MenuItem
import android.content.Intent
import android.widget.TextView
import android.widget.ImageView
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.content.BroadcastReceiver
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.appcompat.app.AppCompatActivity

class AccountActivity: AppCompatActivity() {
    private var networkUtil = InternetAvailableImpl()

    private lateinit var imageView: ImageView
    private lateinit var deleteTextView: TextView
    private lateinit var logoutTextView: TextView
    private lateinit var receiver: BroadcastReceiver
    private lateinit var contentFirstColumn: TextView
    private lateinit var viewModelUser: UserViewModel
    private lateinit var viewModelGroup: GroupViewModel
    private lateinit var viewModelNetwork: NetworkViewModel
    private lateinit var permissionUtil: AccountActivityPermissionUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_account_activity)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setSupportActionBar(toolbar)

        contentFirstColumn = findViewById(R.id.text_view_content_first_column)

        imageView = findViewById(R.id.image_view_account)
        deleteTextView = findViewById(R.id.text_view_delete_account)
        logoutTextView = findViewById(R.id.text_view_logout_account)

        checkConnectivity()
        registerReceiver()

        setUpView()

        findViewById<TextView>(R.id.text_view_username_account).text =
            (MyUser.username)

        findViewById<TextView>(R.id.text_view_user_address).text =
            defineSubscriberAddress(MyUser.country, MyUser.city)

        findViewById<TextView>(R.id.text_view_danger_zone).text =
            ("You are entering a danger area. Please proceed with caution or exit immediately")

        // Inside there is the callback to start the new intent
        permissionUtil = AccountActivityPermissionUtil(this) { pickImage() }

        val db = BrockDB.getInstance(this)
        val file = File(this.filesDir, "user_data.json")
        val s3Client = MyS3ClientProvider.getInstance(this)

        val viewModelGroupFactory = GroupViewModelFactory(s3Client, db)
        viewModelGroup = ViewModelProvider(this, viewModelGroupFactory)[GroupViewModel::class.java]

        val viewModelUserFactory = UserViewModelFactory(db, s3Client, file)
        viewModelUser = ViewModelProvider(this, viewModelUserFactory)[UserViewModel::class.java]

        viewModelNetwork = ViewModelProvider(this)[NetworkViewModel::class.java]

        observeNetwork()
        observeNumberOfFollower()

        viewModelGroup.getCurrentFriends()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, PageLoaderActivity::class.java).putExtra("FRAGMENT_TO_SHOW", R.id.navbar_item_you)
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == REQUEST_CODE_PICKING_IMAGE) {
            val uri = data?.data
            imageView.setImageURI(uri)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private fun checkConnectivity() {
        MyNetwork.isConnected = networkUtil.isInternetActive(this)
    }

    private fun registerReceiver() {
        receiver = ConnectivityReceiver(this)

        ContextCompat.registerReceiver(
            this,
            receiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun setUpView() {
        imageView.setOnClickListener {
            permissionUtil.requestReadStoragePermission()
        }

        logoutTextView.setOnClickListener {
            sync(SupabaseService.Actions.SYNC.toString())
        }

        deleteTextView.setOnClickListener {
            showDangerousDialog()
        }
    }

    private fun sync(action: String) {
        Intent(this, SupabaseService::class.java).also {
            it.action = action
            startService(it)
        }
    }

    private fun showDangerousDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dangerous_dialog_title)
            .setMessage(R.string.dangerous_dialog_message)
            .setPositiveButton(R.string.dangerous_positive_button) { dialog, _ ->
                dialog.dismiss()
                sync(SupabaseService.Actions.DELETE.toString())
            }
            .setNegativeButton(R.string.dangerous_negative_button) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun defineSubscriberAddress(country: String?, city: String?): String {
        return when {
            !country.isNullOrBlank() && !city.isNullOrBlank() -> "$country, $city"
            !country.isNullOrBlank() -> "$country"
            !city.isNullOrBlank() -> "$city"
            else -> ""
        }
    }

    private fun pickImage() {
        val intent = Intent().also {
            it.type = "image/*"
            it.action = Intent.ACTION_PICK
        }

        startActivityForResult(
            intent,
            REQUEST_CODE_PICKING_IMAGE
        )
    }

    private fun observeNetwork() {
        viewModelNetwork.currentNetwork.observe(this) { item ->
            logoutTextView.also {
                it.isEnabled = item

                if (item) {
                    it.setBackgroundResource(R.drawable.border_red)
                } else {
                    it.setBackgroundResource(R.drawable.border_background_grey)
                }
            }

            deleteTextView.also {
                it.isEnabled = item

                if (item) {
                    it.setBackgroundResource(R.drawable.border_red)
                } else {
                    it.setBackgroundResource(R.drawable.border_background_grey)
                }
            }
        }
    }

    private fun observeNumberOfFollower() {
        viewModelGroup.currentFriends.observe(this) {
            contentFirstColumn.text = it.size.toString()
        }
    }
}