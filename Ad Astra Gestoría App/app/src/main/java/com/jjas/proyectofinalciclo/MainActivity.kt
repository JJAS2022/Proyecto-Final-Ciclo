package com.jjas.proyectofinalciclo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.jjas.proyectofinalciclo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var colRef: CollectionReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Instancia el objeto para utilizar Authentication
        auth = Firebase.auth

        // Instancia la referencia a la base de datos de Usuarios
        db = FirebaseFirestore.getInstance()
        colRef = db.collection("Usuarios")

        // Oculta la barra superior en esta pantalla
        supportActionBar?.hide()

        // Funcionalidad del botón Acceder
        binding.btAcceder.setOnClickListener {
            Object.ocultarTeclado(this, it)
            val correo = binding.etEmail.text.toString()
            val contra = binding.etContra.text.toString()

            if (correo.isNotEmpty() && contra.isNotEmpty()) {
                accederCuenta(correo, contra)
            } else {
                Object.mensajeToast(this, R.string.datos_vacios)
                if (correo.isEmpty()){
                    binding.etEmail.error = getString(R.string.obligatorio)
                }
                if (contra.isEmpty()) {
                    binding.etContra.error = getString(R.string.obligatorio)
                }
            }
        }

        // Funcionalidad del TextView "aquí"
        binding.tvAqui.setOnClickListener {
            val intent = Intent(this, AnyadirUsuario::class.java)
            startActivity(intent)
        }
    }

    // Comprueba si ya hay algún usuario autenticado y pasa a la siguiente actividad
    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            lanzarAplicacion()
        }
    }

    // Comprueba los datos de acceso con Authentication
    private fun accederCuenta(correo: String, contra: String) {
        auth.signInWithEmailAndPassword(correo, contra)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    lanzarAplicacion()
                } else {
                    Object.mensajeToast(this, R.string.datos_erroneos)
                }
            }
    }

    // Lanza la aplicación con el usuario autenticado
    private fun lanzarAplicacion() {
        val intent = Intent(this, InfoActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}