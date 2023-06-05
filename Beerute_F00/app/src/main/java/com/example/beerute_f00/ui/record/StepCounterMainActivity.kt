package com.example.beerute_f00.ui.record

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.beerute_f00.R

class StepCounterMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stepcountermain)
    }

    fun navigateToStepCounterActivity(v: View?) {
        val mIntent = Intent(this, StepsCounterActivity::class.java)
        startActivity(mIntent)
    }

    fun navigateToStepHistoryActivity(v: View?) {
        val mIntent = Intent(this, StepsHistoryActivity::class.java)
        startActivity(mIntent)
    }

    fun navigateToCustomAlgoActivity(v: View?) {
        val mIntent = Intent(this, CustomAlgoResultsActivity::class.java)
        startActivity(mIntent)
    }
}