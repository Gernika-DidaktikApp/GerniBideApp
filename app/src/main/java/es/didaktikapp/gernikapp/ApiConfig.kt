package es.didaktikapp.gernikapp

/**
 * Configuración de endpoints de la API REST.
 * Contiene todas las rutas de los endpoints organizadas por categoría.
 *
 * La URL base se configura en:
 * - local.properties: API_BASE_URL=http://...
 * - O usa el valor por defecto en build.gradle.kts
 *
 * @property BASE_URL URL base de la API (configurada en BuildConfig)
 *
 * @author Wara Pacheco
 * @version 1.0
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
    /** GET - Obtener perfil del usuario (requiere auth) */
    const val USER_PROFILE = "/api/v1/usuarios/{usuario_id}"

    /** PUT - Actualizar perfil del usuario (requiere auth) */
    const val USER_UPDATE = "/api/v1/usuarios/{usuario_id}"

    /** GET - Obtener estadísticas del usuario (requiere auth) */
    const val USER_STATS = "/api/v1/usuarios/{usuario_id}/estadisticas"

    // ============ PROGRESO DE ACTIVIDADES ============
    /** POST - Iniciar actividad dentro de un punto */
    const val ACTIVIDAD_PROGRESO_INICIAR = "/api/v1/actividad-progreso/iniciar"

    /** PUT - Completar actividad con puntuación y respuesta */
    const val ACTIVIDAD_PROGRESO_COMPLETAR = "/api/v1/actividad-progreso/{progreso_id}/completar"

    /** GET - Obtener progreso de actividad por ID */
    const val ACTIVIDAD_PROGRESO_GET = "/api/v1/actividad-progreso/{progreso_id}"

    /** PUT - Actualizar progreso de actividad (solo respuesta_contenido si está completada) */
    const val ACTIVIDAD_PROGRESO_UPDATE = "/api/v1/actividad-progreso/{progreso_id}"

    /** GET - Obtener resumen de progreso de un punto */
    const val ACTIVIDAD_PROGRESO_RESUMEN = "/api/v1/actividad-progreso/punto/{id_juego}/{id_punto}/resumen"

    // ============ PARTIDAS ============
    /** POST - Crear nueva partida */
    const val PARTIDAS_CREATE = "/api/v1/partidas"

    /** GET - Obtener partida por ID */
    const val PARTIDAS_GET = "/api/v1/partidas/{id}"

    /** POST - Obtener partida activa del usuario, o crear una si no existe */
    const val PARTIDAS_OBTENER_O_CREAR = "/api/v1/partidas/activa/usuario/{usuario_id}/obtener-o-crear"

}