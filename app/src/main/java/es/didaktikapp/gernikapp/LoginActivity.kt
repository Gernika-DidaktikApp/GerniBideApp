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
import es.didaktikapp.gernikapp.data.repository.UserRepository
import es.didaktikapp.gernikapp.databinding.ActivityLoginBinding
import es.didaktikapp.gernikapp.utils.Resource
import es.didaktikapp.gernikapp.utils.SyncManager
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

    /** ViewBinding para acceder a las vistas del layout de login. */
    private lateinit var binding: ActivityLoginBinding

    /** Repository encargado de gestionar la autenticación (login, logout, tokens). */
    private lateinit var authRepository: AuthRepository

    /** Repository responsable de obtener o crear partidas activas del usuario. */
    private lateinit var gameRepository: GameRepository

    /** Repository que gestiona la obtención y sincronización del perfil del usuario. */
    private lateinit var userRepository: UserRepository

    /** Gestor de sesión: almacena tokens, IDs y estado persistente del usuario. */
    private lateinit var tokenManager: TokenManager

    /** Tag para logs de depuración específicos de esta Activity. */
    companion object {
        private const val TAG = "LoginActivity"
    }

    /**
     * Metodo principal de inicialización de la Activity.
     * - Infla el layout
     * - Inicializa repositorios y gestores
     * - Comprueba si existe sesión activa
     * - Configura listeners del formulario
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = AuthRepository(this)
        gameRepository = GameRepository(this)
        userRepository = UserRepository(this)
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
     * Si es exitoso, sincroniza el progreso del usuario y crea automáticamente una partida.
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

                    // Sincronizar progreso del servidor
                    syncUserProgress()

                    // Crear partida después del login exitoso
                    crearPartida()
                }

                is Resource.Error -> {
                    LogManager.write(this@LoginActivity, "Error en login (${result.code}): ${result.message}")

                    setLoading(false)

                    // Mostrar mensaje amigable según el código de error
                    val errorMsg = when (result.code) {
                        401 -> getString(R.string.error_login_credenciales)
                        422 -> getString(R.string.error_datos_invalidos)
                        else -> getString(R.string.error_login_servidor)
                    }

                    Toast.makeText(
                        this@LoginActivity,
                        errorMsg,
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
     * Sincroniza el progreso del usuario del servidor a SharedPreferences locales.
     *
     * Este metodo se ejecuta después del login exitoso para recuperar
     * el progreso del usuario desde el servidor y sincronizarlo localmente.
     *
     * Si falla la sincronización, no bloquea el flujo - continúa con datos locales.
     */
    private suspend fun syncUserProgress() {
        when (val result = userRepository.getPerfilProgreso()) {
            is Resource.Success -> {
                // Sincronizar datos del servidor a SharedPreferences
                SyncManager.syncPerfilProgreso(this@LoginActivity, result.data)

                LogManager.write(this@LoginActivity, "✅ Progreso sincronizado: ${result.data.estadisticas.actividadesCompletadas} actividades")

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "📊 Progreso del usuario:")
                    Log.d(TAG, "  - TopScore: ${result.data.usuario.topScore}")
                    Log.d(TAG, "  - Actividades: ${result.data.estadisticas.actividadesCompletadas}/${result.data.estadisticas.totalActividadesDisponibles}")
                    Log.d(TAG, "  - Racha: ${result.data.estadisticas.rachaDias} días")
                    Log.d(TAG, "  - Puntos: ${result.data.estadisticas.totalPuntosAcumulados}")
                }
            }

            is Resource.Error -> {
                // No bloquear el flujo si falla la sincronización
                LogManager.write(this@LoginActivity, "⚠️ No se pudo sincronizar progreso: ${result.message}")
                Log.w(TAG, "Sincronización fallida, usando datos locales: ${result.message}")

                // Opcional: Mostrar notificación al usuario
                // Toast.makeText(this, "No se pudo sincronizar progreso", Toast.LENGTH_SHORT).show()
            }

            is Resource.Loading -> { /* Ya manejado */ }
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
                LogManager.write(this@LoginActivity, "Error al obtener partida (${result.code}): ${result.message}")

                setLoading(false)
                Toast.makeText(
                    this,
                    getString(R.string.error_crear_partida),
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