package es.didaktikapp.gernikapp.utils

/**
 * Sealed class para representar el estado de operaciones asíncronas.
 * Usado principalmente para respuestas de la API.
 *
 * Estados:
 * - Success: Operación exitosa con datos
 * - Error: Operación fallida con mensaje y código HTTP opcional
 * - Loading: Operación en progreso
 *
 * Uso típico:
 * ```
 * when (result) {
 *     is Resource.Success -> // Manejar datos
 *     is Resource.Error -> // Manejar error
 *     is Resource.Loading -> // Mostrar loading
 * }
 * ```
 *
 * @param T Tipo de datos en caso de éxito
 *
 * @author Wara Pacheco
 * @version 1.0
 */
sealed class Resource<out T> {
    /**
     * Operación exitosa.
     * @property data Datos devueltos por la operación
     */
    data class Success<T>(val data: T) : Resource<T>()

    /**
     * Operación fallida.
     * @property message Mensaje de error
     * @property code Código HTTP de error (opcional)
     */
    data class Error(val message: String, val code: Int? = null) : Resource<Nothing>()

    /**
     * Operación en progreso.
     */
    data object Loading : Resource<Nothing>()
}