package es.didaktikapp.gernikapp

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import es.didaktikapp.gernikapp.data.models.RegisterRequest
import es.didaktikapp.gernikapp.databinding.ActivityRegisterBinding
import es.didaktikapp.gernikapp.network.ApiErrorParser
import es.didaktikapp.gernikapp.network.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Activity para el registro de nuevos usuarios en la aplicación.
 *
 * @author Erlantz
 * @version 1.0
 * @see AppCompatActivity
 * @see R.layout.activity_register
 */
class RegisterActivity : AppCompatActivity() {

    /** Binding para acceder a los elementos del layout activity_register.xml */
    private lateinit var binding: ActivityRegisterBinding

    /**
     * Metodo principal de inicialización de la Activity.
     * Configura el binding ViewBinding e inicializa todos los listeners.
     *
     * @param savedInstanceState Estado guardado de la instancia
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        LogManager.write(this@RegisterActivity, "RegisterActivity iniciada")

        setupInputFilters()
        setupClassCheckbox()
        setupRegisterButton()
        setupLoginLink()
    }

    /**
     * Configura los filtros de entrada InputFilter para restringir caracteres en cada EditText.
     *
     * - Username: Solo letras (a-z, A-Z) y números (0-9)
     * - Nombre/Apellido: Solo letras, acentos y espacios
     * - ID Clase: Solo alfanumérico + máximo 10 caracteres
     */
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

    /**
     * Configura el listener del CheckBox para mostrar/ocultar el campo ID de clase.
     */
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

    /**
     * Configura el listener del botón REGISTRARSE.
     * Valida campos y maneja errores con ApiErrorParser.
     */
    private fun setupRegisterButton() {
        binding.btnRegistro.setOnClickListener {
            if (validateFields()) {
                LogManager.write(this@RegisterActivity, "Intento de registro con usuario: ${binding.editTextUsername.text}")

                val registerRequest = RegisterRequest (
                    username = binding.editTextUsername.text.toString().trim(),
                    nombre = binding.editTextNombre.text.toString().trim(),
                    apellido = binding.editTextApellido.text.toString().trim(),
                    password = binding.editTextPassword.text.toString().trim(),
                    claseId = if (binding.checkBoxClase.isChecked &&
                                !binding.editTextIdClase.text.isBlank())
                        binding.editTextIdClase.text.toString().trim()
                    else null
                )

                binding.btnRegistro.isEnabled = false
                binding.btnRegistro.text = getString(R.string.registrandose)

                lifecycleScope.launch {
                    try {
                        val apiService = RetrofitClient.getApiService(this@RegisterActivity)
                        val response = apiService.register(registerRequest)

                        if (response.isSuccessful) {
                            LogManager.write(this@RegisterActivity, "Registro exitoso para usuario: ${registerRequest.username}")

                            Toast.makeText(this@RegisterActivity, getString(R.string.registro_exitoso), Toast.LENGTH_LONG).show()
                            val intent = Intent(this@RegisterActivity, MapaActivity::class.java)
                            startActivity(intent)
                            finish()

                        } else {
                            val errorMsg = when (response.code()) {
                                400 -> getString(R.string.error_usuario_existe)
                                401 -> getString(R.string.error_credenciales_invalidas)
                                422 -> getString(R.string.error_datos_invalidos)
                                else -> getString(R.string.error_servidor, response.code())
                            }
                            LogManager.write(this@RegisterActivity, "Error en registro: $errorMsg")

                            Toast.makeText(this@RegisterActivity, errorMsg, Toast.LENGTH_LONG).show()
                        }

                    } catch (e: HttpException) {
                        val errorBody = e.response()?.errorBody()?.string()
                        val errorMsg = ApiErrorParser.parse(errorBody, e.code(), this@RegisterActivity)

                        LogManager.write(this@RegisterActivity, "HttpException en registro: $errorMsg")

                        Toast.makeText(this@RegisterActivity, errorMsg, Toast.LENGTH_LONG).show()

                    } catch (e: Exception) {
                        LogManager.write(this@RegisterActivity, "Error de conexión en registro: ${e.message}")

                        Toast.makeText(this@RegisterActivity, getString(R.string.error_de_conexion), Toast.LENGTH_LONG).show()
                    } finally {
                        binding.btnRegistro.isEnabled = true
                        binding.btnRegistro.text = getString(R.string.izena_eman)
                    }
                }
            }
        }
    }

    /**
     * Configura el enlace "Inicia sesión" para navegar a LoginActivity
     * Crea un Intent explícito y cierra la actividad actual.
     */
    private fun setupLoginLink() {
        binding.tvLoginLink.setOnClickListener {
            LogManager.write(this@RegisterActivity, "Usuario navegó a LoginActivity desde RegisterActivity")

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * Valida todos los campos del formulario de registro.
     *
     * @return true si todos los campos son válidos, false en caso contrario
     */
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

        // Contraseña vacia
        binding.editTextPassword.error = if (binding.editTextPassword.text.isBlank()) {
            isValid = false
            getString(R.string.pasahitza_requerida)
        } else if (binding.editTextPassword.text.length < 6) {
            isValid = false
            getString(R.string.pasahitza_laburgia)
        } else null

        // ID Clase si checkbox marcado
        if (binding.checkBoxClase.isChecked && binding.editTextIdClase.text.isBlank()) {
            binding.editTextIdClase.error = getString(R.string.id_clase_requerido)
            isValid = false
        }

        return isValid
    }

}