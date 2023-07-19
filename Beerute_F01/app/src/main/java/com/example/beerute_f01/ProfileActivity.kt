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

        // Obtener el correo electrónico y el proveedor del usuario de las variables globales
        val email = GlobalVariables.userEmail
        val provider = GlobalVariables.userProvider

        // Configurar la interfaz de usuario con el correo electrónico y el proveedor
        setup(email ?: "", provider ?: "")

        // Guardar los datos del usuario en las preferencias compartidas
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.apply()
    }

    private fun setup(email: String, provider: String) {
        // Configurar la interfaz de usuario

        title = "Inicio"

        emailTextView = findViewById(R.id.emailTextView)
        providerTextView = findViewById(R.id.providerTextView)
        logOutButton = findViewById(R.id.logOutButton)

        // Mostrar el correo electrónico y el proveedor en los TextView correspondientes
        emailTextView.text = email
        providerTextView.text = provider

        logOutButton.setOnClickListener {
            // Realizar el cierre de sesión del usuario

            // Borrar los datos de las preferencias compartidas
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            GlobalVariables.userEmail = ""
            GlobalVariables.userProvider = ""
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            onBackPressed()

            // Iniciar la actividad de autenticación (login)
            val loIntent = Intent(this, AuthActivity::class.java)
            // Agregar esta bandera para limpiar el historial de actividades
            loIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(loIntent)
            finish()
        }
    }

    override fun onBackPressed() {
        // Volver a la actividad principal (inicio)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}