package com.example.beerute_f01.DB

import com.example.beerute_f01.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot

import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class DB_Methods : AppCompatActivity(){

    private val db = FirebaseFirestore.getInstance()
    private val docRef: DocumentReference = db.collection("routes").document("0000001")

    lateinit var kmEditText: EditText
    lateinit var stepsEditText: EditText
    lateinit var timeEditText: EditText
    lateinit var userEditText: EditText

    private var tsvcp01: String = ""
    private var tsvcp02: String = ""
    private var tsvcp03: String = ""
    private var tsvcp04: String = ""

    fun connectedDB (v0:Int, cl00: String, d00: String, cp00: String, cp000: String, cp001: String,
                            cp0000: ArrayList<Long>, cp0001: ArrayList<Long>, cp01: String, cp010: Long,
                            cp02: String, cp020: Int, cp03: String, cp030: Long, cp04: String, cp040: String,
                            ts: Int) {

        //Controlamos las variables

        when (v0) {
            0 -> {
                setDB(cl00, d00, cp00, cp000, cp001, cp0000, cp0001, cp01, cp010, cp02, cp020, cp03,
                    cp030, cp04, cp040)
            }

            1 -> {
                getDB(cl00, d00, cp00, cp000, cp001, cp01, cp02, cp03, cp04)
            }

            2 -> {
                deleteDB(cl00, d00)
            }
        }
    }

    // Guardar DATOS
    fun setDB (cl00: String, d00: String, cp00: String, cp000: String, cp001: String,
                       cp0000: ArrayList<Long>, cp0001: ArrayList<Long>, cp01: String, cp010: Long,
                       cp02: String, cp020: Int, cp03: String, cp030: Long, cp04: String, cp040: String) {

        db.collection(cl00).document(d00).set(
            hashMapOf(
                cp01 to cp010,
                cp02 to cp020,
                cp03 to cp030,
                cp04 to cp040
            )
        )
    }

    /*fun getDB (cl00: String, d00: String, cp00: String, cp000: String, cp001: String,
               cp01: String, cp02: String, cp03: String, cp04: String): Quadruple<String, String, String, String> {

        db.collection(cl00).document(d00).get().addOnSuccessListener {

            // km
            var vcp01: Double = it.getDouble(cp01) ?: 0.0
            // steps
            var vcp02: Double = it.getDouble(cp02) ?: 0.0
            // time
            var vcp03: Double = it.getDouble(cp03) ?: 0.0
            // user
            var vcp04: String = it.getString(cp04) as String

            tsvcp01 = vcp01.toString()
            tsvcp02 = vcp02.toString()
            tsvcp03 = vcp03.toString()
            tsvcp04 = vcp04

            println("primero "+vcp01.toString()+" "+vcp02.toString()+" "+vcp03.toString()+" "+vcp04)
            println("segundo "+tsvcp01+" "+tsvcp02+" "+tsvcp03+" "+tsvcp04)
        }

        //println("tercero "+vcp01.toString()+" "+vcp02.toString()+" "+vcp03.toString()+" "+vcp04)
        println("tercero "+tsvcp01+" "+tsvcp02+" "+tsvcp03+" "+tsvcp04)

        return Quadruple(tsvcp01, tsvcp02, tsvcp03, tsvcp04)
    }*/

    // Recuperar DATOS
    fun getDB (cl00: String, d00: String, cp00: String, cp000: String, cp001: String,
               cp01: String, cp02: String, cp03: String, cp04: String) {

        db.collection(cl00).document(d00).get().addOnSuccessListener {

            kmEditText = findViewById(R.id.kmEditText)
            stepsEditText = findViewById(R.id.stepsEditText)
            timeEditText = findViewById(R.id.timeEditText)
            userEditText = findViewById(R.id.userEditText)

            // km
            val vcp01: Double = it.getDouble(cp01) ?: 0.0
            // steps
            val vcp02: Double = it.getDouble(cp02) ?: 0.0
            // time
            val vcp03: Double = it.getDouble(cp03) ?: 0.0
            // user
            val vcp04: String = it.getString(cp04) as String

            tsvcp01 = vcp01.toString()
            tsvcp02 = vcp02.toString()
            tsvcp03 = vcp03.toString()
            tsvcp04 = vcp04

            kmEditText.setText(tsvcp01)
            stepsEditText.setText(tsvcp02)
            timeEditText.setText(tsvcp03)
            userEditText.setText(tsvcp04)
        }
    }

    // Borrar DATOS
    fun deleteDB (cl00: String, d00: String) {

        db.collection(cl00).document(d00).delete()
    }

    data class Quadruple<T1, T2, T3, T4>(val fst: T1, val snd: T2, val trd: T3, val fth: T4)

}