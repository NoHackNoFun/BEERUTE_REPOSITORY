package com.example.beerute_f01

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

data class Route(
    val documentId: String = "",
    val place: String = "",
    val km: Double = 0.0,
    val time: Double = 0.0,
    val user: String = "",
    val bestUser: String = ""
) {
    companion object {
        fun fromDocumentSnapshot(document: DocumentSnapshot): Route {
            val documentId = document.id
            val place = document.getString("place") ?: ""
            val km = document.getDouble("km") ?: 0.0
            val time = document.getDouble("time") ?: 0.0
            val user = document.getString("user") ?: ""
            val bestUser = document.getString("bestuser") ?: ""

            return Route(documentId, place, km, time, user, bestUser)
        }

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
    }
}