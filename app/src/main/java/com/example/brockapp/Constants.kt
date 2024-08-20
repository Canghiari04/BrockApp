package com.example.brockapp

// Errori visualizzati nei Toast delle attività iniziali.
const val SIGN_IN_ERROR = "Credenziali già presenti, passa a Login."
const val LOGIN_ERROR = "Credenziali errate, riprova."
const val BLANK_ERROR = "Inserisci le credenziali."
const val GEOFENCE_ERROR = "Impossibile connettersi al servizio."

const val ROOM_DATABASE_VERSION = 17

const val REQUEST_CODE_PERMISSION_ACTIVITY_RECOGNITION = 1

// Request code utilizzati per i Pending Intent.
const val REQUEST_CODE_ACTIVITY_BROADCAST_RECEIVER = 10
const val REQUEST_CODE_GEOFENCE_BROADCAST_RECEIVER = 58

// Costanti utilizzate per definire al corretta visualizzazione grafica del calendario.
const val DATE_SEPARATOR = "-"
const val CALENDAR_DATE_FORMAT = "yyyy-MM-dd"
const val ISO_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
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

// Tipologie di notifiche ricevute.
const val ACTIVITY_RECOGNITION_NOTIFY = "ACTIVITY_RECOGNITION"
const val GEOFENCE_NOTIFY = "GEOFENCE_RECOGNITION"
const val CONNECTIVITY_NOTIFY = "CONNECTIVITY_RECOGNITION"