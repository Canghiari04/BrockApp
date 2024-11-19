package com.example.brockapp.interfaces

import com.example.brockapp.room.BrockDB
import com.example.brockapp.extraObject.MyUser

import java.util.Locale
import android.content.Context
import android.location.Address
import kotlinx.coroutines.async
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope

class ReverseGeocodingImpl(private val context: Context): ReverseGeocoding {

    override fun getAddress(name: String?, latitude: Double, longitude: Double): Address {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 10)

        if (!name.isNullOrEmpty() && !addresses.isNullOrEmpty()) {
            for (address in addresses) {
                if (address.featureName.equals(name)) return address
            }
        }

        return addresses!![0]
    }

    override suspend fun getGeofenceName(latitude: Double, longitude: Double): String? {
        val db = BrockDB.getInstance(context)
        val geocoder = Geocoder(context, Locale.getDefault())

        return try {
            var geofenceName: String? = null
            val addresses = geocoder.getFromLocation(latitude, longitude, 10)

            if (!addresses.isNullOrEmpty()) {
                val items = CoroutineScope(Dispatchers.IO).async {
                    db.GeofenceAreasDao().getGeofenceAreasByUsername(MyUser.username).map { it.name }
                }.await()

                if (items.isNotEmpty()) {
                    addresses.forEach {
                        if (items.contains(it.featureName)) {
                            geofenceName = it.featureName
                        }
                    }
                }
            }

            geofenceName
        } catch (e: Exception) {
            null
        }
    }
}