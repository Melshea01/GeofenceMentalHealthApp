package com.example.firstapp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button


class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Référencez le bouton par son ID
        val geofenceButton = view.findViewById<Button>(R.id.geofencebutton)

        // Configurez un écouteur de clic pour le bouton
        geofenceButton.setOnClickListener {
            // Lancer l'activité QuestionnaireActivity lorsque le bouton est cliqué
            val intent = Intent(requireActivity(), GeofenceActivity::class.java)
            startActivity(intent)
        }

        return view
    }

}