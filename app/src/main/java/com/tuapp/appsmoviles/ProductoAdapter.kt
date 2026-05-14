package com.tuapp.appsmoviles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductoAdapter(
    private val productos: List<Producto>,
    private val onAgregar: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    // ViewHolder representa cada item de la lista
    class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreProducto)
        val tvCategoria: TextView = itemView.findViewById(R.id.tvCategoriaProducto)
        val tvPrecio: TextView = itemView.findViewById(R.id.tvPrecioProducto)
        val tvRequiereFrio: TextView = itemView.findViewById(R.id.tvRequiereFrio)
        val btnAgregar: Button = itemView.findViewById(R.id.btnAgregar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]

        holder.tvNombre.text = producto.nombre
        holder.tvCategoria.text = producto.categoria.uppercase()
        holder.tvPrecio.text = "$${formatearPrecio(producto.precio)}"
        holder.tvRequiereFrio.text = if (producto.requiereFrio) "* Requiere cadena de frio" else ""
        holder.tvRequiereFrio.visibility = if (producto.requiereFrio) View.VISIBLE else View.GONE

        holder.btnAgregar.setOnClickListener {
            onAgregar(producto)
        }
    }

    override fun getItemCount() = productos.size

    private fun formatearPrecio(precio: Int): String {
        return String.format("%,d", precio).replace(",", ".")
    }
}