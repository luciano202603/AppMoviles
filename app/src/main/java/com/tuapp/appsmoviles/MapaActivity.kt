package com.tuapp.appsmoviles

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlin.math.roundToInt

class MapaActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private val ubicacionLocal = LatLng(-33.44751, -70.66169)

    private val totalCompraEjemplo = 30000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.addMarker(
            MarkerOptions()
                .position(ubicacionLocal)
                .title("Local base AIEP")
        )

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        mMap.isMyLocationEnabled = true

        val fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->

            if (location != null) {

                val ubicacionCliente = LatLng(location.latitude, location.longitude)

                mMap.addMarker(
                    MarkerOptions()
                        .position(ubicacionCliente)
                        .title("Mi ubicación")
                )

                mMap.addPolyline(
                    PolylineOptions()
                        .add(ubicacionLocal, ubicacionCliente)
                        .width(6f)
                        .color(Color.BLUE)
                )

                val distanciaMetros = FloatArray(1)

                Location.distanceBetween(
                    ubicacionLocal.latitude,
                    ubicacionLocal.longitude,
                    ubicacionCliente.latitude,
                    ubicacionCliente.longitude,
                    distanciaMetros
                )

                val distanciaKm = distanciaMetros[0] / 1000.0
                val prefs = getSharedPreferences("datos_despacho", MODE_PRIVATE)
                prefs.edit()
                    .putFloat("distancia_km", distanciaKm.toFloat())
                    .apply()

                val texto = "Distancia Aproximada: %.2f km".format(
                    distanciaKm
                )

                mMap.addMarker(
                    MarkerOptions()
                        .position(ubicacionCliente)
                        .title("Informacion Despacho")
                        .snippet(texto)
                )?.showInfoWindow()

                val bounds = LatLngBounds.builder()
                    .include(ubicacionLocal)
                    .include(ubicacionCliente)
                    .build()

                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(bounds, 150)
                )
            }
        }
    }

    private fun calcularDespacho(totalCompra: Int, distanciaKm: Double): Int {
        return when {
            totalCompra >= 50000 -> 0
            totalCompra >= 25000 -> (distanciaKm * 150).roundToInt()
            else -> (distanciaKm * 300).roundToInt()
        }
    }
}