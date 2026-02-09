package es.didaktikapp.gernikapp.picasso

import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.LogManager
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.repository.GameRepository
import es.didaktikapp.gernikapp.databinding.PicassoMyMessageBinding
import es.didaktikapp.gernikapp.utils.Constants
import es.didaktikapp.gernikapp.ZoneCompletionActivity
import es.didaktikapp.gernikapp.utils.Constants.Puntos
import es.didaktikapp.gernikapp.utils.Resource
import es.didaktikapp.gernikapp.utils.ZoneConfig
import kotlinx.coroutines.launch
import java.io.File

/**
 * Activity para escribir mensajes de paz.
 * Permite al usuario escribir un mensaje corto sobre paz que se guarda y se muestra
 * junto con otros mensajes de usuarios anteriores.
 *
 * Características:
 * - Editor de texto con contador de caracteres
 * - Validación de longitud mínima (Constants.Messages.MIN_MESSAGE_LENGTH)
 * - Guardado del mensaje personal del usuario
 * - Visualización de mensajes recientes en tarjetas (CardView)
 * - Mensajes de ejemplo precargados si no existen mensajes previos
 * - Límite de mensajes mostrados (Constants.Messages.MAX_DISPLAYED_MESSAGES)
 *
 * @property binding ViewBinding del layout picasso_my_message.xml
 * @property gameRepository Repositorio para gestionar eventos del juego
 * @property tokenManager Gestor de tokens JWT y juegoId
 * @property actividadProgresoId ID del estado del evento actual (puede ser null)
 * @property messagesFile Archivo externo donde se almacenan todos los mensajes
 *
 * Condiciones:
 * - Requiere SharedPreferences "my_message_prefs" para el mensaje del usuario
 * - Requiere SharedPreferences "picasso_progress" para marcar actividad completada
 * - Guarda mensajes en archivo externo: Constants.Files.PEACE_MESSAGES_FILENAME
 * - Longitud mínima del mensaje: Constants.Messages.MIN_MESSAGE_LENGTH caracteres
 * - Muestra máximo Constants.Messages.MAX_DISPLAYED_MESSAGES mensajes recientes
 *
 * @author Wara Pacheco
 */
class MyMessageActivity : BaseMenuActivity() {

    private lateinit var binding: PicassoMyMessageBinding
    private lateinit var gameRepository: GameRepository
    private lateinit var tokenManager: TokenManager
    private var actividadProgresoId: String? = null

    private val messagesFile by lazy {
        File(getExternalFilesDir(null), Constants.Files.PEACE_MESSAGES_FILENAME)
    }

    companion object {
        private const val PREFS_NAME = "my_message_prefs"
        private const val KEY_USER_MESSAGE = "user_message"
        private const val KEY_HAS_MESSAGE = "has_message"
        private const val PROGRESS_PREFS = "picasso_progress"
        private const val KEY_MY_MESSAGE_COMPLETED = "my_message_completed"
    }

