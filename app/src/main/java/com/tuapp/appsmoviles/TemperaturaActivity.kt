package com.tuapp.appsmoviles

import androidx.core.app.NotificationCompat
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TemperaturaActivity : AppCompatActivity() {

    private lateinit var tvTemperatura: TextView
    private lateinit var tvEstado: TextView
    private lateinit var tvUltimaActualizacion: TextView
    private lateinit var tvIconoTemp: TextView
    private lateinit var btnVolver: Button

    private val database = FirebaseDatabase.getInstance()
    private lateinit var listenerTemperatura: ValueEventListener
    private var alarmaDisparada = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TemperaturaActivity", "onCreate iniciado")
        setContentView(R.layout.activity_temperatura)
        Log.d("TemperaturaActivity", "layout cargado")

        tvTemperatura = findViewById(R.id.tvTemperatura)
        tvEstado = findViewById(R.id.tvEstado)
        tvUltimaActualizacion = findViewById(R.id.tvUltimaActualizacion)
        tvIconoTemp = findViewById(R.id.tvIconoTemp)
        btnVolver = findViewById(R.id.btnVolver)

        Log.d("TemperaturaActivity", "vistas vinculadas correctamente")

        btnVolver.setOnClickListener {
            finish()
        }

        Log.d("TemperaturaActivity", "iniciando escucha de Firebase...")
        escucharTemperatura()
    }

    private fun escucharTemperatura() {
        val refTemperatura = database.reference
            .child("temperatura")
            .child("camion1")
            .child("valor")

        Log.d("TemperaturaActivity", "referencia Firebase creada")

        listenerTemperatura = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("TemperaturaActivity", "onDataChange recibido")
                Log.d("TemperaturaActivity", "snapshot existe: ${snapshot.exists()}")
                Log.d("TemperaturaActivity", "snapshot value: ${snapshot.value}")

                val temperatura = when {
                    snapshot.getValue(Double::class.java) != null ->
                        snapshot.getValue(Double::class.java)!!
                    snapshot.getValue(Long::class.java) != null ->
                        snapshot.getValue(Long::class.java)!!.toDouble()
                    snapshot.getValue(Int::class.java) != null ->
                        snapshot.getValue(Int::class.java)!!.toDouble()
                    else -> null
                }

                Log.d("TemperaturaActivity", "temperatura parseada: $temperatura")

                if (temperatura != null) {
                    actualizarUI(temperatura)
                    verificarAlarma(temperatura)
                } else {
                    Log.e("TemperaturaActivity", "temperatura es null")
                    tvEstado.text = "Sin datos de temperatura"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TemperaturaActivity", "Firebase cancelado: ${error.message}")
                tvEstado.text = "Error de conexion"
                tvEstado.setTextColor(Color.GRAY)
            }
        }

        refTemperatura.addValueEventListener(listenerTemperatura)
        Log.d("TemperaturaActivity", "listener agregado a Firebase")
    }

    private fun actualizarUI(temperatura: Double) {
        Log.d("TemperaturaActivity", "actualizando UI con temperatura: $temperatura")

        tvTemperatura.text = "${temperatura}°C"

        val formato = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        tvUltimaActualizacion.text = "Ultima actualizacion: ${formato.format(Date())}"

        if (temperatura > AppMoviles.LIMITE_TEMPERATURA) {
            tvEstado.text = "ALERTA — Temperatura fuera de rango"
            tvEstado.setTextColor(Color.RED)
            tvTemperatura.setTextColor(Color.RED)
            tvIconoTemp.text = "!"
        } else {
            tvEstado.text = "Temperatura OK"
            tvEstado.setTextColor(Color.parseColor("#2E7D32"))
            tvTemperatura.setTextColor(Color.parseColor("#1A1A2E"))
            tvIconoTemp.text = "OK"
            alarmaDisparada = false
        }
    }

    private fun verificarAlarma(temperatura: Double) {
        if (temperatura > AppMoviles.LIMITE_TEMPERATURA && !alarmaDisparada) {
            alarmaDisparada = true
            Log.d("TemperaturaActivity", "disparando alarma!")
            dispararNotificacion(temperatura)
        }
    }

    private fun dispararNotificacion(temperatura: Double) {
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

            val notificacion = NotificationCompat.Builder(this, AppMoviles.CANAL_ALARMA)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("ALARMA Cadena de Frio")
                .setContentText("Temperatura: ${temperatura}°C supera limite de ${AppMoviles.LIMITE_TEMPERATURA}°C")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(1001, notificacion)
            Log.d("TemperaturaActivity", "notificacion enviada")
        } catch (e: Exception) {
            Log.e("TemperaturaActivity", "Error enviando notificacion: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        database.reference
            .child("temperatura")
            .child("camion1")
            .child("valor")
            .removeEventListener(listenerTemperatura)
    }
}