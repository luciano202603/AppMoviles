package com.tuapp.appsmoviles

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CatalogoActivity : AppCompatActivity() {

    private lateinit var recyclerProductos: RecyclerView
    private lateinit var tvTotalCarrito: TextView
    private lateinit var btnIrCarrito: Button

    private val database = FirebaseDatabase.getInstance()
    private val carrito = mutableListOf<Producto>()
    private val listaProductos = mutableListOf<Producto>()
    private var totalCarrito = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalogo)
        Log.d("CatalogoActivity", "onCreate iniciado")

        recyclerProductos = findViewById(R.id.recyclerProductos)
        tvTotalCarrito = findViewById(R.id.tvTotalCarrito)
        btnIrCarrito = findViewById(R.id.btnIrCarrito)

        recyclerProductos.layoutManager = LinearLayoutManager(this)

        btnIrCarrito.setOnClickListener {
            if (carrito.isEmpty()) {
                Toast.makeText(this, "Agrega productos al carrito primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, CarritoActivity::class.java)
            intent.putExtra("total", totalCarrito)
            startActivity(intent)
        }

        cargarProductos()
    }

    private fun cargarProductos() {
        Log.d("CatalogoActivity", "cargando productos desde Firebase")

        database.reference.child("productos")
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("CatalogoActivity", "snapshot existe: " + snapshot.exists())
                    Log.d("CatalogoActivity", "cantidad hijos: " + snapshot.childrenCount)

                    listaProductos.clear()

                    for (item in snapshot.children) {
                        val id = item.key ?: ""
                        val nombre = item.child("nombre").getValue(String::class.java) ?: ""
                        val precio = item.child("precio").getValue(Long::class.java)?.toInt() ?: 0
                        val categoria = item.child("categoria").getValue(String::class.java) ?: ""
                        val requiereFrio = item.child("requiere_frio").getValue(Boolean::class.java) ?: false

                        Log.d("CatalogoActivity", "producto: $nombre precio: $precio")
                        listaProductos.add(Producto(id, nombre, precio, categoria, requiereFrio))
                    }

                    Log.d("CatalogoActivity", "total cargados: " + listaProductos.size)

                    recyclerProductos.adapter = ProductoAdapter(listaProductos) { producto ->
                        agregarAlCarrito(producto)
                    }

                    if (listaProductos.isEmpty()) {
                        Toast.makeText(
                            this@CatalogoActivity,
                            "No hay productos disponibles",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CatalogoActivity", "Error: " + error.message)
                    Toast.makeText(
                        this@CatalogoActivity,
                        "Error cargando productos: " + error.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun agregarAlCarrito(producto: Producto) {
        val existente = carrito.find { it.id == producto.id }
        if (existente != null) {
            existente.cantidad++
        } else {
            carrito.add(producto.copy(cantidad = 1))
        }
        totalCarrito += producto.precio
        actualizarResumenCarrito()
        Toast.makeText(this, producto.nombre + " agregado", Toast.LENGTH_SHORT).show()
    }

    private fun actualizarResumenCarrito() {
        val cantidadTotal = carrito.sumOf { it.cantidad }
        val totalFormateado = String.format("%,d", totalCarrito).replace(",", ".")
        tvTotalCarrito.text = "Carrito: $$totalFormateado ($cantidadTotal productos)"
    }
}