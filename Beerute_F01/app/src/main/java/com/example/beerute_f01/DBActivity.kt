package com.example.beerute_f01

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference

class DBActivity  : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val dr_db: DocumentReference = db.collection("routes").document("0000001")

    lateinit var idRouteEditText: EditText
    lateinit var latitudePosEditText: EditText
    lateinit var longitudePosEditText: EditText
    lateinit var latitudeEditText: EditText
    lateinit var longitudeEditText: EditText
    lateinit var kmEditText: EditText
    lateinit var stepsEditText: EditText
    lateinit var timeEditText: EditText
    lateinit var userEditText: EditText

    lateinit var saveButton: Button
    lateinit var getButton: Button
    lateinit var deleteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_db)

        // Setup
        setup()
    }

    private fun setup () {

        title = "Base de Datos (RUTAS)"

        val idRoute: String = "0000001"
        val idRoute1: String = "0000002"

        idRouteEditText = findViewById(R.id.idRouteEditText)
        latitudePosEditText = findViewById(R.id.latitudePosEditText)
        longitudePosEditText = findViewById(R.id.longitudePosEditText)
        latitudeEditText = findViewById(R.id.latitudeEditText)
        longitudeEditText = findViewById(R.id.longitudeEditText)
        kmEditText = findViewById(R.id.kmEditText)
        stepsEditText = findViewById(R.id.stepsEditText)
        timeEditText = findViewById(R.id.timeEditText)
        userEditText = findViewById(R.id.userEditText)

        saveButton = findViewById(R.id.saveButton)
        getButton = findViewById(R.id.getButton)
        deleteButton = findViewById(R.id.deleteButton)

        saveButton.setOnClickListener {

            // Guardado de datos

            db.collection("routes").document(idRoute).set(
                hashMapOf("km" to kmEditText.text.toString(),
                    "steps" to stepsEditText.text.toString(),
                    "time" to timeEditText.text.toString(),
                    "user" to userEditText.text.toString())
            )
        }

        getButton.setOnClickListener {

            // Recuperado de datos

            get_DB ("routes", idRoute)

            /*dr_db.get().addOnSuccessListener {
                if (it.exists()) {

                    val cd_lst = it.get("coordinatelist") as? Map<*, *>
                    val lgth_lst = cd_lst?.get("length") as Long
                    val lat_lst = cd_lst.get("latitude") as? List<*>
                    val lon_lst = cd_lst.get("longitude") as? List<*>
                    val km: Double = it.getDouble("km") as Double
                    val steps: Double = it.getDouble("steps") as Double
                    val time: Double = it.getDouble("time") as Double
                    val user: String = it.get("user") as String

                    val i: Int = lgth_lst.toInt()
                    val m: Array<Array<Double>> = Array(2) { Array(i) { 0.0 } }

                    var position: Int = 0

                    var pos_lat = 0.0
                    var pos_lon = 0.0

                    if (lgth_lst != null && lon_lst != null && lat_lst != null) {

                        if (pos_lat != null && pos_lon != null) {

                            for (j in 0 until i) {

                                pos_lat = lat_lst.get(j) as Double
                                pos_lon = lon_lst.get(j) as Double

                                m[0][j] = pos_lat
                                m[1][j] = pos_lon

                                position = j
                            }
                        }
                    }

                    idRouteEditText.setText(idRoute)
                    latitudePosEditText.setText(position.toString())
                    longitudePosEditText.setText(position.toString())
                    latitudeEditText.setText(pos_lat.toString())
                    longitudeEditText.setText(pos_lon.toString())
                    kmEditText.setText(km.toString())
                    stepsEditText.setText(steps.toString())
                    timeEditText.setText(time.toString())
                    userEditText.setText(user)
                }
            }.addOnFailureListener {
                // Maneja el error en caso de fallo en la recuperación de datos
                Toast.makeText(this, "Error al manejar la Base de Datos", Toast.LENGTH_SHORT).show()
            }*/
        }

        deleteButton.setOnClickListener {

            // Borrado de datos

            db.collection("routes01").document(idRoute1).delete()
        }
    }

    private fun set_DB (cl00: String, d00: String) {

        idRouteEditText = findViewById(R.id.idRouteEditText)
        latitudePosEditText = findViewById(R.id.latitudePosEditText)
        longitudePosEditText = findViewById(R.id.longitudePosEditText)
        latitudeEditText = findViewById(R.id.latitudeEditText)
        longitudeEditText = findViewById(R.id.longitudeEditText)
        kmEditText = findViewById(R.id.kmEditText)
        stepsEditText = findViewById(R.id.stepsEditText)
        timeEditText = findViewById(R.id.timeEditText)
        userEditText = findViewById(R.id.userEditText)

        db.collection(cl00).document(d00).set(
            hashMapOf("km" to kmEditText.text.toString(),
                "steps" to stepsEditText.text.toString(),
                "time" to timeEditText.text.toString(),
                "user" to userEditText.text.toString())
        )
    }

    private fun get_DB (cl00: String, d00: String) {

        idRouteEditText = findViewById(R.id.idRouteEditText)
        latitudePosEditText = findViewById(R.id.latitudePosEditText)
        longitudePosEditText = findViewById(R.id.longitudePosEditText)
        latitudeEditText = findViewById(R.id.latitudeEditText)
        longitudeEditText = findViewById(R.id.longitudeEditText)
        kmEditText = findViewById(R.id.kmEditText)
        stepsEditText = findViewById(R.id.stepsEditText)
        timeEditText = findViewById(R.id.timeEditText)
        userEditText = findViewById(R.id.userEditText)

        db.collection(cl00).document(d00).get().addOnSuccessListener {
            if (it.exists()) {

                val cd_lst = it.get("coordinatelist") as? Map<*, *>
                val lgth_lst = cd_lst?.get("length") as Long
                val lat_lst = cd_lst.get("latitude") as? List<*>
                val lon_lst = cd_lst.get("longitude") as? List<*>
                val km: Double = it.getDouble("km") as Double
                val steps: Double = it.getDouble("steps") as Double
                val time: Double = it.getDouble("time") as Double
                val user: String = it.get("user") as String

                val i: Int = lgth_lst.toInt()
                val m: Array<Array<Double>> = Array(2) { Array(i) { 0.0 } }

                var position: Int = 0

                var pos_lat = 0.0
                var pos_lon = 0.0

                if (lgth_lst != null && lon_lst != null && lat_lst != null) {

                    if (pos_lat != null && pos_lon != null) {

                        for (j in 0 until i) {

                            pos_lat = lat_lst.get(j) as Double
                            pos_lon = lon_lst.get(j) as Double

                            m[0][j] = pos_lat
                            m[1][j] = pos_lon

                            position = j
                        }
                    }
                }

                idRouteEditText.setText(d00)
                latitudePosEditText.setText(position.toString())
                longitudePosEditText.setText(position.toString())
                latitudeEditText.setText(pos_lat.toString())
                longitudeEditText.setText(pos_lon.toString())
                kmEditText.setText(km.toString())
                stepsEditText.setText(steps.toString())
                timeEditText.setText(time.toString())
                userEditText.setText(user)
            }
        }.addOnFailureListener {
            // Maneja el error en caso de fallo en la recuperación de datos
            Toast.makeText(this, "Error al manejar la Base de Datos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun delete_DB (cl00: String, d00: String) {

        db.collection(cl00).document(d00).delete()
    }
}