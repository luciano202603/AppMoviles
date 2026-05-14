package com.tuapp.appsmoviles

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.tuapp.appsmoviles.utils.GpsHelper

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var gpsHelper: GpsHelper

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogle: Button
    private lateinit var tvRegistrar: TextView
    private lateinit var progressBar: ProgressBar

    // Manejador moderno de permisos (reemplaza onRequestPermissionsResult)
    private val solicitarPermisoUbicacion = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { otorgado ->
        if (otorgado) {
            // Permiso otorgado, guardar GPS
            guardarGps()
        } else {
            // Permiso denegado, ir al menu sin GPS
            Toast.makeText(
                this,
                "GPS no disponible, continuando sin ubicación",
                Toast.LENGTH_SHORT
            ).show()
            irAlMenu()
        }
    }

    // Manejador de Google Sign-In
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthConGoogle(account.idToken!!)
        } catch (e: ApiException) {
            mostrarCarga(false)
            Toast.makeText(this, "Error Google: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        gpsHelper = GpsHelper(this)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogle = findViewById(R.id.btnGoogle)
        tvRegistrar = findViewById(R.id.tvRegistrar)
        progressBar = findViewById(R.id.progressBar)

        // Configurar Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Boton login con email
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (email.isEmpty()) {
                etEmail.error = "Ingresa tu correo"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Ingresa tu contrasena"
                return@setOnClickListener
            }
            mostrarCarga(true)
            loginConEmail(email, password)
        }

        // Boton login con Google
        btnGoogle.setOnClickListener {
            mostrarCarga(true)
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }

        tvRegistrar.setOnClickListener {
            Toast.makeText(this, "Funcion de registro proximamente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginConEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                mostrarCarga(false)
                if (task.isSuccessful) {
                    verificarYPedirGps()
                } else {
                    Toast.makeText(
                        this,
                        "Error: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }



    private fun firebaseAuthConGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                mostrarCarga(false)
                if (task.isSuccessful) {
                    verificarYPedirGps()
                } else {
                    Toast.makeText(
                        this,
                        "Error: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    // Verificar permisos antes de obtener GPS
    private fun verificarYPedirGps() {
        if (gpsHelper.tienePermisos()) {
            guardarGps()
        } else {
            solicitarPermisoUbicacion.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Obtener y guardar GPS en Firebase
    private fun guardarGps() {
        gpsHelper.obtenerYGuardarUbicacion(
            onExito = { latitud, longitud ->
                Toast.makeText(
                    this,
                    "Ubicacion guardada: $latitud, $longitud",
                    Toast.LENGTH_SHORT
                ).show()
                irAlMenu()
            },
            onError = { error ->
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                irAlMenu()
            }
        )
    }

    private fun irAlMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun mostrarCarga(mostrar: Boolean) {
        progressBar.visibility = if (mostrar) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !mostrar
        btnGoogle.isEnabled = !mostrar
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            irAlMenu()
        }
    }
}