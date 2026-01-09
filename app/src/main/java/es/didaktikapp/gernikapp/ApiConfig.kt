package es.didaktikapp.gernikapp

object ApiConfig {
    // URL base de la API (se configura automáticamente desde BuildConfig)
    val BASE_URL: String = BuildConfig.API_BASE_URL

    // Endpoints de la API
    const val AUTH_LOGIN = "/api/v1/auth/login"
    const val AUTH_REGISTER = "/api/v1/auth/register"
    const val STATS = "/api/v1/stats"

    // Función para construir URLs completas
    fun url(endpoint: String): String = "$BASE_URL$endpoint"
}