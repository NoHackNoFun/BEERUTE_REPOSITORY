package com.example.beerute_f01

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

data class Route(
    val documentId: String = "",
    val place: String = "",
    val steps: Double = 0.0,
    val km: Double = 0.0,
    val time: Double = 0.0,
    val user: String = "",
    val bestUser: String = ""
) {
    companion object {
        // Método estático para crear una instancia de la clase Route a partir de un DocumentSnapshot de Firestore.
        fun fromDocumentSnapshot(document: DocumentSnapshot): Route {
            val documentId = document.id
            val place = document.getString("place") ?: ""
            val steps = document.getDouble("steps") ?: 0.0
            val km = document.getDouble("km") ?: 0.0
            val time = document.getDouble("time") ?: 0.0
            val user = document.getString("user") ?: ""
            val bestUser = document.getString("bestuser") ?: ""

            return Route(documentId, place, steps, km, time, user, bestUser)
        }

        /** Método estático para obtener todas las rutas de Firestore.
         * Recibe dos funciones de callback: onSuccess para manejar la lista de rutas obtenidas exitosamente
         * y onFailure para manejar cualquier error ocurrido durante la obtención de las rutas.*/
        fun getAllRoutes(onSuccess: (List<Route>) -> Unit, onFailure: (Exception) -> Unit) {
            val db = FirebaseFirestore.getInstance()
            val routesCollection = db.collection("routes")

            routesCollection.get()
                .addOnSuccessListener { querySnapshot ->
                    val routesList: MutableList<Route> = mutableListOf()

                    for (document: DocumentSnapshot in querySnapshot.documents) {
                        val route = Route.fromDocumentSnapshot(document)
                        routesList.add(route)
                    }

                    onSuccess(routesList)
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        }

        // Método estático para darle formato al tiempo en milisegundos.
        fun formatTime(milliseconds: Double): String {
            val sdf = SimpleDateFormat("HH:mm:ss")
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val time = Date(milliseconds.toLong())
            return sdf.format(time)
        }
    }
}