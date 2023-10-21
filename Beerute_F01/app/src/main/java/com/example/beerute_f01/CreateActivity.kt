package com.example.beerute_f01

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.beerute_f01.Object.GlobalVariables
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.example.beerute_f01.Object.RuteObject
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

@Suppress("NAME_SHADOWING")
class CreateActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener,
    SensorEventListener {

    private val firestore = FirebaseFirestore.getInstance()

    val latitudeArray = mutableListOf<Double>()
    val longitudeArray = mutableListOf<Double>()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    private lateinit var chronometer: Chronometer
    private var isRunning = false

    private lateinit var map: GoogleMap
    private val ruteObject = RuteObject

    private lateinit var linearStepCounter: LinearLayout

    private lateinit var savePlaceEditText: EditText

    private lateinit var placeTextView: TextView
    private lateinit var stepCountTextView: TextView
    private lateinit var differenceTextView: TextView
    private lateinit var distanceTextView: TextView
    private lateinit var timeTextView: TextView

    private lateinit var createRouteButton: Button
    private lateinit var exploreRouteButton: Button
    private lateinit var clearRouteButton: Button

    private lateinit var playButton: Button
    private lateinit var ePlayButton: Button
    private lateinit var readyButton: Button
    private lateinit var saveButton: Button

    private val handler = Handler()

    private val permissionRequestCode = 123
    private var initialStepCount = 0
    private var isChronometerRunning = false
    private var elapsedTime: Long = 0

    private var elapsedMillis: Long = 0

    companion object {
        const val REQUEST_CODE_LOCATION = 0
    }

    /**
     * Método de creación de la actividad.
     */
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        createMapFragment()
        setupButtons()
    }

    /**
     * Método para configurar los botones y las interacciones con el usuario.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("ResourceAsColor", "SetTextI18n")
    private fun setupButtons() {

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        chronometer = findViewById(R.id.chronometer)

        linearStepCounter = findViewById(R.id.linearStepCounter)

        savePlaceEditText = findViewById(R.id.savePlaceEditText)

        placeTextView = findViewById(R.id.placeTextView)
        stepCountTextView = findViewById(R.id.stepCountTextView)
        differenceTextView = findViewById(R.id.differenceTextView)
        distanceTextView = findViewById(R.id.distanceTextView)
        timeTextView = findViewById(R.id.timeTextView)

        createRouteButton = findViewById(R.id.createRouteButton)
        exploreRouteButton = findViewById(R.id.exploreRouteButton)
        clearRouteButton = findViewById(R.id.clearRouteButton)
        playButton = findViewById(R.id.playButton)
        ePlayButton = findViewById(R.id.ePlayButton)
        readyButton = findViewById(R.id.readyButton)
        saveButton = findViewById(R.id.saveButton)

        // Verificar disponibilidad del sensor de contador de pasos
        if (stepSensor == null) {
            Toast.makeText(this, "El sensor de contador de pasos no está disponible en este dispositivo.", Toast.LENGTH_SHORT).show()
        } else {
            requestSensorPermission()
        }

        // Obtener número de documentos en la colección
        firestore.collection("routes").get()
            .addOnSuccessListener { querySnapshot ->
                val count = querySnapshot.size()
                GlobalVariables.lastid = count
            }
            .addOnFailureListener { exception ->
                println("Error al acceder a Firestore: ${exception.message}")
            }

        // Configurar listeners de botones
        createRouteButton.setOnClickListener {
            if (linearStepCounter.visibility == View.VISIBLE) {

                if (isChronometerRunning) chronometer.stop()

                differenceTextView.text = "Pasos: 0"
                distanceTextView.text = "Distancia: 0,000 km"
                elapsedTime = 0
                timeTextView.text = "      Tiempo: " + formatElapsedTime(elapsedTime) + "      "
                chronometer.base = SystemClock.elapsedRealtime()
                chronometer.format = "%s"

                playButton.text = "Play"

                // Ocultar LinearLayout y cambiar texto del botón
                linearStepCounter.visibility = View.GONE
                readyButton.visibility = View.GONE
                savePlaceEditText.visibility = View.GONE
                saveButton.visibility = View.GONE
                createRouteButton.text = "Grabar Ruta"

            } else {

                clearGlobalVariables()

                linearStepCounter.visibility = View.VISIBLE
                ePlayButton.visibility = View.GONE
                playButton.visibility = View.VISIBLE
                readyButton.visibility = View.GONE
                savePlaceEditText.visibility = View.GONE
                saveButton.visibility = View.GONE
                createRouteButton.text = "Eliminar Ruta"
            }
        }

        exploreRouteButton.setOnClickListener {
            val intent = Intent(this, ExploreActivity::class.java)
            startActivity(intent)
        }

        clearRouteButton.setOnClickListener {

            ruteObject.clearMatrix(this)

            clearGlobalVariables()

            val intent = Intent(this, CreateActivity::class.java)
            startActivity(intent)
        }

        playButton.setOnClickListener {
            val currentStepCount = stepCountTextView.text.toString().toInt()
            val difference = currentStepCount - initialStepCount
            differenceTextView.text = "Pasos: $difference"

            val distance = calculateDistance(difference)
            distanceTextView.text = "   Distancia: ${formatDistance(distance)} km   "

            if (isChronometerRunning && playButton.text == "Stop") {

                // Detener el bucle
                stopLoop()

                chronometer.stop()
                elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
                timeTextView.text = "      Tiempo: " + formatElapsedTime(elapsedTime) + "      "
                playButton.text = "Play"

                // Mostrar botón "listo"
                readyButton.visibility = View.VISIBLE

                // Guardar valores en variables globales
                GlobalVariables.selectedSteps = difference
                GlobalVariables.selectedKm = formatDistance(distance)
                GlobalVariables.selectedTime = elapsedTime.toDouble()

            } else if (playButton.text == "Play") {

                // Iniciar bucle
                startLoop()

                chronometer.base = SystemClock.elapsedRealtime() - elapsedTime
                chronometer.start()
                chronometer.format = "%s"
                timeTextView.text = "      Tiempo: " + formatElapsedTime(elapsedTime) + "      "
                playButton.text = "Stop"

                // Ocultar botón "listo"
                readyButton.visibility = View.GONE
                savePlaceEditText.visibility = View.GONE
                saveButton.visibility = View.GONE
            }

            isChronometerRunning = !isChronometerRunning
        }

        ePlayButton.setOnClickListener {
            if (isChronometerRunning) {

                chronometer.stop()
                elapsedTime = SystemClock.elapsedRealtime() - chronometer.base

                if (elapsedTime < GlobalVariables.selectedTime) {

                    firestore.collection("routes").document(GlobalVariables.selectedRouteId).update("time", elapsedTime)
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "ERROR, La actualización NO se realizó con éxito", Toast.LENGTH_SHORT).show()
                        }

                    firestore.collection("routes").document(GlobalVariables.selectedRouteId).update("bestuser", GlobalVariables.userEmail)
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "ERROR, La actualización NO se realizó con éxito", Toast.LENGTH_SHORT).show()
                        }


                    Toast.makeText(this, "¡¡¡ HAS BATIDO EL RECORD\nENHORABUENA !!!", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this, "¡¡¡ Esfuérzate cada día\ny LO CONSEGUIRÁS !!!", Toast.LENGTH_SHORT).show()
                }
                clearGlobalVariables()

            } else {
                startChronometer()
            }
        }

        readyButton.setOnClickListener {
            savePlaceEditText.visibility = View.VISIBLE
            saveButton.visibility = View.VISIBLE
        }

        saveButton.setOnClickListener {

            // Obtener número de documentos en la colección
            firestore.collection("routes").get()
                .addOnSuccessListener { querySnapshot ->
                    val count = querySnapshot.size()
                    GlobalVariables.lastid = count
                }
                .addOnFailureListener { exception ->
                    println("Error al acceder a Firestore: ${exception.message}")
                }

            val place: String = savePlaceEditText.text.toString()

            if (place.isNotEmpty()) {

                GlobalVariables.selectedPlace = place

                val routeData = hashMapOf(
                    "bestuser" to GlobalVariables.selectedUser,
                    "km" to GlobalVariables.selectedKm,
                    "place" to GlobalVariables.selectedPlace,
                    "steps" to GlobalVariables.selectedSteps,
                    "time" to GlobalVariables.selectedTime,
                    "user" to GlobalVariables.userEmail,
                    "coordinatelist" to hashMapOf(
                        "latitude" to latitudeArray.toList(),
                        "longitude" to longitudeArray.toList(),
                        "length" to latitudeArray.size
                    )
                )

                val routeRef = firestore.collection("routes").document(GlobalVariables.lastid.toString())
                routeRef.set(routeData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Datos guardados en Firebase", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al guardar los datos en Firebase", Toast.LENGTH_SHORT).show()
                    }

                if (GlobalVariables.userEmail != null) {
                    // Definir la referencia al documento del usuario actual
                    val userRef = firestore.collection("users").document(GlobalVariables.userEmail)

                    // Leer el valor actual de "kmmax" del documento del usuario
                    userRef.get().addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val currentKmMax = documentSnapshot.getLong("kmmax")

                            // Comparar con GlobalVariables.selectedKm
                            if (currentKmMax == null || GlobalVariables.selectedKm > currentKmMax) {
                                // Actualizar el valor de "kmmax" con GlobalVariables.selectedKm
                                userRef.update("kmmax", GlobalVariables.selectedKm)
                                    .addOnSuccessListener {

                                        // La actualización fue exitosa
                                        Toast.makeText(this, "Actualizados tus LOGROS", Toast.LENGTH_SHORT).show()

                                        // Aquí puedes realizar cualquier acción adicional si es necesario
                                    }
                                    .addOnFailureListener { e ->

                                        // Error al actualizar el campo "kmmax"
                                        Toast.makeText(this, "Error al Actualizar tus LOGROS", Toast.LENGTH_SHORT).show()

                                        // Manejar el error según sea necesario
                                    }
                            }
                        } else {

                            // El documento del usuario no existe, crearlo con el valor de "kmmax"
                            val newUser = hashMapOf(
                                "kmmax" to GlobalVariables.selectedKm
                                // Puedes agregar otros campos aquí si es necesario
                            )

                            userRef.set(newUser)
                                .addOnSuccessListener {

                                    // Documento creado exitosamente
                                    Toast.makeText(this, "Subidos tus LOGROS", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->

                                    // Error al crear el documento
                                    Toast.makeText(this, "Error al Subir tus LOGROS", Toast.LENGTH_SHORT).show()

                                    // Manejar el error según sea necesario
                                }
                        }
                    }.addOnFailureListener { e ->
                        // Error al obtener el documento del usuario
                        // Manejar el error según sea necesario
                    }
                }

            } else {
                Toast.makeText(this, "Debes ingresar un lugar", Toast.LENGTH_SHORT).show()
            }

            differenceTextView.text = "Pasos: 0"
            distanceTextView.text = "Distancia: 0,000 km"
            elapsedTime = 0
            timeTextView.text = "      Tiempo: " + formatElapsedTime(elapsedTime) + "      "
            chronometer.base = SystemClock.elapsedRealtime()
            chronometer.format = "%s"

            saveButton.visibility = View.GONE
            readyButton.visibility = View.GONE
            linearStepCounter.visibility = View.GONE

            createRouteButton.text = "Grabar Ruta"

        }
    }

    /**
     * Método para crear el fragmento del mapa.
     */
    private fun createMapFragment() {
        val mapFragment : SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Método llamado cuando el mapa está listo para ser usado.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        setRouteDetails()
        createPolylines()

        map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationClickListener(this)
        enableLocation()
    }

    /**
     * Método para visualizar los detalles de la ruta seleccionada.
     */
    @SuppressLint("SetTextI18n")
    fun setRouteDetails() {

        if (GlobalVariables.selectedPlace != "") {

            linearStepCounter.visibility = View.VISIBLE
            placeTextView.visibility = View.VISIBLE
            stepCountTextView.visibility = View.GONE
            differenceTextView.visibility = View.VISIBLE
            distanceTextView.visibility = View.VISIBLE
            timeTextView.visibility = View.VISIBLE
            playButton.visibility = View.GONE
            ePlayButton.visibility = View.VISIBLE


            placeTextView.text = "Lugar: ${GlobalVariables.selectedPlace}"
            differenceTextView.text = "Pasos: ${GlobalVariables.selectedSteps}"
            distanceTextView.text = "   Distancia: ${GlobalVariables.selectedKm} km   "
            timeTextView.text = "      Tiempo: ${GlobalVariables.selectedTime}      "
        }
    }

    /**
     * Método para crear las polilíneas en el mapa.
     */
    private fun createPolylines() {
        val sm = ruteObject.getMatrix(this)
        val l = sm?.get(0)?.size

        val polylineOptions = PolylineOptions()
        if (l != null) {
            for (i in 0 until l) {
                val latLng = LatLng(sm?.get(0)?.get(i) ?: 0.0, sm?.get(1)?.get(i) ?: 0.0)
                polylineOptions.add(latLng)
            }
        } else {
            Toast.makeText(this, "Se ha limpiado el mapa", Toast.LENGTH_LONG).show()
        }

        polylineOptions.width(15f).color(ContextCompat.getColor(this, R.color.kotlin))

        val polyline = map.addPolyline(polylineOptions)

        val pattern = listOf(
            Dot(), Gap(10F), Dash(50F), Gap(10F)
        )
        polyline.pattern = pattern

        polyline.isClickable = true
        map.setOnPolylineClickListener { polyline -> changeColor(polyline) }

        val fc = LatLng(sm?.get(0)?.get(0) ?: 0.0, sm?.get(1)?.get(0) ?: 0.0)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(fc, 17f), 3000, null)
    }

    /**
     * Método para cambiar el color de una polilínea cuando se hace clic en ella.
     */
    private fun changeColor(polyline: Polyline) {
        val color = (0..3).random()
        val colorId = when(color) {
            0 -> R.color.red
            1 -> R.color.yellow
            2 -> R.color.green
            3 -> R.color.blue
            else -> R.color.red
        }
        polyline.color = ContextCompat.getColor(this, colorId)
    }

    /**
     * Método para verificar si se ha concedido el permiso de ubicación.
     */
    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Método para habilitar la ubicación en el mapa.
     */
    private fun enableLocation() {
        if (!::map.isInitialized) return
        if (isLocationPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            map.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
    }

    /**
     * Método para solicitar el permiso de ubicación al usuario.
     */
    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_LONG).show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_LOCATION)
        }
    }

    /**
     * Método para manejar la respuesta del usuario al solicitar permisos.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestCode) {
            if (grantResults.isNotEmpty()) {
                when (requestCode) {
                    REQUEST_CODE_LOCATION -> {
                        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            map.isMyLocationEnabled = true
                        } else {
                            Toast.makeText(this, "Para activar la localización ve a ajustes y acepta los permisos", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            Toast.makeText(this, "Permiso denegado. La función de contador de pasos no funcionará.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Método llamado al reanudar las fragmentos de la actividad.
     */
    override fun onResumeFragments() {
        super.onResumeFragments()
        if (!::map.isInitialized) return
        if (!isLocationPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {}
            map.isMyLocationEnabled = false
            Toast.makeText(this, "Para activar la localización ve a ajustes y acepta los permisos", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Método llamado cuando se hace clic en el botón de ubicación del mapa.
     */
    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "Botón pulsado", Toast.LENGTH_SHORT).show()
        return false
    }

    /**
     * Método llamado cuando se hace clic en la ubicación actual en el mapa.
     */
    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(this, "Estás en ${p0.latitude}, ${p0.longitude}", Toast.LENGTH_SHORT).show()
    }

    /**
     * Método para iniciar el cronómetro.
     */
    private fun startChronometer() {
        chronometer.base = SystemClock.elapsedRealtime() - elapsedMillis
        chronometer.start()
        isChronometerRunning = true
    }

    /**
     * Método para detener el cronómetro.
     */
    private fun stopChronometer() {
        chronometer.stop()
        elapsedMillis = SystemClock.elapsedRealtime() - chronometer.base
        isChronometerRunning = false

        val elapsedSeconds = elapsedMillis / 1000

        // Realizar alguna operación con el valor guardado
    }

    /**
     * Método llamado cuando se produce un cambio en el sensor de conteo de pasos.
     */
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val steps = event.values[0].toInt()
            stepCountTextView.text = steps.toString()

            if (initialStepCount == 0) {
                initialStepCount = steps
            }
        }
    }

    /**
     * Método llamado cuando cambia la precisión de un sensor (no se utiliza en este caso).
     */
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // No se utiliza
    }

    /**
     * Método para solicitar permiso de reconocimiento de actividad al usuario.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestSensorPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                permissionRequestCode
            )
        }
    }

    /**
     * Método para calcular la distancia.
     */
    private fun calculateDistance(steps: Int): Double {
        // Suponiendo una longitud de paso promedio de 0.60 metros
        val stepLength = 0.56
        return steps * stepLength / 1000 // Convertir los pasos a kilómetros
    }

    /**
     * Método para dar formato a la distancia.
     */
    private fun formatDistance(distance: Double): Double {
        val decimalFormat = DecimalFormat("#.###")
        return decimalFormat.format(distance).toDouble()
    }

    /**
     * Método para dar formato al tiempo transcurrido.
     */
    private fun formatElapsedTime(time: Long): String {
        val hours = (time / (1000 * 60 * 60)) % 24
        val minutes = (time / (1000 * 60)) % 60
        val seconds = (time / 1000) % 60

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    /**
     * Método para dar formato al tiempo.
     */
    @SuppressLint("SimpleDateFormat")
    private fun formatTime(milliseconds: Double): String {
        val sdf = SimpleDateFormat("HH:mm:ss")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val time = Date(milliseconds.toLong())
        return sdf.format(time)
    }

    /**
     * Método para iniciar el bucle de actualización de ubicación.
     */
    private fun startLoop() {
        isRunning = true

        handler.postDelayed(object : Runnable {
            override fun run() {
                // Aquí se realizan las operaciones o acciones deseadas en el bucle

                startLocationUpdates()
                // Test
                if (GlobalVariables.latitude == 0.0 || GlobalVariables.latitude != GlobalVariables.oldlatitude) {
                    GlobalVariables.oldlatitude = GlobalVariables.latitude
                    GlobalVariables.oldlongitude = GlobalVariables.longitude

                    if (GlobalVariables.latitude != 0.0) {
                        latitudeArray.add(GlobalVariables.latitude)
                        longitudeArray.add(GlobalVariables.longitude)
                    }
                }
                if (isRunning) {
                    handler.postDelayed(this, 500)// Repite el bucle después de 500 milisegundos
                }
            }
        }, 1000) // Inicia el bucle después de 1 segundo
    }

    /**
     * Método para detener el bucle de actualización de ubicación.
     */
    private fun stopLoop() {
        isRunning = false
    }

    /**
     * Método para iniciar las actualizaciones de ubicación.
     */
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
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
                }
            }
    }

    private fun clearGlobalVariables() {
        GlobalVariables.selectedRouteId = ""
        GlobalVariables.selectedPlace = ""
        GlobalVariables.selectedSteps = 0
        GlobalVariables.selectedKm = 0.0
        GlobalVariables.selectedTime = 0.0
        GlobalVariables.selectedUser = ""
        GlobalVariables.selectedBestUser = ""
    }

    /**
     * Método llamado cuando se presiona el botón de retroceso.
     * Obliga a la clase a volver a la clase MainActivity.
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}