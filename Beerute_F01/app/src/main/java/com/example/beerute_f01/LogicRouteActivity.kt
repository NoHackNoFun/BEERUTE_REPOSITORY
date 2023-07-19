package com.example.beerute_f01

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.TextView

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.beerute_f01.Object.GlobalVariables
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.firebase.firestore.FirebaseFirestore

class LogicRouteActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var startButton: Button
    private lateinit var saveButton: Button
    private lateinit var stepCountTextView: TextView

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    private var stepCount = 0
    private var isRunning = false
    private val handler = Handler()

    val latitudeArray = mutableListOf<Double>()
    val longitudeArray = mutableListOf<Double>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logicroute)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        startButton = findViewById(R.id.startButton)
        saveButton = findViewById(R.id.saveButton)
        stepCountTextView = findViewById(R.id.stepCountTextView)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        // Verificar y solicitar los permisos de ubicación si es necesario
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_LOCATION
            )
        } else {
            startButton.setOnClickListener {
                if (isRunning) {
                    stopLoop()
                } else {
                    startLoop()
                }
            }

            saveButton.setOnClickListener {
                // Crear un bucle que recoja cada una de las posiciones del vector lista
                // y las vaya introduciendo en la variables de la base de datos
                guardarCoordenadasFirestore(latitudeArray, longitudeArray)
            }
        }
    }

    private fun startLoop() {
        isRunning = true
        startButton.text = "Stop"

        handler.postDelayed(object : Runnable {
            override fun run() {
                // Realiza las operaciones o acciones deseadas en el bucle

                // Aquí se incluye la lógica del método onSensorChanged
                val steps = stepSensor?.let { getStepCount(it) } ?: 0

                //

                stepCountTextView.text = steps.toString()

                if (stepCount == 0) {
                    stepCount = steps
                }

                startLocationUpdates()
                if (GlobalVariables.latitude == 0.0 || GlobalVariables.latitude != GlobalVariables.oldlatitude) {

                    GlobalVariables.oldlatitude = GlobalVariables.latitude
                    GlobalVariables.oldlongitude = GlobalVariables.longitude

                    if (GlobalVariables.latitude != 0.0) {
                        latitudeArray.add(GlobalVariables.latitude)
                        longitudeArray.add(GlobalVariables.longitude)
                    }
                }

                /*if (steps % 6 == 0) {
                    // Recoger la ubicación
                    startLocationUpdates()
                    if (GlobalVariables.latitude == 0.0 || GlobalVariables.latitude != GlobalVariables.oldlatitude) {

                        GlobalVariables.oldlatitude = GlobalVariables.latitude
                        GlobalVariables.oldlongitude = GlobalVariables.longitude

                        latitudeArray.add(GlobalVariables.latitude)
                        longitudeArray.add(GlobalVariables.longitude)
                    }
                }*/

                // Fin de la lógica del método onSensorChanged

                if (isRunning) {
                     handler.postDelayed(this, 500)// Repite el bucle después de 500 milisegundos (65)
                }
            }
        }, 1000) // Inicia el bucle después de 1 segundo
    }

    private fun stopLoop() {
        isRunning = false
        startButton.text = "Start"
    }

    override fun onResume() {
        super.onResume()
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        if (stepSensor != null) {
            sensorManager.unregisterListener(this)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // No se utiliza
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val steps = event.values[0].toInt()
            stepCountTextView.text = steps.toString()

            if (stepCount == 0) {
                stepCount = steps
            }
        }
    }

    private fun getStepCount(sensor: Sensor): Int {
        // Obtener el valor de contador de pasos del sensor
        // Puedes implementar aquí tu lógica para obtener el valor de contador de pasos actual
        return 0
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Obtener la ubicación actual si está disponible
                location?.let {
                    val latitude = location.latitude
                    GlobalVariables.latitude = latitude

                    val longitude = location.longitude
                    GlobalVariables.longitude = longitude

                    // Hacer algo con la ubicación (mostrar en el mapa, guardar en una variable, etc.)
                }
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                // Permiso denegado, manejar el caso según tus necesidades
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_LOCATION = 123
    }

    fun guardarCoordenadasFirestore(latitudeArray: MutableList<Double>, longitudeArray: MutableList<Double>) {

        val vdLength = latitudeArray.size
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("prueba").document("location")

        val coordenatelist = hashMapOf(
            "latitude" to latitudeArray.toList(),
            "longitude" to longitudeArray.toList(),
            "length" to vdLength
        )

        docRef.update("coordinatelist", coordenatelist)
            .addOnSuccessListener {

                // Éxito al guardar los datos en Firestore
                Toast.makeText(this, "Coordenadas guardadas en Firestore", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->

                // Error al guardar los datos en Firestore
                Toast.makeText(this, "Error al guardar las coordenadas en Firestore", Toast.LENGTH_SHORT).show()

                // Imprimir el mensaje de error en la consola:
                Log.e("Firestore", "Error al guardar las coordenadas: ${e.message}", e)
            }
    }
}