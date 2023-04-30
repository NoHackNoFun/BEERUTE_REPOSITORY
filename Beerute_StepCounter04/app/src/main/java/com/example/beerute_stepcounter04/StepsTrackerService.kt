package com.example.beerute_stepcounter04

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.os.SystemClock
import java.util.Calendar
import java.util.Collections
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class StepsTrackerService : Service() {
    private var mSensorManager: SensorManager? = null
    private var mStepDetectorSensor: Sensor? = null
    private var mAccelerometerSensor: Sensor? = null
    private var mAccelerometerListener: AccelerometerListener? = null
    private var mStepDetectorListener: StepDetectorListener? = null
    private var mStepsTrackerDBHelper: StepsTrackerDBHelper? = null
    override fun onCreate() {
        super.onCreate()
        mSensorManager = this.getSystemService(SENSOR_SERVICE) as SensorManager
        if (mSensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            mStepDetectorSensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
            mStepDetectorListener = StepDetectorListener()
            mSensorManager!!.registerListener(
                mStepDetectorListener as SensorEventListener?,
                mStepDetectorSensor,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        }
        if (mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mAccelerometerSensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }
        mStepsTrackerDBHelper = StepsTrackerDBHelper(this)
    }

    var mScheduledExecutorService = Executors.newScheduledThreadPool(2)
    private var mScheduledUnregisterAccelerometerTask: ScheduledFuture<*>? = null
    private lateinit var mScheduledProcessDataTask: ScheduledFuture<*>
    private var mUnregisterAcceleromterTask: UnregisterAcceleromterTask? = null
    private lateinit var mProcessDataTask: ProcessDataTask
    private var isScheduleUnregistered = false
    private var isAccelerometerRegistered = false
    private var sessionId: String? = null

    internal inner class StepDetectorListener : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (!isAccelerometerRegistered && mAccelerometerSensor != null) {
                mAccelerometerListener = AccelerometerListener()
                mSensorManager!!.registerListener(
                    mAccelerometerListener,
                    mAccelerometerSensor,
                    SensorManager.SENSOR_DELAY_FASTEST
                )
                sessionId = Calendar.getInstance().time.toLocaleString()
                isAccelerometerRegistered = true
            }
            if (isScheduleUnregistered) {
                mScheduledUnregisterAccelerometerTask!!.cancel(true)
            }
            mUnregisterAcceleromterTask = UnregisterAcceleromterTask()
            mScheduledUnregisterAccelerometerTask = mScheduledExecutorService.schedule(
                mUnregisterAcceleromterTask,
                20000,
                TimeUnit.MILLISECONDS
            )
            isScheduleUnregistered = true
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    internal inner class UnregisterAcceleromterTask : Runnable {
        override fun run() {
            isAccelerometerRegistered = false
            mSensorManager!!.unregisterListener(mAccelerometerListener)
            isScheduleUnregistered = false
            mScheduledProcessDataTask.cancel(false)
        }
    }

    private var timeOffsetValue: Long = 0
    var mAccelerometerDataList = ArrayList<AccelerometerData>()
    var mRawDataList = ArrayList<AccelerometerData>()
    var mAboveThresholdValuesList = ArrayList<AccelerometerData>()
    var mHighestPeakList = ArrayList<AccelerometerData>()

    internal inner class AccelerometerListener : SensorEventListener {
        init {
            mProcessDataTask = ProcessDataTask()
            mScheduledProcessDataTask = mScheduledExecutorService.scheduleWithFixedDelay(
                mProcessDataTask,
                10000,
                10000,
                TimeUnit.MILLISECONDS
            )
        }

        override fun onSensorChanged(event: SensorEvent) {
            val mAccelerometerData = AccelerometerData()
            mAccelerometerData.x = event.values[0]
            mAccelerometerData.y = event.values[1]
            mAccelerometerData.z = event.values[2]
            mAccelerometerData.time = event.timestamp
            mAccelerometerDataList.add(mAccelerometerData)
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    internal inner class ProcessDataTask : Runnable {
        override fun run() {

            //Copy accelerometer data from main sensor array in separate array for processing
            mRawDataList.addAll(mAccelerometerDataList)
            mAccelerometerDataList.clear()

            //Calculating the magnitude (Square root of sum of squares of x, y, z) & converting time from nano seconds from boot time to epoc time
            timeOffsetValue = System.currentTimeMillis() - SystemClock.elapsedRealtime()
            val dataSize = mRawDataList.size
            for (i in 0 until dataSize) {
                mRawDataList[i].value = Math.sqrt(
                    Math.pow(mRawDataList[i].x.toDouble(), 2.0) + Math.pow(
                        mRawDataList[i].y.toDouble(), 2.0
                    ) + Math.pow(mRawDataList[i].z.toDouble(), 2.0)
                )
                mRawDataList[i].time = mRawDataList[i].time / 1000000L + timeOffsetValue
            }

            //Calculating the High Peaks
            findHighPeaks()
            //Remove high peaks close to each other which are within range of 0.4 seconds
            removeClosePeaks()
            //Find the type of step (Running, jogging, walking) & store in Database
            findStepTypeAndStoreInDB()
            mRawDataList.clear()
            mAboveThresholdValuesList.clear()
            mHighestPeakList.clear()
        }

        fun findHighPeaks() {
            //Calculating the High Peaks
            var isAboveMeanLastValueTrue = false
            val dataSize = mRawDataList.size
            for (i in 0 until dataSize) {
                isAboveMeanLastValueTrue = if (mRawDataList[i].value > WALKINGPEAK) {
                    mAboveThresholdValuesList.add(mRawDataList[i])
                    false
                } else {
                    if (!isAboveMeanLastValueTrue && mAboveThresholdValuesList.size > 0) {
                        Collections.sort(mAboveThresholdValuesList, DataSorter())
                        mHighestPeakList.add(mAboveThresholdValuesList[mAboveThresholdValuesList.size - 1])
                        mAboveThresholdValuesList.clear()
                    }
                    true
                }
            }
        }

        fun removeClosePeaks() {
            for (i in 0 until mHighestPeakList.size - 1) {
                if (mHighestPeakList[i].isRealPeak) {
                    if (mHighestPeakList[i + 1].time - mHighestPeakList[i].time < 400) {
                        if (mHighestPeakList[i + 1].value > mHighestPeakList[i].value) {
                            mHighestPeakList[i].isRealPeak = false
                        } else {
                            mHighestPeakList[i + 1].isRealPeak = false
                        }
                    }
                }
            }
        }

        fun findStepTypeAndStoreInDB() {
            val size = mHighestPeakList.size
            for (i in 0 until size) {
                if (mHighestPeakList[i].isRealPeak) {
                    if (mHighestPeakList[i].value > RUNNINGPEAK) {
                        mStepsTrackerDBHelper!!.createStepsEntry(
                            mHighestPeakList[i].time,
                            RUNNING,
                            sessionId
                        )
                    } else {
                        if (mHighestPeakList[i].value > JOGGINGPEAK) {
                            mStepsTrackerDBHelper!!.createStepsEntry(
                                mHighestPeakList[i].time,
                                JOGGING,
                                sessionId
                            )
                        } else {
                            mStepsTrackerDBHelper!!.createStepsEntry(
                                mHighestPeakList[i].time,
                                WALKING,
                                sessionId
                            )
                        }
                    }
                }
            }
        }

        inner class DataSorter : Comparator<AccelerometerData> {
            override fun compare(obj1: AccelerometerData, obj2: AccelerometerData): Int {
                var returnVal = 0
                if (obj1.value < obj2.value) {
                    returnVal = -1
                } else if (obj1.value > obj2.value) {
                    returnVal = 1
                }
                return returnVal
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mScheduledExecutorService.shutdown()
    }

    companion object {
        private const val WALKINGPEAK = 18
        private const val JOGGINGPEAK = 25
        private const val RUNNINGPEAK = 32
        private const val RUNNING = 3
        private const val JOGGING = 2
        private const val WALKING = 1
    }
}