package es.didaktikapp.gernikapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.repository.AuthRepository
import es.didaktikapp.gernikapp.data.repository.GameRepository
import es.didaktikapp.gernikapp.databinding.ActivityLoginBinding
import es.didaktikapp.gernikapp.utils.Resource
import kotlinx.coroutines.launch

/**
 * Activity de inicio de sesión.
 * Gestiona la autenticación del usuario y la creación de partida inicial.
 *
 * Flujo:
 * 1. Verifica si hay sesión activa (redirige a MapaActivity)
 * 2. Muestra formulario de login
 * 3. Autentica con la API
 * 4. Crea partida automáticamente
 * 5. Navega a MapaActivity
 *
 * @property binding ViewBinding del layout activity_login.xml
 * @property authRepository Repository para autenticación
 * @property gameRepository Repository para crear partidas
 * @property tokenManager Gestor de sesión y tokens
 *
 * @author Wara Pacheco
 * @version 1.0
 */
class LoginActivity : AppCompatActivity() {

    /**  */
    private lateinit var binding: ActivityLoginBinding

    /**  */
    private lateinit var authRepository: AuthRepository

    /**  */
    private lateinit var gameRepository: GameRepository

    /**  */
    private lateinit var tokenManager: TokenManager

    /**  */
    companion object {
        private const val TAG = "LoginActivity"
    }

    /**
     *
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = AuthRepository(this)
        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)

        LogManager.write(this@LoginActivity, "LoginActivity iniciada")

        // Log del estado de la sesión al iniciar (modo DEBUG)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "🚀 LoginActivity iniciada")
            tokenManager.logSessionState(TAG)
        }

        // Si ya hay una sesión activa, ir directamente al mapa
        if (authRepository.hasActiveSession()) {

            LogManager.write(this@LoginActivity, "Sesión activa detectada, navegando al mapa")

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "➡️ Sesión activa detectada, redirigiendo al mapa...")
            }

            navigateToMap()
            return
        }

        LogManager.write(this@LoginActivity, "No hay sesión activa, mostrando formulario de login")

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "📝 No hay sesión activa, mostrando formulario de login")
        }

        setupClickListeners()
    }

    /**
     * Configura los listeners de los elementos interactivos.
     * - Botón login: Valida campos y ejecuta performLogin()
     * - Link registro: Navega a RegisterActivity
     */
    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val usuario = binding.editTextUsuario.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (usuario.isEmpty()) {
                binding.editTextUsuario.error = getString(R.string.error_username_required)
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.editTextPassword.error = getString(R.string.error_password_required)
                return@setOnClickListener
            }

            LogManager.write(this@LoginActivity, "Intento de login con usuario: $usuario")

            performLogin(usuario, password)
        }

        binding.tvRegisterLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * Ejecuta el proceso de login.
     * Si es exitoso, crea automáticamente una partida.
     *
     * @param username Nombre de usuario
     * @param password Contraseña
     */
    private fun performLogin(username: String, password: String) {
        lifecycleScope.launch {
            setLoading(true)

            when (val result = authRepository.login(username, password)) {
                is Resource.Success -> {
                    LogManager.write(this@LoginActivity, "Login exitoso para usuario: $username")

                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.login_welcome, username),
                        Toast.LENGTH_SHORT
                    ).show()

                    // Crear partida después del login exitoso
                    crearPartida()
                }

                is Resource.Error -> {
                    LogManager.write(this@LoginActivity, "Error en login: ${result.message}")

                    setLoading(false)
                    Toast.makeText(
                        this@LoginActivity,
                        result.message,
                        Toast.LENGTH_LONG
                    ).show()
                    binding.editTextPassword.text.clear()
                }

                is Resource.Loading -> {
                    // Ya está en loading
                }
            }
        }
    }

    /**
     * Obtiene la partida activa del usuario o crea una nueva si no existe.
     * Este metodo se llama automáticamente después del login exitoso.
     * Guarda el juegoId en TokenManager y navega al mapa.
     *
     * Ventajas sobre crearPartida():
     * - No falla si ya existe una partida activa
     * - Permite continuar una partida existente
     * - Simplifica la lógica del cliente
     */
    private suspend fun crearPartida() {
        val userId = tokenManager.getUserId()

        if (userId == null) {
            LogManager.write(this@LoginActivity, "Error: userId es null al crear partida")

            Toast.makeText(
                this,
                "Error: No se pudo obtener el ID de usuario",
                Toast.LENGTH_LONG
            ).show()
            setLoading(false)
            return
        }

        // Usar el endpoint obtener-o-crear en lugar de crear directamente
        when (val result = gameRepository.obtenerOCrearPartidaActiva(userId)) {
            is Resource.Success -> {
                // Guardar el ID de la partida (puede ser una existente o nueva)
                tokenManager.saveJuegoId(result.data.id)

                LogManager.write(this@LoginActivity, "Partida obtenida/creada - ID: ${result.data.id}")

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "🎮 Partida obtenida/creada exitosamente - ID: ${result.data.id}")
                    tokenManager.logSessionState(TAG)
                }

                navigateToMap()
            }

            is Resource.Error -> {
                LogManager.write(this@LoginActivity, "Error al obtener partida: ${result.message}")

                setLoading(false)
                Toast.makeText(
                    this,
                    "Error al obtener partida: ${result.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

            is Resource.Loading -> {
                // Ya está en loading
            }
        }
    }

    /**
     * Activa/desactiva el estado de carga de la UI.
     *
     * @param isLoading true para deshabilitar controles, false para habilitarlos
     */
    private fun setLoading(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.editTextUsuario.isEnabled = !isLoading
        binding.editTextPassword.isEnabled = !isLoading
    }

    /**
     * Navega al mapa y cierra esta activity.
     */
    private fun navigateToMap() {
        LogManager.write(this@LoginActivity, "Navegando a MapaActivity")

        val intent = Intent(this, MapaActivity::class.java)
        startActivity(intent)
        finish()
    }
}