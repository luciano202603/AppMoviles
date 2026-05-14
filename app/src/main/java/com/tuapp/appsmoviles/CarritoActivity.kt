package com.tuapp.appsmoviles

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.tuapp.appsmoviles.utils.DespachoCalculator

class CarritoActivity : AppCompatActivity() {

    private lateinit var tvTotalCompra: TextView
    private lateinit var etDistancia: TextInputEditText
    private lateinit var btnCalcular: Button
    private lateinit var layoutResultado: LinearLayout
    private lateinit var tvReglaAplicada: TextView
    private lateinit var tvCostoDespacho: TextView
    private lateinit var tvTotalFinal: TextView
    private lateinit var btnConfirmar: Button

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var totalCompra = 0
    private var resultadoDespacho: DespachoCalculator.ResultadoDespacho? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrito)

        // Vincular vistas
        tvTotalCompra = findViewById(R.id.tvTotalCompra)
        etDistancia = findViewById(R.id.etDistancia)
        btnCalcular = findViewById(R.id.btnCalcular)
        layoutResultado = findViewById(R.id.layoutResultado)
        tvReglaAplicada = findViewById(R.id.tvReglaAplicada)
        tvCostoDespacho = findViewById(R.id.tvCostoDespacho)
        tvTotalFinal = findViewById(R.id.tvTotalFinal)
        btnConfirmar = findViewById(R.id.btnConfirmar)

        // Obtener total desde CatalogoActivity
        totalCompra = intent.getIntExtra("total", 0)
        tvTotalCompra.text = "$${DespachoCalculator.formatearPrecio(totalCompra)}"

        // Boton calcular despacho
        btnCalcular.setOnClickListener {
            calcularDespacho()
        }

        // Boton confirmar pedido
        btnConfirmar.setOnClickListener {
            confirmarPedido()
        }
    }

    private fun calcularDespacho() {
        val distanciaTexto = etDistancia.text.toString().trim()

        if (distanciaTexto.isEmpty()) {
            etDistancia.error = "Ingresa la distancia"
            return
        }

        val distancia = distanciaTexto.toDoubleOrNull()
        if (distancia == null || distancia <= 0) {
            etDistancia.error = "Ingresa una distancia valida"
            return
        }

        // Calcular usando las reglas de negocio
        resultadoDespacho = DespachoCalculator.calcular(totalCompra, distancia)
        val resultado = resultadoDespacho!!

        // Mostrar resultado
        tvReglaAplicada.text = resultado.reglaAplicada

        if (resultado.esGratis) {
            tvCostoDespacho.text = "Costo de despacho: GRATIS"
            tvCostoDespacho.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
        } else {
            tvCostoDespacho.text = "Costo de despacho: $${DespachoCalculator.formatearPrecio(resultado.costoDespacho)}"
            tvCostoDespacho.setTextColor(android.graphics.Color.parseColor("#1565C0"))
        }

        tvTotalFinal.text = "TOTAL A PAGAR: $${DespachoCalculator.formatearPrecio(resultado.totalFinal)}"

        // Mostrar el panel de resultado
        layoutResultado.visibility = View.VISIBLE
    }

    private fun confirmarPedido() {
        val resultado = resultadoDespacho ?: return
        val uid = auth.currentUser?.uid ?: return

        val pedido = mapOf(
            "total_compra" to totalCompra,
            "costo_despacho" to resultado.costoDespacho,
            "total_final" to resultado.totalFinal,
            "regla_aplicada" to resultado.reglaAplicada,
            "fecha" to System.currentTimeMillis()
        )

        // Guardar pedido en Firebase bajo /pedidos/{uid}/{pedidoId}
        database.reference
            .child("pedidos")
            .child(uid)
            .push()
            .setValue(pedido)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Pedido confirmado exitosamente",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al confirmar pedido: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}