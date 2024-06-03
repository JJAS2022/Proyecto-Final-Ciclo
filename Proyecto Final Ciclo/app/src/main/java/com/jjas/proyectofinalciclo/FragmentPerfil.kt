package com.jjas.proyectofinalciclo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.jjas.proyectofinalciclo.databinding.FragmentPerfilBinding

class FragmentPerfil : Fragment() {
    private lateinit var binding: FragmentPerfilBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var colRef: CollectionReference
    private lateinit var usuario: FirebaseUser
    private lateinit var id: String
    private lateinit var nombre: String
    private lateinit var apellido: String
    private lateinit var correo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        usuario = auth.currentUser!!

        // Recupera el correo del usuario logueado
        correo = usuario.email.toString()

        // Instancia la referencia a la base de datos de Usuarios
        db = FirebaseFirestore.getInstance()
        colRef = db.collection("Usuarios")

        // Obtiene los datos del usuario logueado
        obtenerDatos()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPerfilBinding.inflate(inflater)

        with(binding) {
            // Funcionalidad del TextView con el correo electrónico
            tvEmailPerfil.setOnClickListener {
                Object.mensajeToast(requireContext(), R.string.correo_inmutable)
            }

            // Funcionalidad del botón para enviar feedback
            btFeedback?.setOnClickListener {
                val uri = Uri.parse("https://docs.google.com/forms/d/e/1FAIpQLSdp0c3Zk5aMXZLAGDxQ4ytqx6F03iStDRQDkMXwbj_bch_ahQ/viewform?vc=0&c=0&w=1&flr=0")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }

            // Funcionalidad del botón para contactar con soporte
            btContactar?.setOnClickListener {
                Object.ocultarTeclado(requireContext(), it)
                if (activity is InfoActivity) {
                    (activity as InfoActivity).mostrarFragment(FragmentSoporte())
                }
            }

            // Funcionalidad de los botones de actualización
            btActualizarNombre.setOnClickListener {
                Object.ocultarTeclado(requireContext(), it)
                val nuevoValor = etNombrePerfil.text.toString()
                if(nuevoValor != nombre) {
                    if(nuevoValor.isNotEmpty()) {
                        actualizarDatos("nombre", nuevoValor)
                    } else {
                        Object.mensajeToast(requireContext(), R.string.dato_vacio)
                        etNombrePerfil.error = getString(R.string.incorrecto)
                    }
                } else {
                    Object.mensajeToast(requireContext(), R.string.dato_igual)
                    etNombrePerfil.error = getString(R.string.igual)
                }
            }

            btActualizarApellido.setOnClickListener {
                Object.ocultarTeclado(requireContext(), it)
                val nuevoValor = etApellidoPerfil.text.toString()
                if(nuevoValor != apellido) {
                    if(nuevoValor.isNotEmpty()) {
                        actualizarDatos("apellido", nuevoValor)
                    } else {
                        Object.mensajeToast(requireContext(), R.string.dato_vacio)
                        etApellidoPerfil.error = getString(R.string.incorrecto)
                    }
                } else {
                    Object.mensajeToast(requireContext(), R.string.dato_igual)
                    etApellidoPerfil.error = getString(R.string.igual)
                }
            }

            btActualizarContra.setOnClickListener {
                Object.ocultarTeclado(requireContext(), it)
                val nuevoValor = etPassPerfil.text.toString()
                if (nuevoValor.isNotEmpty()) {
                    usuario.updatePassword(nuevoValor)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Object.mensajeToast(
                                    requireContext(),
                                    R.string.modificacion_correcta
                                )
                            } else {
                                // Informa al usuario del tipo de error que se ha producido
                                try {
                                    throw task.exception!!
                                } catch (e: FirebaseAuthWeakPasswordException) {
                                    Object.mensajeToast(requireContext(), R.string.contra_debil)
                                    etPassPerfil.error = getString(R.string.incorrecto)
                                }  catch (e: Exception) {
                                    Object.mensajeToast(requireContext(), R.string.modificacion_incorrecta)
                                    etPassPerfil.error = getString(R.string.incorrecto)
                                }
                            }
                        }
                } else {
                    Object.mensajeToast(requireContext(), R.string.dato_vacio)
                    etPassPerfil.error = getString(R.string.dato_vacio)
                }
            }
        }
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activity is InfoActivity) {
            (activity as InfoActivity).cerrarFragment()
        }
    }

    // Recupera de la base de datos de Firestore los datos del usuario logueado
    private fun obtenerDatos() {
        colRef.whereEqualTo("correo", correo)
            .addSnapshotListener { querySnapshot, exception ->
                if (exception != null) {
                    return@addSnapshotListener
                }
                for (document in querySnapshot!!) {
                    id = document.id
                    nombre = document["nombre"].toString()
                    apellido = document["apellido"].toString()
                    correo = document["correo"].toString()

                    // Muestra los datos en los campos del layout
                    with(binding) {
                        etNombrePerfil.setText(nombre)
                        etApellidoPerfil.setText(apellido)
                        tvEmailPerfil.text = correo
                    }
                }
            }
    }

    // Actualiza el campo con el valor indicado
    private fun actualizarDatos(campo: String, valor: String) {
        colRef.document(id).update(campo, valor)
            .addOnSuccessListener {
                Object.mensajeToast(requireContext(), R.string.modificacion_correcta)
            }
            .addOnFailureListener {
                Object.mensajeToast(requireContext(), R.string.modificacion_incorrecta)
            }
    }
}