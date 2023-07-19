package com.example.beerute_f01

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.example.beerute_f01.Object.GlobalVariables
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthActivity : AppCompatActivity() {

    private val GOOGLE_SIGN_IN = 100

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var loginButton: Button
    private lateinit var googleButton: Button
    private lateinit var authLayout: LinearLayout

    /**
     * Método de creación de la actividad de autenticación
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // Analytics Event
        val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "Integración de Firebase completa")
        analytics.logEvent("InitScreen", bundle)

        // Configuración inicial y comprobación de sesión iniciada
        setup()
        session()
    }

    /**
     * Método que se ejecuta al iniciar la actividad
     */
    override fun onStart() {
        super.onStart()
        authLayout = findViewById(R.id.authLayout)
        authLayout.visibility = View.VISIBLE
    }

    /**
     * Método para comprobar si hay una sesión iniciada
     */
    private fun session() {
        authLayout = findViewById(R.id.authLayout)
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val provider = prefs.getString("provider", null)
        if (email != null && provider != null) {
            authLayout.visibility = View.INVISIBLE
            showMain(email, ProviderType.valueOf(provider))
            finish() // Niega el acceso a la pantalla de autenticación
        }
    }

    /**
     * Método para configurar la interfaz de usuario y los listeners de los botones
     */
    private fun setup() {
        title = "Autenticación"
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        signUpButton = findViewById(R.id.signUpButton)
        loginButton = findViewById(R.id.loginButton)
        googleButton = findViewById(R.id.googleButton)

        // Listener para el botón de registro
        signUpButton.setOnClickListener {
            val ruaIntent = Intent(this, RegisterUserActivity::class.java)
            startActivity(ruaIntent)
        }

        // Listener para el botón de inicio de sesión
        loginButton.setOnClickListener {
            if (emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    emailEditText.text.toString(),
                    passwordEditText.text.toString()
                ).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showMain(task.result?.user?.email ?: "", ProviderType.BASIC)
                        finish() // Niega el acceso a la pantalla de autenticación
                    } else {
                        showAlert()
                    }
                }
            }
        }

        // Listener para el botón de inicio de sesión con Google
        googleButton.setOnClickListener {
            // Configuracion
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()

            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }
    }

    /**
     * Método para mostrar un diálogo de alerta en caso de error de autenticación
     */
    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    /**
     * Método para mostrar la actividad principal y guardar información de usuario en variables globales
     */
    private fun showMain(email: String, provider: ProviderType) {
        GlobalVariables.userEmail = email
        GlobalVariables.userProvider = provider.toString()
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
    }

    /**
     * Método para manejar el resultado de la actividad de inicio de sesión con Google
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            showMain(account.email ?: "", ProviderType.GOOGLE)
                            finish() // Niega el acceso a la pantalla de autenticación
                        } else {
                            showAlert()
                        }
                    }
                }
            } catch (e: ApiException) {
                showAlert()
            }
        }
    }
}