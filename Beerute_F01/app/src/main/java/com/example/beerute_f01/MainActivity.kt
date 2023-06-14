package com.example.beerute_f01


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(){

    lateinit var exploreButton: Button
    lateinit var createButton: Button
    lateinit var profileButton: Button
    lateinit var dbButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Setup
        setup()
    }

    private fun setup() {

        exploreButton = findViewById(R.id.exploreButton)
        createButton = findViewById(R.id.createButton)
        profileButton = findViewById(R.id.profileButton)
        dbButton = findViewById(R.id.dbButton)

        exploreButton.setOnClickListener {
            val eIntent = Intent(this, ExploreActivity::class.java)
            startActivity(eIntent)
        }

        createButton.setOnClickListener {
            val cIntent = Intent(this, CreateActivity::class.java)
            startActivity(cIntent)
        }

        profileButton.setOnClickListener {
            val pIntent = Intent(this, ProfileActivity::class.java)
            startActivity(pIntent)
        }

        dbButton.setOnClickListener {
            val dbIntent = Intent(this, DBActivity::class.java)
            startActivity(dbIntent)
        }
    }
}