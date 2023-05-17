package com.example.beerute_f00.ui.profile

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.beerute_f00.R
import com.google.firebase.auth.FirebaseAuth

/*enum class ProviderType {
    BASIC,
    GOOGLE
}*/

class ProfileActivity : AppCompatActivity() {

    lateinit var emailTextView: TextView
    lateinit var providerTextView: TextView
    lateinit var logOutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_profile)

        // Setup

        val bundle:Bundle? = intent.extras
        val email:String? = bundle?.getString("email")
        val provider:String? = bundle?.getString("provider")
        setup(email ?: "", provider ?: "")

        //Guardo de datos
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
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }
    }
}