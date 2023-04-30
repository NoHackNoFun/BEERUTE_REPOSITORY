package com.example.beerute_stepcounter04

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class StepsHistoryActivity : AppCompatActivity() {
    var mStepsDBHelper: StepsDBHelper? = null
    var mSensorListView: ListView? = null
    var mListAdapter: ListAdapter? = null
    var mStepCountList: ArrayList<DateStepsModel>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedometerlist_layout)
        mSensorListView = findViewById<View>(R.id.steps_list) as ListView
        dataForList
        mListAdapter = ListAdapter(mStepCountList!!, this)
        mSensorListView!!.adapter = mListAdapter
        val stepsIntent = Intent(applicationContext, StepsService::class.java)
        startService(stepsIntent)
    }

    val dataForList: Unit
        get() {
            mStepsDBHelper = StepsDBHelper(this)
            mStepCountList = mStepsDBHelper!!.readStepsEntries()
        }
}