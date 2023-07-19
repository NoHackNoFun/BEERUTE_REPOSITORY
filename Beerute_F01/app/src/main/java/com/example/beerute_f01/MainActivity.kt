package com.example.beerute_f01

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity(){

    lateinit var exploreButton: Button
    lateinit var createButton: Button
    lateinit var IAHelpButton: Button
    lateinit var profileButton: Button
    lateinit var newStepButton: Button
    lateinit var testButton: Button
    lateinit var logicRouteButton: Button

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }

    /**
     * Método de creación de la actividad principal
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Solicitar permisos
        requestPermissions()

        // Solicitar el token de registro
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d(TAG, "Token: $token")
                // Aquí se envía el token a Firebase para identificar a los dispositivos
            } else {
                Log.e(TAG, "Failed to get token")
            }
        }

        // Configuración inicial
        setup()
    }

    /**
     * Método para configurar la interfaz de usuario y los listeners de los botones
     */
    private fun setup() {
        exploreButton = findViewById(R.id.exploreButton)
        createButton = findViewById(R.id.createButton)
        IAHelpButton = findViewById(R.id.IAHelpButton)
        profileButton = findViewById(R.id.profileButton)
        newStepButton = findViewById(R.id.newStepButton)
        testButton = findViewById(R.id.testButton)
        logicRouteButton = findViewById(R.id.logicRouteButton)

        // Asignar listeners a los botones
        exploreButton.setOnClickListener {
            val eIntent = Intent(this, ExploreActivity::class.java)
            startActivity(eIntent)
        }

        createButton.setOnClickListener {
            val cIntent = Intent(this, CreateActivity::class.java)
            startActivity(cIntent)
        }

        IAHelpButton.setOnClickListener {
            val iaIntent = Intent(this, IAHelpActivity::class.java)
            startActivity(iaIntent)
        }

        profileButton.setOnClickListener {
            val pIntent = Intent(this, ProfileActivity::class.java)
            startActivity(pIntent)
        }

        newStepButton.setOnClickListener {
            val nspIntent = Intent(this, NewStepCounterActivity::class.java)
            startActivity(nspIntent)
        }

        testButton.setOnClickListener {
            val taIntent = Intent(this, testActivity::class.java)
            startActivity(taIntent)
        }

        logicRouteButton.setOnClickListener {
            val lIntent = Intent(this, LogicRouteActivity::class.java)
            startActivity(lIntent)
        }
    }

    /**
     * Método para solicitar los permisos necesarios para el funcionamiento de la aplicación
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.INTERNET
        )

        val permissionsToRequest = mutableListOf<String>()

        for (permission in permissions) {
            val permissionCheck = ContextCompat.checkSelfPermission(this, permission)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    /**
     * Método para manejar la respuesta a la solicitud de permisos
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    // Permiso denegado
                }
            }
        }
    }
}