package es.didaktikapp.gernikapp.network

import android.util.Log
import es.didaktikapp.gernikapp.ApiConfig
import es.didaktikapp.gernikapp.BuildConfig
import es.didaktikapp.gernikapp.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor que añade el token JWT a las peticiones autenticadas.
 * Los endpoints públicos (login, register, health) no requieren token.
 */
class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    companion object {
        private const val TAG = "AuthInterceptor"

        // Endpoints que no requieren autenticación
        private val PUBLIC_ENDPOINTS = listOf(
            ApiConfig.AUTH_LOGIN,
            ApiConfig.AUTH_REGISTER,
            ApiConfig.HEALTH
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath
        val method = originalRequest.method

        Log.d(TAG, "🌐 Interceptando: $method $path")

        // Verificar si es un endpoint público
        // Usar coincidencia exacta o inicio de path para evitar falsos positivos
        val isPublicEndpoint = when {
            // Login y Health - coincidencia exacta
            path == ApiConfig.AUTH_LOGIN || path == ApiConfig.HEALTH -> true
            // Registro - solo POST a /api/v1/usuarios (sin ID)
            path == ApiConfig.AUTH_REGISTER && method == "POST" -> true
            else -> false
        }

        if (isPublicEndpoint) {
            Log.d(TAG, "🔓 Endpoint público: $method $path")
            return chain.proceed(originalRequest)
        }

        // Añadir token a endpoints protegidos
        val token = tokenManager.getToken()
        val hasToken = token != null

        Log.d(TAG, "🔐 Endpoint protegido: $method $path | Token presente: $hasToken")
        if (!hasToken) {
            Log.e(TAG, "⚠️ NO HAY TOKEN - La petición fallará con 401")
        } else {
            val tokenPreview = "${token?.take(20)}..."
            Log.d(TAG, "🔑 Añadiendo Authorization header con token: $tokenPreview")
        }

        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}