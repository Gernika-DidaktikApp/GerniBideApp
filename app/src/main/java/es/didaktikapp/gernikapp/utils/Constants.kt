package es.didaktikapp.gernikapp.utils

/**
 * Constantes de la aplicación
 */
object Constants {

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