package com.example.brockapp

// Activity
const val ID_ACTIVITY_NOTIFY = 10
const val REQUEST_CODE_ACTIVITY_NOTIFY = 13
const val CHANNEL_ID_ACTIVITY_NOTIFY = "ACTIVITY RECOGNITION"
const val NAME_CHANNEL_ACTIVITY_NOTIFY = "ACTIVITY RECOGNITION NOTIFICATION CHANNEL"
const val DESCRIPTION_CHANNEL_ACTIVITY_NOTIFY = "CHANNEL FOR ACTIVITY RECOGNITION NOTIFY"

// Geofence
const val ID_GEOFENCE_NOTIFY = 11
const val REQUEST_CODE_GEOFENCE_NOTIFY = 14
const val CHANNEL_ID_CONNECTIVITY_NOTIFY = "CONNECTIVITY"
const val NAME_CHANNEL_CONNECTIVITY_NOTIFY = "CONNECTIVITY NOTIFICATION CHANNEL"
const val DESCRIPTION_CHANNEL_CONNECTIVITY_NOTIFY = "CHANNEL FOR CONNECTIVITY NOTIFY"

// Connectivity
const val ID_CONNECTIVITY_NOTIFY = 12
const val REQUEST_CODE_CONNECTIVITY_NOTIFY = 15
const val CHANNEL_ID_GEOFENCE_NOTIFY = "GEOFENCE"
const val NAME_CHANNEL_GEOFENCE_NOTIFY = "GEOFENCE NOTIFICATION CHANNEL"
const val DESCRIPTION_CHANNEL_GEOFENCE_NOTIFY = "CHANNEL FOR GEOFENCE NOTIFY"

// Errori visualizzati nei Toast delle attività iniziali.
const val SIGN_IN_ERROR = "Credenziali già presenti, passa a Login."
const val LOGIN_ERROR = "Credenziali errate, riprova."
const val BLANK_ERROR = "Inserisci le credenziali."

const val ROOM_DATABASE_VERSION = 18

// Request code utilizzati per i Pending Intent dei receiver.
const val REQUEST_CODE_PERMISSION_ACTIVITY_RECOGNITION = 1
const val REQUEST_CODE_GEOFENCE_BROADCAST_RECEIVER = 2

// Costanti utilizzate per definire al corretta visualizzazione grafica del calendario.
const val DATE_SEPARATOR = "-"
const val CALENDAR_DATE_FORMAT = "yyyy-MM-dd"
const val ISO_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
const val CHARTS_DATE_FORMAT = "MM-yyyy"
const val POSITION_UPDATE_INTERVAL_MILLIS = 10000

// Suddivisione della tipologia di attività condotta dall'utente.
const val WALK_ACTIVITY_TYPE = "WALK"
const val STILL_ACTIVITY_TYPE = "STILL"
const val VEHICLE_ACTIVITY_TYPE = "VEHICLE"

// Diversificazione della tipologia di connessione per il Connectivity Service.
const val WI_FI_TYPE_CONNECTION = "WI-FI"
const val CELLULAR_TYPE_CONNECTION = "CELLULAR"
const val NO_CONNECTION_TYPE_CONNECTION = "NO-CONNECTION"

// Intent filter utilizzati per risvegliare i servizi.
const val NOTIFICATION_INTENT_TYPE = "NOTIFICATION_TRANSITION_ACTION"
const val GEOFENCE_INTENT_TYPE = "GEOFENCE_TRANSITION_ACTION"
const val ACTIVITY_RECOGNITION_INTENT_TYPE = "ACTIVITY_TRANSITION_ACTION"


const val BUCKET_NAME = "brock-app"