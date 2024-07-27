package com.example.brockapp.detect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.brockapp.database.DbHelper
import com.google.android.gms.location.ActivityTransitionResult

/*
* BroadcastReceiver è un componente che rimane in ascolto di ricevere intent,
* in base alla tipologia di intent provvederà a realizzare un determinato comportamento.
* In questo caso provvederà a definire la tipologia di attività compiuta dall'utente.
*/
class UserActivityBroadcastReceiver : BroadcastReceiver() {
    /*
     * Gestione dell'intent per definire la tipologia di attività da detect.
     */
    override fun onReceive(context: Context, intent: Intent) {
        /*
         * Condizione per accertarsi se l'intent contiene un risultato di activity recognition.
         */

        val dbHelper = DbHelper(context)


        if (ActivityTransitionResult.hasResult(intent)) {
            /*
             * !! il risultato non può essere null
             */
            val result = ActivityTransitionResult.extractResult(intent)!!

            for(event in result.transitionEvents) {
                Log.d("User Activity", event.toString())
            }
        }
    }
}
