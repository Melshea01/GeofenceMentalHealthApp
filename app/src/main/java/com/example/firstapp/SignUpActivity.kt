package com.example.firstapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.firstapp.databinding.SignupActivityBinding
import com.google.firebase.auth.FirebaseAuth


class SignUpActivity : FragmentActivity(){
    private lateinit var editConfirmPassword: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var btnSignUp: Button
    private lateinit var tvAlreadyRegistered: TextView
    private lateinit var auth : FirebaseAuth
    private lateinit var binding: SignupActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SignupActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Authentification using database
        auth = FirebaseAuth.getInstance()

        // Initialiser les vues
        editConfirmPassword = binding.editConfirmPassword
        editTextEmail = binding.editTextEmail
        editTextPassword = binding.editTextPassword
        btnSignUp = binding.btnSignUp
        tvAlreadyRegistered = binding.tvAlreadyRegistered

        //Créer un champ confirm password



        btnSignUp.setOnClickListener {
            signUp()
        }

        // Click listener for  "Already registered?"
        tvAlreadyRegistered.setOnClickListener {
            // Ajoutez ici le code pour rediriger vers l'écran de connexion
            // par exemple, l'utilisation d'une Intent pour passer à l'activité de connexion
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }


    }

    private fun signUp() {
        val email = editTextEmail.text.toString()
        val password = editTextPassword.text.toString()
        val confirmPass= editTextPassword.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty() && confirmPass.isNotEmpty()) {
            if (password == confirmPass) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // L'utilisateur est créé avec succès
                            Toast.makeText(this, "SignUp Successful", Toast.LENGTH_SHORT).show()
                            // Par exemple, rediriger l'utilisateur vers une autre activité
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish() // Facultatif : fermez l'activité d'inscription pour éviter le retour en arrière
                        } else {
                            // La création de l'utilisateur a échoué
                            Toast.makeText(this, "SignUp Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }else {
                Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
            }

        }else {
            Toast.makeText(this, "Email or Password is empty ", Toast.LENGTH_SHORT).show()
        }


    }


}