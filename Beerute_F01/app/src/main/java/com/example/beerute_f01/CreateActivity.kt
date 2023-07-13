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
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat

class CreateActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener,
    SensorEventListener {

    private lateinit var createRouteButton: Button
    private lateinit var exploreRouteButton: Button
    private lateinit var clearRouteButton: Button

    private lateinit var map: GoogleMap
    private val ruteObject = RuteObject

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    private lateinit var linearStepCounter: LinearLayout
    private lateinit var stepCountTextView: TextView
    private lateinit var differenceTextView: TextView
    private lateinit var distanceTextView: TextView
    private lateinit var playButton: Button
    private lateinit var readyButton: Button
    private lateinit var savePlaceEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var timeTextView: TextView
    private lateinit var chronometer: Chronometer

    private val permissionRequestCode = 123
    private var initialStepCount = 0
    private var isChronometerRunning = false
    private var elapsedTime: Long = 0

    private var elapsedMillis: Long = 0

    companion object {
        const val REQUEST_CODE_LOCATION = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)
        createMapFragment()
        setupButtons()
    }

    @SuppressLint("ResourceAsColor")
    private fun setupButtons() {
        createRouteButton = findViewById(R.id.createRouteButton)
        exploreRouteButton = findViewById(R.id.exploreRouteButton)
        clearRouteButton = findViewById(R.id.clearRouteButton)

        linearStepCounter = findViewById(R.id.linearStepCounter)
        stepCountTextView = findViewById(R.id.stepCountTextView)
        differenceTextView = findViewById(R.id.differenceTextView)
        distanceTextView = findViewById(R.id.distanceTextView)
        playButton = findViewById(R.id.playButton)
        readyButton = findViewById(R.id.readyButton)
        savePlaceEditText = findViewById(R.id.savePlaceEditText)
        saveButton = findViewById(R.id.saveButton)
        timeTextView = findViewById(R.id.timeTextView)
        chronometer = findViewById(R.id.chronometer)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            Toast.makeText(this, "El sensor de contador de pasos no está disponible en este dispositivo.", Toast.LENGTH_SHORT).show()
        } else {
            requestSensorPermission()
        }

        createRouteButton.setOnClickListener {
            if (linearStepCounter.visibility == View.VISIBLE) {
                // El LinearLayout está visible, lo ocultamos y cambiamos el texto del botón
                linearStepCounter.visibility = View.GONE
                readyButton.visibility = View.GONE
                savePlaceEditText.visibility = View.GONE
                saveButton.visibility = View.GONE
                createRouteButton.text = "Grabar Ruta"
            } else {
                // El LinearLayout está oculto, lo mostramos y cambiamos el texto del botón
                linearStepCounter.visibility = View.VISIBLE
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
            val intent = Intent(this, CreateActivity::class.java)
            startActivity(intent)
        }

        playButton.setOnClickListener {
            val currentStepCount = stepCountTextView.text.toString().toInt()
            val difference = currentStepCount - initialStepCount
            differenceTextView.text = "Pasos: $difference"

            val distance = calculateDistance(difference)
            distanceTextView.text = "   Distancia: ${formatDistance(distance)} km   "

            if (isChronometerRunning) {
                chronometer.stop()
                elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
                timeTextView.text = "      Tiempo: " + formatElapsedTime(elapsedTime) + "      "
                playButton.text = "Play"

                // Aparece el botón "listo"
                readyButton.visibility = View.VISIBLE

                // Guarda los valores en las variables globales
                GlobalVariables.selectedSteps = difference
                GlobalVariables.selectedKm = distance
                GlobalVariables.selectedTime = elapsedTime.toDouble()

            } else {
                chronometer.base = SystemClock.elapsedRealtime() - elapsedTime
                chronometer.start()
                chronometer.format = "%s"
                timeTextView.text = "      Tiempo: " + formatElapsedTime(elapsedTime) + "      "
                playButton.text = "Stop"

                // Desaparece el botón "listo"
                readyButton.visibility = View.GONE
                savePlaceEditText.visibility = View.GONE
                saveButton.visibility = View.GONE
            }

            isChronometerRunning = !isChronometerRunning
        }

        // Botón Listo (guarda los datos en variables globales) y hace aparecer al textview y al botón guardar
        readyButton.setOnClickListener {
            savePlaceEditText.visibility = View.VISIBLE
            saveButton.visibility = View.VISIBLE
        }

        // Al poner el lugar (debe contener algo) le das al botón guardar y guarda las variables globales en firebase
        saveButton.setOnClickListener {
            val place = savePlaceEditText.text.toString()
            if (place.isNotEmpty()) {
                GlobalVariables.selectedPlace = place

                val routeData = hashMapOf(
                    "bestuser" to GlobalVariables.selectedUser,
                    "km" to GlobalVariables.selectedKm,
                    "place" to GlobalVariables.selectedPlace,
                    "steps" to GlobalVariables.selectedSteps,
                    "time" to GlobalVariables.selectedTime,
                    "user" to GlobalVariables.selectedUser
                )

                val firestore = FirebaseFirestore.getInstance()
                val routeRef = firestore.collection("routes").document("0000003")
                routeRef.set(routeData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Datos guardados en Firebase", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al guardar los datos en Firebase", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Debes ingresar un lugar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createMapFragment() {
        val mapFragment : SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        createPolylines()
        map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationClickListener(this)
        enableLocation()
    }

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
            //error("La longitud del Array no puede ser nula")
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

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

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
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            map.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_LONG).show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_LOCATION)
        }
    }

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
                    // Agrega otros casos de acuerdo a tus necesidades
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        // Lógica adicional que necesites realizar después de obtener los permisos

        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            Toast.makeText(this, "Permiso denegado. La función de contador de pasos no funcionará.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (!::map.isInitialized) return
        if (!isLocationPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {}
            map.isMyLocationEnabled = false
            Toast.makeText(this, "Para activar la localización ve a ajustes y acepta los permisos", Toast.LENGTH_LONG).show()
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "Botón pulsado", Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(this, "Estás en ${p0.latitude}, ${p0.longitude}", Toast.LENGTH_SHORT).show()
    }

    private fun startChronometer() {
        chronometer.base = SystemClock.elapsedRealtime() - elapsedMillis
        chronometer.start()
        playButton.text = "Stop"
        isChronometerRunning = true
    }

    private fun stopChronometer() {
        elapsedMillis = SystemClock.elapsedRealtime() - chronometer.base
        chronometer.stop()
        playButton.text = "Play"
        isChronometerRunning = false
        // Guardar el valor en una variable global
        val elapsedSeconds = elapsedMillis / 1000
        // Hacer algo con el valor guardado
    }

    // testActivity

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

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val steps = event.values[0].toInt()
            stepCountTextView.text = steps.toString()

            if (initialStepCount == 0) {
                initialStepCount = steps
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not used
    }

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

    private fun calculateDistance(steps: Int): Double {
        // Assuming average step length of 0.60 meters
        val stepLength = 0.56
        return steps * stepLength / 1000 // Convert to kilometers
    }

    private fun formatDistance(distance: Double): String {
        val decimalFormat = DecimalFormat("#.###")
        return decimalFormat.format(distance)
    }

    private fun formatElapsedTime(time: Long): String {
        val hours = (time / (1000 * 60 * 60)) % 24
        val minutes = (time / (1000 * 60)) % 60
        val seconds = (time / 1000) % 60

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}

/*import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import java.text.DecimalFormat

class CreateActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener,
    SensorEventListener {

    private lateinit var createRouteButton: Button
    private lateinit var exploreRouteButton: Button
    private lateinit var clearRouteButton: Button

    private lateinit var map: GoogleMap
    private val ruteObject = RuteObject

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    private lateinit var linearStepCounter: LinearLayout
    private lateinit var stepCountTextView: TextView
    private lateinit var differenceTextView: TextView
    private lateinit var distanceTextView: TextView
    private lateinit var playButton: Button
    private lateinit var timeTextView: TextView
    private lateinit var chronometer: Chronometer

    private val permissionRequestCode = 123
    private var initialStepCount = 0
    private var isChronometerRunning = false
    private var elapsedTime: Long = 0

    private var elapsedMillis: Long = 0

    companion object {
        const val REQUEST_CODE_LOCATION = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)
        createMapFragment()
        setupButtons()
    }

    private fun setupButtons() {
        createRouteButton = findViewById(R.id.createRouteButton)
        exploreRouteButton = findViewById(R.id.exploreRouteButton)
        clearRouteButton = findViewById(R.id.clearRouteButton)

        linearStepCounter = findViewById(R.id.linearStepCounter)
        stepCountTextView = findViewById(R.id.stepCountTextView)
        differenceTextView = findViewById(R.id.differenceTextView)
        distanceTextView = findViewById(R.id.distanceTextView)
        playButton = findViewById(R.id.playButton)
        timeTextView = findViewById(R.id.timeTextView)
        chronometer = findViewById(R.id.chronometer)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            Toast.makeText(this, "El sensor de contador de pasos no está disponible en este dispositivo.", Toast.LENGTH_SHORT).show()
        } else {
            requestSensorPermission()
        }

        createRouteButton.setOnClickListener {
            if (linearStepCounter.visibility == View.VISIBLE) {
                // El LinearLayout está visible, lo ocultamos y cambiamos el texto del botón
                linearStepCounter.visibility = View.GONE
                createRouteButton.text = "Grabar Ruta"
            } else {
                // El LinearLayout está oculto, lo mostramos y cambiamos el texto del botón
                linearStepCounter.visibility = View.VISIBLE
                createRouteButton.text = "Eliminar Ruta"
            }
        }

        exploreRouteButton.setOnClickListener {
            val intent = Intent(this, ExploreActivity::class.java)
            startActivity(intent)
        }

        clearRouteButton.setOnClickListener {
            ruteObject.clearMatrix(this)
            val intent = Intent(this, CreateActivity::class.java)
            startActivity(intent)
        }

        playButton.setOnClickListener {
            val currentStepCount = stepCountTextView.text.toString().toInt()
            val difference = currentStepCount - initialStepCount
            differenceTextView.text = "Pasos: $difference"

            val distance = calculateDistance(difference)
            distanceTextView.text = "   Distancia: ${formatDistance(distance)} km   "

            if (isChronometerRunning) {
                chronometer.stop()
                elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
                timeTextView.text = "      Tiempo: " + formatElapsedTime(elapsedTime) + "      "
                playButton.text = "Play"
            } else {
                chronometer.base = SystemClock.elapsedRealtime() - elapsedTime
                chronometer.start()
                chronometer.format = "%s"
                timeTextView.text = "      Tiempo: " + formatElapsedTime(elapsedTime) + "      "
                playButton.text = "Stop"
            }

            isChronometerRunning = !isChronometerRunning
        }
    }

    private fun createMapFragment() {
        val mapFragment : SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        createPolylines()
        map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationClickListener(this)
        enableLocation()
    }

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
            //error("La longitud del Array no puede ser nula")
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

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

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
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            map.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_LONG).show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_LOCATION)
        }
    }

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
                    // Agrega otros casos de acuerdo a tus necesidades
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        // Lógica adicional que necesites realizar después de obtener los permisos

        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            Toast.makeText(this, "Permiso denegado. La función de contador de pasos no funcionará.", Toast.LENGTH_SHORT).show()
        }
    }

    /*override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        when (requestCode) {
            REQUEST_CODE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                map.isMyLocationEnabled = true
            } else {
                Toast.makeText(this, "Para activar la localización ve a ajustes y acepta los permisos", Toast.LENGTH_LONG).show()
            }
        }
    }*/

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (!::map.isInitialized) return
        if (!isLocationPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {}
            map.isMyLocationEnabled = false
            Toast.makeText(this, "Para activar la localización ve a ajustes y acepta los permisos", Toast.LENGTH_LONG).show()
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "Botón pulsado", Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(this, "Estás en ${p0.latitude}, ${p0.longitude}", Toast.LENGTH_SHORT).show()
    }

    private fun startChronometer() {
        chronometer.base = SystemClock.elapsedRealtime() - elapsedMillis
        chronometer.start()
        playButton.text = "Stop"
        isChronometerRunning = true
    }

    private fun stopChronometer() {
        elapsedMillis = SystemClock.elapsedRealtime() - chronometer.base
        chronometer.stop()
        playButton.text = "Play"
        isChronometerRunning = false
        // Guardar el valor en una variable global
        val elapsedSeconds = elapsedMillis / 1000
        // Hacer algo con el valor guardado
    }

    //testActivity

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

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val steps = event.values[0].toInt()
            stepCountTextView.text = steps.toString()

            if (initialStepCount == 0) {
                initialStepCount = steps
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not used
    }

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

    /*override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                // Register sensor listener
                if (stepSensor != null) {
                    sensorManager.registerListener(
                        this,
                        stepSensor,
                        SensorManager.SENSOR_DELAY_NORMAL
                    )
                }
            } else {
                // Permission denied
                Toast.makeText(
                    this,
                    "Permission denied. Step counter feature won't work.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }*/

    private fun calculateDistance(steps: Int): Double {
        // Assuming average step length of 0.60 meters
        val stepLength = 0.60
        return steps * stepLength / 1000 // Convert to kilometers
    }

    private fun formatDistance(distance: Double): String {
        val decimalFormat = DecimalFormat("#.###")
        return decimalFormat.format(distance)
    }

    private fun formatElapsedTime(time: Long): String {
        val hours = (time / (1000 * 60 * 60)) % 24
        val minutes = (time / (1000 * 60)) % 60
        val seconds = (time / 1000) % 60

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}*/

/*import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

class CreateActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private lateinit var createRouteButton: Button
    private lateinit var exploreRouteButton: Button
    private lateinit var clearRouteButton: Button

    private lateinit var map: GoogleMap
    private val ruteObject = RuteObject

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    private lateinit var linearStepCounter: LinearLayout
    private lateinit var stepCountTextView: TextView
    private lateinit var differenceTextView: TextView
    private lateinit var distanceTextView: TextView
    private lateinit var playButton: Button
    private lateinit var timeTextView: TextView
    private lateinit var chronometer: Chronometer

    private val permissionRequestCode = 123
    private var initialStepCount = 0
    private var isChronometerRunning = false
    private var elapsedTime: Long = 0

    private var elapsedMillis: Long = 0

    companion object {
        const val REQUEST_CODE_LOCATION = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)
        createMapFragment()
        setupButtons()
    }

    private fun setupButtons() {
        createRouteButton = findViewById(R.id.createRouteButton)
        exploreRouteButton = findViewById(R.id.exploreRouteButton)
        clearRouteButton = findViewById(R.id.clearRouteButton)

        linearStepCounter = findViewById(R.id.linearStepCounter)

        chronometer = findViewById(R.id.chronometer)
        playButton = findViewById(R.id.playButton)

        createRouteButton.setOnClickListener {
            if (linearStepCounter.visibility == View.VISIBLE) {
                // El LinearLayout está visible, lo ocultamos y cambiamos el texto del botón
                linearStepCounter.visibility = View.GONE
                createRouteButton.text = "Grabar Ruta"
            } else {
                // El LinearLayout está oculto, lo mostramos y cambiamos el texto del botón
                linearStepCounter.visibility = View.VISIBLE
                createRouteButton.text = "Eliminar Ruta"
            }
        }

        exploreRouteButton.setOnClickListener {
            val intent = Intent(this, ExploreActivity::class.java)
            startActivity(intent)
        }

        clearRouteButton.setOnClickListener {
            ruteObject.clearMatrix(this)
            val intent = Intent(this, CreateActivity::class.java)
            startActivity(intent)
        }

        playButton.setOnClickListener {
            if (isChronometerRunning) {
                stopChronometer()
            } else {
                startChronometer()
            }
        }
    }

    private fun createMapFragment() {
        val mapFragment : SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        createPolylines()
        map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationClickListener(this)
        enableLocation()
    }

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
            //error("La longitud del Array no puede ser nula")
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

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

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
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            map.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_LONG).show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        when (requestCode) {
            REQUEST_CODE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                map.isMyLocationEnabled = true
            } else {
                Toast.makeText(this, "Para activar la localización ve a ajustes y acepta los permisos", Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (!::map.isInitialized) return
        if (!isLocationPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {}
            map.isMyLocationEnabled = false
            Toast.makeText(this, "Para activar la localización ve a ajustes y acepta los permisos", Toast.LENGTH_LONG).show()
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "Botón pulsado", Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(this, "Estás en ${p0.latitude}, ${p0.longitude}", Toast.LENGTH_SHORT).show()
    }

    private fun startChronometer() {
        chronometer.base = SystemClock.elapsedRealtime() - elapsedMillis
        chronometer.start()
        playButton.text = "Stop"
        isChronometerRunning = true
    }

    private fun stopChronometer() {
        elapsedMillis = SystemClock.elapsedRealtime() - chronometer.base
        chronometer.stop()
        playButton.text = "Play"
        isChronometerRunning = false
        // Guardar el valor en una variable global
        val elapsedSeconds = elapsedMillis / 1000
        // Hacer algo con el valor guardado
    }
}*/

/*import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

class CreateActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private val ro = RuteObject

    lateinit var createRouteButton: Button
    lateinit var createdRouteButton: Button
    lateinit var clearRouteButton: Button

    private lateinit var map:GoogleMap

    companion object {
        const val REQUEST_CODE_LOCATION = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)
        createMapFragment()

        //Setup
        setup()
    }

    private fun setup() {

        createRouteButton = findViewById(R.id.createRouteButton)
        createdRouteButton = findViewById(R.id.createdRouteButton)
        clearRouteButton = findViewById(R.id.clearRouteButton)

        createRouteButton.setOnClickListener {
        }

        createdRouteButton.setOnClickListener {

            val cIntent = Intent(this, CreatedActivity::class.java)
            startActivity(cIntent)
        }

        clearRouteButton.setOnClickListener {

            RuteObject.clearMatrix(this)

            val clIntent = Intent(this, CreateActivity::class.java)
            startActivity(clIntent)
        }
    }

    private fun createMapFragment() {
        val mapFragment : SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        //createMarker()
        createPolylines()
        map.setOnMyLocationButtonClickListener (this)
        map.setOnMyLocationClickListener (this)
        enableLocation()
    }

    fun createPolylines(){

        val sm = ro.getMatrix(this)

        // Primera Coordenada
        val fc = LatLng(sm?.get(0)?.get(0)?: 0.0, sm?.get(1)?.get(0)?: 0.0)

        val l = sm?.get(0)?.size

        val polylineOptions = PolylineOptions()

        if (l != null) {
            // Recorrer la matriz de latitudes y longitudes
            for (i in 0 until l) {
                val latLng = LatLng(sm?.get(0)?.get(i) ?: 0.0, sm?.get(1)?.get(i) ?: 0.0)
                polylineOptions.add(latLng)
            }
        } else
            // Condición no cumplida, se lanza un mensaje de error
            error("La longitud del Array no puede ser nula")

        polylineOptions.width(15f).color(ContextCompat.getColor(this, R.color.kotlin))

        // Crear una polilínea en el mapa con las opciones configuradas
        val polyline = map.addPolyline(polylineOptions)

        //polyline.startCap = RoundCap()
        //polyline.endCap = CustomCap(BitmapDescriptorFactory.fromResource(R.drawable.red_point))

        val pattern = listOf(
            Dot(), Gap(10F), Dash(50F), Gap(10F)
        )
        polyline.pattern = pattern

        polyline.isClickable = true
        map.setOnPolylineClickListener { polyline -> changeColor(polyline) }

        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(fc, 17f),
            3000,
            null
        )
    }

    fun changeColor(polyline: Polyline){
        val color = (0..3).random()
        when(color){
            0 -> polyline.color = ContextCompat.getColor(this, R.color.red)
            1 -> polyline.color = ContextCompat.getColor(this, R.color.yellow)
            2 -> polyline.color = ContextCompat.getColor(this, R.color.green)
            3 -> polyline.color = ContextCompat.getColor(this, R.color.blue)
        }
    }

    private fun createMarker() {
        val coordinates = LatLng(17.63088777893707, -101.54671998557623)
        val marker : MarkerOptions = MarkerOptions().position(coordinates).title("Zihuatanejo")
        map.addMarker(marker)
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(coordinates, 18f),
            4000,
            null
        )
    }

    private fun isLocationPermissionGranted (): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun enableLocation (){
        if(!::map.isInitialized) return
        if(isLocationPermissionGranted()){
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
        } else{
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission (){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
            Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_LONG).show()
        } else{
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
        when (requestCode) {
            REQUEST_CODE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                map.isMyLocationEnabled = true
            } else {
                Toast.makeText(this, "Para activar la localización ve a ajustes y acepta los permisos", Toast.LENGTH_LONG).show()
            } else -> {}
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if(!::map.isInitialized) return
        if (!isLocationPermissionGranted()){
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {}
            map.isMyLocationEnabled = false
            Toast.makeText(this, "Para activar la localización ve a ajustes y acepta los permisos", Toast.LENGTH_LONG).show()
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "Botón pulsado", Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(this, "Estás en ${p0.latitude}, ${p0.longitude}", Toast.LENGTH_SHORT).show()
    }
}*/