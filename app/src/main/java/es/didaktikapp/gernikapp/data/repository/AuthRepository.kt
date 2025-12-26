package es.didaktikapp.gernikapp.data.repository

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.models.ApiError
import es.didaktikapp.gernikapp.data.models.LoginResponse
import es.didaktikapp.gernikapp.network.ApiService
import es.didaktikapp.gernikapp.network.RetrofitClient
import es.didaktikapp.gernikapp.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AuthRepository(context: Context) {

    private val apiService: ApiService = RetrofitClient.getApiService(context)
    private val tokenManager = TokenManager(context)
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    suspend fun login(username: String, password: String): Resource<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val loginRequest = es.didaktikapp.gernikapp.data.models.LoginRequest(
                    nombre = username,
                    contrasenya = password
                )
                val response = apiService.login(loginRequest)
                handleApiResponse(response) { loginResponse ->
                    tokenManager.saveToken(loginResponse.accessToken, loginResponse.tokenType)
                    tokenManager.saveUsername(username)
                    loginResponse
                }
            } catch (e: UnknownHostException) {
                Resource.Error("No hay conexión a internet")
            } catch (e: SocketTimeoutException) {
                Resource.Error("Tiempo de espera agotado")
            } catch (e: Exception) {
                Resource.Error("Error de conexión: ${e.localizedMessage}")
            }
        }
    }

    fun logout() {
        tokenManager.clearSession()
    }

    fun hasActiveSession(): Boolean {
        return tokenManager.hasActiveSession()
    }

    fun getUsername(): String? {
        return tokenManager.getUsername()
    }

    private fun <T> handleApiResponse(
        response: Response<T>,
        onSuccess: (T) -> T
    ): Resource<T> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Resource.Success(onSuccess(body))
            } else {
                Resource.Error("Respuesta vacía del servidor", response.code())
            }
        } else {
            val errorBody = response.errorBody()?.string()
            val errorMessage = if (errorBody != null) {
                try {
                    val apiError = moshi.adapter(ApiError::class.java).fromJson(errorBody)
                    apiError?.detail ?: "Error desconocido"
                } catch (e: Exception) {
                    "Error del servidor: ${response.code()}"
                }
            } else {
                "Error del servidor: ${response.code()}"
            }
            Resource.Error(errorMessage, response.code())
        }
    }
}