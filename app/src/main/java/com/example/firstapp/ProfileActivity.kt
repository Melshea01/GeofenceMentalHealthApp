package com.example.firstapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.example.firstapp.databinding.ProfileActivityBinding
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast

class ProfileActivity : FragmentActivity() {

    private lateinit var binding: ProfileActivityBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var geofencingClient: GeofencingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ProfileActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val textViewTitle = findViewById<TextView>(binding.textViewTitle.id)
        val textViewUserName = findViewById<TextView>(binding.textViewUserName.id)
        val textViewEmail = findViewById<TextView>(binding.textViewEmail.id)
        val buttonLogout = findViewById<Button>(binding.buttonLogout.id)

        // retrieve user information
        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser

        // Geofence initialization
        geofencingClient = LocationServices.getGeofencingClient(this)

        if (user != null) {
            // User is connected
            val email = user.email
            val creationDate = user.metadata!!.creationTimestamp.toString()
            textViewTitle.text = "Vos Informations"
            textViewUserName.text = "Nom d'utilisateur: ${user.displayName ?: "N/A"}"
            textViewEmail.text = "E-mail: $email\nCréation du compte: $creationDate"
        }

        // Logout listener
        buttonLogout.setOnClickListener {
            geofencingClient.removeGeofences(MainActivity.getGeofencePendingIntent(this))
            firebaseAuth.signOut()
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish() // Close the activity after clicking on deconnexion
        }
        
    }
}
