package com.example.beerute_f01

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterUserActivity : AppCompatActivity() {

    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registeruser)

        // Obtener las referencias a los elementos de la vista
        firstNameEditText = findViewById(R.id.firstNameEditText)
        lastNameEditText = findViewById(R.id.lastNameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        emailEditText = findViewById(R.id.emailEditText)
        registerButton = findViewById(R.id.registerButton)

        // Configurar el botón de registro
        registerButton.setOnClickListener {
            // Obtener los valores ingresados en los campos de texto
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()
            val email = emailEditText.text.toString().trim()

            // Validar que los campos no estén vacíos
            if (firstName.isNotEmpty() && lastName.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                // Validar que las contraseñas coincidan
                if (password == confirmPassword) {
                    val auth = FirebaseAuth.getInstance()
                    if (email != null) {
                        // Crear el usuario en Firebase Authentication con el correo y la contraseña proporcionados
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Actualizar el perfil del usuario con el nombre y apellido
                                    val user = auth.currentUser
                                    val profileUpdates = UserProfileChangeRequest.Builder()
                                        .setDisplayName("$firstName $lastName")
                                        .build()

                                    user?.updateProfile(profileUpdates)
                                        ?.addOnCompleteListener { updateTask ->
                                            if (updateTask.isSuccessful) {
                                                // Registro exitoso, iniciar la actividad principal
                                                val mainIntent = Intent(this, MainActivity::class.java)
                                                startActivity(mainIntent)
                                                finish()
                                            } else {
                                                // Mostrar una alerta en caso de error al actualizar el perfil
                                                showAlert("Error", "No se pudo actualizar el perfil de usuario")
                                            }
                                        }
                                } else {
                                    // Mostrar una alerta en caso de error al crear la cuenta de usuario
                                    showAlert("Error", "No se pudo crear la cuenta de usuario")
                                }
                            }
                    }
                } else {
                    // Mostrar una alerta en caso de que las contraseñas no coincidan
                    showAlert("Error", "Las contraseñas no coinciden")
                }
            } else {
                // Mostrar una alerta en caso de que haya campos vacíos
                showAlert("Error", "Por favor rellena todos los campos")
            }
        }
    }

    private fun showAlert(title: String, message: String) {
        // Mostrar una alerta con el título y mensaje proporcionados
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK", null)
        val dialog = builder.create()
        dialog.show()
    }
}