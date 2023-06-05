package com.example.beerute_f01


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(){

    lateinit var exploreButton: Button
    lateinit var createButton: Button
    lateinit var profileButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup
        setup()
    }

    private fun setup() {

        exploreButton = findViewById(R.id.exploreButton)
        createButton = findViewById(R.id.createButton)
        profileButton = findViewById(R.id.profileButton)

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
    }
}