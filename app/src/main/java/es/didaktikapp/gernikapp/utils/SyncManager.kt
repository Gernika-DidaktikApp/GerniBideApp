package es.didaktikapp.gernikapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import es.didaktikapp.gernikapp.LogManager
import es.didaktikapp.gernikapp.data.models.ActividadDetalle
import es.didaktikapp.gernikapp.data.models.PerfilProgresoResponse
import es.didaktikapp.gernikapp.utils.Constants.Puntos

/**
 * Manager para sincronizar el progreso del usuario entre servidor y almacenamiento local.
 *
 * Este objeto singleton se encarga de sincronizar el estado del usuario obtenido desde
 * el endpoint `/api/v1/usuarios/{usuario_id}/perfil-progreso` con las SharedPreferences
 * locales de cada módulo.
 *
 * **Funcionalidad principal:**
 * - Sincroniza estadísticas globales (actividades completadas, racha, puntos totales)
 * - Mapea progreso de cada módulo (Árbol, Bunkers, Picasso, Plaza, Frontón)
 * - Convierte estados de actividades del servidor a formato local
 * - Permite recuperar progreso al cambiar de dispositivo
 *
 * **Uso típico:**
 * ```kotlin
 * // Al hacer login:
 * val perfilProgreso = userRepository.getPerfilProgreso()
 * SyncManager.syncPerfilProgreso(context, perfilProgreso.data)
 *
 * // Al cerrar sesión:
 * SyncManager.clearAllProgress(context)
 * ```
 *
 * **Mapeo de módulos:**
 * - `arbol_progress`: Actividades del módulo Árbol de Gernika
 * - `bunkers_progress`: Actividades del módulo Refugios
 * - `picasso_progress`: Actividades del módulo Guernica/Picasso
 * - `plaza_progress`: Actividades del módulo Plaza/Mercado
 * - `fronton_progress`: Actividades del módulo Frontón/Pelota Vasca
 *
 * @author Wara Pacheco
 * @version 1.0
 * @since 2026-02-10
 */
object SyncManager {

    private const val TAG = "SyncManager"
    private const val PREFS_PROFILE = "user_profile"

    /**
     * Sincroniza los datos del perfil completo con SharedPreferences locales.
     *
     * Este metodo es el punto de entrada principal del SyncManager. Procesa la respuesta
     * del endpoint `/perfil-progreso` y la distribuye en las SharedPreferences correspondientes.
     *
     * **Proceso de sincronización:**
     * 1. Sincroniza estadísticas globales en `user_profile`
     * 2. Itera sobre cada módulo/punto del perfil
     * 3. Mapea cada actividad a su clave de SharedPreferences correspondiente
     * 4. Guarda estado de completado y puntuación obtenida
     *
     * **Manejo de errores:**
     * - Si ocurre una excepción, se registra en logs pero no detiene la app
     * - Actividades no reconocidas se registran con warning pero no causan fallo
     *
     * @param context Contexto de la aplicación para acceder a SharedPreferences
     * @param perfilProgreso Datos del servidor con progreso completo del usuario
     *
     * @throws Exception Si hay error crítico en el proceso de sincronización
     */
    fun syncPerfilProgreso(context: Context, perfilProgreso: PerfilProgresoResponse) {
        try {
            // 1. Sincronizar estadísticas globales
            syncEstadisticasGlobales(context, perfilProgreso)

            // 2. Sincronizar progreso por módulo
            perfilProgreso.puntos.forEach { punto ->
                syncModulo(context, punto.idPunto, punto.actividades)
            }

            LogManager.write(context, "✅ Sincronización completa exitosa")
            Log.d(TAG, "Sincronización completada: ${perfilProgreso.estadisticas.actividadesCompletadas} actividades")

        } catch (e: Exception) {
            LogManager.write(context, "❌ Error en sincronización: ${e.message}")
            Log.e(TAG, "Error al sincronizar", e)
        }
    }

