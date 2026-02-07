package es.didaktikapp.gernikapp.utils

/**
 * Constantes de la aplicación GernikApp.
 * Contiene IDs de puntos y actividades, configuraciones de red, mapa, archivos y más.
 *
 * Organización:
 * - Puntos: UUIDs de puntos (Árbol, Bunkers, etc.) y sus actividades
 * - Network: Configuración de timeouts
 * - Permissions: Códigos de permisos
 * - Map: Coordenadas y configuración del mapa
 * - Files: Nombres de archivos
 * - Messages: Configuración de mensajes
 * - Paint: Configuración de pintura
 *
 * @author Wara Pacheco
 * @version 1.0
 */
object Constants {

    /**
     * IDs de Puntos y Actividades (UUIDs de la API).
     * Estos IDs deben coincidir con los registrados en la base de datos.
     *
     * Estructura:
     * - Punto (antes "Actividad"): Árbol, Bunkers, Picasso, Plaza, Fronton
     * - Actividad (antes "Evento"): Sub-elementos dentro de cada punto
     *
     * IMPORTANTE: No modificar estos UUIDs sin coordinar con el backend.
     */
    object Puntos {

        object Arbol {
            const val ID = "05a05a04-f045-48cf-8ec3-e28d14eec63f"              // ID del punto
            const val AUDIO_QUIZ = "aa52349d-4734-409d-a2b1-d18621804f7e"      // Actividad: Audio Quiz
            const val PUZZLE = "d10064e1-b73c-4ca9-8d81-e6d4f06c5297"          // Actividad: Puzzle
            const val MY_TREE = "33c9254a-cbb3-4a57-9c5a-a5e8669defcb"         // Actividad: Mi Árbol
        }

        object Bunkers {
            const val ID = "a233c17d-75a8-417d-8862-d0e1550fe59e"              // ID del punto
            const val SOUND_GAME = "20f2c2d5-6f7f-4e91-a8bb-f28be9fa5748"      // Soinu Jokoa
            const val PEACE_MURAL = "6e74c7c5-6ad7-4714-bc8d-de2484291692"     // Bake murala
            const val REFLECTION = "03ddf94d-6be0-4e54-a76d-824f596a0c98"      // Hausnarketa
        }

        object Picasso {
            const val ID = "d66f036b-c0d3-4765-a64b-dd3643c84c19"              // ID del punto
            const val COLOR_PEACE = "2f85d0a6-c54f-49e7-a4cb-31d18a3bb286"     // Colorea la paz
            const val VIEW_INTERPRET = "9899bd1a-d2a9-4421-b520-f1fd8ad53d9a"  // Observa y adivina
            const val MY_MESSAGE = "9a5bbb72-827a-4b7e-bd3b-1e010dff191b"      // Mi mensaje para el mundo
        }

        object Plaza {
            const val ID = "b85e8a14-caa9-4fc4-8d18-b3010f51cd0a"              // ID del punto
            const val VIDEO = "f2c3582a-be3f-4baf-9705-b92d4ac5e01c"           // Plaza Video
            const val DRAG_PRODUCTS = "c5aed844-fbda-48f5-93b6-54e6db74fa1f"   // Conoce el mercado
            const val VERSE_GAME = "4cd2eed9-93a4-491e-ae23-222b6a4ea58f"      // Adivina el verso
            const val PHOTO_MISSION = "42c66d56-4ad5-4a22-a594-97f9f6184fa1"   // Misión fotográfica
        }

        object Fronton {
            const val ID = "31d03477-7125-47e2-bad3-63e9c85e17ad"              // ID del punto
            const val INFO = "86a97de4-4ce2-4ab1-b3bc-08af5058716e"            // Información del fronton
            const val DANCING_BALL = "27847f32-a237-4639-a9f1-825e833e286f"    // Pelota Dantzan
            const val CESTA_TIP = "6a1049c1-c5ce-44c1-a277-85b493bee06f"       // Valores del equipo
            const val VALUES_GROUP = "d45b5c9d-8805-4cf0-95eb-ba89abcc55e2"    // Tu valor del equipo
        }
    }

    /**
     * Configuración de red
     */
    object Network {
        const val TIMEOUT_SECONDS = 30L
    }

    /**
     * Configuración de permisos
     */
    object Permissions {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    /**
     * Configuración del mapa
     */
    object Map {
        const val DEFAULT_ZOOM_LEVEL = 16f

        // Coordenadas de Gernika
        const val GERNIKA_CENTER_LAT = 43.3156
        const val GERNIKA_CENTER_LNG = -2.6760

        // Puntos de interés
        const val ARBOLA_LAT = 43.3156
        const val ARBOLA_LNG = -2.6760

        const val BUNKER_LAT = 43.3200
        const val BUNKER_LNG = -2.6800

        const val GUERNICA_LAT = 43.3140
        const val GUERNICA_LNG = -2.6750

        const val PLAZA_LAT = 43.3150
        const val PLAZA_LNG = -2.6765

        const val FRONTOI_LAT = 43.3145
        const val FRONTOI_LNG = -2.6755
    }

    /**
     * Configuración de archivos
     */
    object Files {
        const val GUERNICA_IMAGE_FILENAME = "guernica_coloreado.png"
        const val PEACE_MESSAGES_FILENAME = "peace_messages.txt"
    }

    /**
     * Configuración de mensajes
     */
    object Messages {
        const val MIN_MESSAGE_LENGTH = 10
        const val MAX_DISPLAYED_MESSAGES = 20
    }

    /**
     * Configuración de pintura
     */
    object Paint {
        const val STROKE_WIDTH = 8f
        const val ALPHA_VALUE = 100
        const val MIN_ZOOM = 0.5f
        const val MAX_ZOOM = 5.0f
    }
}