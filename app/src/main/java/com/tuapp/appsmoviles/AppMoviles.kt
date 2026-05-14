package com.tuapp.appsmoviles

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class AppMoviles : Application() {

    companion object {
        const val CANAL_ALARMA = "canal_temperatura"
        const val LIMITE_TEMPERATURA = -18.0
    }

    override fun onCreate() {
        super.onCreate()
        crearCanalNotificacion()
    }

    private fun crearCanalNotificacion() {
        val canal = NotificationChannel(
            CANAL_ALARMA,
            "Alarma de Temperatura",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alertas cuando la temperatura supera el limite permitido"
            enableVibration(true)
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(canal)
    }
}