    /**
     * Sincroniza estadísticas globales del usuario en `user_profile` SharedPreferences.
     *
     * Guarda métricas agregadas que se usan en ProfileActivity y otras pantallas
     * para mostrar el progreso general del usuario.
     *
     * **Datos sincronizados:**
     * - `top_score`: Puntuación máxima alcanzada
     * - `actividades_completadas`: Total de actividades completadas
     * - `racha_dias`: Días consecutivos con actividad
     * - `ultima_partida`: Fecha de última sesión
     * - `puntos_acumulados`: Total de puntos obtenidos
     *
     * @param context Contexto de la aplicación
     * @param perfil Datos completos del perfil del usuario
     */
    private fun syncEstadisticasGlobales(context: Context, perfil: PerfilProgresoResponse) {
        val prefs = context.getSharedPreferences(PREFS_PROFILE, Context.MODE_PRIVATE)

        prefs.edit {
            putInt("top_score", perfil.usuario.topScore)
            putInt("actividades_completadas", perfil.estadisticas.actividadesCompletadas)
            putInt("racha_dias", perfil.estadisticas.rachaDias)
            putString("ultima_partida", perfil.estadisticas.ultimaPartida)
            putInt("puntos_acumulados", perfil.estadisticas.totalPuntosAcumulados.toInt())
        }

        Log.d(TAG, "Estadísticas globales sincronizadas: topScore=${perfil.usuario.topScore}")
    }

    /**
     * Sincroniza el progreso de un módulo específico.
     *
     * Mapea el UUID del módulo del servidor al nombre de SharedPreferences correspondiente
     * y sincroniza todas las actividades asociadas.
     *
     * **Mapeo de módulos:**
     * - UUID Árbol → `arbol_progress`
     * - UUID Bunkers → `bunkers_progress`
     * - UUID Picasso → `picasso_progress`
     * - UUID Plaza → `plaza_progress`
     * - UUID Frontón → `fronton_progress`
     *
     * Si el UUID no se reconoce, se registra un warning y se omite el módulo.
     *
     * @param context Contexto de la aplicación
     * @param idPunto UUID del punto/módulo del servidor
     * @param actividades Lista de actividades del módulo con sus estados
     */
    private fun syncModulo(
        context: Context,
        idPunto: String,
        actividades: List<ActividadDetalle>
    ) {
        // Mapear ID del punto a nombre de SharedPreferences
        val prefsName = when (idPunto) {
            Puntos.Arbol.ID -> "arbol_progress"
            Puntos.Bunkers.ID -> "bunkers_progress"
            Puntos.Picasso.ID -> "picasso_progress"
            Puntos.Plaza.ID -> "plaza_progress"
            Puntos.Fronton.ID -> "fronton_progress"
            else -> {
                Log.w(TAG, "ID de punto desconocido: $idPunto")
                return
            }
        }

        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

        actividades.forEach { actividad ->
            syncActividad(prefs, actividad)
        }

        Log.d(TAG, "Módulo $prefsName sincronizado: ${actividades.size} actividades")
    }

    /**
     * Sincroniza una actividad individual en SharedPreferences.
     *
     * Convierte el UUID de la actividad del servidor a la clave local usada en
     * SharedPreferences y guarda su estado de completado y puntuación.
     *
     * **Formato de claves:**
     * - `{activity_key}_completed`: Boolean indicando si está completada
     * - `{activity_key}_score`: Float con la puntuación obtenida
     * - `{activity_key}_completion_date`: String con fecha de completado
     *
     * **Ejemplo:**
     * - Servidor: `id_actividad = "aa52349d-..."`, `estado = "completado"`, `puntuacion = 100.0`
     * - Local: `audio_quiz_completed = true`, `audio_quiz_score = 100.0f`
     *
     * @param prefs SharedPreferences del módulo donde se guardarán los datos
     * @param actividad Datos de la actividad del servidor con estado y puntuación
     */
    private fun syncActividad(
        prefs: SharedPreferences,
        actividad: ActividadDetalle
    ) {
        // Obtener nombre de la actividad para la clave
        val activityKey = getActivityKeyFromId(actividad.idActividad)

        if (activityKey == null) {
            Log.w(TAG, "⚠️ Actividad no mapeada:")
            Log.w(TAG, "  - UUID servidor: ${actividad.idActividad}")
            Log.w(TAG, "  - Nombre: ${actividad.nombreActividad}")
            Log.w(TAG, "  - Estado: ${actividad.estado}")
            return
        }

        Log.d(TAG, "✓ Sincronizando: $activityKey (${actividad.nombreActividad}) - ${actividad.estado}")

        prefs.edit {
            // Guardar estado de completado
            putBoolean("${activityKey}_completed", actividad.estado == "completado")

            // Guardar puntuación si existe
            actividad.puntuacion?.let { puntuacion ->
                putFloat("${activityKey}_score", puntuacion.toFloat())
                Log.d(TAG, "  → Puntuación: $puntuacion")
            }

            // Opcional: Guardar fecha de completado
            actividad.fechaCompletado?.let { fecha ->
                putString("${activityKey}_completion_date", fecha)
            }
        }
    }

