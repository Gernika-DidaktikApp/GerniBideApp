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

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var gameRepository: GameRepository
    private lateinit var tokenManager: TokenManager

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = AuthRepository(this)
        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)

        // Log del estado de la sesión al iniciar (modo DEBUG)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "🚀 LoginActivity iniciada")
            tokenManager.logSessionState(TAG)
        }

        // Si ya hay una sesión activa, ir directamente al mapa
        if (authRepository.hasActiveSession()) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "➡️ Sesión activa detectada, redirigiendo al mapa...")
            }
            navigateToMap()
            return
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "📝 No hay sesión activa, mostrando formulario de login")
        }

        setupClickListeners()
    }

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

            performLogin(usuario, password)
        }

        binding.tvRegisterLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun performLogin(username: String, password: String) {
        lifecycleScope.launch {
            setLoading(true)

            when (val result = authRepository.login(username, password)) {
                is Resource.Success -> {
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.login_welcome, username),
                        Toast.LENGTH_SHORT
                    ).show()

                    // Crear partida después del login exitoso
                    crearPartida()
                }

                is Resource.Error -> {
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

    private suspend fun crearPartida() {
        val userId = tokenManager.getUserId()

        if (userId == null) {
            Toast.makeText(
                this,
                "Error: No se pudo obtener el ID de usuario",
                Toast.LENGTH_LONG
            ).show()
            setLoading(false)
            return
        }

        when (val result = gameRepository.crearPartida(userId)) {
            is Resource.Success -> {
                // Guardar el ID de la partida
                tokenManager.saveJuegoId(result.data.id)

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "🎮 Partida creada exitosamente")
                    tokenManager.logSessionState(TAG)
                }

                navigateToMap()
            }

            is Resource.Error -> {
                setLoading(false)
                Toast.makeText(
                    this,
                    "Error al crear partida: ${result.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

            is Resource.Loading -> {
                // Ya está en loading
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.editTextUsuario.isEnabled = !isLoading
        binding.editTextPassword.isEnabled = !isLoading
    }

    private fun navigateToMap() {
        val intent = Intent(this, MapaActivity::class.java)
        startActivity(intent)
        finish()
    }
}