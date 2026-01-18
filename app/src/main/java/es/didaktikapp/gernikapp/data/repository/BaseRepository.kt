package es.didaktikapp.gernikapp.data.repository

import android.content.Context
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.network.ApiErrorParser
import es.didaktikapp.gernikapp.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Clase base para repositories.
 * Proporciona manejo común de errores y respuestas de la API.
 */
abstract class BaseRepository(protected val context: Context) {

    /**
     * Ejecuta una llamada a la API de forma segura.
     * Maneja errores de red y parsea respuestas.
     *
     * @param apiCall Lambda que ejecuta la llamada a la API
     * @param onSuccess Transformación opcional del resultado exitoso
     */
    protected suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<T>,
        onSuccess: (T) -> T = { it }
    ): Resource<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiCall()
                handleApiResponse(response, onSuccess)
            } catch (e: UnknownHostException) {
                Resource.Error(context.getString(R.string.error_no_internet))
            } catch (e: SocketTimeoutException) {
                Resource.Error(context.getString(R.string.error_timeout))
            } catch (e: Exception) {
                Resource.Error(
                    context.getString(R.string.error_connection, e.localizedMessage ?: "")
                )
            }
        }
    }

    /**
     * Maneja la respuesta de la API.
     */
    private fun <T> handleApiResponse(
        response: Response<T>,
        onSuccess: (T) -> T
    ): Resource<T> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Resource.Success(onSuccess(body))
            } else {
                Resource.Error(
                    context.getString(R.string.error_empty_response),
                    response.code()
                )
            }
        } else {
            val errorMessage = ApiErrorParser.parse(
                errorBody = response.errorBody()?.string(),
                statusCode = response.code(),
                context = context
            )
            Resource.Error(errorMessage, response.code())
        }
    }
}