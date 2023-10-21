package com.example.beerute_f01

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.beerute_f01.Object.GlobalVariables
import com.example.beerute_f01.Object.RuteObject
import com.google.firebase.firestore.FirebaseFirestore

class ExploreActivity : AppCompatActivity() {

    private lateinit var routeAdapter: RouteAdapter
    private var routes: List<Route> = emptyList()

    private lateinit var firestore: FirebaseFirestore

    private val collection: String = "routes"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)

        // Configurar FirebaseFirestore
        firestore = FirebaseFirestore.getInstance()

        // Configurar RecyclerView y Adapter
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        routeAdapter = RouteAdapter(routes) { route -> onRouteClick(route) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = routeAdapter

        // Configurar el filtro de búsqueda
        val searchEditText: EditText = findViewById(R.id.searchEditText)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No se requiere implementación
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val selectedOption = (findViewById<Spinner>(R.id.spinnerFilter)).selectedItem.toString()
                val query = s.toString().trim()
                filterRoutes(selectedOption, query)
            }

            override fun afterTextChanged(s: Editable?) {
                // No se requiere implementación
            }
        })

        // Configurar el Spinner y el ArrayAdapter
        val spinnerFilter: Spinner = findViewById(R.id.spinnerFilter)
        val filterOptions = arrayOf("Número de RUTA", "Lugar", "Usuario CREADOR", "Usuario RECORD")
        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        spinnerFilter.adapter = filterAdapter

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedOption = filterOptions[position]
                val query = searchEditText.text.toString().trim()
                filterRoutes(selectedOption, query)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No se requiere implementación
            }
        }

        // Obtener las rutas de la base de datos y actualizar el adaptador
        fetchRoutesFromFirestore()
    }

    // Función para obtener las rutas de la base de datos de Firestore
    private fun fetchRoutesFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val routesCollection = db.collection("routes")

        routesCollection.get()
            .addOnSuccessListener { result ->
                val routesList = mutableListOf<Route>()
                for (document in result) {
                    val routeId = document.id
                    val place = document.getString("place") ?: ""
                    val steps = document.getDouble("steps") ?: 0.0
                    val km = document.getDouble("km") ?: 0.0
                    val time = document.getDouble("time") ?: 0.0
                    val user = document.getString("user") ?: ""
                    val bestUser = document.getString("bestuser") ?: ""

                    val route = Route(routeId, place, steps, km, time, user, bestUser)
                    routesList.add(route)
                }

                routes = routesList
                routeAdapter.updateRoutes(routes)
            }
            .addOnFailureListener { exception ->
                // Manejar el error en caso de que falle la obtención de rutas
            }
    }

    // Función de filtrado de rutas
    private fun filterRoutes(filterOption: String, query: String) {
        val filteredRoutes = if (query.isNotEmpty()) {
            routes.filter { route ->
                when (filterOption) {
                    "Número de RUTA" -> route.documentId.contains(query, ignoreCase = true)
                    "Lugar" -> route.place.contains(query, ignoreCase = true)
                    "Usuario CREADOR" -> route.user.contains(query, ignoreCase = true)
                    "Usuario RECORD" -> route.bestUser.contains(query, ignoreCase = true)
                    else -> false
                }
            }
        } else {
            routes
        }
        routeAdapter.updateRoutes(filteredRoutes)
    }

    // Función de callback al hacer clic en una ruta
    private fun onRouteClick(route: Route) {
        val db = FirebaseFirestore.getInstance()
        val ro = RuteObject

        // Asigna los valores de la ruta seleccionada a las variables globales
        GlobalVariables.selectedRouteId = route.documentId
        GlobalVariables.selectedPlace = route.place
        GlobalVariables.selectedSteps = route.steps.toInt()
        GlobalVariables.selectedKm = route.km
        GlobalVariables.selectedTime = route.time
        GlobalVariables.selectedUser = route.user
        GlobalVariables.selectedBestUser = route.bestUser

        // Recuperado de datos
        db.collection(collection).document(GlobalVariables.selectedRouteId).get().addOnSuccessListener {

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

                ro.saveMatrix(this, m)

                val cIntent = Intent(this, CreateActivity::class.java)
                startActivity(cIntent)
            }
        }.addOnFailureListener {

            // Maneja el error en caso de fallo en la recuperación de datos
            Toast.makeText(this, "Error al manejar la Base de Datos", Toast.LENGTH_SHORT).show()
        }
        // Implementa la lógica adicional que deseas realizar al hacer clic en una ruta
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}