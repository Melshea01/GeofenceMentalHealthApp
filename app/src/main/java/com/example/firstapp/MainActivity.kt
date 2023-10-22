package com.example.firstapp

import android.Manifest.permission
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat
import com.example.firstapp.ui.theme.FirstAppTheme
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {
    private val REQUEST_CODE_PERMISSIONS = 1
    private lateinit var geofencingClient: GeofencingClient
    private val  TAG = "MainActivity";

    // Run broadcast receiver
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create channel for notification
        createChannel(this)

        // Get geofencing client
        geofencingClient = LocationServices.getGeofencingClient(this)

        //Request the permission
        ActivityCompat.requestPermissions(
            this, arrayOf<String>(
                permission.ACCESS_COARSE_LOCATION,
                permission.ACCESS_FINE_LOCATION,
                permission.ACCESS_BACKGROUND_LOCATION,
            ),
            PackageManager.PERMISSION_GRANTED
        )

        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            // Handle the case where the user has not granted location
            //Request the permission
            ActivityCompat.requestPermissions(
                this, arrayOf<String>(
                    permission.ACCESS_COARSE_LOCATION,
                    permission.ACCESS_FINE_LOCATION,
                    permission.ACCESS_BACKGROUND_LOCATION,
                ),
                PackageManager.PERMISSION_GRANTED
            )

            setContent {
                    FirstAppTheme {
                        Text(text = "No authorization granted or no GPS enabled")
                        Log.d(TAG, "No authorization granted or no GPS enabled")
                    }
                }
        } else {

            //If permission is granted , we can create and add the geofence
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




            //Display graphic
            setContent {
                FirstAppTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Greeting("Android!")
                    }
                }
            }

        }

    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FirstAppTheme {
        Greeting("Android")
    }
}
