package com.jjas.proyectofinalciclo

import android.content.Context
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class Object {
    companion object {
        fun ocultarTeclado(context: Context, view: View) {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun mensajeToast(context: Context, message: Int) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        // Comprueba si el formato del CIF es correcto
        fun validarCif(cif: String): Boolean {
            val patron = Regex("[A-Z]\\d{8}")
            return patron.matches(cif)
        }

        // Comprueba si el formato del DNI es correcto
        fun validarDni(dni: String): Boolean {
            val patron = Regex("^[0-9]{8}[A-Z]$")
            return patron.matches(dni)
        }

        // Comprueba si el CIF ya está asignado
        fun cifUnico(cif: String, callback: (Boolean) -> Unit) {
            val db = FirebaseFirestore.getInstance()
            val colRef = db.collection("Empresas")
            colRef.whereEqualTo("cif", cif).get()
                .addOnSuccessListener { querySnapshot ->
                    val coincidencias = querySnapshot.isEmpty
                    callback(coincidencias)
                }
                .addOnFailureListener {
                    callback(false)
                }
        }

        // Comprueba si el DNI ya está asignado
        fun dniUnico(dni: String, callback: (Boolean) -> Unit) {
            val db = FirebaseFirestore.getInstance()
            val colRef = db.collection("Empleados")
            colRef.whereEqualTo("dni", dni).get()
                .addOnSuccessListener { querySnapshot ->
                    val coincidencias = querySnapshot.isEmpty
                    callback(coincidencias)
                }
                .addOnFailureListener {
                    callback(false)
                }
        }

        // Comprueba si el formato del teléfono es correcto
        fun validarTelefono(telefono: String): Boolean {
            val patron = Regex("^[0-9]{9}$")
            return patron.matches(telefono)
        }

        // Comprueba si el formato del correo es correcto
        fun validarEmail(email: String): Boolean {
            val patron = Patterns.EMAIL_ADDRESS
            return patron.matcher(email).matches()
        }

        // Comprueba si el formato de la web es correcto
        fun validarWeb(web: String): Boolean {
            val patron = Patterns.WEB_URL
            return patron.matcher(web).matches()
        }

        // Comprueba que la fecha elegida sea posterior a 1950 y anterior a 2008
        fun validarFecha(fechaNac: String): Boolean {
            return if (fechaNac.isNotEmpty()) {
                val arrayFecha = fechaNac.split("/")
                val anyo = arrayFecha[2].toInt()
                anyo in 1950..2008
            } else {
                false
            }
        }
    }
}