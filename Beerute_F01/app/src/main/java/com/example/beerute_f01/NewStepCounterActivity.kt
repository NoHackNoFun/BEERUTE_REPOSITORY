package com.example.beerute_f01

import android.Manifest
import android.annotation.SuppressLint
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

class NewStepCounterActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var isSensorPresent = false
    private lateinit var recordRouteButton: Button
    private lateinit var restartCounterButton: Button
    private lateinit var stepsCountTextView: TextView
    private lateinit var distanceTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var chronometer: Chronometer

    private var totalStepCount = 0
    private var distance = 0.0
    private var startTime: Long = 0
    private var elapsedTime: Long = 0

    private val permissionRequestCode = 123

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_step_counter)

        // Initialize views
        recordRouteButton = findViewById(R.id.recordRouteButton)
        restartCounterButton = findViewById(R.id.restartCounterButton)
        stepsCountTextView = findViewById(R.id.stepsCountTextView)
        distanceTextView = findViewById(R.id.distanceTextView)
        timeTextView = findViewById(R.id.timeTextView)
        chronometer = findViewById(R.id.chronometer)

        // Request permission for step counter sensor
        requestStepCounterPermission()

        // Set up button click listeners
        recordRouteButton.setOnClickListener {
            if (isSensorPresent) {
                if (recordRouteButton.text == getString(R.string.start_record)) {
                    // Start recording
                    startRecording()
                } else {
                    // Stop recording
                    stopRecording()
                }
            } else {
                Toast.makeText(this, "Step counter sensor is not available on this device.", Toast.LENGTH_SHORT).show()
            }
        }

        restartCounterButton.setOnClickListener {
            restartCounter()
        }
    }

    override fun onResume() {
        super.onResume()
        // Register sensor listener
        if (isSensorPresent) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        // Unregister sensor listener
        if (isSensorPresent) {
            sensorManager.unregisterListener(this)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            // Update step count
            val stepsSinceReboot = event.values[0].toInt()
            val steps = stepsSinceReboot - totalStepCount
            totalStepCount = stepsSinceReboot
            stepsCountTextView.text = steps.toString()

            // Calculate distance
            distance = calculateDistance(steps)
            distanceTextView.text = String.format("%.2f", distance)

            // Calculate elapsed time
            elapsedTime = SystemClock.elapsedRealtime() - startTime
            timeTextView.text = formatElapsedTime(elapsedTime)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not used
    }

    private fun requestStepCounterPermission() {
        // Check if permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                permissionRequestCode
            )
        } else {
            // Permission is already granted
            initializeStepCounter()
        }
    }

    private fun initializeStepCounter() {
        // Initialize sensor manager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Check if step counter sensor is available
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor != null) {
            isSensorPresent = true
        } else {
            isSensorPresent = false
        }
    }

    private fun startRecording() {
        // Reset values
        totalStepCount = 0
        distance = 0.0
        startTime = SystemClock.elapsedRealtime()

        // Update UI
        recordRouteButton.text = getString(R.string.stop_record)
        stepsCountTextView.text = "0"
        distanceTextView.text = "0.00"
        timeTextView.text = "00:00:00"
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()
    }

    private fun stopRecording() {
        // Stop recording
        chronometer.stop()

        // Update UI
        recordRouteButton.text = getString(R.string.start_record)

        // Save recorded data to global variables or perform any desired action
        val steps = totalStepCount
        val recordedDistance = distance
        val recordedTime = elapsedTime

        // Print recorded data
        println("Steps: $steps")
        println("Distance: $recordedDistance km")
        println("Time: ${formatElapsedTime(recordedTime)}")
    }

    private fun restartCounter() {
        // Reset values
        totalStepCount = 0
        distance = 0.0
        elapsedTime = 0
        startTime = SystemClock.elapsedRealtime()

        // Update UI
        stepsCountTextView.text = "0"
        distanceTextView.text = "0.00"
        timeTextView.text = "00:00:00"
        chronometer.base = SystemClock.elapsedRealtime()
    }

    private fun calculateDistance(steps: Int): Double {
        // Assuming average step length of 0.75 meters
        val stepLength = 0.75
        return steps * stepLength / 1000 // Convert to kilometers
    }

    private fun formatElapsedTime(time: Long): String {
        val seconds = (time / 1000).toInt()
        val minutes = seconds / 60
        val hours = minutes / 60

        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
    }
}

