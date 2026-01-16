package es.didaktikapp.gernikapp

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import es.didaktikapp.gernikapp.databinding.ActivityRegisterBinding

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
     * Valida campos y muestra mensaje de éxito (TODO: integrar con API real)
     */
    private fun setupRegisterButton() {
        binding.btnRegistro.setOnClickListener {
            if (validateFields()) {
                Toast.makeText(this, getString(R.string.registro_exitoso), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /**
     * Configura el enlace "Inicia sesión" para navegar a LoginActivity
     * Crea un Intent explícito y cierra la actividad actual.
     */
    private fun setupLoginLink() {
        binding.tvLoginLink.setOnClickListener {
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
        } else if (binding.editTextPassword.text.length <= 6) {
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