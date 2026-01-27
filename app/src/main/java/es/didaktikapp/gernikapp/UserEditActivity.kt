package es.didaktikapp.gernikapp

import android.text.InputFilter
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.models.UpdateUserRequest
import es.didaktikapp.gernikapp.data.models.UserResponse
import es.didaktikapp.gernikapp.data.repository.UserRepository
import es.didaktikapp.gernikapp.databinding.ActivityUserEditBinding
import es.didaktikapp.gernikapp.utils.Resource
import kotlinx.coroutines.launch

/**
 * Activity para la edición de datos de usuario.
 */
class UserEditActivity : BaseMenuActivity() {

    private lateinit var binding: ActivityUserEditBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var userRepository: UserRepository

    override fun onContentInflated() {
        binding = ActivityUserEditBinding.inflate(layoutInflater, contentContainer, true)

        tokenManager = TokenManager(this)
        userRepository = UserRepository(this)

        setupInputFilters()
        setupClaseToggle()
        setupSaveButton()

        loadUserDataFromApi()
    }

    private fun loadUserDataFromApi() {
        lifecycleScope.launch {
            when (val result = userRepository.getUserProfile()) {
                is Resource.Success -> {
                    result.data?.let { user ->
                        populateFields(user)
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(
                        this@UserEditActivity,
                        result.message ?: getString(R.string.error_cargar_datos),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Resource.Loading -> { /* No-op */ }
            }
        }
    }

    private fun populateFields(user: UserResponse) {
        binding.editTextUsername.setText(user.username)
        binding.editTextNombre.setText(user.nombre)
        binding.editTextApellido.setText(user.apellido)

        // Cargar clase_id si existe
        if (!user.claseId.isNullOrEmpty()) {
            binding.checkBoxClase.isChecked = true
            binding.editTextIdClase.setText(user.claseId)
            binding.editTextIdClase.visibility = View.VISIBLE
        }
    }

    private fun setupClaseToggle() {
        binding.checkBoxClase.setOnCheckedChangeListener { _, isChecked ->
            binding.editTextIdClase.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) {
                binding.editTextIdClase.text.clear()
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnGuardar.setOnClickListener {
            if (validateFields()) {
                saveUserData()
            }
        }
    }

    private fun saveUserData() {
        // Obtener valores actualizados
        val username = binding.editTextUsername.text.toString().trim()
        val nombre = binding.editTextNombre.text.toString().trim()
        val apellido = binding.editTextApellido.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim().ifEmpty { null }
        val claseId = if (binding.checkBoxClase.isChecked) {
            binding.editTextIdClase.text.toString().trim().ifEmpty { null }
        } else {
            null
        }

        // Crear request solo con los campos a actualizar
        val updateRequest = UpdateUserRequest(
            username = username,
            nombre = nombre,
            apellido = apellido,
            password = password,
            idClase = claseId
        )

        lifecycleScope.launch {
            when (val result = userRepository.updateUserProfile(updateRequest)) {
                is Resource.Success -> {
                    // Actualizar username en TokenManager
                    tokenManager.saveUsername(username)

                    Toast.makeText(
                        this@UserEditActivity,
                        getString(R.string.update_success),
                        Toast.LENGTH_SHORT
                    ).show()

                    // Limpiar campo de password por seguridad
                    binding.editTextPassword.text.clear()

                    finish()
                }
                is Resource.Error -> {
                    Toast.makeText(
                        this@UserEditActivity,
                        result.message ?: getString(R.string.error_actualizar),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Resource.Loading -> { /* No-op */ }
            }
        }
    }

    private fun setupInputFilters() {
        // Username: solo letras y números
        binding.editTextUsername.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            if (source.matches(Regex("[a-zA-Z0-9]*"))) null else ""
        })

        // Nombre y Apellido: solo letras (incluyendo caracteres especiales vascos/españoles)
        val alphaFilter = InputFilter { source, _, _, _, _, _ ->
            if (source.matches(Regex("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*"))) null else ""
        }
        binding.editTextNombre.filters = arrayOf(alphaFilter)
        binding.editTextApellido.filters = arrayOf(alphaFilter)
    }

    private fun validateFields(): Boolean {
        var isValid = true

        if (binding.editTextUsername.text.isBlank()) {
            binding.editTextUsername.error = getString(R.string.nombre_usuario_requerido)
            isValid = false
        }
        if (binding.editTextNombre.text.isBlank()) {
            binding.editTextNombre.error = getString(R.string.nombre_requerido)
            isValid = false
        }
        if (binding.editTextApellido.text.isBlank()) {
            binding.editTextApellido.error = getString(R.string.apellido_requerido)
            isValid = false
        }
        if (binding.editTextPassword.text.isNotEmpty() && binding.editTextPassword.text.length < 6) {
            binding.editTextPassword.error = getString(R.string.pasahitza_laburgia)
            isValid = false
        }
        if (binding.checkBoxClase.isChecked && binding.editTextIdClase.text.isBlank()) {
            binding.editTextIdClase.error = getString(R.string.clase_id_requerido)
            isValid = false
        }

        return isValid
    }
}