package com.example.brockapp.activity.authenticate

import com.example.brockapp.R
import com.example.brockapp.room.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.extraObject.MyNetwork
import com.example.brockapp.viewmodel.UserViewModel
import com.example.brockapp.viewmodel.NetworkViewModel
import com.example.brockapp.activity.PageLoaderActivity
import com.example.brockapp.singleton.MyS3ClientProvider
import com.example.brockapp.interfaces.ShowCustomToastImpl
import com.example.brockapp.viewmodel.UserViewModelFactory
import com.example.brockapp.extraObject.MySharedPreferences
import com.example.brockapp.interfaces.InternetAvailableImpl
import com.example.brockapp.util.PostNotificationsPermissionUtil

import java.io.File
import java.util.Locale
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.content.Intent
import android.widget.Spinner
import android.widget.EditText
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.appcompat.app.AppCompatActivity

class SignInActivity: AppCompatActivity() {
    private var toastUtil = ShowCustomToastImpl()
    private var networkUtil = InternetAvailableImpl()

    private lateinit var city: String
    private lateinit var username: String
    private lateinit var password: String
    private lateinit var typeActivity: String
    private lateinit var buttonSignIn: Button
    private lateinit var viewModelUser: UserViewModel
    private lateinit var country: Pair<String, String?>
    private lateinit var viewModelNetwork: NetworkViewModel
    private lateinit var permissionUtil: PostNotificationsPermissionUtil

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        supportActionBar?.hide()

        buttonSignIn = findViewById(R.id.button_sign_in)

        val db = BrockDB.getInstance(this)
        val file = File(this.filesDir, "user_data.json")
        val s3Client = MyS3ClientProvider.getInstance(this)

        val factoryViewModelUser = UserViewModelFactory(db, s3Client, file)
        viewModelUser = ViewModelProvider(this, factoryViewModelUser)[UserViewModel::class.java]

        viewModelNetwork = ViewModelProvider(this)[NetworkViewModel::class.java]

        checkConnectivity()

        observeNetwork()
        observeSignIn()
        observeCities()

        permissionUtil = PostNotificationsPermissionUtil(this) {
            observeUser()
        }

        setUpSpinnerCountry(findViewById(R.id.spinner_sign_in_country))
        setUpSpinnerActivity(findViewById(R.id.spinner_sign_in_activity))

        buttonSignIn.setOnClickListener {
            username = findViewById<EditText>(R.id.edit_text_sign_in_username).text.toString()
            password = findViewById<EditText>(R.id.edit_text_sign_in_password).text.toString()

            if(username.isNotEmpty() && password.isNotEmpty()) {
                viewModelUser.registerUser(username, password, typeActivity, country.first, city)
            } else {
                toastUtil.showWarningToast(
                    "At least enter your credentials",
                    this
                )
            }
        }
    }

    private fun checkConnectivity() {
        MyNetwork.isConnected = networkUtil.isInternetActive(this)
    }

    private fun observeNetwork() {
        viewModelNetwork.authNetwork.observe(this) {
            buttonSignIn.isEnabled = it
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun observeSignIn() {
        viewModelUser.auth.observe(this) {
            if (it) {
                permissionUtil.requestPostNotificationPermission()
            } else {
                toastUtil.showWarningToast(
                    "Credentials already present",
                    this
                )
            }
        }
    }

    private fun observeCities() {
        viewModelUser.cities.observe(this) { items ->
            if (items.isEmpty()) {
                toastUtil.showWarningToast(
                    "No cities retrieved",
                    this
                )
            } else {
                setUpSpinnerCity(
                    items,
                    findViewById(R.id.spinner_sign_in_city)
                )
            }
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
                goToHome()
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

    private fun setUpSpinnerActivity(spinner: Spinner) {
        val spinnerItems = resources.getStringArray(R.array.spinner_activities)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                typeActivity = spinnerItems[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }
        }
    }

    private fun setUpSpinnerCountry(spinner: Spinner) {
        val locales = Locale.getAvailableLocales()
        val spinnerItems = locales
            .mapNotNull { locale ->
                val country = locale.displayCountry.takeIf { it.isNotEmpty() }
                val countryCode = locale.country.takeIf { it.isNotEmpty() }

                country?.let { it to countryCode}
            }
            .distinctBy { it.first }
            .sortedBy { it.first }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                country = spinnerItems[position]
                viewModelUser.getCitiesFromCountry(country.second!!)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }
        }
    }

    private fun setUpSpinnerCity(spinnerItems: List<String>, spinner: Spinner) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                city = spinnerItems[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }
        }
    }
}