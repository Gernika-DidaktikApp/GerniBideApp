package es.didaktikapp.gernikapp.picasso

import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.databinding.PicassoMyMessageBinding
import es.didaktikapp.gernikapp.utils.Constants
import java.io.File

class MyMessageActivity : BaseMenuActivity() {

    private lateinit var binding: PicassoMyMessageBinding

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
        binding = PicassoMyMessageBinding.inflate(layoutInflater, contentContainer, true)
        setupCharacterCounter()
        setupSendButton()
        setupBackButton()
        checkForSavedMessage()
        loadMessages()
    }

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

    private fun setupBackButton() {
        val progressPrefs = getSharedPreferences(PROGRESS_PREFS, MODE_PRIVATE)

        // Si ya estaba completada, habilitar botón
        if (progressPrefs.getBoolean(KEY_MY_MESSAGE_COMPLETED, false)) {
            binding.btnBack.isEnabled = true
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

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
        progressPrefs.edit().putBoolean(KEY_MY_MESSAGE_COMPLETED, true).apply()

        // Limpiar input
        binding.messageInput.text.clear()

        // Ocultar teclado
        hideKeyboard()

        // Recargar mensajes
        loadMessages()

        // Mostrar mensaje de confirmación con el mensaje enviado
        showConfirmationDialog(message)
    }

    private fun saveMessage(message: String) {
        // Guardar mensaje personal del usuario
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

    private fun clearSavedMessage() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

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
            setTextColor(Color.parseColor("#1976D2"))
            setPadding(dpToPx(24), dpToPx(16), dpToPx(24), dpToPx(16))
            gravity = Gravity.START
        }

        messageCard.addView(messageText)
        binding.messagesContainer.addView(messageCard)
    }

    private fun addEmptyStateMessage() {
        val emptyText = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = getString(R.string.my_message_empty_state)
            textSize = 16f
            setTextColor(Color.parseColor("#90A4AE"))
            gravity = Gravity.CENTER
            setPadding(dpToPx(24), dpToPx(48), dpToPx(24), dpToPx(48))
        }

        binding.messagesContainer.addView(emptyText)
    }

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

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.messageInput.windowToken, 0)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}