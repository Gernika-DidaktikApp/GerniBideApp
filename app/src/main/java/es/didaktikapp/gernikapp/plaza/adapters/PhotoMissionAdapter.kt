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

    class FotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivFoto: ImageView = view.findViewById(R.id.ivFoto)
        val tvEtiqueta: TextView = view.findViewById(R.id.tvEtiqueta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.plaza_item_photo_mission, parent, false)
        return FotoViewHolder(view)
    }

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

    override fun getItemCount() = fotos.size
}
