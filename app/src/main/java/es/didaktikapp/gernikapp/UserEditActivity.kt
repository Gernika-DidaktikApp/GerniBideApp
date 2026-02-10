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
 * Pantalla de edición de datos del usuario.
 * Permite modificar nombre, apellido, clase y otros datos del perfil,
 * validando la entrada y enviando los cambios a la API.
 *
 * @author Wara Pacheco
 * @version 1.0
 * @see UserRepository
 */
class UserEditActivity : BaseMenuActivity() {

    /** Binding de la vista de edición de usuario. */
    private lateinit var binding: ActivityUserEditBinding

    /** Gestor de tokens de autenticación. */
    private lateinit var tokenManager: TokenManager

    /** Repositorio para operaciones de usuario contra la API. */
    private lateinit var userRepository: UserRepository

    /** Inicializa las vistas, filtros de entrada y carga los datos del usuario. */
    override fun onContentInflated() {
        binding = ActivityUserEditBinding.inflate(layoutInflater, contentContainer, true)

        LogManager.write(this@UserEditActivity, "UserEditActivity iniciada")

        tokenManager = TokenManager(this)
        userRepository = UserRepository(this)

        setupInputFilters()
        setupClaseToggle()
        setupSaveButton()

        loadUserDataFromApi()
    }

    /**
     * Obtiene los datos del usuario desde la API y los muestra en el formulario.
     */
    private fun loadUserDataFromApi() {
        lifecycleScope.launch {
            when (val result = userRepository.getUserProfile()) {
                is Resource.Success -> {
                    populateFields(result.data)
                }
                is Resource.Error -> {
                    LogManager.write( this@UserEditActivity, "Error cargando datos de usuario: ${result.message}" )

                    Toast.makeText(
                        this@UserEditActivity,
                        result.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Resource.Loading -> { /* No-op */ }
            }
        }
    }

    /**
     * Rellena los campos del formulario con los datos del usuario.
     *
     * @param user Datos del usuario obtenidos de la API.
     */
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

    /**
     * Configura el comportamiento del checkbox de clase.
     * Si está activado, muestra el campo de ID de clase.
     * Si se desactiva, limpia y oculta el campo.
     */
    private fun setupClaseToggle() {
        binding.checkBoxClase.setOnCheckedChangeListener { _, isChecked ->
            binding.editTextIdClase.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) {
                binding.editTextIdClase.text.clear()
            }
        }
    }

    /**
     * Configura el botón de guardar.
     * Antes de enviar los datos, valida los campos.
     */
    private fun setupSaveButton() {
        binding.btnGuardar.setOnClickListener {
            if (validateFields()) {
                saveUserData()
            }
        }
    }

    /**
     * Envía los datos actualizados del usuario a la API.
     */
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
                    LogManager.write(
                        this@UserEditActivity,
                        "Perfil actualizado correctamente para usuario: $username"
                    )

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
                    LogManager.write(
                        this@UserEditActivity,
                        "Error actualizando perfil: ${result.message}"
                    )

                    Toast.makeText(
                        this@UserEditActivity,
                        result.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Resource.Loading -> { /* No-op */ }
            }
        }
    }

    /**
     * Configura filtros de entrada para validar caracteres permitidos.
     */
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

    /**
     * Valida los campos del formulario antes de guardar.
     *
     * @return true si todos los campos son válidos, false si hay errores.
     */
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