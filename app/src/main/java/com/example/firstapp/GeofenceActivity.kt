package com.example.firstapp

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID


class
GeofenceActivity: FragmentActivity(),OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var addGeofenceButton: Button
    private var geofenceList = mutableListOf<Geofence>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: DatabaseReference
    private var isPointSelected = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.geofence_management)

        // Initialize fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().reference.child("Students")




        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        // Set up the button click listener to add geofence
        addGeofenceButton = findViewById(R.id.addGeofenceButton)
        addGeofenceButton.setOnClickListener {

            // Call the method to add geofences to the database
            addGeofencesToDatabase()

//            val nom = "Melinda"
//            val nouveauNomRef = database.child("noms").push()
//            nouveauNomRef.setValue(nom)
//                .addOnSuccessListener {
//                    // Succès, le nom a été ajouté à la base de données
//                    println("Nom ajouté avec succès à la base de données")
//                }
//                .addOnFailureListener { e ->
//                    // Erreur lors de l'ajout du nom à la base de données
//                    println("Erreur lors de l'ajout du nom à la base de données : $e")
//                }


        }



    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //Zoom on the last position
        getLastKnownLocation()

        // Set up the map click listener
        mMap.setOnMapClickListener { latLng ->
            // Call the method to handle map click
            handleMapClick(latLng)
        }
    }



    private fun getLastKnownLocation() {
        // Check location permissions
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        // Center the map on the user's last known location
                        val userLocation = LatLng(location.latitude, location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                    }
                }
        }
    }

    //Retrieve information on a map to create the needed information
    private fun handleMapClick(latLng: LatLng) {
        val latitude = latLng.latitude
        val longitude = latLng.longitude

        // Ajouter un marqueur à l'endroit sélectionné
        mMap.clear() // Effacer les marqueurs existants (si nécessaire)
        mMap.addMarker(MarkerOptions().position(latLng).title("Chosen area"))

        // Générer un ID unique pour la géofence
        val uniqueGeofenceId = UUID.randomUUID().toString()

        // Créer un objet Geofence
        val geofence = Geofence.Builder()
            .setRequestId(uniqueGeofenceId) // ID unique pour la géofence
            .setCircularRegion(latitude, longitude, 200f)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        // Ajouter la géofence à la liste
        geofenceList.add(geofence)

        // Mettre à jour la variable pour indiquer qu'un point a été sélectionné
        isPointSelected = true

        // Afficher un Toast avec les coordonnées
        val toastMessage = "Latitude: $latitude, Longitude: $longitude"
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
    }
    private fun addGeofencesToDatabase() {
        // Check if a point has been selected
        if (isPointSelected) {
            // Loop through the geofenceList and add each geofence to the Firebase database
            for (geofence in geofenceList) {
                // Use the geofence request ID as the key in the database
                database.child(geofence.requestId).setValue(geofence)
                Toast.makeText(this, "added to database", Toast.LENGTH_SHORT).show()
            }


            // Clear the geofenceList after adding to the database
            geofenceList.clear()

            // Reset the variable to indicate that no point is selected
            isPointSelected = false
        } else {
            // Inform the user that they need to select a point on the map before adding a geofence
            Toast.makeText(this, "Select a point on the map first", Toast.LENGTH_SHORT).show()
        }
    }

}



