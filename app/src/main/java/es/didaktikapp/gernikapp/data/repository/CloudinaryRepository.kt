package es.didaktikapp.gernikapp.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import es.didaktikapp.gernikapp.BuildConfig
import es.didaktikapp.gernikapp.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Repository para operaciones con Cloudinary.
 * Maneja la subida de imágenes al servicio de almacenamiento en la nube.
 *
 * @author Wara Pacheco
 * @version 1.0
 */
class CloudinaryRepository(context: Context) : BaseRepository(context) {

    companion object {
        private const val TAG = "CloudinaryRepository"
        private const val FOLDER_PHOTO_MISSIONS = "gernikapp/photo_missions"
        private const val COMPRESSION_QUALITY = 80

        @Volatile
        private var isInitialized = false
    }

    init {
        inicializarCloudinary()
    }

    /**
     * Inicializa Cloudinary con las credenciales del BuildConfig.
     * Solo se ejecuta una vez.
     */
    private fun inicializarCloudinary() {
        if (isInitialized) return

        try {
            if (BuildConfig.CLOUDINARY_CLOUD_NAME.isNotEmpty()) {
                val config = mapOf(
                    "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
                    "api_key" to BuildConfig.CLOUDINARY_API_KEY,
                    "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
                )
                MediaManager.init(context, config)
                isInitialized = true

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "✅ Cloudinary inicializado correctamente")
                }
            } else {
                Log.w(TAG, "⚠️ Cloudinary no configurado en local.properties")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error inicializando Cloudinary: ${e.message}", e)
        }
    }

    /**
     * Sube una imagen (Bitmap) a Cloudinary.
     *
     * @param bitmap Imagen a subir
     * @param folder Carpeta en Cloudinary donde se guardará (por defecto: gernikapp/photo_missions)
     * @return Resource con la URL segura de la imagen subida
     */
    suspend fun subirImagen(
        bitmap: Bitmap,
        folder: String = FOLDER_PHOTO_MISSIONS
    ): Resource<String> = withContext(Dispatchers.IO) {
        try {
            if (!isInitialized || BuildConfig.CLOUDINARY_CLOUD_NAME.isEmpty()) {
                Log.e(TAG, "❌ Cloudinary no configurado")
                return@withContext Resource.Error("Cloudinary no está configurado", 0)
            }

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "📤 Iniciando subida de imagen a Cloudinary...")
            }

            // Convertir Bitmap a ByteArray
            val byteArray = convertBitmapToByteArray(bitmap)

            // Subir a Cloudinary usando suspend coroutine
            val imageUrl = uploadToCloudinary(byteArray, folder)

            if (imageUrl != null) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "✅ Imagen subida exitosamente: $imageUrl")
                }
                Resource.Success(imageUrl)
            } else {
                Log.e(TAG, "❌ Error: URL de imagen es null")
                Resource.Error("Error al subir la imagen a Cloudinary", 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Excepción al subir imagen: ${e.message}", e)
            Resource.Error("Error al procesar la imagen: ${e.message}", 0)
        }
    }

    /**
     * Convierte un Bitmap a ByteArray en formato JPEG.
     *
     * @param bitmap Imagen a convertir
     * @return ByteArray comprimido
     */
    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, stream)
        val byteArray = stream.toByteArray()
        stream.close()
        return byteArray
    }

    /**
     * Sube un ByteArray a Cloudinary y devuelve la URL segura.
     * Usa suspendCoroutine para convertir el callback de Cloudinary en una función suspend.
     *
     * @param byteArray Datos de la imagen
     * @param folder Carpeta de destino en Cloudinary
     * @return URL segura de la imagen, o null si hay error
     */
    private suspend fun uploadToCloudinary(
        byteArray: ByteArray,
        folder: String
    ): String? = suspendCoroutine { continuation ->
        MediaManager.get().upload(byteArray)
            .option("folder", folder)
            .option("resource_type", "image")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "🔄 Iniciando subida: $requestId")
                    }
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    if (BuildConfig.DEBUG) {
                        val progress = (bytes * 100 / totalBytes).toInt()
                        Log.d(TAG, "📊 Progreso: $progress% ($bytes / $totalBytes bytes)")
                    }
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "✅ Subida exitosa: $url")
                    }
                    continuation.resume(url)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    Log.e(TAG, "❌ Error en subida: ${error.description} (code: ${error.code})")
                    continuation.resume(null)
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    Log.w(TAG, "⚠️ Subida reprogramada: ${error.description}")
                }
            })
            .dispatch()
    }
}