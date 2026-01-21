package es.didaktikapp.gernikapp.picasso

import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.utils.Constants
import java.io.File

class MyMessageActivity : BaseMenuActivity() {

    private lateinit var messageInput: EditText
    private lateinit var characterCount: TextView
    private lateinit var sendButton: Button
    private lateinit var backButton: Button
    private lateinit var messagesContainer: LinearLayout

    private val messagesFile by lazy {
        File(getExternalFilesDir(null), Constants.Files.PEACE_MESSAGES_FILENAME)
    }

    companion object {
        private const val PREFS_NAME = "my_message_prefs"
        private const val KEY_USER_MESSAGE = "user_message"
        private const val KEY_HAS_MESSAGE = "has_message"
    }

    override fun getContentLayoutId(): Int = R.layout.picasso_my_message

    override fun onContentInflated() {
        initViews()
        setupCharacterCounter()
        setupSendButton()
        setupBackButton()
        checkForSavedMessage()
        loadMessages()
    }

    private fun initViews() {
        messageInput = contentContainer.findViewById(R.id.messageInput)
        characterCount = contentContainer.findViewById(R.id.characterCount)
        sendButton = contentContainer.findViewById(R.id.sendButton)
        backButton = contentContainer.findViewById(R.id.backButton)
        messagesContainer = contentContainer.findViewById(R.id.messagesContainer)
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
                    messageInput.setText(savedMessage)
                    messageInput.setSelection(savedMessage.length)
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
        messageInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                characterCount.text = getString(R.string.my_message_char_count_format, s?.length ?: 0)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupSendButton() {
        sendButton.setOnClickListener { sendMessage() }
    }

    private fun setupBackButton() {
        backButton.setOnClickListener { finish() }
    }

    private fun sendMessage() {
        val message = messageInput.text.toString().trim()

        if (message.isEmpty()) {
            Toast.makeText(this, getString(R.string.my_message_error_empty), Toast.LENGTH_SHORT).show()
            return
        }

        if (message.length < Constants.Messages.MIN_MESSAGE_LENGTH) {
            Toast.makeText(this, getString(R.string.my_message_error_short), Toast.LENGTH_SHORT).show()
            return
        }

        saveMessage(message)
        messageInput.text.clear()
        hideKeyboard()
        loadMessages()
        showConfirmationDialog(message)
    }

    private fun saveMessage(message: String) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean(KEY_HAS_MESSAGE, true)
            putString(KEY_USER_MESSAGE, message)
            apply()
        }

        try {
            messagesFile.appendText("$message\n")
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.my_message_error_save), Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearSavedMessage() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply()
    }

    private fun loadMessages() {
        messagesContainer.removeAllViews()

        if (!messagesFile.exists()) {
            addSampleMessages()
        }

        try {
            val messages = messagesFile.readLines().filter { it.isNotBlank() }.takeLast(Constants.Messages.MAX_DISPLAYED_MESSAGES).reversed()

            if (messages.isEmpty()) {
                addEmptyStateMessage()
            } else {
                messages.forEach { message -> addMessageView(message) }
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
        messagesContainer.addView(messageCard)
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

        messagesContainer.addView(emptyText)
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
        } catch (e: Exception) {}
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
        imm.hideSoftInputFromWindow(messageInput.windowToken, 0)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}