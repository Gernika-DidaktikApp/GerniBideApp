package es.didaktikapp.gernikapp

import android.text.InputFilter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.models.UserResponse
import es.didaktikapp.gernikapp.databinding.ActivityUserEditBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

// Interfaz para la API
interface ApiService {
    @GET("api/v1/users/me")
    suspend fun getCurrentUser(): Response<UserResponse>

    @PUT("api/v1/users/me")
    suspend fun updateCurrentUser(@Body updates: Map<String, String?>): Response<UserResponse>
}

/**
 * Activity para la edición de datos de usuario.
 */
class UserEditActivity : BaseMenuActivity() {

    private lateinit var binding: ActivityUserEditBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var apiService: ApiService

    override fun onContentInflated() {
        binding = ActivityUserEditBinding.inflate(layoutInflater, contentContainer, true)

        tokenManager = TokenManager(this)
        initRetrofit()

        setupInputFilters()
        setupSaveButton()

        loadUserDataFromApi()
    }

    private fun initRetrofit() {
        val authInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
            val token = tokenManager.getToken()
            if (!token.isNullOrEmpty()) {
                request.addHeader("Authorization", "Bearer $token")
            }
            chain.proceed(request.build())
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        apiService = Retrofit.Builder()
            .baseUrl("https://gernibide.up.railway.app/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private fun loadUserDataFromApi() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { apiService.getCurrentUser() }
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    binding.editTextUsername.setText(user.username)
                    binding.editTextNombre.setText(user.nombre)
                    binding.editTextApellido.setText(user.apellido)
                }
            } catch (e: Exception) {
                Toast.makeText(this@UserEditActivity, "Error de red", Toast.LENGTH_SHORT).show()
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
        val updateData = mutableMapOf<String, String?>(
            "username" to binding.editTextUsername.text.toString().trim(),
            "nombre" to binding.editTextNombre.text.toString().trim(),
            "apellido" to binding.editTextApellido.text.toString().trim()
        )

        val pass = binding.editTextPassword.text.toString()
        if (pass.isNotEmpty()) {
            updateData["password"] = pass
        }

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { apiService.updateCurrentUser(updateData) }
                if (response.isSuccessful) {
                    tokenManager.saveUsername(updateData["username"] ?: "")
                    Toast.makeText(this@UserEditActivity, getString(R.string.update_success), Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@UserEditActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@UserEditActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupInputFilters() {
        binding.editTextUsername.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            if (source.matches(Regex("[a-zA-Z0-9]*"))) null else ""
        })

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

        return isValid
    }
}