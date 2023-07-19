package com.example.beerute_f01

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RouteAdapter(
    private var routes: List<Route>,  // Lista de rutas
    private val onItemClick: (Route) -> Unit  // Función de callback al hacer clic en una ruta
) : RecyclerView.Adapter<RouteAdapter.RouteViewHolder>() {

    // Crea una nueva instancia de RouteViewHolder inflando el diseño del elemento de ruta
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_route, parent, false)
        return RouteViewHolder(view)
    }

    // Vincula los datos de la ruta con los elementos de la vista en la posición especificada
    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        val route = routes[position]

        // Asigna los datos de la ruta a las vistas correspondientes en el ViewHolder
        holder.bind(route)

        // Configura el listener de clic en el elemento de vista
        holder.itemView.setOnClickListener {
            onItemClick(route)
        }
    }

    // Devuelve la cantidad de elementos en la lista de rutas
    override fun getItemCount(): Int {
        return routes.size
    }

    // Actualiza la lista de rutas con una nueva lista y notifica los cambios
    @SuppressLint("NotifyDataSetChanged")
    fun updateRoutes(newRoutes: List<Route>) {
        routes = newRoutes
        notifyDataSetChanged()
    }

    // ViewHolder que contiene las vistas de un elemento de ruta
    inner class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val idTextView: TextView = itemView.findViewById(R.id.idTextView)
        private val placeTextView: TextView = itemView.findViewById(R.id.placeTextView)
        private val stepsTextView: TextView = itemView.findViewById(R.id.stepsTextView)
        private val kmTextView: TextView = itemView.findViewById(R.id.kmTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        private val userTextView: TextView = itemView.findViewById(R.id.userTextView)
        private val bestUserTextView: TextView = itemView.findViewById(R.id.bestUserTextView)

        // Asigna los datos de la ruta a las vistas correspondientes en el ViewHolder
        @SuppressLint("SetTextI18n")
        fun bind(route: Route) {
            idTextView.text = "ID de la Ruta: ${route.documentId}"
            placeTextView.text = "Lugar: ${route.place}"
            stepsTextView.text = "Pasos: ${route.steps.toString()}"
            kmTextView.text = "Distancia: ${route.km.toString()} km"
            timeTextView.text = "Marca de Tiempo: ${route.time}"
            userTextView.text = "Creador: ${route.user}"
            bestUserTextView.text = "RECORD: ${route.bestUser}"
        }
    }
}