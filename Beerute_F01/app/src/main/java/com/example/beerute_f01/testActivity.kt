package com.example.beerute_f01

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.DecimalFormat

class testActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        stepCountTextView = findViewById(R.id.stepCountTextView)
        differenceTextView = findViewById(R.id.differenceTextView)
        distanceTextView = findViewById(R.id.distanceTextView)
        playButton = findViewById(R.id.playButton)
        timeTextView = findViewById(R.id.timeTextView)
        chronometer = findViewById(R.id.chronometer)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            Toast.makeText(this, "Step counter sensor is not available on this device.", Toast.LENGTH_SHORT).show()
        } else {
            requestSensorPermission()
        }

        var isChronometerRunning = false

        playButton.setOnClickListener {
            val currentStepCount = stepCountTextView.text.toString().toInt()
            val difference = currentStepCount - initialStepCount
            differenceTextView.text = "Steps: $difference"

            val distance = calculateDistance(difference)
            distanceTextView.text = "Distance: ${formatDistance(distance)} km"

            if (isChronometerRunning) {
                chronometer.stop()
                elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
                timeTextView.text = formatElapsedTime(elapsedTime)
                playButton.text = "Play"
            } else {

                chronometer.base = SystemClock.elapsedRealtime()

                chronometer.start()
                chronometer.format = "%s"

                timeTextView.text = formatElapsedTime(0)
                playButton.text = "Stop"
                differenceTextView.text = "Steps: 0"
                distanceTextView.text = "Distance: 0 km"
                elapsedTime = 0
            }

            isChronometerRunning = !isChronometerRunning
        }
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

    override fun onRequestPermissionsResult(
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
    }

    private fun calculateDistance(steps: Int): Double {
        // Assuming average step length of 0.75 meters
        val stepLength = 0.75
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

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}