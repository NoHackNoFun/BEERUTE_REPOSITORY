package com.example.beerute_f01

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class IAHelpActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    private lateinit var chatContainer: LinearLayout
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iahelp)

        chatContainer = findViewById(R.id.chatContainer)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)

        sendButton.setOnClickListener {
            val message = messageEditText.text.toString().trim()
            if (message.isNotEmpty()) {
                addMessageToChat(message, isUser = true)
                messageEditText.text.clear()

                sendMessageToGPT(message)
            } else {
                Snackbar.make(chatContainer, "Ingrese un mensaje", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendMessageToGPT(message: String) {
        // Configura tu lógica para enviar el mensaje a GPT y recibir la respuesta
        // Puedes usar bibliotecas como Retrofit, Volley, etc., o realizar una solicitud HTTP directa

        // Ejemplo usando OkHttp
        val url = "https://api.openai.com/v1/engines/davinci-codex/completions"
        val requestBody = FormBody.Builder()
            .add("prompt", message)
            .build()
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer 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")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                showErrorInChat()
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    val responseData = body.string()
                    val responseJson = JSONObject(responseData)
                    val completionText = responseJson.getJSONArray("choices").getJSONObject(0).getString("text")

                    addMessageToChat(completionText, isUser = false)
                } else {
                    showErrorInChat()
                }
            }
        })
    }

    @SuppressLint("InflateParams")
    private fun addMessageToChat(message: String, isUser: Boolean) {
        val messageView = LayoutInflater.from(this).inflate(R.layout.item_chat_message, null)
        val messageTextView = messageView.findViewById<TextView>(R.id.messageTextView)
        val bubbleLayout = messageView.findViewById<View>(R.id.bubbleLayout)

        messageTextView.text = message

        if (isUser) {
            bubbleLayout.setBackgroundResource(R.drawable.bg_chat_bubble_user)
        } else {
            bubbleLayout.setBackgroundResource(R.drawable.bg_chat_bubble_gpt)
        }

        chatContainer.addView(messageView)
        scrollToBottom()
    }

    private fun scrollToBottom() {
        val nestedScrollView = findViewById<NestedScrollView>(R.id.scrollView)
        nestedScrollView.post {
            nestedScrollView.fullScroll(NestedScrollView.FOCUS_DOWN)
        }
    }

    private fun showErrorInChat() {
        runOnUiThread {
            Snackbar.make(chatContainer, "Error al obtener la respuesta del servidor", Snackbar.LENGTH_SHORT).show()
        }
    }
}
