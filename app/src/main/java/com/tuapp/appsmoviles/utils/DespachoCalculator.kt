package com.tuapp.appsmoviles.utils

object DespachoCalculator {

    private const val DESPACHO_GRATIS = 50000
    private const val TARIFA_MEDIA = 150
    private const val TARIFA_BAJA = 300
    private const val LIMITE_MEDIO = 25000

    data class ResultadoDespacho(
        val costoDespacho: Int,
        val reglaAplicada: String,
        val totalFinal: Int,
        val esGratis: Boolean
    )

    fun calcular(totalCompra: Int, distanciaKm: Double): ResultadoDespacho {
        return when {
            // Regla 1 — Despacho gratis
            totalCompra >= DESPACHO_GRATIS -> {
                ResultadoDespacho(
                    costoDespacho = 0,
                    reglaAplicada = "Compra mayor a $50.000 — Despacho GRATIS",
                    totalFinal = totalCompra,
                    esGratis = true
                )
            }
            // Regla 2 — Tarifa media
            totalCompra >= LIMITE_MEDIO -> {
                val costo = (distanciaKm * TARIFA_MEDIA).toInt()
                ResultadoDespacho(
                    costoDespacho = costo,
                    reglaAplicada = "Compra entre $25.000 y $49.999 — Tarifa: $150/km",
                    totalFinal = totalCompra + costo,
                    esGratis = false
                )
            }
            // Regla 3 — Tarifa alta
            else -> {
                val costo = (distanciaKm * TARIFA_BAJA).toInt()
                ResultadoDespacho(
                    costoDespacho = costo,
                    reglaAplicada = "Compra menor a $25.000 — Tarifa: $300/km",
                    totalFinal = totalCompra + costo,
                    esGratis = false
                )
            }
        }
    }

    fun formatearPrecio(valor: Int): String {
        return String.format("%,d", valor).replace(",", ".")
    }
}