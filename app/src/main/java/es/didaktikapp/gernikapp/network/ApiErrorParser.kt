package es.didaktikapp.gernikapp.network

import android.content.Context
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.data.models.ApiError
import es.didaktikapp.gernikapp.data.models.ValidationError

/**
 * Parser para errores de la API.
 * Soporta errores simples y errores de validación (422).
 *
 * @author Wara Pacheco
 * @version 1.0
 * @see ApiError
 * @see ValidationError
 */
object ApiErrorParser {

    /**
     * Parsea el cuerpo de error de una respuesta de la API.
     *
     * @param errorBody El cuerpo del error como String
     * @param statusCode El código de estado HTTP
     * @param context Contexto para obtener strings localizados
     * @return Mensaje de error legible
     */
    fun parse(errorBody: String?, statusCode: Int, context: Context): String {
        if (errorBody.isNullOrBlank()) {
            return context.getString(R.string.error_server, statusCode)
        }

        return try {
            // Intentar parsear como error de validación (422)
            if (statusCode == 422) {
                parseValidationError(errorBody)
                    ?: parseSimpleError(errorBody)
                    ?: context.getString(R.string.error_server, statusCode)
            } else {
                // Parsear como error simple
                parseSimpleError(errorBody)
                    ?: context.getString(R.string.error_server, statusCode)
            }
        } catch (e: Exception) {
            context.getString(R.string.error_server, statusCode)
        }
    }

    /**
     * Intenta parsear como error simple.
     */
    private fun parseSimpleError(errorBody: String): String? {
        return try {
            val adapter = RetrofitClient.moshi.adapter(ApiError::class.java)
            adapter.fromJson(errorBody)?.detail
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Intenta parsear como error de validación.
     */
    private fun parseValidationError(errorBody: String): String? {
        return try {
            val adapter = RetrofitClient.moshi.adapter(ValidationError::class.java)
            adapter.fromJson(errorBody)?.getReadableMessage()
        } catch (e: Exception) {
            null
        }
    }
}