package com.example.firstapp

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.controls.ControlsProviderService.TAG
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent



class GeofenceBroadcastReceiver : BroadcastReceiver() {
    // ...
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = intent?.let { GeofencingEvent.fromIntent(it) }


        if (geofencingEvent != null) {
            if (geofencingEvent.hasError()) {
                val errorMessage = GeofenceStatusCodes
                    .getStatusCodeString(geofencingEvent.errorCode)
                Log.e(TAG, errorMessage)
                return
            }
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent?.geofenceTransition
        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            // Get the transition details as a String.
            val geofenceTransitionDetails = getGeofenceTransitionDetails(
                this,
                geofenceTransition,
                triggeringGeofences
            )

            // Send notification and log the transition details.
            // Creating and sending Notification
            val notificationManager = ContextCompat.getSystemService(
                context!!,
                NotificationManager::class.java
            ) as NotificationManager


            notificationManager.sendGeofenceEnteredNotification(context, geofenceTransitionDetails )


            Log.i(TAG, geofenceTransitionDetails)
        } else {
            // Log the error.
            Log.e(TAG, "error !  :')")
        }

    }


    //Information on the geofence
    @SuppressLint("VisibleForTests")
    private fun getGeofenceTransitionDetails(
        context: GeofenceBroadcastReceiver, transitionType: Int, triggeringGeofences: List<Geofence>
    ): String {
        val geofenceIdsList = mutableListOf<String>()

        for (geofence in triggeringGeofences) {
            geofenceIdsList.add(geofence.requestId)
        }

        val geofenceIds = TextUtils.join(", ", geofenceIdsList)
        return when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "Entering geofences : $geofenceIds"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "Leaving geofences : $geofenceIds"
            else -> "Transition inconnue : $geofenceIds"
        }
    }

}
