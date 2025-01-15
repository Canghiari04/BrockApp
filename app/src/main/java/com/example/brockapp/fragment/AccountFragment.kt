package com.example.brockapp.fragment

import com.example.brockapp.*
import com.example.brockapp.R
import com.example.brockapp.room.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.service.SupabaseService
import com.example.brockapp.viewModel.UserViewModel
import com.example.brockapp.viewModel.GroupViewModel
import com.example.brockapp.viewModel.NetworkViewModel
import com.example.brockapp.singleton.MyS3ClientProvider
import com.example.brockapp.viewModel.UserViewModelFactory
import com.example.brockapp.viewModel.GroupViewModelFactory

import java.io.File
import android.net.Uri
import android.Manifest
import android.view.View
import android.os.Bundle
import android.content.Intent
import android.widget.TextView
import android.widget.ImageView
import android.provider.Settings
import androidx.fragment.app.Fragment
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.activity.result.contract.ActivityResultContracts

class AccountFragment: Fragment(R.layout.fragment_account) {

    private val requestExternalStoragePermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()) { isGranted ->

            when {
                isGranted -> {
                    pickImage()
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) -> {
                    showRationaleDialog()
                }

                else -> {
                    showPermissionDeniedDialog()
                }
            }
        }

    private lateinit var imageView: ImageView
    private lateinit var deleteTextView: TextView
    private lateinit var logoutTextView: TextView
    private lateinit var contentFirstColumn: TextView
    private lateinit var viewModelUser: UserViewModel
    private lateinit var viewModelGroup: GroupViewModel
    private lateinit var viewModelNetwork: NetworkViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = BrockDB.getInstance(requireContext())
        val file = File(requireContext().filesDir, "user_data.json")
        val s3Client = MyS3ClientProvider.getInstance(requireContext())

        val viewModelGroupFactory = GroupViewModelFactory(s3Client, db)
        val viewModelUserFactory = UserViewModelFactory(db, s3Client, file)

        viewModelNetwork = ViewModelProvider(requireActivity())[NetworkViewModel::class.java]
        viewModelUser = ViewModelProvider(this, viewModelUserFactory)[UserViewModel::class.java]
        viewModelGroup = ViewModelProvider(this, viewModelGroupFactory)[GroupViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.text_view_username_account).text =
            (MyUser.username)

        view.findViewById<TextView>(R.id.text_view_user_address).text =
            defineSubscriberAddress(MyUser.country, MyUser.city)

        imageView = view.findViewById(R.id.image_view_account)
        contentFirstColumn = view.findViewById(R.id.text_view_content_first_column)

        deleteTextView = view.findViewById(R.id.text_view_delete_account)
        logoutTextView = view.findViewById(R.id.text_view_logout_account)

        setupView()

        observeNetwork()
        observeNumberOfFollowing()

        viewModelGroup.getCurrentFriends()
    }

    private fun defineSubscriberAddress(country: String?, city: String?): String {
        return when {
            !country.isNullOrBlank() && !city.isNullOrBlank() -> "$country, $city"
            !country.isNullOrBlank() -> "$country"
            !city.isNullOrBlank() -> "$city"
            else -> ""
        }
    }

    private fun setupView() {
        imageView.setOnClickListener {
            requestExternalStoragePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        logoutTextView.setOnClickListener {
            sync(SupabaseService.Actions.SYNC.toString())
        }

        deleteTextView.setOnClickListener {
            showDangerousDialog()
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

    private fun showRationaleDialog() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.rationale_permission_title)
            .setMessage(R.string.rationale_read_storage_message)
            .setPositiveButton(R.string.positive_button) { dialog, _ ->
                dialog.dismiss()
                requestExternalStoragePermission.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
            .setNegativeButton(R.string.negative_button) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showPermissionDeniedDialog() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.setting_permission_title)
            .setMessage(R.string.settings_message)
            .setPositiveButton(R.string.positive_button) { dialog, _ ->
                dialog.dismiss()
                requireContext().startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", requireContext().packageName, null)
                    )
                )
            }
            .setNegativeButton(R.string.negative_button) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun sync(item: String) {
        val intent = Intent(
            requireContext(),
            SupabaseService::class.java
        ).apply {
            action = item
        }

        requireContext().startService(intent)
    }

    private fun showDangerousDialog() {
        AlertDialog.Builder(requireContext())
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

    private fun observeNetwork() {
        viewModelNetwork.currentNetwork.observe(viewLifecycleOwner) { item ->
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

    private fun observeNumberOfFollowing() {
        viewModelGroup.currentFriends.observe(viewLifecycleOwner) {
            contentFirstColumn.text = it.size.toString()
        }
    }
}