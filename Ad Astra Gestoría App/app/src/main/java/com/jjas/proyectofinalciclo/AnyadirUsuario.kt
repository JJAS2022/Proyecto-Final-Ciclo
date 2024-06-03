package com.jjas.proyectofinalciclo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.jjas.proyectofinalciclo.databinding.ActivityAnyadirUsuarioBinding

class AnyadirUsuario : AppCompatActivity() {
    private lateinit var binding: ActivityAnyadirUsuarioBinding
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    lateinit var colRef: CollectionReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnyadirUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Instancia el objeto para utilizar Authentication
        auth = Firebase.auth

        // Instancia el objeto para utilizar la base de datos
        db = FirebaseFirestore.getInstance()
        colRef = db.collection("Usuarios")

        // Oculta la barra superior en esta pantalla
        supportActionBar?.hide()

        // Funcionalidad del botón Registrar
        binding.btRegistro.setOnClickListener {
            // Oculta el teclado
            Object.ocultarTeclado(this, it)

            // Almacena los valores de los campos
            val nombre = binding.etNombre.text.toString()
            val apellido = binding.etApellido.text.toString()
            val correo = binding.etEmail.text.toString()
            val contra = binding.etPass.text.toString()

            if (nombre.isNotEmpty() && apellido.isNotEmpty() && correo.isNotEmpty() && contra.isNotEmpty()) {
                crearCuenta(nombre, apellido, correo, contra)
            } else {
                Object.mensajeToast(this, R.string.datos_vacios)
                if (nombre.isEmpty()){
                    binding.etNombre.error = getString(R.string.obligatorio)
                }
                if (apellido.isEmpty()){
                    binding.etApellido.error = getString(R.string.obligatorio)
                }
                if (correo.isEmpty()){
                    binding.etEmail.error = getString(R.string.obligatorio)
                }
                if (contra.isEmpty()) {
                    binding.etPass.error = getString(R.string.obligatorio)
                }
            }
        }
    }

    // Crea una cuenta con Authenticator con el correo y la contraseña indicadas
    fun crearCuenta(nombre: String, apellido: String, correo: String, contra: String) {
        auth.createUserWithEmailAndPassword(correo, contra)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Actualiza la base de datos de Usuarios con los datos del usuario
                    anyadirUsuario(nombre, apellido, correo)

                    // Cierra la actividad
                    finish()
                } else {
                    // Informa al usuario del tipo de error que se ha producido
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthUserCollisionException) {
                        Object.mensajeToast(this, R.string.usuario_repetido)
                        binding.etEmail.error = getString(R.string.incorrecto)
                    } catch (e: FirebaseAuthWeakPasswordException) {
                        Object.mensajeToast(this, R.string.contra_debil)
                        binding.etPass.error = getString(R.string.incorrecto)
                    }  catch (e: Exception) {
                        Object.mensajeToast(this, R.string.error_anyadir_usuario)
                    }
                }
            }
    }

    // Actualiza la base de datos de Firestore con los datos del usuario
    private fun anyadirUsuario(nombre: String, apellido: String, correo: String) {
        val usuario = hashMapOf(
            "nombre" to nombre,
            "apellido" to apellido,
            "correo" to correo
        )

        colRef.add(usuario)
            .addOnSuccessListener {
                Object.mensajeToast(this, R.string.usuario_anyadido)
            }
            .addOnFailureListener {
                Object.mensajeToast(this, R.string.error_anyadir_usuario)
            }
    }
}