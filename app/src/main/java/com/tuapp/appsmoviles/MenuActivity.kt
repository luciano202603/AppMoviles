package com.tuapp.appsmoviles

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MenuActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        auth = FirebaseAuth.getInstance()
        val usuario = auth.currentUser

        // Mostrar datos del usuario
        val tvBienvenida = findViewById<TextView>(R.id.tvBienvenida)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)

        tvBienvenida.text = "Bienvenido " + (usuario?.displayName ?: "Usuario")
        tvEmail.text = usuario?.email ?: ""

        // Boton Catalogo
        findViewById<Button>(R.id.btnCatalogo).setOnClickListener {
            startActivity(Intent(this, CatalogoActivity::class.java))
        }

        // Boton Temperatura
        findViewById<Button>(R.id.btnTemperatura).setOnClickListener {
            startActivity(Intent(this, TemperaturaActivity::class.java))
        }

        // Boton Mapa
        findViewById<Button>(R.id.btnMapa).setOnClickListener {
            startActivity(Intent(this, MapaActivity::class.java))
        }

        // Boton Cerrar Sesion
        findViewById<Button>(R.id.btnCerrarSesion).setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Reemplaza onBackPressed deprecado
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                moveTaskToBack(true)
            }
        })
    }
}