package com.example.brockapp.fragment

import com.example.brockapp.R
import com.example.brockapp.room.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.extraObject.MyNetwork
import com.example.brockapp.viewmodel.UserViewModel
import com.example.brockapp.viewmodel.NetworkViewModel
import com.example.brockapp.activity.PageLoaderActivity
import com.example.brockapp.singleton.MyS3ClientProvider
import com.example.brockapp.activity.AuthenticatorActivity
import com.example.brockapp.viewmodel.UserViewModelFactory
import com.example.brockapp.interfaces.ShowCustomToastImpl
import com.example.brockapp.extraObject.MySharedPreferences
import com.example.brockapp.interfaces.InternetAvailableImpl
import com.example.brockapp.util.PostNotificationsPermissionUtil

import java.io.File
import java.util.Locale
import android.util.Log
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.Button
import android.content.Intent
import android.widget.Spinner
import android.widget.EditText
import android.widget.TextView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider

class SignInFragment: Fragment(R.layout.fragment_sign_in) {
    private var toastUtil = ShowCustomToastImpl()

    private val networkUtil = InternetAvailableImpl()

    private lateinit var db: BrockDB
    private lateinit var city: String
    private lateinit var username: String
    private lateinit var password: String
    private lateinit var typeActivity: String
    private lateinit var viewModelUser: UserViewModel
    private lateinit var country: Pair<String, String?>
    private lateinit var viewModelNetwork: NetworkViewModel
    private lateinit var util: PostNotificationsPermissionUtil

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkConnectivity()

        val spinnerActivity = view.findViewById<Spinner>(R.id.spinner_type_activity)
        setUpSpinnerActivity(spinnerActivity)

        val spinnerCountry = view.findViewById<Spinner>(R.id.spinner_country)
        setUpSpinnerCountry(spinnerCountry)

        viewModelNetwork = ViewModelProvider(requireActivity())[NetworkViewModel::class.java]

        db = BrockDB.getInstance(requireContext())
        val file = File(context?.filesDir, "user_data.json")
        val s3Client = MyS3ClientProvider.getInstance(requireContext())

        val factoryUserViewModel = UserViewModelFactory(db, s3Client, file)
        viewModelUser = ViewModelProvider(this, factoryUserViewModel)[UserViewModel::class.java]

        util = PostNotificationsPermissionUtil(requireActivity()) {
            observeUser()
        }

        observeCities()
        observeNetwork()
        observeSignIn()

        view.findViewById<Button>(R.id.button_sign_in)?.setOnClickListener {
            username = view.findViewById<EditText>(R.id.edit_text_username).text.toString()
            password = view.findViewById<EditText>(R.id.edit_text_password).text.toString()

            if(username.isNotEmpty() && password.isNotEmpty()) {
                viewModelUser.registerUser(username, password, typeActivity, country.first, city)
            } else {
                Toast.makeText(requireContext(), "Insert the access credentials", Toast.LENGTH_LONG).show()
            }
        }

        view.findViewById<TextView>(R.id.text_view_login).setOnClickListener {
            (requireActivity() as AuthenticatorActivity).showLoginFragment()
        }
    }

    private fun checkConnectivity() {
        MyNetwork.isConnected = networkUtil.isInternetActive(requireContext())
    }

    private fun setUpSpinnerActivity(spinner: Spinner) {
        val spinnerItems = resources.getStringArray(R.array.spinner_activities)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerItems)
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

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerItems)
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

    private fun observeUser() {
        viewModelUser.currentUser.observe(viewLifecycleOwner) { currentUser ->
            if (currentUser != null) {
                MyUser.apply {
                    id = currentUser.id
                    username = currentUser.username
                    password = currentUser.password
                    typeActivity = currentUser.typeActivity
                    country = currentUser.country
                    city = currentUser.city
                }

                MySharedPreferences.setUpSharedPreferences(requireContext())
                goToHome()
            } else {
                Log.e("SIGN_IN_FRAGMENT", "User not found")
            }
        }
    }

    private fun observeCities() {
        viewModelUser.cities.observe(viewLifecycleOwner) { items ->
            if (items.isEmpty()) {
                toastUtil.showWarningToast(
                    "No cities retrieved",
                    requireContext()
                )
            } else {
                setUpSpinnerCity(
                    items,
                    requireView().findViewById(R.id.spinner_city)
                )
            }
        }
    }

    private fun setUpSpinnerCity(spinnerItems: List<String>, spinner: Spinner) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerItems)
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

    private fun observeNetwork() {
        viewModelNetwork.authNetwork.observe(viewLifecycleOwner) { authNetwork ->
            view?.findViewById<Button>(R.id.button_sign_in)?.isEnabled = authNetwork
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun observeSignIn() {
        viewModelUser.auth.observe(viewLifecycleOwner) { auth ->
            if (auth) {
                viewModelUser.getUser(username, password)
                util.requestPostNotificationPermission()
            } else {
                toastUtil.showWarningToast(
                    "Credentials already present",
                    requireContext()
                )
            }
        }
    }

    private fun goToHome() {
        val intent = Intent(requireContext(), PageLoaderActivity::class.java).putExtra("FRAGMENT_TO_SHOW", R.id.navbar_item_you)
        startActivity(intent)
        activity?.finish()
    }
}