    /**
     * Mapea el UUID de actividad del servidor a la clave local de SharedPreferences.
     *
     * Este mapeo es crítico para la sincronización correcta entre servidor y cliente.
     * Los UUIDs se obtienen de `Constants.Puntos.{Modulo}.{ACTIVIDAD}`.
     *
     * **Módulos soportados:**
     * - **Árbol**: audio_quiz, puzzle, interactive (Nire Arbola)
     * - **Bunkers**: sound_game, peace_mural, reflection, video_bunker
     * - **Picasso**: color_peace, view_interpret, my_message
     * - **Plaza**: video, drag_products, verse_game, photo_mission
     * - **Frontón**: info, dancing_ball, cesta_tip, values_group
     *
     * Si el UUID no se reconoce, retorna `null` y se registra un warning.
     *
     * @param idActividad UUID de la actividad del servidor
     * @return Clave de la actividad para SharedPreferences (ej: "audio_quiz"), o null si no se reconoce
     */
    private fun getActivityKeyFromId(idActividad: String): String? {
        return when (idActividad) {
            // ÁRBOL
            Puntos.Arbol.AUDIO_QUIZ -> "audio_quiz"
            Puntos.Arbol.PUZZLE -> "puzzle"
            Puntos.Arbol.MY_TREE -> "interactive"

            // BUNKERS
            Puntos.Bunkers.SOUND_GAME -> "sound_game"
            Puntos.Bunkers.PEACE_MURAL -> "peace_mural"
            Puntos.Bunkers.REFLECTION -> "reflection"
            Puntos.Bunkers.VIDEO_BUNKER -> "video_bunker"

            // PICASSO
            Puntos.Picasso.COLOR_PEACE -> "color_peace"
            Puntos.Picasso.VIEW_INTERPRET -> "view_interpret"
            Puntos.Picasso.MY_MESSAGE -> "my_message"

            // PLAZA
            Puntos.Plaza.VIDEO -> "video"
            Puntos.Plaza.DRAG_PRODUCTS -> "drag_products"
            Puntos.Plaza.VERSE_GAME -> "verse_game"
            Puntos.Plaza.PHOTO_MISSION -> "photo_mission"

            // FRONTON
            Puntos.Fronton.INFO -> "info"
            Puntos.Fronton.DANCING_BALL -> "dancing_ball"
            Puntos.Fronton.CESTA_TIP -> "cesta_tip"
            Puntos.Fronton.VALUES_GROUP -> "values_group"

            else -> null
        }
    }

    /**
     * Limpia todos los datos de progreso local almacenados en SharedPreferences.
     *
     * Este metodo se debe llamar al cerrar sesión para eliminar el rastro del
     * progreso del usuario anterior. Limpia todas las SharedPreferences relacionadas
     * con el juego.
     *
     * **SharedPreferences eliminadas:**
     * - `user_profile`: Estadísticas globales
     * - `arbol_progress`: Progreso del módulo Árbol
     * - `bunkers_progress`: Progreso del módulo Bunkers
     * - `picasso_progress`: Progreso del módulo Picasso
     * - `plaza_progress`: Progreso del módulo Plaza
     * - `fronton_progress`: Progreso del módulo Frontón
     *
     * **Uso:**
     * ```kotlin
     * // Al cerrar sesión:
     * tokenManager.clearSession()
     * SyncManager.clearAllProgress(context)
     * ```
     *
     * @param context Contexto de la aplicación
     */
    fun clearAllProgress(context: Context) {
        listOf(
            "user_profile",
            "arbol_progress",
            "bunkers_progress",
            "picasso_progress",
            "plaza_progress",
            "fronton_progress"
        ).forEach { prefsName ->
            context.getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit().clear().apply()
        }

        LogManager.write(context, "Todos los datos de progreso local limpiados")
    }
}
