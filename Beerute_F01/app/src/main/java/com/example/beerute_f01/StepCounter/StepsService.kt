package com.example.beerute_f01.StepCounter

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder

class StepsService : Service(), SensorEventListener {
    var mSensorManager: SensorManager? = null
    var mStepDetectorSensor: Sensor? = null
    var mStepsDBHelper: StepsDBHelper? = null
    override fun onCreate() {
        super.onCreate()
        mSensorManager = this.getSystemService(SENSOR_SERVICE) as SensorManager
        if (mSensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            mStepDetectorSensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
            mSensorManager!!.registerListener(
                this,
                mStepDetectorSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            mStepsDBHelper = StepsDBHelper(applicationContext)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent) {
        mStepsDBHelper!!.createStepsEntry()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}