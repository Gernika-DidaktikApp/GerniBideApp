package es.didaktikapp.gernikapp.plazagernika.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.plazagernika.models.MisionFoto

class MisionFotoAdapter(
    private val misiones: List<MisionFoto>,
    private val onTomarFoto: (MisionFoto) -> Unit
) : RecyclerView.Adapter<MisionFotoAdapter.MisionViewHolder>() {

    class MisionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcono: ImageView = view.findViewById(R.id.ivIcono)
        val tvTitulo: TextView = view.findViewById(R.id.tvTitulo)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcion)
        val btnTomarFoto: Button = view.findViewById(R.id.btnTomarFoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MisionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.plaza_item_mision_foto, parent, false)
        return MisionViewHolder(view)
    }

    override fun onBindViewHolder(holder: MisionViewHolder, position: Int) {
        val mision = misiones[position]
        holder.ivIcono.setImageResource(mision.iconoRes)
        holder.tvTitulo.text = mision.titulo
        holder.tvDescripcion.text = mision.descripcion

        if (mision.completada) {
            holder.btnTomarFoto.text = "✓ Completada"
            holder.btnTomarFoto.isEnabled = false
            holder.btnTomarFoto.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.correcto)
            )
        } else {
            holder.btnTomarFoto.text = "Tomar foto"
            holder.btnTomarFoto.isEnabled = true
            holder.btnTomarFoto.setOnClickListener {
                onTomarFoto(mision)
            }
        }
    }

    override fun getItemCount() = misiones.size
}
