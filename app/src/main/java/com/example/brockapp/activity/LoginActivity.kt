package com.example.brockapp.activity

import com.example.brockapp.R
import com.example.brockapp.room.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.extraObject.MyNetwork
import com.example.brockapp.viewmodel.UserViewModel
import com.example.brockapp.service.SupabaseService
import com.example.brockapp.viewmodel.NetworkViewModel
import com.example.brockapp.singleton.MyS3ClientProvider
import com.example.brockapp.receiver.AuthenticatorReceiver
import com.example.brockapp.viewmodel.UserViewModelFactory
import com.example.brockapp.interfaces.ShowCustomToastImpl
import com.example.brockapp.extraObject.MySharedPreferences
import com.example.brockapp.interfaces.InternetAvailableImpl
import com.example.brockapp.util.PostNotificationsPermissionUtil

import java.io.File
import android.os.Build
import android.util.Log
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.widget.EditText
import android.widget.TextView
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.annotation.RequiresApi
import android.content.BroadcastReceiver
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.appcompat.app.AppCompatActivity

class LoginActivity: AppCompatActivity() {
    private var toastUtil = ShowCustomToastImpl()
    private var networkUtil = InternetAvailableImpl()

    private lateinit var buttonLogin: Button
    private lateinit var receiver: BroadcastReceiver
    private lateinit var viewModelUser: UserViewModel
    private lateinit var viewModelNetwork: NetworkViewModel
    private lateinit var permissionUtil: PostNotificationsPermissionUtil

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        val db = BrockDB.getInstance(this)
        val file = File(this.filesDir, "user_data.json")
        val s3Client = MyS3ClientProvider.getInstance(this)

        val factoryViewModelUser = UserViewModelFactory(db, s3Client, file)
        viewModelUser = ViewModelProvider(this, factoryViewModelUser)[UserViewModel::class.java]

        val (savedUsername, savedPassword) = MySharedPreferences.getCredentialsSaved(this)

        if (savedUsername != null && savedPassword != null) {
            permissionUtil = PostNotificationsPermissionUtil(this) {
                viewModelUser.getUserFromRoom(
                    savedUsername,
                    savedPassword
                )
            }

            observeUserFromPreferences()

            permissionUtil.requestPostNotificationPermission()
        } else {
            setContentView(R.layout.activity_login)

            buttonLogin = findViewById(R.id.button_login)

            registerReceiver()

            checkConnectivity()

            viewModelNetwork = ViewModelProvider(this)[NetworkViewModel::class.java]

            observeNetwork()
            observeLogin()

            permissionUtil = PostNotificationsPermissionUtil(this) {
                observeUser()
            }

            buttonLogin.setOnClickListener {
                val username = findViewById<EditText>(R.id.edit_text_login_username).text.toString()
                val password = findViewById<EditText>(R.id.edit_text_login_password).text.toString()

                if (username.isNotEmpty() && password.isNotEmpty()) {
                    viewModelUser.getUserFromSupabase(
                        username,
                        password
                    )
                } else {
                    toastUtil.showWarningToast(
                        "You must insert the field required",
                        this
                    )
                }
            }

            findViewById<TextView>(R.id.text_view_sign_in).setOnClickListener {
                Intent(this, SignInActivity::class.java).also {
                    unregisterReceiver(receiver)
                    startActivity(it)
                    finish()
                }
            }
        }
    }

    private fun observeUserFromPreferences() {
        viewModelUser.user.observe(this) {
            viewModelUser.user.observe(this) {
                if (it != null) {
                    MyUser.apply {
                        username = it.username
                        password = it.password
                        typeActivity = it.typeActivity
                        country = it.country
                        city = it.city
                    }

                    goToHome()
                } else {
                    Log.e("LOGIN_ACTIVITY", "User not found.")
                }
            }
        }
    }

    private fun goToHome() {
        Intent(this, PageLoaderActivity::class.java).also {
            it.putExtra("FRAGMENT_TO_SHOW", R.id.navbar_item_you)
            startActivity(it)
            finish()
        }
    }

    private fun registerReceiver() {
        receiver = AuthenticatorReceiver(this)

        ContextCompat.registerReceiver(
            this,
            receiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun checkConnectivity() {
        MyNetwork.isConnected = networkUtil.isInternetActive(this)
    }

    private fun observeNetwork() {
        viewModelNetwork.authNetwork.observe(this) {
            buttonLogin.isEnabled = it

            if (it) {
                buttonLogin.setTextColor(resources.getColor(R.color.white))
                buttonLogin.backgroundTintList = resources.getColorStateList(R.color.uni_red)
            } else {
                buttonLogin.setTextColor(resources.getColor(R.color.black))
                buttonLogin.backgroundTintList = resources.getColorStateList(R.color.grey)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun observeLogin() {
        viewModelUser.auth.observe(this) { auth ->
            if (auth) {
                permissionUtil.requestPostNotificationPermission()
            } else {
                toastUtil.showWarningToast(
                    "Credentials wrong",
                    this
                )
            }
        }
    }

    private fun observeUser() {
        viewModelUser.user.observe(this) {
            if (it != null) {
                MyUser.apply {
                    username = it.username
                    password = it.password
                    typeActivity = it.typeActivity
                    country = it.country
                    city = it.city
                }

                MySharedPreferences.setCredentials(this)
                unregisterReceiver(receiver)
                sync()
            }
        }
    }

    private fun sync() {
        Intent(this, SupabaseService::class.java).also {
            it.action = SupabaseService.Actions.READ.toString()
            startService(it)
        }
    }
}