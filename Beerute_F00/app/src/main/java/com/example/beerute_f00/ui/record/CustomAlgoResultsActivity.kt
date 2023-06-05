package com.example.beerute_f00.ui.record

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.beerute_f00.R
import java.util.Calendar

class CustomAlgoResultsActivity : AppCompatActivity() {
    private var mTotalStepsTextView: TextView? = null
    private var mTotalDistanceTextView: TextView? = null
    private var mTotalDurationTextView: TextView? = null
    private var mAverageSpeedTextView: TextView? = null
    private var mAveragFrequencyTextView: TextView? = null
    private var mTotalCalorieBurnedTextView: TextView? = null
    private var mPhysicalActivityTypeTextView: TextView? = null
    var mStepsTrackerDBHelper: StepsTrackerDBHelper? = null

    //@SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.capability_layout)

        mStepsTrackerDBHelper = StepsTrackerDBHelper(this)
        mTotalStepsTextView = findViewById<View>(R.id.total_steps) as TextView
        mTotalDistanceTextView = findViewById<View>(R.id.total_distance) as TextView
        mTotalDurationTextView = findViewById<View>(R.id.total_duration) as TextView
        mAverageSpeedTextView = findViewById<View>(R.id.average_speed) as TextView
        mAveragFrequencyTextView = findViewById<View>(R.id.average_frequency) as TextView
        mTotalCalorieBurnedTextView = findViewById<View>(R.id.calories_burned) as TextView
        mPhysicalActivityTypeTextView = findViewById<View>(R.id.physical_activitytype) as TextView

        val stepsAnalysisIntent = Intent(applicationContext, StepsTrackerService::class.java)
        startService(stepsAnalysisIntent)
        calculateDataMatrix()
    }

    @SuppressLint("SetTextI18n")
    fun calculateDataMatrix() {
        val calendar = Calendar.getInstance()
        val todayDate =
            (calendar[Calendar.MONTH] + 1).toString() + "/" + calendar[Calendar.DAY_OF_MONTH].toString() + "/" + calendar[Calendar.YEAR].toString()
        val stepType = mStepsTrackerDBHelper!!.getStepsByDate(todayDate)
        val walkingSteps = stepType[0]
        val joggingSteps = stepType[1]
        val runningSteps = stepType[2]

        //Calculating total steps
        val totalStepTaken = walkingSteps + joggingSteps + runningSteps
        mTotalStepsTextView!!.text = "$totalStepTaken Steps"

        //Calculating total distance traveled
        val totalDistance = walkingSteps * 0.5f + joggingSteps * 1.0f + runningSteps * 1.5f
        mTotalDistanceTextView!!.text = "$totalDistance meters"

        //Calculating total duration
        val totalDuration = walkingSteps * 1.0f + joggingSteps * 0.7f + runningSteps * 0.4f
        val hours = totalDuration / 3600
        val minutes = totalDuration % 3600 / 60
        val seconds = totalDuration % 60

        mTotalDurationTextView!!.text =
            String.format("%.0f", hours) + " hrs " + String.format(
                "%.0f",
                minutes
            ) + " mins " + String.format("%.0f", seconds) + " secs"

        //Calculating average speed
        if (totalDistance > 0) {
            mAverageSpeedTextView!!.text =
                String.format("%.2f", totalDistance / totalDuration) + " meter per seconds"
        } else {
            mAverageSpeedTextView!!.text = "0 meter per seconds"
        }

        //Calculating average step frequency
        if (totalStepTaken > 0) {
            mAveragFrequencyTextView!!.text =
                String.format("%.0f", totalStepTaken / minutes) + " steps per minute"
        } else {
            mAveragFrequencyTextView!!.text = "0 steps per minute"
        }

        //Calculating total calories burned
        val totalCaloriesBurned = walkingSteps * 0.05f + joggingSteps * 0.1f + runningSteps * 0.2f
        mTotalCalorieBurnedTextView!!.text =
            String.format("%.0f", totalCaloriesBurned) + " Calories"

        //Calculating type of physical activity
        mPhysicalActivityTypeTextView!!.text = """$walkingSteps Walking Steps 
$joggingSteps Jogging Steps 
$runningSteps""" + " Running Steps"
    }
}