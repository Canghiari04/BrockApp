package com.example.brockapp

// Sync
const val ID_SUPABASE_SERVICE_NOTIFY = 10
const val CHANNEL_ID_SUPABASE_SERVICE = "Supabase service"
const val NAME_CHANNEL_SUPABASE_SERVICE = "ChannelSupabaseService"
const val DESCRIPTION_CHANNEL_SUPABASE_SERVICE = "Channel for supabase service"

// Connectivity
const val ID_CONNECTIVITY_WORKER_NOTIFY = 11
const val CHANNEL_ID_CONNECTIVITY_WORKER = "Connectivity worker"
const val NAME_CHANNEL_CONNECTIVITY_WORKER = "ChannelConnectivityWorker"
const val DESCRIPTION_CHANNEL_CONNECTIVITY_WORKER = "Channel for connectivity worker"

const val REQUEST_CODE_CONNECTIVITY_NOTIFY = 1

// Activity Recognition
const val ID_ACTIVITY_RECOGNITION_WORKER_NOTIFY = 12
const val CHANNEL_ID_ACTIVITY_RECOGNITION_WORKER = "Activity recognition worker"
const val NAME_CHANNEL_ACTIVITY_RECOGNITION_WORKER = "ChannelActivityRecognitionWorker"
const val DESCRIPTION_CHANNEL_ACTIVITY_RECOGNITION_WORKER = "Channel for activity recognition worker"

const val REQUEST_CODE_ACTIVITY_RECOGNITION_BROADCAST_RECEIVER = 2
const val ACTIVITY_RECOGNITION_INTENT_TYPE = "ACTIVITY_RECOGNITION_ACTION"

// Geofence
const val ID_GEOFENCE_WORKER_NOTIFY = 13
const val CHANNEL_ID_GEOFENCE_WORKER = "Geofence worker"
const val NAME_CHANNEL_GEOFENCE_WORKER = "ChannelGeofenceWorker"
const val DESCRIPTION_CHANNEL_GEOFENCE_WORKER = "Channel for geofence worker"

const val REQUEST_CODE_GEOFENCE_BROADCAST_RECEIVER = 3
const val GEOFENCE_INTENT_TYPE = "GEOFENCE_TRANSITION_ACTION"

// Memo
const val ID_MEMO_WORKER_NOTIFY = 14
const val CHANNEL_ID_MEMO_WORKER = "Memo worker"
const val NAME_CHANNEL_MEMO_WORKER = "ChannelMemoWorker"
const val DESCRIPTION_CHANNEL_MEMO_WORKER = "Channel for memo worker"

// Distance service
const val ID_DISTANCE_SERVICE_NOTIFY = 15
const val CHANNEL_ID_DISTANCE_SERVICE = "Distance service"
const val NAME_CHANNEL_DISTANCE_SERVICE = "ChannelDistanceService"
const val DESCRIPTION_CHANNEL_DISTANCE_SERVICE = "Channel for distance service"

// Step counter service
const val ID_STEP_COUNTER_SERVICE_NOTIFY = 16
const val CHANNEL_ID_STEP_COUNTER_SERVICE = "Step counter service"
const val NAME_CHANNEL_STEP_COUNTER_SERVICE = "ChannelStepCounterService"
const val DESCRIPTION_CHANNEL_STEP_COUNTER_SERVICE = "Channel for step counter service"

// Height difference service
const val ID_HEIGHT_DIFFERENCE_SERVICE_NOTIFY = 17
const val CHANNEL_ID_HEIGHT_DIFFERENCE_SERVICE = "Height difference service"
const val NAME_CHANNEL_HEIGHT_DIFFERENCE_SERVICE = "ChannelHeightDifferenceService"
const val DESCRIPTION_CHANNEL_HEIGHT_DIFFERENCE_SERVICE = "Channel for height difference service"

// External storage
const val REQUEST_CODE_PICKING_IMAGE = 4

// Database Version
const val ROOM_DATABASE_VERSION = 32

// Date Formatting
const val DATE_TEXT_VIEW = ", "
const val CALENDAR_DATE_FORMAT = "yyyy-MM-dd"
const val ISO_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"

// Activity Types
const val RUN_ACTIVITY_TYPE = "Run"
const val WALK_ACTIVITY_TYPE = "Walk"
const val STILL_ACTIVITY_TYPE = "Still"
const val VEHICLE_ACTIVITY_TYPE = "Vehicle"