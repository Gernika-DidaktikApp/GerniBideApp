package es.didaktikapp.gernikapp.utils

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import androidx.annotation.DrawableRes

/**
 * Utilidades para operaciones con Bitmaps
 */
object BitmapUtils {

    /**
     * Combina dos bitmaps: un fondo y una capa superior (overlay)
     *
     * @param background Bitmap de fondo
     * @param overlay Bitmap que se dibuja encima (puede ser null)
     * @return Bitmap combinado
     */
    fun combineBitmaps(
        background: Bitmap,
        overlay: Bitmap?
    ): Bitmap {
        val combinedBitmap = Bitmap.createBitmap(
            background.width,
            background.height,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(combinedBitmap)
        canvas.drawBitmap(background, 0f, 0f, null)

        overlay?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }

        return combinedBitmap
    }

    /**
     * Combina dos bitmaps con escala y centramiento automáticos
     *
     * El bitmap de fondo se escala para ajustarse al tamaño del foreground
     * manteniendo su aspect ratio y centrándose.
     *
     * @param background Bitmap de fondo que se escalará
     * @param foreground Bitmap de primer plano (determina el tamaño final)
     * @return Bitmap combinado con scaling y centramiento
     */
    fun combineBitmapsWithScaling(background: Bitmap, foreground: Bitmap): Bitmap {
        val width = foreground.width
        val height = foreground.height
        val combined = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(combined)

        // Calcular escala manteniendo aspect ratio
        val bgWidth = background.width.toFloat()
        val bgHeight = background.height.toFloat()
        val fgWidth = width.toFloat()
        val fgHeight = height.toFloat()

        val scaleX = fgWidth / bgWidth
        val scaleY = fgHeight / bgHeight
        val scale = minOf(scaleX, scaleY)

        val scaledWidth = (bgWidth * scale).toInt()
        val scaledHeight = (bgHeight * scale).toInt()

        // Centrar la imagen escalada
        val left = (width - scaledWidth) / 2f
        val top = (height - scaledHeight) / 2f

        val scaledBackground = Bitmap.createScaledBitmap(background, scaledWidth, scaledHeight, true)
        canvas.drawBitmap(scaledBackground, left, top, null)
        canvas.drawBitmap(foreground, 0f, 0f, null)

        return combined
    }

    /**
     * Carga un drawable como bitmap, lo combina con un overlay y libera memoria
     *
     * @param resources Resources de la aplicación
     * @param backgroundRes ID del recurso drawable de fondo
     * @param overlay Bitmap que se dibuja encima (puede ser null)
     * @return Bitmap combinado
     */
    fun loadAndCombine(
        resources: Resources,
        @DrawableRes backgroundRes: Int,
        overlay: Bitmap?
    ): Bitmap {
        val background = BitmapFactory.decodeResource(resources, backgroundRes)
        val result = combineBitmaps(background, overlay)
        background.recycle() // Liberar memoria del bitmap temporal
        return result
    }
}