package es.didaktikapp.gernikapp.plazagernika.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.plazagernika.models.FotoGaleria

class MisionFotoAdapter(
    private val fotos: List<FotoGaleria>
) : RecyclerView.Adapter<MisionFotoAdapter.FotoViewHolder>() {

    class FotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivFoto: ImageView = view.findViewById(R.id.ivFoto)
        val tvEtiqueta: TextView = view.findViewById(R.id.tvEtiqueta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.plaza_item_mision_foto, parent, false)
        return FotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
        val foto = fotos[position]
        holder.ivFoto.setImageBitmap(foto.bitmap)
        holder.tvEtiqueta.text = foto.etiqueta.etiquetaEuskera
    }

    override fun getItemCount() = fotos.size
}
