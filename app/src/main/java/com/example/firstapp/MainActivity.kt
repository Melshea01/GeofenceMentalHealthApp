package com.example.firstapp

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.firstapp.databinding.ActivityMainBinding
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices


class MainActivity : ComponentActivity() {
    private lateinit var geofencingClient: GeofencingClient
    private val  TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private var locationManager: LocationManager? = null

    val questionnaire = QuestionnaireGAD7()

    // Run broadcast receiver
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create channel for notification
        createChannel(this)

        // Get geofencing client
        geofencingClient = LocationServices.getGeofencingClient(this)

        //find the view
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkGpsStatus()

        val permissionsToCheck = arrayOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            android.Manifest.permission.POST_NOTIFICATIONS
        )

        val allPermissionsGranted = permissionsToCheck.all {
            ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allPermissionsGranted) {
            // Permissions are granted. You can proceed with your main activity's functionality.

            // Creation of the geofence
            val geofence = Geofence.Builder()
                .setRequestId("TestGeofence")
                .setCircularRegion(46.049989, 14.468190, 100F)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()

            //Adding the geofence
            val geofencingRequest = GeofencingRequest.Builder().apply {
                setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                addGeofence(geofence)
            }.build()

            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {

                // Handle success and failure of adding geofence
                addOnSuccessListener {
                    println("Geofence added successfully")
                }
                addOnFailureListener { e ->
                    println("Error adding geofence: ${e.message}")
                }
            }

            displayQuestion()

        } else {
            // Permissions denied. Show a message to the user.
            val t = Toast.makeText(this, "Permissions are not granted. Please enable notifications and localization", Toast.LENGTH_LONG)
            t.setGravity(Gravity.TOP, 0, 0)
            t.show()
            Log.d(TAG, "Permission not granted")

            // Optionally, provide the option for the user to redirect to the app settings to grant permissions.
            // You can add a button or link in your UI to trigger this function.
            redirectToAppSettings()
        }

            //Display graphic and interesting icon for the next grpahique
            //<a href="https://storyset.com/data">Data illustrations by Storyset</a>
            //<a href="https://storyset.com/people">People illustrations by Storyset</a>


    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStart() {
        super.onStart()
    }

    private fun checkGpsStatus() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (locationManager != null && !locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // GPS is not enabled. You can show a dialog or prompt the user to enable it.
            // Ask the user to enable GPS.
            val enableGpsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(enableGpsIntent)
        }
    }

    private fun redirectToAppSettings() {
        // Redirect the user to the app settings to request permissions.
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun displayQuestion(){
        binding.QuestionText.text = questionnaire.getNextQuestion()
        binding.radioButtonOption1.text = questionnaire.getResponseByIndex(0)
        binding.radioButtonOption2.text = questionnaire.getResponseByIndex(1)
        binding.radioButtonOption3.text = questionnaire.getResponseByIndex(2)
        binding.radioButtonOption4.text = questionnaire.getResponseByIndex(3)
    }

    private fun nextButton(){

    }


}




