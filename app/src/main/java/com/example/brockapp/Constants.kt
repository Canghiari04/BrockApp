package com.example.brockapp

import android.Manifest

const val SIGN_IN_ERROR = "Credenziali già presenti, passa a Login."
const val LOGIN_ERROR = "Credenziali errate, riprova."
const val BLANK_ERROR = "Inserisci le credenziali."

const val ROOM_DATABASE_VERSION = 12

const val REQUEST_CODE_PERMISSION_ACCESS_LOCATION = 1000
const val REQUEST_CODE_PERMISSION_ACTIVITY_RECOGNITION = 1001
const val REQUEST_CODE_BROADCAST_RECEIVER = 1010
const val REQUEST_CODE_GEOFENCE_BROADCAST_RECEIVER = 1011

const val GEOFENCE_INTENT_TYPE = "GEOFENCE_TRANSITION_ACTION"

val PERMISSIONS_LOCATION = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
const val PERMISSION_BACKGROUND = Manifest.permission.ACCESS_BACKGROUND_LOCATION
const val PERMISSION_NOTIFICATION = Manifest.permission.POST_NOTIFICATIONS

const val CHARTS_DATE_FORMAT = "MM-yyyy"

const val DATE_FORMAT = "dd-MM-yyyy HH:mm:ss"
const val DATE_SEPARATOR = "-"
const val UNIVERSAL_DATE = "d-M-yyyy"
const val CALENDAR_DATE_FORMAT = "yyyy-MM-dd"