    override fun onContentInflated() {
        LogManager.write(this@MyMessageActivity, "MyMessageActivity iniciada")

        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)
        binding = PicassoMyMessageBinding.inflate(layoutInflater, contentContainer, true)
        iniciarActividad()
        setupCharacterCounter()
        setupSendButton()
        setupBackButton()
        checkForSavedMessage()
        loadMessages()
    }

    /**
     * Verifica si el usuario tiene un mensaje guardado previamente.
     * Si existe, muestra un diálogo preguntando si quiere editar el mensaje existente
     * o escribir uno nuevo.
     */
    private fun checkForSavedMessage() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val hasMessage = prefs.getBoolean(KEY_HAS_MESSAGE, false)

        if (hasMessage) {
            val savedMessage = prefs.getString(KEY_USER_MESSAGE, "") ?: ""

            AlertDialog.Builder(this)
                .setTitle(getString(R.string.my_message_load_title))
                .setMessage(getString(R.string.my_message_load_message, savedMessage))
                .setPositiveButton(getString(R.string.my_message_load_edit)) { dialog, _ ->
                    dialog.dismiss()
                    binding.messageInput.setText(savedMessage)
                    binding.messageInput.setSelection(savedMessage.length)
                }
                .setNegativeButton(getString(R.string.my_message_load_new)) { dialog, _ ->
                    dialog.dismiss()
                    clearSavedMessage()
                }
                .setCancelable(false)
                .show()
        }
    }

    /**
     * Configura el contador de caracteres en tiempo real.
     * Actualiza el TextView con el formato "X caracteres".
     */
    private fun setupCharacterCounter() {
        binding.messageInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.characterCount.text = getString(R.string.my_message_char_count_format, s?.length ?: 0)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupSendButton() {
        binding.sendButton.setOnClickListener {
            sendMessage()
        }
    }

    /**
     * Configura el botón de retroceso.
     * Solo se habilita si la actividad ya fue completada anteriormente.
     */
    private fun setupBackButton() {
        val progressPrefs = getSharedPreferences(PROGRESS_PREFS, MODE_PRIVATE)

        // Si ya estaba completada, habilitar botón
        if (progressPrefs.getBoolean(KEY_MY_MESSAGE_COMPLETED, false)) {
            binding.btnBack.isEnabled = true
        }

        binding.btnBack.setOnClickListener {
            val intent = android.content.Intent(this, MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    /**
     * Envía el mensaje escrito por el usuario.
     *
     * Validaciones:
     * - No puede estar vacío
     * - Debe tener al menos Constants.Messages.MIN_MESSAGE_LENGTH caracteres
     *
     * Proceso:
     * 1. Valida el mensaje
     * 2. Guarda en SharedPreferences y archivo compartido
     * 3. Habilita botón de retroceso y marca actividad completada
     * 4. Completa el evento en la API
     * 5. Limpia el input y oculta el teclado
     * 6. Recarga la lista de mensajes
     * 7. Muestra diálogo de confirmación
     */
    private fun sendMessage() {
        val message = binding.messageInput.text.toString().trim()

        if (message.isEmpty()) {
            Toast.makeText(this, getString(R.string.my_message_error_empty), Toast.LENGTH_SHORT).show()
            return
        }

        if (message.length < Constants.Messages.MIN_MESSAGE_LENGTH) {
            Toast.makeText(this, getString(R.string.my_message_error_short), Toast.LENGTH_SHORT).show()
            return
        }

        // Guardar mensaje
        saveMessage(message)

        // Habilitar botón y guardar progreso
        binding.btnBack.isEnabled = true
        val progressPrefs = getSharedPreferences(PROGRESS_PREFS, MODE_PRIVATE)
        progressPrefs.edit()
            .putBoolean(KEY_MY_MESSAGE_COMPLETED, true)
            .putFloat("my_message_score", 100f)
            .apply()
        ZoneCompletionActivity.launchIfComplete(this, ZoneConfig.PICASSO)
        completarActividad()

        // Limpiar input
        binding.messageInput.text.clear()

        // Ocultar teclado
        hideKeyboard()

        // Recargar mensajes
        loadMessages()

        // Mostrar mensaje de confirmación con el mensaje enviado
        showConfirmationDialog(message)
    }

    /**
     * Guarda el mensaje del usuario en dos lugares:
     * 1. SharedPreferences personal del usuario (para recordar su mensaje)
     * 2. Archivo compartido externo (para mostrar en la lista general)
     *
     * @param message Texto del mensaje a guardar
     */
    private fun saveMessage(message: String) {
        // Guardar mensaje personal del usuario
        LogManager.write(this@MyMessageActivity, "Mensaje guardado localmente en Picasso")

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean(KEY_HAS_MESSAGE, true)
            putString(KEY_USER_MESSAGE, message)
            apply()
        }

        // Guardar en archivo compartido
        try {
            messagesFile.appendText("$message\n")
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.my_message_error_save), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Limpia el mensaje personal guardado del usuario en SharedPreferences.
     * No elimina el mensaje del archivo compartido.
     */
    private fun clearSavedMessage() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    /**
     * Carga y muestra los mensajes más recientes del archivo.
     *
     * Proceso:
     * 1. Si el archivo no existe, crea mensajes de ejemplo
     * 2. Lee las últimas N líneas (Constants.Messages.MAX_DISPLAYED_MESSAGES)
     * 3. Las muestra en orden inverso (más recientes primero)
     * 4. Si no hay mensajes, muestra mensaje de estado vacío
     */
    private fun loadMessages() {
        binding.messagesContainer.removeAllViews()

        if (!messagesFile.exists()) {
            addSampleMessages()
        }

        try {
            val messages = messagesFile.readLines().filter { it.isNotBlank() }.takeLast(Constants.Messages.MAX_DISPLAYED_MESSAGES).reversed()

            if (messages.isEmpty()) {
                addEmptyStateMessage()
            } else {
                messages.forEach { message ->
                    addMessageView(message)
                }
            }
        } catch (e: Exception) {
            addEmptyStateMessage()
        }
    }

    /**
     * Crea y añade una tarjeta (CardView) con un mensaje al contenedor.
     * Diseño: fondo azul claro (#E3F2FD), texto azul (#1976D2), bordes redondeados.
     *
     * @param message Texto del mensaje a mostrar
     */
    private fun addMessageView(message: String) {
        val messageCard = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dpToPx(16))
            }
            radius = dpToPx(12).toFloat()
            cardElevation = dpToPx(4).toFloat()
            setCardBackgroundColor(Color.parseColor("#E3F2FD"))
        }

        val messageText = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "\"$message\""
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@MyMessageActivity, R.color.btnSecundario))
            setPadding(dpToPx(24), dpToPx(16), dpToPx(24), dpToPx(16))
            gravity = Gravity.START
        }

        messageCard.addView(messageText)
        binding.messagesContainer.addView(messageCard)
    }

    /**
     * Añade un TextView centrado con mensaje de estado vacío cuando no hay mensajes.
     * Texto gris (#90A4AE) indicando que aún no hay mensajes.
     */
    private fun addEmptyStateMessage() {
        val emptyText = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = getString(R.string.my_message_empty_state)
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@MyMessageActivity, R.color.grayLight))
            gravity = Gravity.CENTER
            setPadding(dpToPx(24), dpToPx(48), dpToPx(24), dpToPx(48))
        }

        binding.messagesContainer.addView(emptyText)
    }

    /**
     * Crea el archivo de mensajes con 4 mensajes de ejemplo predefinidos.
     * Se ejecuta solo la primera vez que se accede a la actividad.
     * Los mensajes de ejemplo se obtienen de strings.xml para soporte multiidioma.
     */
    private fun addSampleMessages() {
        val sampleMessages = listOf(
            getString(R.string.my_message_sample_1),
            getString(R.string.my_message_sample_2),
            getString(R.string.my_message_sample_3),
            getString(R.string.my_message_sample_4)
        )

        try {
            messagesFile.writeText(sampleMessages.joinToString("\n") + "\n")
        } catch (e: Exception) {
            // Ignorar error
        }
    }

    /**
     * Muestra un diálogo de confirmación después de enviar el mensaje.
     * Pregunta al usuario si quiere mantener el mensaje o escribir uno nuevo.
     *
     * @param message El mensaje que acaba de enviar el usuario
     */
    private fun showConfirmationDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.my_message_confirm_title))
            .setMessage(getString(R.string.my_message_confirm_message, message))
            .setPositiveButton(getString(R.string.my_message_confirm_keep)) { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.my_message_confirm_new)) { dialog, _ ->
                dialog.dismiss()
                clearSavedMessage()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Oculta el teclado virtual después de enviar el mensaje.
     */
    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.messageInput.windowToken, 0)
    }

    /**
     * Convierte density-independent pixels (dp) a píxeles de pantalla.
     *
     * @param dp Valor en dp a convertir
     * @return Valor equivalente en píxeles
     */
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    /**
     * Inicia el evento en la API del juego.
     * Requiere un juegoId válido del TokenManager.
     * Guarda el actividadProgresoId devuelto por la API para completar el evento después.
     *
     * IDs utilizados:
     * - idActividad: Puntos.Picasso.ID
     * - idEvento: Puntos.Picasso.MY_MESSAGE
     */
    private fun iniciarActividad() {
        val juegoId = tokenManager.getJuegoId() ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.iniciarActividad(juegoId, Puntos.Picasso.ID, Puntos.Picasso.MY_MESSAGE)) {
                is Resource.Success -> {
                    actividadProgresoId = result.data.id
                    LogManager.write(this@MyMessageActivity, "API iniciarActividad PICASSO_MY_MESSAGE id=$actividadProgresoId")
                }
                is Resource.Error -> {
                    Log.e("MyMessage", "Error: ${result.message}")
                    LogManager.write(this@MyMessageActivity, "Error iniciarActividad PICASSO_MY_MESSAGE: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }

    /**
     * Completa el evento en la API del juego.
     * Requiere un actividadProgresoId válido obtenido de iniciarActividad().
     *
     * @see iniciarActividad
     * Puntuación enviada: 100.0 (actividad completada)
     */
    private fun completarActividad() {
        val estadoId = actividadProgresoId ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.completarActividad(estadoId, 100.0)) {
                is Resource.Success -> {
                    Log.d("MyMessage", "Completado")
                    LogManager.write(this@MyMessageActivity, "API completarActividad PICASSO_MY_MESSAGE")
                }
                is Resource.Error -> {
                    Log.e("MyMessage", "Error: ${result.message}")
                    LogManager.write(this@MyMessageActivity, "Error completarActividad PICASSO_MY_MESSAGE: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }
}