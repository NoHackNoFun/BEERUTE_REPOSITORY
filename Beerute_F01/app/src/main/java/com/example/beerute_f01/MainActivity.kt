package com.example.beerute_f01


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.beerute_f01.StepCounter.StepCounterMain

class MainActivity : AppCompatActivity(){

    lateinit var exploreButton: Button
    lateinit var createButton: Button
    lateinit var IAHelpButton: Button
    lateinit var profileButton: Button
    lateinit var dbButton: Button
    lateinit var newStepButton: Button
    lateinit var stepButton: Button
    lateinit var testButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Setup
        setup()
    }

    private fun setup() {

        exploreButton = findViewById(R.id.exploreButton)
        createButton = findViewById(R.id.createButton)
        IAHelpButton = findViewById(R.id.IAHelpButton)
        profileButton = findViewById(R.id.profileButton)
        dbButton = findViewById(R.id.dbButton)
        newStepButton = findViewById(R.id.newStepButton)
        stepButton = findViewById(R.id.stepButton)
        testButton = findViewById(R.id.testButton)

        exploreButton.setOnClickListener {
            val eIntent = Intent(this, ExploreActivity::class.java)
            startActivity(eIntent)
        }

        createButton.setOnClickListener {
            val cIntent = Intent(this, CreateActivity::class.java)
            startActivity(cIntent)
        }

        IAHelpButton.setOnClickListener {
            val iaIntent = Intent(this, IAHelpActivity::class.java)
            startActivity(iaIntent)
        }

        profileButton.setOnClickListener {
            val pIntent = Intent(this, ProfileActivity::class.java)
            startActivity(pIntent)
        }

        dbButton.setOnClickListener {
            val dbIntent = Intent(this, DBActivity::class.java)
            startActivity(dbIntent)
        }

        newStepButton.setOnClickListener {
            val nspIntent = Intent(this, NewStepCounterActivity::class.java)
            startActivity(nspIntent)
        }

        stepButton.setOnClickListener {
            val spIntent = Intent(this, StepCounterMain::class.java)
            startActivity(spIntent)
        }

        testButton.setOnClickListener {
            val taIntent = Intent(this, testActivity::class.java)
            startActivity(taIntent)
        }
    }
}