package com.example.firstapp

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID


class GeofenceActivity : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var geofenceList = mutableListOf<GeofenceData>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: DatabaseReference
    private lateinit var helpButton: FloatingActionButton
    private val geofenceAdapter: ArrayAdapter<GeofenceData> by lazy {
        ArrayAdapter<GeofenceData>(requireContext(), android.R.layout.simple_list_item_1, geofenceList)
    }
    val geofencesReference = FirebaseDatabase.getInstance().reference.child("Geofences")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.geofence_management, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().reference.child("Geofences")

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialiser l'écouteur pour récupérer les geofences depuis la base de données
        geofencesReference.addValueEventListener(geofencesValueEventListener)

        // Initialiser le bouton d'aide
        helpButton = view.findViewById(R.id.helpButton)
        helpButton.setOnClickListener {
            showHelpDialog()
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

            // Mettre à jour la carte après avoir récupéré les données
            displayGeofencesOnMap()
        }

        override fun onCancelled(error: DatabaseError) {
            // Gérer les erreurs d'annulation
            Toast.makeText(requireContext(), "Error retrieving geofences from database", Toast.LENGTH_SHORT).show()
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
            handleMapClick(latLng)
        }

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
                val marker = mMap.addMarker(
                    MarkerOptions()
                        .position(geofenceLatLng)
//                        .title("Geofence: ${geofence.name}") // Utilisez le nom de la geofence (si disponible)
                )

                marker?.tag = geofence
            }

            mMap.setOnMarkerClickListener { marker ->
                // Récupérez la geofence associée au marqueur
                val geofence = marker.tag as? GeofenceData
                geofence?.let {
                    // Affichez une boîte de dialogue de confirmation de suppression
                    showDeleteGeofenceConfirmationDialog(it)
                }
                // Indiquez que le clic sur le marqueur est géré ici
                true
            }

        }
    }

    private fun showDeleteGeofenceConfirmationDialog(geofence: GeofenceData) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())

        // Configure the AlertDialog
        alertDialogBuilder.setTitle("Delete Geofence")
        alertDialogBuilder.setMessage("Are you sure you want to delete this geofence?")
        alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
            // User clicked "Yes," so proceed with deleting the geofence
            deleteGeofence(geofence)
        }

        alertDialogBuilder.setNegativeButton("No") { _, _ ->
            // User clicked "No," so do nothing
        }

        // Create and show the AlertDialog
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun deleteGeofence(geofence: GeofenceData) {
        // Supprimer la geofence de la liste
        geofenceList.remove(geofence)

        // Supprimer la geofence de la base de données
        val geofenceReference = geofencesReference.child(geofence.id)
        geofenceReference.removeValue()

        // Mettre à jour la carte et la liste après la suppression
        displayGeofencesOnMap()
    }



    private fun getLastKnownLocation() {
        // Check location permissions
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
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
        val alertDialogBuilder = AlertDialog.Builder(requireContext())

        // Configure the AlertDialog
        alertDialogBuilder.setTitle("Add Geofence")
        alertDialogBuilder.setMessage("Are you sure you want to add a geofence at this location?")
        alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
            // User clicked "Yes," so proceed with adding the geofence

            // Add a marker
            mMap.addMarker(MarkerOptions().position(latLng).title("Chosen area"))

            // Generate new geofence
            val uniqueGeofenceId = UUID.randomUUID().toString()
            val geofenceInfo = GeofenceData(uniqueGeofenceId, latLng.latitude, latLng.longitude, 200)
            geofenceList.add(geofenceInfo)

            //Add geofence to database
            addGeofencesToDatabase()
            displayGeofencesOnMap()

        }

        alertDialogBuilder.setNegativeButton("No") { _, _ ->
            // User clicked "No," so do nothing
        }

        // Create and show the AlertDialog
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
    private fun addGeofencesToDatabase() {
        // Check if a point has been selected
            // Loop through the geofenceList and add each geofence to the Firebase database
            for (geofence in geofenceList) {
                // Use the geofence request ID as the key in the database
                database.child(geofence.id).setValue(geofence)
            }
            // Clear the geofenceList after adding to the database
            geofenceList.clear()
        }

    fun showHelpDialog() {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())

        // Configure the AlertDialog
        alertDialogBuilder.setTitle("Help")
        alertDialogBuilder.setMessage(
            "To add a geofence, tap on the map. A marker representing the geofence will be placed on the map. " +
                    "To delete a geofence, tap on the one you are interested in and confirm the deletion. " +
                    "If no geofences appear, it may be due to a connection issue."
        )
        alertDialogBuilder.setPositiveButton("OK") { _, _ ->
            // User clicked "OK," so close the dialog
        }

        // Create and show the AlertDialog
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

}