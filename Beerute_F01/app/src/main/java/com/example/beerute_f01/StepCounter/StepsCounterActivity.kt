package com.example.beerute_f01.StepCounter

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.beerute_f01.R

class StepsCounterActivity : AppCompatActivity(), SensorEventListener {
    private var mSensorManager: SensorManager? = null
    private var mSensor: Sensor? = null
    private var isSensorPresent = false
    private var mStepsSinceReboot: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stepcounter_layout)
        mStepsSinceReboot = findViewById<View>(R.id.stepssincereboot) as TextView
        mSensorManager = this.getSystemService(SENSOR_SERVICE) as SensorManager
        if (mSensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            mSensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            isSensorPresent = true
        } else {
            isSensorPresent = false
        }
    }

    override fun onResume() {
        super.onResume()
        if (isSensorPresent) {
            mSensorManager!!.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        if (isSensorPresent) {
            mSensorManager!!.unregisterListener(this)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        mStepsSinceReboot!!.text = "Steps since reboot:" + event.values[0].toString()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    override fun onDestroy() {
        super.onDestroy()
        mSensorManager = null
        mSensor = null
    }
}