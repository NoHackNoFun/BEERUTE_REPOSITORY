package com.example.beerute_f01


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.beerute_f01.StepCounter.StepCounterMain

class MainActivity : AppCompatActivity(){

    lateinit var exploreButton: Button
    lateinit var createButton: Button
    lateinit var createdButton: Button
    lateinit var IAHelpButton: Button
    lateinit var profileButton: Button
    lateinit var dbButton: Button
    lateinit var stepButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Setup
        setup()
    }

    private fun setup() {

        exploreButton = findViewById(R.id.exploreButton)
        createButton = findViewById(R.id.createButton)
        createdButton = findViewById(R.id.createdButton)
        IAHelpButton = findViewById(R.id.IAHelpButton)
        profileButton = findViewById(R.id.profileButton)
        dbButton = findViewById(R.id.dbButton)
        stepButton = findViewById(R.id.stepButton)

        exploreButton.setOnClickListener {
            val eIntent = Intent(this, ExploreActivity::class.java)
            startActivity(eIntent)
        }

        createButton.setOnClickListener {
            val cIntent = Intent(this, CreateActivity::class.java)
            startActivity(cIntent)
        }

        createdButton.setOnClickListener {
            val cdIntent = Intent(this, CreatedActivity::class.java)
            startActivity(cdIntent)
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

        stepButton.setOnClickListener {
            val dbIntent = Intent(this, StepCounterMain::class.java)
            startActivity(dbIntent)
        }
    }
}