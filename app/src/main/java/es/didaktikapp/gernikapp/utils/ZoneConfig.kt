package es.didaktikapp.gernikapp.utils

/**
 * Configuración de una actividad dentro de una zona del recorrido.
 * Contiene las claves de SharedPreferences para el estado de completado y la puntuación.
 *
 * @author Arantxa Main
 * @version 1.0
 * @property completedKey Clave de SharedPreferences que indica si la actividad fue completada.
 * @property scoreKey Clave de SharedPreferences que almacena la puntuación obtenida.
 * @property displayName Nombre visible de la actividad en la pantalla de resumen.
 */
data class ActivityConfig(
    val completedKey: String,
    val scoreKey: String,
    val displayName: String
)

/**
 * Información de una zona del recorrido, incluyendo su nombre y la lista de actividades que contiene.
 *
 * @author Arantxa Main
 * @version 1.0
 * @property prefsName Nombre del archivo de SharedPreferences de la zona.
 * @property zoneName Nombre visible de la zona.
 * @property activities Lista de actividades que componen la zona.
 */
data class ZoneInfo(
    val prefsName: String,
    val zoneName: String,
    val activities: List<ActivityConfig>
)

/**
 * Objeto de configuración central que define todas las zonas del recorrido
 * (Árbol, Búnkers, Picasso, Plaza y Frontón) y sus actividades asociadas.
 *
 * @author Arantxa Main
 * @version 1.0
 * @see ActivityConfig
 * @see ZoneInfo
 */
object ZoneConfig {

    val ARBOL = ZoneInfo(
        prefsName = "arbol_progress",
        zoneName = "Gernikako Arbola",
        activities = listOf(
            ActivityConfig("audio_quiz_completed", "audio_quiz_score", "Audio Quiz"),
            ActivityConfig("puzzle_completed", "puzzle_score", "Puzzle"),
            ActivityConfig("interactive_completed", "interactive_score", "Nire zuhaitza")
        )
    )

    val BUNKERS = ZoneInfo(
        prefsName = "bunkers_progress",
        zoneName = "Bunkerrak",
        activities = listOf(
            ActivityConfig("video_bunker_completed", "video_bunker_score", "Bideoa"),
            ActivityConfig("sound_game_completed", "sound_game_score", "Soinu jolasa"),
            ActivityConfig("peace_mural_completed", "peace_mural_score", "Bake murala"),
            ActivityConfig("reflection_completed", "reflection_score", "Hausnarketa")
        )
    )

    val PICASSO = ZoneInfo(
        prefsName = "picasso_progress",
        zoneName = "Picasso",
        activities = listOf(
            ActivityConfig("audio_picasso_completed", "audio_picasso_score", "Audio"),
            ActivityConfig("color_peace_completed", "color_peace_score", "Bakea margotu"),
            ActivityConfig("view_interpret_completed", "view_interpret_score", "Begira eta asmatu"),
            ActivityConfig("my_message_completed", "my_message_score", "Nire mezua")
        )
    )

    val PLAZA = ZoneInfo(
        prefsName = "plaza_progress",
        zoneName = "Plaza",
        activities = listOf(
            ActivityConfig("video_completed", "video_score", "Bideoa"),
            ActivityConfig("drag_products_completed", "drag_products_score", "Merkatua"),
            ActivityConfig("verse_game_completed", "verse_game_score", "Bertsoak"),
            ActivityConfig("photo_mission_completed", "photo_mission_score", "Argazkiak")
        )
    )

    val FRONTON = ZoneInfo(
        prefsName = "fronton_progress",
        zoneName = "Frontoia",
        activities = listOf(
            ActivityConfig("info_completed", "info_score", "Informazioa"),
            ActivityConfig("dancing_ball_completed", "dancing_ball_score", "Pilota dantzan"),
            ActivityConfig("cesta_tip_completed", "cesta_tip_score", "Zesta punta"),
            ActivityConfig("values_group_completed", "values_group_score", "Balioak")
        )
    )

    val ALL_ZONES = listOf(ARBOL, BUNKERS, PICASSO, PLAZA, FRONTON)

    fun findByPrefsName(prefsName: String): ZoneInfo? =
        ALL_ZONES.find { it.prefsName == prefsName }
}
