package com.example.beerute_f01

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.beerute_f01.Object.GlobalVariables
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class RankingActivity : AppCompatActivity() {

    val db = FirebaseFirestore.getInstance()

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var scrollView: ScrollView

    private lateinit var usernameTextView: TextView
    private lateinit var userLegendTextView: TextView
    private lateinit var userImageView: ImageView

    private lateinit var username0TextView: TextView
    private lateinit var userLegend0TextView: TextView
    private lateinit var user0ImageView: ImageView
    private lateinit var kmmax0TextView: TextView

    private lateinit var username1TextView: TextView
    private lateinit var userLegend1TextView: TextView
    private lateinit var user1ImageView: ImageView
    private lateinit var kmmax1TextView: TextView

    private lateinit var username2TextView: TextView
    private lateinit var userLegend2TextView: TextView
    private lateinit var user2ImageView: ImageView
    private lateinit var kmmax2TextView: TextView

    private lateinit var username3TextView: TextView
    private lateinit var userLegend3TextView: TextView
    private lateinit var user3ImageView: ImageView
    private lateinit var kmmax3TextView: TextView

    private lateinit var username4TextView: TextView
    private lateinit var userLegend4TextView: TextView
    private lateinit var user4ImageView: ImageView
    private lateinit var kmmax4TextView: TextView

    private lateinit var username5TextView: TextView
    private lateinit var userLegend5TextView: TextView
    private lateinit var user5ImageView: ImageView
    private lateinit var kmmax5TextView: TextView

    private lateinit var username6TextView: TextView
    private lateinit var userLegend6TextView: TextView
    private lateinit var user6ImageView: ImageView
    private lateinit var kmmax6TextView: TextView

    private lateinit var username7TextView: TextView
    private lateinit var userLegend7TextView: TextView
    private lateinit var user7ImageView: ImageView
    private lateinit var kmmax7TextView: TextView

    private lateinit var username8TextView: TextView
    private lateinit var userLegend8TextView: TextView
    private lateinit var user8ImageView: ImageView
    private lateinit var kmmax8TextView: TextView

    private lateinit var username9TextView: TextView
    private lateinit var userLegend9TextView: TextView
    private lateinit var user9ImageView: ImageView
    private lateinit var kmmax9TextView: TextView

    lateinit var userName: TextView
    lateinit var userLegend: TextView
    lateinit var userImage: ImageView
    lateinit var userKmmax: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        // Inicializar las vistas
        usernameTextView = findViewById(R.id.usernameTextView)
        userLegendTextView = findViewById(R.id.userLegendTextView)
        userImageView = findViewById(R.id.userImageView)

        username0TextView = findViewById(R.id.username0TextView)
        userLegend0TextView = findViewById(R.id.userLegend0TextView)
        user0ImageView = findViewById(R.id.user0ImageView)
        kmmax0TextView = findViewById(R.id.kmmax0TextView)

        username1TextView = findViewById(R.id.username1TextView)
        userLegend1TextView = findViewById(R.id.userLegend1TextView)
        user1ImageView = findViewById(R.id.user1ImageView)
        kmmax1TextView = findViewById(R.id.kmmax1TextView)

        username2TextView = findViewById(R.id.username2TextView)
        userLegend2TextView = findViewById(R.id.userLegend2TextView)
        user2ImageView = findViewById(R.id.user2ImageView)
        kmmax2TextView = findViewById(R.id.kmmax2TextView)

        username3TextView = findViewById(R.id.username3TextView)
        userLegend3TextView = findViewById(R.id.userLegend3TextView)
        user3ImageView = findViewById(R.id.user3ImageView)
        kmmax3TextView = findViewById(R.id.kmmax3TextView)

        username4TextView = findViewById(R.id.username4TextView)
        userLegend4TextView = findViewById(R.id.userLegend4TextView)
        user4ImageView = findViewById(R.id.user4ImageView)
        kmmax4TextView = findViewById(R.id.kmmax4TextView)

        username5TextView = findViewById(R.id.username5TextView)
        userLegend5TextView = findViewById(R.id.userLegend5TextView)
        user5ImageView = findViewById(R.id.user5ImageView)
        kmmax5TextView = findViewById(R.id.kmmax5TextView)

        username6TextView = findViewById(R.id.username6TextView)
        userLegend6TextView = findViewById(R.id.userLegend6TextView)
        user6ImageView = findViewById(R.id.user6ImageView)
        kmmax6TextView = findViewById(R.id.kmmax6TextView)

        username7TextView = findViewById(R.id.username7TextView)
        userLegend7TextView = findViewById(R.id.userLegend7TextView)
        user7ImageView = findViewById(R.id.user7ImageView)
        kmmax7TextView = findViewById(R.id.kmmax7TextView)

        username8TextView = findViewById(R.id.username8TextView)
        userLegend8TextView = findViewById(R.id.userLegend8TextView)
        user8ImageView = findViewById(R.id.user8ImageView)
        kmmax8TextView = findViewById(R.id.kmmax8TextView)

        username9TextView = findViewById(R.id.username9TextView)
        userLegend9TextView = findViewById(R.id.userLegend9TextView)
        user9ImageView = findViewById(R.id.user9ImageView)
        kmmax9TextView = findViewById(R.id.kmmax9TextView)

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        scrollView = findViewById(R.id.scrollView)

        dbQuery("users", GlobalVariables.userEmail, "kmmax") {

            // Establecer el nombre de usuario
            usernameTextView.text = GlobalVariables.userEmail

            GlobalVariables.kmmax = GlobalVariables.aux

            // Establecer la leyenda del usuario
            GlobalVariables.legend = getLegend(GlobalVariables.kmmax)
            userLegendTextView.text = "Eres MEDALLA " + GlobalVariables.legend

            // Establecer la imagen del usuario (medalla_diamante.png desde la carpeta drawable)
            val imageUrl: Int = getMedalIMG(GlobalVariables.legend)
            Glide.with(this) // Pasa el contexto actual
                .load(imageUrl) // Especifica la URL o la ruta de la imagen
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Opciones de almacenamiento en caché
                .into(userImageView) // ImageView para cargar la imagen
        }

        db.collection("users")
            .orderBy("kmmax", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Limpia la lista antes de agregar nuevos resultados
                GlobalVariables.userRankingList.clear()

                for (document in querySnapshot) {
                    val username = document.id
                    print(username + " - ")

                    val kmmax = document.getDouble("kmmax") ?: 0.0
                    println(kmmax.toString())

                    // Crea una nueva instancia de RankingUserObject en cada iteración
                    val rankingUser = RankingUserClass()
                    rankingUser.username = username
                    rankingUser.kmmax = kmmax

                    // Agrega el objeto a la lista
                    GlobalVariables.userRankingList.add(rankingUser)
                }

                // Ahora GlobalVariables.userRankingList contiene los 10 usuarios con el valor más alto en "kmmax"
                // Puedes usar esta lista en otras partes de tu actividad
                // Por ejemplo, aquí puedes mostrar los usuarios en un RecyclerView o ListView
            }
            .addOnFailureListener { exception ->
                // Maneja el error si la consulta falla
                Log.e("Firestore", "Error al consultar usuarios: $exception")
            }

        swipeRefreshLayout.setOnRefreshListener {
            // Aquí se realiza la acción de actualización

            for (i in 0 until GlobalVariables.userRankingList.size) {

                val rankingUser = GlobalVariables.userRankingList[i]
                val usernameRankingUser = rankingUser.username
                print("Bucle for - " + usernameRankingUser + " - ")

                val kmmaxRankingUser = rankingUser.kmmax
                println(kmmaxRankingUser.toString())

                updateUserCardViews(usernameRankingUser, kmmaxRankingUser, i)
            }

            // Después de completar la actualización, detenemos la animación de "pull-to-refresh"
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun getLegend(kmaux: Double): String {
        return when {
            kmaux >= 50.0 -> "DIAMANTE"
            kmaux >= 40.0 -> "ESMERALDA"
            kmaux >= 35.0 -> "PALADIO"
            kmaux >= 30.0 -> "PLATINO"
            kmaux >= 25.0 -> "ORO"
            kmaux >= 20.0 -> "PLATA"
            kmaux >= 15.0 -> "BRONCE"
            kmaux >= 10.0 -> "PLOMO"
            kmaux >= 7.0 -> "HIERRO"
            else -> "BLANCA"
        }
    }

    private fun getMedalIMG(medal: String): Int {

        return when (medal) {

            "DIAMANTE" -> R.drawable.medalla_diamante
            "ESMERALDA" -> R.drawable.medalla_esmeralda
            "PALADIO" -> R.drawable.medalla_paladio
            "PLATINO" -> R.drawable.medalla_platino
            "ORO" -> R.drawable.medalla_oro
            "PLATA" -> R.drawable.medalla_plata
            "BRONCE" -> R.drawable.medalla_bronce
            "PLOMO" -> R.drawable.medalla_plomo
            "HIERRO" -> R.drawable.medalla_hierro
            "BLANCA" -> R.drawable.medalla_blanco

            else -> 0 // Valor predeterminado si `medal` no coincide con ningún caso
        }
    }

    private fun dbQuery(collection: String, document: String, field: String, callback: () -> Unit) {
        val db = FirebaseFirestore.getInstance()

        // Referencia al documento en la colección
        val docRef = db.collection(collection).document(document)

        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Documento existe, obtén el valor del campo como Double
                    val fieldValue = documentSnapshot.getDouble(field)

                    // Asigna el valor a GlobalVariables.aux como Double
                    GlobalVariables.aux = fieldValue ?: 0.0 // Valor predeterminado si es nulo

                    // Llama al callback después de que se establezca GlobalVariables.aux
                    callback()
                } else {
                    // El documento no existe
                    GlobalVariables.aux = 0.0 // Valor predeterminado en caso de documento no encontrado

                    // Llama al callback después de que se establezca GlobalVariables.aux
                    callback()
                }
            }
            .addOnFailureListener { exception ->
                // Maneja el error si la consulta falla
                GlobalVariables.aux = 0.0 // Valor predeterminado en caso de error

                // Llama al callback después de que se establezca GlobalVariables.aux
                callback()
            }
    }

    @SuppressLint("DiscouragedApi")
    private fun updateUserCardViews(user: String, kmmax: Double, i: Int) {

        //Se paraleliza y no carga la información

        // Realizar la consulta a Firestore para obtener kmmax
        dbQuery("users", user, "kmmax") {

            when (i) {
                in 0..9 -> {
                    userName = findViewById<TextView>(resources.getIdentifier("username${i}TextView", "id", packageName))
                    userLegend = findViewById<TextView>(resources.getIdentifier("userLegend${i}TextView", "id", packageName))
                    userImage = findViewById<ImageView>(resources.getIdentifier("user${i}ImageView", "id", packageName))
                    userKmmax = findViewById<TextView>(resources.getIdentifier("kmmax${i}TextView", "id", packageName))
                }
                else -> {
                    // Código para otros casos
                }
            }

            // Establecer el nombre de usuario
            userName.text = user

            GlobalVariables.kmmax = kmmax
            userKmmax.text = "KM Recorridos: " + kmmax.toString()

            // Establecer la leyenda del usuario
            GlobalVariables.legend = getLegend(GlobalVariables.kmmax)
            userLegend.text = GlobalVariables.legend

            // Establecer la imagen del usuario (medalla_diamante.png desde la carpeta drawable)
            val imageUrl: Int = getMedalIMG(GlobalVariables.legend)
            Glide.with(this) // Pasa el contexto actual
                .load(imageUrl) // Especifica la URL o la ruta de la imagen
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Opciones de almacenamiento en caché
                .into(userImage) // ImageView para cargar la imagen
        }
    }
}