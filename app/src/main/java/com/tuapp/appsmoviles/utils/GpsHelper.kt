package com.tuapp.appsmoviles.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class GpsHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Verificar si los permisos de ubicación están otorgados
    fun tienePermisos(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Obtener ubicación y guardar en Firebase
    fun obtenerYGuardarUbicacion(
        onExito: (Double, Double) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!tienePermisos()) {
            onError("Permisos de ubicación no otorgados")
            return
        }

        try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location ->
                if (location != null) {
                    val latitud = location.latitude
                    val longitud = location.longitude
                    guardarEnFirebase(latitud, longitud, onExito, onError)
                } else {
                    // Si getCurrentLocation falla, intentar con lastLocation
                    obtenerUltimaUbicacion(onExito, onError)
                }
            }.addOnFailureListener { e ->
                onError("Error obteniendo ubicación: ${e.message}")
            }
        } catch (e: SecurityException) {
            onError("Error de permisos: ${e.message}")
        }
    }

    // Fallback con última ubicación conocida
    private fun obtenerUltimaUbicacion(
        onExito: (Double, Double) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        guardarEnFirebase(location.latitude, location.longitude, onExito, onError)
                    } else {
                        onError("No se pudo obtener la ubicación")
                    }
                }
                .addOnFailureListener { e ->
                    onError("Error: ${e.message}")
                }
        } catch (e: SecurityException) {
            onError("Error de permisos: ${e.message}")
        }
    }

    // Guardar coordenadas en Firebase Realtime Database
    private fun guardarEnFirebase(
        latitud: Double,
        longitud: Double,
        onExito: (Double, Double) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: run {
            onError("Usuario no autenticado")
            return
        }

        val datosGps = mapOf(
            "latitud" to latitud,
            "longitud" to longitud,
            "timestamp" to System.currentTimeMillis()
        )

        database.reference
            .child("usuarios")
            .child(uid)
            .child("gps")
            .setValue(datosGps)
            .addOnSuccessListener {
                onExito(latitud, longitud)
            }
            .addOnFailureListener { e ->
                onError("Error guardando en Firebase: ${e.message}")
            }
    }
}