/*import android.Manifest
import android.annotation.SuppressLint
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

class NewStepCounterActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var isSensorPresent = false
    private lateinit var recordRouteButton: Button
    private lateinit var restartCounterButton: Button
    private lateinit var stepsCountTextView: TextView
    private lateinit var distanceTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var chronometer: Chronometer

    private var stepCount = 0
    private var distance = 0.0
    private var startTime: Long = 0
    private var elapsedTime: Long = 0

    private val permissionRequestCode = 123

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_step_counter)

        // Initialize views
        recordRouteButton = findViewById(R.id.recordRouteButton)
        restartCounterButton = findViewById(R.id.restartCounterButton)
        stepsCountTextView = findViewById(R.id.stepsCountTextView)
        distanceTextView = findViewById(R.id.distanceTextView)
        timeTextView = findViewById(R.id.timeTextView)
        chronometer = findViewById(R.id.chronometer)

        // Request permission for step counter sensor
        requestStepCounterPermission()

        // Set up button click listeners
        recordRouteButton.setOnClickListener {
            if (isSensorPresent) {
                if (recordRouteButton.text == getString(R.string.start_record)) {
                    // Start recording
                    startRecording()
                } else {
                    // Stop recording
                    stopRecording()
                }
            } else {
                Toast.makeText(this, "Step counter sensor is not available on this device.", Toast.LENGTH_SHORT).show()
            }
        }

        restartCounterButton.setOnClickListener {
            restartCounter()
        }
    }

    override fun onResume() {
        super.onResume()
        // Register sensor listener
        if (isSensorPresent) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        // Unregister sensor listener
        if (isSensorPresent) {
            sensorManager.unregisterListener(this)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            // Update step count
            stepCount = event.values[0].toInt()
            stepsCountTextView.text = stepCount.toString()

            // Calculate distance
            distance = calculateDistance(stepCount)
            distanceTextView.text = String.format("%.2f", distance)

            // Calculate elapsed time
            elapsedTime = SystemClock.elapsedRealtime() - startTime
            timeTextView.text = formatElapsedTime(elapsedTime)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not used
    }

    private fun requestStepCounterPermission() {
        // Check if permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                permissionRequestCode
            )
        } else {
            // Permission is already granted
            initializeStepCounter()
        }
    }

    private fun initializeStepCounter() {
        // Initialize sensor manager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Check if step counter sensor is available
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor != null) {
            isSensorPresent = true
        } else {
            isSensorPresent = false
        }
    }

    private fun startRecording() {
        // Reset values
        stepCount = 0
        distance = 0.0
        startTime = SystemClock.elapsedRealtime()

        // Update UI
        recordRouteButton.text = getString(R.string.stop_record)
        stepsCountTextView.text = "0"
        distanceTextView.text = "0.00"
        timeTextView.text = "00:00:00"
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()
    }

    private fun stopRecording() {
        // Stop recording
        chronometer.stop()

        // Update UI
        recordRouteButton.text = getString(R.string.start_record)

        // Save recorded data to global variables or perform any desired action
        val steps = stepCount
        val recordedDistance = distance
        val recordedTime = elapsedTime

        // Print recorded data
        println("Steps: $steps")
        println("Distance: $recordedDistance km")
        println("Time: ${formatElapsedTime(recordedTime)}")
    }

    private fun restartCounter() {
        // Reset values
        stepCount = 0
        distance = 0.0
        elapsedTime = 0

        // Update UI
        stepsCountTextView.text = "0"
        distanceTextView.text = "0.00"
        timeTextView.text = "00:00:00"
        chronometer.base = SystemClock.elapsedRealtime()
    }

    private fun calculateDistance(steps: Int): Double {
        // Assuming average step length of 0.75 meters
        val stepLength = 0.75
        return steps * stepLength / 1000 // Convert to kilometers
    }

    private fun formatElapsedTime(time: Long): String {
        val seconds = (time / 1000).toInt()
        val minutes = seconds / 60
        val hours = minutes / 60

        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
    }
}

import android.Manifest
import android.annotation.SuppressLint
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

class NewStepCounterActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var isSensorPresent = false
    private lateinit var recordRouteButton: Button
    private lateinit var stepsCountTextView: TextView
    private lateinit var distanceTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var chronometer: Chronometer

    private var stepCount = 0
    private var distance = 0.0
    private var startTime: Long = 0
    private var elapsedTime: Long = 0

    private val permissionRequestCode = 123

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_step_counter)

        // Initialize views
        recordRouteButton = findViewById(R.id.recordRouteButton)
        stepsCountTextView = findViewById(R.id.stepsCountTextView)
        distanceTextView = findViewById(R.id.distanceTextView)
        timeTextView = findViewById(R.id.timeTextView)
        chronometer = findViewById(R.id.chronometer)

        // Request permission for step counter sensor
        requestStepCounterPermission()

        // Set up button click listener
        recordRouteButton.setOnClickListener {
            if (isSensorPresent) {
                if (recordRouteButton.text == getString(R.string.start_record)) {
                    // Start recording
                    startRecording()
                } else {
                    // Stop recording
                    stopRecording()
                }
            } else {
                Toast.makeText(this, "Step counter sensor is not available on this device.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Register sensor listener
        if (isSensorPresent) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        // Unregister sensor listener
        if (isSensorPresent) {
            sensorManager.unregisterListener(this)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            // Update step count
            stepCount = event.values[0].toInt()
            stepsCountTextView.text = stepCount.toString()

            // Calculate distance
            distance = calculateDistance(stepCount)
            distanceTextView.text = String.format("%.2f", distance)

            // Calculate elapsed time
            elapsedTime = SystemClock.elapsedRealtime() - startTime
            timeTextView.text = formatElapsedTime(elapsedTime)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not used
    }

    private fun requestStepCounterPermission() {
        // Check if permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                permissionRequestCode
            )
        } else {
            // Permission is already granted
            initializeStepCounter()
        }
    }

    private fun initializeStepCounter() {
        // Initialize sensor manager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Check if step counter sensor is available
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor != null) {
            isSensorPresent = true
        } else {
            isSensorPresent = false
        }
    }

    private fun startRecording() {
        // Reset values
        stepCount = 0
        distance = 0.0
        startTime = SystemClock.elapsedRealtime()

        // Update UI
        recordRouteButton.text = getString(R.string.stop_record)
        stepsCountTextView.text = "0"
        distanceTextView.text = "0.00"
        timeTextView.text = "00:00:00"
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()
    }

    private fun stopRecording() {
        // Stop recording
        chronometer.stop()

        // Update UI
        recordRouteButton.text = getString(R.string.start_record)

        // Save recorded data to global variables or perform any desired action
        val steps = stepCount
        val recordedDistance = distance
        val recordedTime = elapsedTime

        // Print recorded data
        println("Steps: $steps")
        println("Distance: $recordedDistance km")
        println("Time: ${formatElapsedTime(recordedTime)}")
    }

    private fun calculateDistance(steps: Int): Double {
        // Assuming average step length of 0.75 meters
        val stepLength = 0.75
        return steps * stepLength / 1000 // Convert to kilometers
    }

    private fun formatElapsedTime(time: Long): String {
        val seconds = (time / 1000).toInt()
        val minutes = seconds / 60
        val hours = minutes / 60

        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
    }
}*/