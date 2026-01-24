package es.didaktikapp.gernikapp.network

import es.didaktikapp.gernikapp.ApiConfig
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
        // Endpoints que no requieren autenticación
        private val PUBLIC_ENDPOINTS = listOf(
            ApiConfig.AUTH_LOGIN,
            ApiConfig.AUTH_REGISTER,
            ApiConfig.HEALTH
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Verificar si es un endpoint público
        val isPublicEndpoint = PUBLIC_ENDPOINTS.any { endpoint ->
            originalRequest.url.encodedPath.contains(endpoint)
        }

        if (isPublicEndpoint) {
            return chain.proceed(originalRequest)
        }

        // Añadir token a endpoints protegidos
        val token = tokenManager.getToken()
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