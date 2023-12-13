package com.example.firstapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.example.firstapp.databinding.MenuPrincipalBinding
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class MainActivity : FragmentActivity() {
    private lateinit var geofencingClient: GeofencingClient
    private val  TAG = "MainActivity"
    private lateinit var binding2: MenuPrincipalBinding
    private var locationManager: LocationManager? = null
    private lateinit var FragmentManager: FragmentManager
    private val LOCATION_PERMISSION_CODE=100
    private val BACKGROUND_LOCATION_PERMISSION_CODE= 102
    private val NOTIFICATION_PERMISSION_CODE = 103
    private lateinit var firebaseAuth: FirebaseAuth


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

        //Initialize database
        FirebaseApp.initializeApp(this)

        // Get geofencing client
        geofencingClient = LocationServices.getGeofencingClient(this)


        //Set binfing view
        binding2 = MenuPrincipalBinding.inflate(layoutInflater)
        val firstpage = HomeFragment()
        setCurrentFragment(firstpage)
        setContentView(binding2.root)


        //Check the authorization
        checkGpsStatus()
        checkPermission()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission()
        }


        val permissionsToCheck = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        )


        val allPermissionsGranted = permissionsToCheck.all {
            ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }


        if(!allPermissionsGranted){
            val t = Toast.makeText(this, "Permissions are not granted. Please enable notifications and localization", Toast.LENGTH_LONG)
            t.show()
            Log.d(TAG, "Permission not granted")
        }


        binding2.bottomNav.setOnItemSelectedListener{
            when(it.itemId){
                R.id.home->setCurrentFragment(HomeFragment())
                R.id.quizz->setCurrentFragment(QuizzFragment())
                R.id.analyticss->setCurrentFragment(AnalysisFragment())
                R.id.geofence->setCurrentFragment(GeofenceActivity())
            }
            true
        }

        binding2.toolbarImage.setOnClickListener(){
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }


    }

    private fun setCurrentFragment(fragment: Fragment) {
        FragmentManager = supportFragmentManager
        FragmentManager.beginTransaction().replace(R.id.fragment_container,fragment).commit()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.account_menu, menu)
        return true
    }

    //TODO : Manage item
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_manage_account -> {
                // Handle the "Manage Account" menu item click
                return true
            }
            R.id.action_logout -> {
                // Handle the "Logout" menu item click
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
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


    private fun geofenceAddition(geofenceList: List<Geofence>) {
        val geofencingRequest = getGeofencingRequest(geofenceList)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                .addOnSuccessListener {
                    Log.d(TAG, "Geofences added successfully")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error adding geofences: ${e.message}")
                }
        }
    }

    private fun getExistingGeofencesFromDatabase() {

        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser


        user?.let { FirebaseDatabase.getInstance().reference.child(it.uid).child("Geofences") }
            ?.addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("VisibleForTests")
                override fun onDataChange(snapshot: DataSnapshot) {
                    val geofenceList = mutableListOf<Geofence>()

                    for (dataSnapshot in snapshot.children) {
                        val geofenceData = dataSnapshot.getValue(GeofenceData::class.java)


                        geofenceData?.let {
                            val geofence = Geofence.Builder()
                                .setRequestId(it.id)
                                .setCircularRegion(it.latitude, it.longitude, it.radius.toFloat())
                                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                                .build()
                            geofenceList.add(geofence)
                        }
                    }

                    // Ajouter les geofences à la GeofencingClient
                    if (geofenceList.isNotEmpty()) {
                        if (ActivityCompat.checkSelfPermission(
                                this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return
                        }

                        geofencingClient.addGeofences(
                            getGeofencingRequest(geofenceList),
                            geofencePendingIntent
                        )
                            .addOnSuccessListener {
                                Log.d(
                                    TAG,
                                    "Existing geofences ${geofenceList.size}  added successfully"
                                )
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error adding existing geofences: ${e.message}")
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error retrieving geofences from database: ${error.message}")
                }
            })
    }


    private fun getGeofencingRequest(geofenceList: List<Geofence>): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()
    }
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Fine Location permission is granted
            // Check if current android version >= 11, if >= 11 check for Background Location permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (ContextCompat.checkSelfPermission(this@MainActivity,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                )
                {
                    geofencingClient = LocationServices.getGeofencingClient(this)
                    getExistingGeofencesFromDatabase()
                } else {
                    // Ask for Background Location Permission
                    askPermissionForBackgroundUsage()
                }
            }
        } else {
            // Fine Location Permission is not granted so ask for permission
            askForLocationPermission()
        }
    }

    private fun askForLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            AlertDialog.Builder(this)
                .setTitle("Permission Needed!")
                .setMessage("Location Permission Needed!")
                .setPositiveButton("OK",
                    DialogInterface.OnClickListener { _, _ ->
                        ActivityCompat.requestPermissions(
                            this@MainActivity, arrayOf<String>(
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ), LOCATION_PERMISSION_CODE
                        )
                    })
                .setNegativeButton("CANCEL", DialogInterface.OnClickListener { dialog, which ->
                    // Permission is denied by the user
                })
                .create().show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun askPermissionForBackgroundUsage() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this@MainActivity,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        ) {
            AlertDialog.Builder(this)
                .setTitle("Permission Needed!")
                .setMessage("Background Location Permission Needed!, tap \"Allow all time in the next screen\"")
                .setPositiveButton("OK",
                    DialogInterface.OnClickListener { dialog, which ->
                        ActivityCompat.requestPermissions(
                            this@MainActivity, arrayOf<String>(
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            ), BACKGROUND_LOCATION_PERMISSION_CODE
                        )
                    })
                .setNegativeButton("CANCEL", DialogInterface.OnClickListener { dialog, which ->
                    // User declined for Background Location Permission.
                })
                .create().show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                BACKGROUND_LOCATION_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // User granted location permission
                // Now check if android version >= 11, if >= 11 check for Background Location Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Background Location Permission is granted so do your work here
                    } else {
                        // Ask for Background Location Permission
                        askPermissionForBackgroundUsage();
                    }
                }
            } else {
                // User denied location permission
            }
        } else if (requestCode == BACKGROUND_LOCATION_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // User granted for Background Location Permission.
            } else {
                // User declined for Background Location Permission.
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS)) {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Notification Permission Needed!")
                    .setMessage("This app requires notification permission.")
                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                        ActivityCompat.requestPermissions(
                            this@MainActivity,
                            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                            NOTIFICATION_PERMISSION_CODE
                        )
                    })
                    .setNegativeButton("CANCEL", DialogInterface.OnClickListener { dialog, which ->
                        // Notification permission is denied by the user
                    })
                    .create()
                    .show()
            } else {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }

    companion object {
        private lateinit var geofencePendingIntent: PendingIntent
        private const val TAG = "MainActivity"

        fun getGeofencePendingIntent(context: Context): PendingIntent {
            if (!::geofencePendingIntent.isInitialized) {
                val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
                geofencePendingIntent =
                    PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            }
            return geofencePendingIntent
        }

        // Add this function to remove all monitored geofences
        fun removeAllGeofences(context: Context) {
            val geofencingClient = LocationServices.getGeofencingClient(context)
            geofencingClient.removeGeofences(getGeofencePendingIntent(context))?.run {
                addOnSuccessListener {
                    Log.i(TAG, "All geofences removed successfully.")
                }
                addOnFailureListener {
                    Log.e(TAG, "Failed to remove geofences: ${it.message}")
                }
            }
        }

        fun startMonitoringGeofence(context: Context, geofence: Geofence) {
            val geofencingClient = LocationServices.getGeofencingClient(context)
            val geofenceRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            val pendingIntent = getGeofencePendingIntent(context)

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            geofencingClient.addGeofences(geofenceRequest, pendingIntent)?.run {
                addOnSuccessListener {
                    Log.i(TAG, "Geofence monitoring started successfully.")
                    Log.i(TAG, "NEW  NEWWWWW Geofence monitoring started successfully.")
                }
                addOnFailureListener {
                    Log.e(TAG, "Failed to start geofence monitoring: ${it.message}")
                }
            }
        }

        fun removeGeofence(context: Context, geofenceId: String) {
            val geofencingClient = LocationServices.getGeofencingClient(context)
            val geofenceIds = listOf(geofenceId)

            geofencingClient.removeGeofences(geofenceIds)?.run {
                addOnSuccessListener {
                    Log.i(TAG, "Monitoring on Geofence $geofenceId removed successfully.")
                }
                addOnFailureListener {
                    Log.e(TAG, "Failed to remove geofence $geofenceId: ${it.message}")
                }
            }
        }

    }


}