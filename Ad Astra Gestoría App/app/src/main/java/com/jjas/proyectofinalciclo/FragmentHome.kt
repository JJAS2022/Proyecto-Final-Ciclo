package com.jjas.proyectofinalciclo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.jjas.proyectofinalciclo.databinding.FragmentHomeBinding

class FragmentHome: Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var colRef: CollectionReference
    private lateinit var correo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        // Recupera el correo del usuario logueado
        correo = auth.currentUser?.email.toString()

        // Instancia la referencia a la base de datos de Usuarios
        db = FirebaseFirestore.getInstance()
        colRef = db.collection("Usuarios")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)

        // Funcionalidad de los botones
        with(binding) {
            btListaEmpresas.setOnClickListener {
                val argumentos = Bundle()
                argumentos.putString("id", null)
                val fragment = FragmentRecyclerEmpresas()
                fragment.arguments = argumentos
                if (activity is InfoActivity) {
                    (activity as InfoActivity).mostrarFragment(fragment)
                }
            }

            btAnyadirEmpresa.setOnClickListener {
                if (activity is InfoActivity) {
                    (activity as InfoActivity).mostrarFragment(FragmentAnyadirEmpresa())
                }
            }

            btListaEmpleados.setOnClickListener {
                if (activity is InfoActivity) {
                    (activity as InfoActivity).mostrarFragment(FragmentRecyclerEmpleados())
                }
            }

            btAnyadirEmpleado.setOnClickListener {
                val argumentos = Bundle()
                argumentos.putString("id", null)
                val fragment = FragmentAnyadirEmpleado()
                fragment.arguments = argumentos
                if (activity is InfoActivity) {
                    (activity as InfoActivity).mostrarFragment(fragment)
                }
            }
        }

        // Muestra el saludo al usuario en pantalla
        saludarUsuario()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        saludarUsuario()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activity is InfoActivity) {
            (activity as InfoActivity).cerrarFragment()
        }
    }

    // Muestra un texto de bienvenida personalizado con el nombre del usuario
    private fun saludarUsuario() {
        var texto = ""
        colRef.whereEqualTo("correo", correo)
            .addSnapshotListener { querySnapshot, exception ->
                if (exception != null) {
                    return@addSnapshotListener
                }
                for (document in querySnapshot!!) {
                    texto += "Bienvenido/a,\n${document["nombre"]}"
                    binding.tvBienvenida.text = texto
                }
            }
    }
}