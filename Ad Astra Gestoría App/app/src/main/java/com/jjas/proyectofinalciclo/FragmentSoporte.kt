package com.jjas.proyectofinalciclo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.jjas.proyectofinalciclo.databinding.FragmentSoporteBinding

class FragmentSoporte: Fragment() {
    private lateinit var binding: FragmentSoporteBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var usuario: FirebaseUser
    private lateinit var correo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        usuario = auth.currentUser!!

        // Recupera el correo del usuario logueado
        correo = usuario.email.toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSoporteBinding.inflate(inflater)

        with(binding) {
            // Funcionalidad del Spinner de tipos de contacto
            configurarSpinner()

            // Funcionalidad del botón Enviar
            binding.btEnviar.setOnClickListener {
                // Oculta el teclado
                Object.ocultarTeclado(requireContext(), it)

                // Almacena los valores de los campos
                val asunto = etAsunto.text.toString()
                val descripcion = etDescripcion.text.toString()
                val tipoContacto = spTipos.selectedItem.toString()

                if (asunto.isNotEmpty() && descripcion.isNotEmpty()) {
                    enviarComunicacion(asunto, tipoContacto, descripcion)
                } else {
                    Object.mensajeToast(requireContext(), R.string.datos_vacios)
                    if (asunto.isEmpty()){
                        etAsunto.error = getString(R.string.obligatorio)
                    }
                    if (descripcion.isEmpty()){
                        etDescripcion.error = getString(R.string.obligatorio)
                    }
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

    // Configura el Spinner de tipos de contacto
    private fun configurarSpinner() {
        val tipos: MutableList<String> = arrayListOf()
        // Añade valores al array tipos uno por uno
        tipos.add(getString(R.string.incidencia))
        tipos.add(getString(R.string.fallo))
        tipos.add(getString(R.string.recomendacion))

        val adaptador = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            tipos)
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spTipos.adapter = adaptador
    }

    // Actualiza el campo con el valor indicado
    private fun enviarComunicacion(asunto: String, tipoContacto: String, descripcion: String) {
        val mensaje = "Contacto con soporte.\n\n" +
                           "Usuario: $correo\n" +
                           "Asunto: $asunto\n" +
                           "Motivo de contacto: $tipoContacto\n" +
                           "Mensaje: $descripcion"
        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.soporte), null))

        intent.putExtra(Intent.EXTRA_SUBJECT, "Contacto de usuario de Ad Astra Gestoría")
        intent.putExtra(Intent.EXTRA_TEXT, mensaje)
        context?.startActivity(Intent.createChooser(intent, "Selecciona un cliente de correo:"))
    }
}