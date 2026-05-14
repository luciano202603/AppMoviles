package com.tuapp.appsmoviles

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val precio: Int = 0,
    val categoria: String = "",
    val requiereFrio: Boolean = false,
    var cantidad: Int = 0
)