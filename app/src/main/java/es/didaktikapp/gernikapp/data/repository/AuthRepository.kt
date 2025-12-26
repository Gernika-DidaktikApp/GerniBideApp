package es.didaktikapp.gernikapp.data.repository

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import es.didaktikapp.gernikapp.R
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

class AuthRepository(private val context: Context) {

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
                Resource.Error(context.getString(R.string.error_no_internet))
            } catch (e: SocketTimeoutException) {
                Resource.Error(context.getString(R.string.error_timeout))
            } catch (e: Exception) {
                Resource.Error(context.getString(R.string.error_connection, e.localizedMessage ?: ""))
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
                Resource.Error(context.getString(R.string.error_empty_response), response.code())
            }
        } else {
            val errorBody = response.errorBody()?.string()
            val errorMessage = if (errorBody != null) {
                try {
                    val apiError = moshi.adapter(ApiError::class.java).fromJson(errorBody)
                    apiError?.detail ?: context.getString(R.string.error_unknown)
                } catch (e: Exception) {
                    context.getString(R.string.error_server, response.code())
                }
            } else {
                context.getString(R.string.error_server, response.code())
            }
            Resource.Error(errorMessage, response.code())
        }
    }
}