package es.didaktikapp.gernikapp.plaza.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.plaza.models.FotoGaleria

/**
 * Adaptador para mostrar la galería de fotos en la misión fotográfica.
 * Soporta carga de imágenes desde Bitmap local o URL de Cloudinary usando Coil.
 *
 * @property fotos Lista de fotos a mostrar
 *
 * @author Wara Pacheco
 * @version 1.0
 */
class PhotoMissionAdapter(
    private val fotos: List<FotoGaleria>
) : RecyclerView.Adapter<PhotoMissionAdapter.FotoViewHolder>() {

    /**
     * ViewHolder que contiene las vistas de cada elemento de la galería:
     * - ivFoto: Imagen de la foto
     * - tvEtiqueta: Texto con la etiqueta asociada
     */
    class FotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivFoto: ImageView = view.findViewById(R.id.ivFoto)
        val tvEtiqueta: TextView = view.findViewById(R.id.tvEtiqueta)
    }

    /**
     * Infla el layout de cada elemento de la galería.
     *
     * @param parent Vista padre del RecyclerView
     * @param viewType Tipo de vista (no usado en este adaptador)
     * @return Un nuevo FotoViewHolder con el layout inflado
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.plaza_item_photo_mission, parent, false)
        return FotoViewHolder(view)
    }

    /**
     * Enlaza los datos de una foto con su ViewHolder.
     *
     * Proceso:
     * - Si la foto tiene URL → se carga con Coil desde Cloudinary
     * - Si tiene Bitmap local → se muestra directamente
     * - Se asigna la etiqueta en euskera
     *
     * @param holder ViewHolder que se va a actualizar
     * @param position Posición del elemento en la lista
     */
    override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
        val foto = fotos[position]

        // Cargar imagen desde URL o Bitmap
        if (foto.url != null) {
            // Cargar desde Cloudinary usando Coil
            holder.ivFoto.load(foto.url) {
                crossfade(true)
                placeholder(R.drawable.plaza_bg_producto)
                error(R.drawable.plaza_bg_producto)
            }
        } else if (foto.bitmap != null) {
            // Cargar desde Bitmap local
            holder.ivFoto.setImageBitmap(foto.bitmap)
        }

        holder.tvEtiqueta.text = foto.etiqueta.etiquetaEuskera
    }

    /**
     * Devuelve el número total de fotos en la galería.
     */
    override fun getItemCount() = fotos.size
}
