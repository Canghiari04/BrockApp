package com.example.brockapp.activity.authenticate

import com.example.brockapp.R
import com.example.brockapp.room.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.extraObject.MyNetwork
import com.example.brockapp.viewmodel.UserViewModel
import com.example.brockapp.service.SupabaseService
import com.example.brockapp.viewmodel.NetworkViewModel
import com.example.brockapp.activity.PageLoaderActivity
import com.example.brockapp.singleton.MyS3ClientProvider
import com.example.brockapp.viewmodel.UserViewModelFactory
import com.example.brockapp.interfaces.ShowCustomToastImpl
import com.example.brockapp.extraObject.MySharedPreferences
import com.example.brockapp.interfaces.InternetAvailableImpl
import com.example.brockapp.util.PostNotificationsPermissionUtil

import java.io.File
import android.os.Build
import android.util.Log
import android.view.View
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.appcompat.app.AppCompatActivity

class LoginActivity: AppCompatActivity() {
    private var toastUtil = ShowCustomToastImpl()
    private var networkUtil = InternetAvailableImpl()

    private lateinit var username: String
    private lateinit var password: String
    private lateinit var buttonLogin: Button
    private lateinit var viewModelUser: UserViewModel
    private lateinit var viewModelNetwork: NetworkViewModel
    private lateinit var permissionUtil: PostNotificationsPermissionUtil

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        buttonLogin = findViewById(R.id.button_login)

        val db = BrockDB.getInstance(this)
        val file = File(this.filesDir, "user_data.json")
        val s3Client = MyS3ClientProvider.getInstance(this)

        val factoryViewModelUser = UserViewModelFactory(db, s3Client, file)
        viewModelUser = ViewModelProvider(this, factoryViewModelUser)[UserViewModel::class.java]

        val (id, savedUsername, savedPassword) = MySharedPreferences.getCredentialsSaved(this)

        if (id != 0L && savedUsername != null && savedPassword != null) {
            View.GONE

            permissionUtil = PostNotificationsPermissionUtil(this) {
                viewModelUser.getUserFromRoom(
                    savedUsername,
                    savedPassword
                )
            }

            observeUserFromPreferences()
            permissionUtil.requestPostNotificationPermission()
        } else {
            viewModelNetwork = ViewModelProvider(this)[NetworkViewModel::class.java]

            checkConnectivity()

            observeNetwork()
            observeLogin()

            permissionUtil = PostNotificationsPermissionUtil(this) {
                observeUser()
            }

            buttonLogin.setOnClickListener {
                username = findViewById<EditText>(R.id.edit_text_login_username).text.toString()
                password = findViewById<EditText>(R.id.edit_text_login_password).text.toString()

                if (username.isNotEmpty() && password.isNotEmpty()) {
                    viewModelUser.getUserFromSupabase(username, password)
                } else {
                    toastUtil.showWarningToast(
                        "You must insert the field required",
                        this
                    )
                }
            }
        }
    }

    private fun checkConnectivity() {
        MyNetwork.isConnected = networkUtil.isInternetActive(this)
    }

    private fun observeNetwork() {
        viewModelNetwork.authNetwork.observe(this) {
            buttonLogin.isEnabled = it
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

    private fun observeUserFromPreferences() {
        viewModelUser.user.observe(this) {
            viewModelUser.user.observe(this) {
                if (it != null) {
                    MyUser.apply {
                        id = it.id
                        username = it.username
                        password = it.password
                        typeActivity = it.typeActivity
                        country = it.country
                        city = it.city
                    }

                    goToHome()
                } else {
                    Log.e("LOGIN_FRAGMENT", "User not found.")
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

    private fun observeUser() {
        viewModelUser.user.observe(this) {
            if (it != null) {
                MyUser.apply {
                    id = it.id
                    username = it.username
                    password = it.password
                    typeActivity = it.typeActivity
                    country = it.country
                    city = it.city
                }

                MySharedPreferences.setCredentialsSaved(this)
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