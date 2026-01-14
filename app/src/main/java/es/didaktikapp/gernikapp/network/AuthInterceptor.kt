package es.didaktikapp.gernikapp.network

import es.didaktikapp.gernikapp.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val publicEndpoints = listOf("/api/v1/auth/login", "/api/v1/auth/register")
        val isPublicEndpoint = publicEndpoints.any {
            originalRequest.url.encodedPath.contains(it)
        }

        if (isPublicEndpoint) {
            return chain.proceed(originalRequest)
        }

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