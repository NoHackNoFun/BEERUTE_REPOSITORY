package com.example.beerute_f01

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.beerute_f01.Object.GlobalVariables
import com.example.beerute_f01.databinding.ActivityAuthBinding
import com.google.firebase.auth.FirebaseAuth

//Metodo de autenticacion que utilizamos en nuestra app
enum class ProviderType {
    BASIC,
    GOOGLE
}

class ProfileActivity : AppCompatActivity() {

    lateinit var emailTextView: TextView
    lateinit var providerTextView: TextView
    lateinit var logOutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Setup
        /*val bundle:Bundle? = intent.extras
        val email:String? = bundle?.getString("email")
        val provider:String? = bundle?.getString("provider")
        setup(email ?: "", provider ?: "")*/

        val email = GlobalVariables.userEmail
        val provider = GlobalVariables.userProvider
        setup(email ?: "", provider ?: "")

        //Guardado de datos
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.apply()
    }

    private fun setup(email: String, provider: String) {

        title = "Inicio"

        emailTextView = findViewById(R.id.emailTextView)
        providerTextView = findViewById(R.id.providerTextView)
        logOutButton = findViewById(R.id.logOutButton)

        emailTextView.text = email
        providerTextView.text = provider

        logOutButton.setOnClickListener {

            // Borrado de datos
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()

            GlobalVariables.userEmail = ""
            GlobalVariables.userProvider = ""

            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            onBackPressed()

            val loIntent = Intent(this, AuthActivity::class.java)
            startActivity(loIntent)
        }
    }
}