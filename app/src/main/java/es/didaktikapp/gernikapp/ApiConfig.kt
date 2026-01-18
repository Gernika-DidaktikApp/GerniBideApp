package es.didaktikapp.gernikapp

/**
 * Configuración de endpoints de la API.
 * La URL base se configura en local.properties o build.gradle.kts
 */
object ApiConfig {

    // URL base de la API (se configura automáticamente desde BuildConfig)
    val BASE_URL: String = BuildConfig.API_BASE_URL

    // ============ AUTENTICACIÓN ============
    /** POST - Login de usuario, devuelve JWT token */
    const val AUTH_LOGIN = "/api/v1/auth/login-app"

    /** POST - Registro de nuevo usuario */
    const val AUTH_REGISTER = "/api/v1/usuarios"

    // ============ HEALTH CHECK ============
    /** GET - Verificar estado de la API */
    const val HEALTH = "/health"

    // ============ USUARIO ============
    /** GET - Obtener perfil del usuario actual (requiere auth) */
    const val USER_PROFILE = "/api/v1/users/me"

    /** PUT - Actualizar perfil del usuario (requiere auth) */
    const val USER_UPDATE = "/api/v1/users/me"

    // ============ PROGRESO ============
    /** GET - Obtener progreso del usuario (requiere auth) */
    const val PROGRESS_GET = "/api/v1/progress"

    /** POST - Guardar progreso de actividad (requiere auth) */
    const val PROGRESS_SAVE = "/api/v1/progress"

    /** GET - Obtener puntuación máxima (requiere auth) */
    const val SCORE_TOP = "/api/v1/score/top"

    // ============ PARTIDAS ============
    /** POST - Crear nueva partida */
    const val PARTIDAS_CREATE = "/api/v1/partidas"

    /** GET - Obtener partida por ID */
    const val PARTIDAS_GET = "/api/v1/partidas/{id}"

    // ============ ESTADOS DE ACTIVIDAD ============
    /** POST - Iniciar una actividad para un jugador */
    const val ACTIVIDAD_ESTADO_INICIAR = "/api/v1/actividad-estados/iniciar"

    /** GET - Obtener estado de actividad por ID */
    const val ACTIVIDAD_ESTADO_GET = "/api/v1/actividad-estados/{id}"

    // ============ ESTADOS DE EVENTO ============
    /** POST - Iniciar un evento dentro de una actividad */
    const val EVENTO_ESTADO_INICIAR = "/api/v1/evento-estados/iniciar"

    /** PUT - Completar un evento con puntuación */
    const val EVENTO_ESTADO_COMPLETAR = "/api/v1/evento-estados/{estado_id}/completar"
}