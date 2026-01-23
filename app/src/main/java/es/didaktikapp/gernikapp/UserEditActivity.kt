package es.didaktikapp.gernikapp

import android.text.InputFilter
import android.view.View
import android.widget.Toast
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.databinding.ActivityUserEditBinding

/**
 * Activity para la edición de datos de usuario.
 * @author Erlantz
 */
class UserEditActivity : BaseMenuActivity() {

    private lateinit var binding: ActivityUserEditBinding
    private lateinit var tokenManager: TokenManager

    override fun onContentInflated() {
        binding = ActivityUserEditBinding.inflate(layoutInflater, contentContainer, true)
        tokenManager = TokenManager(this)

        setupInputFilters()
        setupClassCheckbox()
        setupSaveButton()
        loadUserData()
    }

    private fun loadUserData() {
        // Cargar datos del usuario actual
        // Recuperamos el username guardado en local
        val username = tokenManager.getUsername()
        if (!username.isNullOrEmpty()) {
            binding.editTextUsername.setText(username)
        }
        
        // Mock data for other fields as they are not stored locally currently
        // In a real app, these would come from an API call
        // For demonstration, we populate some defaults if it looks empty
        if (binding.editTextNombre.text.isEmpty()) {
            binding.editTextNombre.setText("User") 
        }
        if (binding.editTextApellido.text.isEmpty()) {
            binding.editTextApellido.setText("Gernika")
        }
    }

    private fun setupInputFilters() {
        // Username: SOLO letras + números
        binding.editTextUsername.filters = arrayOf(
            InputFilter { source, _, _, _, _, _ ->
                if (source.matches(Regex("[a-zA-Z0-9]*"))) null else ""
            }
        )

        // Nombre: SOLO letras (acentos + espacios)
        binding.editTextNombre.filters = arrayOf(
            InputFilter { source, _, _, _, _, _ ->
                if (source.matches(Regex("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*"))) null else ""
            }
        )

        // Apellido: SOLO letras (acentos + espacios)
        binding.editTextApellido.filters = arrayOf(
            InputFilter { source, _, _, _, _, _ ->
                if (source.matches(Regex("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*"))) null else ""
            }
        )

        // ID Clase: SOLO letras + números (máx 10)
        binding.editTextIdClase.filters = arrayOf(
            InputFilter { source, _, _, _, _, _ ->
                if (source.matches(Regex("[a-zA-Z0-9]*"))) null else ""
            },
            InputFilter.LengthFilter(10)
        )
    }

    private fun setupClassCheckbox() {
        binding.checkBoxClase.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.editTextIdClase.visibility = View.VISIBLE
            } else {
                binding.editTextIdClase.visibility = View.GONE
                binding.editTextIdClase.text.clear()
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnGuardar.setOnClickListener {
            if (validateFields()) {
                // TODO: Save changes to API
                
                // Update local username if changed
                val newUsername = binding.editTextUsername.text.toString().trim()
                tokenManager.saveUsername(newUsername)

                Toast.makeText(this, getString(R.string.update_success), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true

        // Username vacío o inválido
        binding.editTextUsername.error = if (binding.editTextUsername.text.isBlank()) {
            isValid = false
            getString(R.string.nombre_usuario_requerido)
        } else null

        // Nombre vacío
        binding.editTextNombre.error = if (binding.editTextNombre.text.isBlank()) {
            isValid = false
            getString(R.string.nombre_requerido)
        } else null

        // Apellido vacío
        binding.editTextApellido.error = if (binding.editTextApellido.text.isBlank()) {
            isValid = false
            getString(R.string.apellido_requerido)
        } else null

        // Password validation only if not empty (user wants to change it)
        if (binding.editTextPassword.text.isNotEmpty()) {
             if (binding.editTextPassword.text.length <= 6) {
                binding.editTextPassword.error = getString(R.string.pasahitza_laburgia)
                isValid = false
            } else {
                binding.editTextPassword.error = null
            }
        }

        // ID Clase si checkbox marcado
        if (binding.checkBoxClase.isChecked && binding.editTextIdClase.text.isBlank()) {
            binding.editTextIdClase.error = getString(R.string.id_clase_requerido)
            isValid = false
        }

        return isValid
    }
}
