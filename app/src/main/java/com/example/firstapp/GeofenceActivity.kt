package com.example.firstapp

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
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
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID


class
GeofenceActivity: FragmentActivity(),OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var addGeofenceButton: Button
    private var geofenceList = mutableListOf<GeofenceData>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: DatabaseReference
    private var isPointSelected = false
    private val geofenceAdapter: ArrayAdapter<GeofenceData> by lazy {
        ArrayAdapter<GeofenceData>(this, android.R.layout.simple_list_item_1, geofenceList)
    }
    val geofencesReference = FirebaseDatabase.getInstance().reference.child("Geofences")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.geofence_management)

        // Initialize fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().reference.child("Geofences")

        fetchGeofencesFromFirebase()
        displayGeofencesOnMap()


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)




        // Initialiser l'écouteur pour récupérer les geofences depuis la base de données
        geofencesReference.addValueEventListener(geofencesValueEventListener)

        // Définir l'adaptateur pour le ListView
        val geofenceListView: ListView = findViewById(R.id.geofenceListView)
        geofenceListView.adapter = geofenceAdapter


        // Set up the button click listener to add geofence
        addGeofenceButton = findViewById(R.id.addGeofenceButton)
        addGeofenceButton.setOnClickListener {

            // Call the method to add geofences to the database
            addGeofencesToDatabase()
        }



    }

    private val geofencesValueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            // Effacer la liste existante
            geofenceList.clear()

            // Parcourir toutes les données dans la base de données
            for (dataSnapshot in snapshot.children) {
                // Convertir les données en un objet GeofenceData et l'ajouter à la liste
                val geofenceData = dataSnapshot.getValue(GeofenceData::class.java)
                geofenceData?.let { geofenceList.add(it) }
            }

            // Mettre à jour l'adaptateur pour refléter les nouvelles geofences
            geofenceAdapter.notifyDataSetChanged()
        }

        override fun onCancelled(error: DatabaseError) {
            // Gérer les erreurs d'annulation
            Toast.makeText(applicationContext, "Error retrieving geofences from database", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //Zoom on the last position
        getLastKnownLocation()
        // Display existing geofences on the map
        displayGeofencesOnMap()

        // Set up the map click listener
        mMap.setOnMapClickListener { latLng ->
            // Call the method to handle map click
            handleMapClick(latLng)
        }


    }

    private fun fetchGeofencesFromFirebase() {
        geofencesReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Effacer la liste existante
                geofenceList.clear()

                // Parcourir toutes les données dans la base de données
                for (dataSnapshot in snapshot.children) {
                    // Convertir les données en un objet GeofenceData et l'ajouter à la liste
                    val geofenceData = dataSnapshot.getValue(GeofenceData::class.java)
                    geofenceData?.let { geofenceList.add(it) }
                }

                // Mettre à jour la carte avec les nouvelles geofences
                displayGeofencesOnMap()
            }

            override fun onCancelled(error: DatabaseError) {
                // Gérer les erreurs d'annulation
                Toast.makeText(applicationContext, "Error retrieving geofences from database", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun displayGeofencesOnMap() {
        // Assurez-vous que la carte est prête
        if (::mMap.isInitialized) {
            // Effacez les cercles existants sur la carte
            mMap.clear()

            // Parcourez la liste des geofences
            for (geofence in geofenceList) {
                val geofenceLatLng = LatLng(geofence.latitude, geofence.longitude)

                // Ajoutez un cercle sur la carte représentant la geofence
                mMap.addCircle(
                    CircleOptions()
                        .center(geofenceLatLng)
                        .radius(geofence.radius.toDouble())
                        .strokeColor(Color.RED) // Vous pouvez personnaliser la couleur de la bordure
                        .fillColor(Color.argb(70, 255, 0, 0)) // Vous pouvez personnaliser la couleur de remplissage
                )

                // Ajoutez également un marqueur pour chaque geofence (facultatif)
                mMap.addMarker(
                    MarkerOptions()
                        .position(geofenceLatLng)
//                        .title("Geofence: ${geofence.name}") // Utilisez le nom de la geofence (si disponible)
                )
            }
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

        // Add a marker
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(latLng).title("Chosen area"))

        // Generate
        val uniqueGeofenceId = UUID.randomUUID().toString()
        val geofenceInfo = GeofenceData(uniqueGeofenceId, latitude, longitude,200)
        geofenceList.clear()
        geofenceList.add(geofenceInfo)
        displayGeofencesOnMap()

        // Check if a point is selected
        isPointSelected = true

        // Test the coordinate
        val toastMessage = "Latitude: $latitude, Longitude: $longitude"
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
    }
    private fun addGeofencesToDatabase() {
        // Check if a point has been selected
        if (isPointSelected) {
            // Loop through the geofenceList and add each geofence to the Firebase database
            for (geofence in geofenceList) {

                // Use the geofence request ID as the key in the database

                database.child(geofence.id).setValue(geofence)
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



