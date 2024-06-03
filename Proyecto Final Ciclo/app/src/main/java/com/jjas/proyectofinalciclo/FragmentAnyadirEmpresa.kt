package com.jjas.proyectofinalciclo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.jjas.proyectofinalciclo.databinding.FragmentAnyadirEmpresaBinding

class FragmentAnyadirEmpresa: Fragment() {
    private lateinit var binding: FragmentAnyadirEmpresaBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var colRef: CollectionReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Instancia la referencia a la base de datos de Empresas
        db = FirebaseFirestore.getInstance()
        colRef = db.collection("Empresas")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnyadirEmpresaBinding.inflate(inflater)

        // Funcionalidad de los botones
        with(binding) {
            btRegistrarEmpresa.setOnClickListener {
                // Oculta el teclado
                Object.ocultarTeclado(requireContext(), it)

                // Almacena los valores de los campos
                val cif = etCif.text.toString().uppercase()
                val sector = etSectorEmpresa.text.toString()
                val nombre = etNombreEmpresa.text.toString()
                val domicilio = etDireccionEmpresa.text.toString()
                val telefono = etTelefonoEmpresa.text.toString()
                val email = etCorreoEmpresa.text.toString()
                val web = etUrlEmpresa.text.toString()
                val logo = etUrlLogo.text.toString()

                if (cif.isNotEmpty() && sector.isNotEmpty() && nombre.isNotEmpty() &&
                    domicilio.isNotEmpty() && telefono.isNotEmpty() && email.isNotEmpty() &&
                    web.isNotEmpty()) {
                    if (Object.validarCif(cif)) {
                        Object.cifUnico(cif) {esUnico ->
                            if (esUnico) {
                                if (Object.validarTelefono(telefono)) {
                                    if (Object.validarEmail(email)) {
                                        if (Object.validarWeb(web)) {
                                            anyadirEmpresa(
                                                cif, sector, nombre, domicilio, telefono,
                                                email, web, logo
                                            )
                                        } else {
                                            Object.mensajeToast(
                                                requireContext(),
                                                R.string.web_formato_invalido
                                            )
                                            etUrlEmpresa.error = getString(R.string.incorrecto)
                                        }
                                    } else {
                                        Object.mensajeToast(
                                            requireContext(),
                                            R.string.email_formato_invalido
                                        )
                                        etCorreoEmpresa.error = getString(R.string.incorrecto)
                                    }
                                } else {
                                    Object.mensajeToast(requireContext(), R.string.telefono_formato_invalido)
                                    etTelefonoEmpresa.error = getString(R.string.incorrecto)
                                }
                            } else {
                                Object.mensajeToast(requireContext(), R.string.cif_repetido)
                                etCif.error = getString(R.string.incorrecto)
                            }
                        }
                    } else {
                        Object.mensajeToast(requireContext(), R.string.cif_formato_invalido)
                        etCif.error = getString(R.string.incorrecto)
                    }
                } else {
                    Object.mensajeToast(requireContext(), R.string.datos_vacios)
                    if (cif.isEmpty()) {
                        etCif.error = getString(R.string.obligatorio)
                    }
                    if (sector.isEmpty()) {
                        etSectorEmpresa.error = getString(R.string.obligatorio)
                    }
                    if (nombre.isEmpty()) {
                        etNombreEmpresa.error = getString(R.string.obligatorio)
                    }
                    if (domicilio.isEmpty()) {
                        etDireccionEmpresa.error = getString(R.string.obligatorio)
                    }
                    if (telefono.isEmpty()) {
                        etTelefonoEmpresa.error = getString(R.string.obligatorio)
                    }
                    if (email.isEmpty()) {
                        etCorreoEmpresa.error = getString(R.string.obligatorio)
                    }
                    if (web.isEmpty()) {
                        etUrlEmpresa.error = getString(R.string.obligatorio)
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

    // Añade la empresa a la base de datos de Firestore
    private fun anyadirEmpresa(
        cif: String, sector: String, nombre: String, domicilio: String, telefono: String,
        email: String, web: String?, logo: String?
    ) {
        val empresa = hashMapOf(
            "cif" to cif,
            "sector" to sector,
            "nombre" to nombre,
            "domicilio" to domicilio,
            "telefono" to telefono,
            "email" to email,
            "web" to web,
            "logo" to logo
        )

        colRef.add(empresa)
            .addOnSuccessListener {
                val argumentos = Bundle()
                argumentos.putString("id", it.id)
                argumentos.putBoolean("nuevo", true)
                val fragment = FragmentVerEmpresa()
                fragment.arguments = argumentos

                if (activity is InfoActivity) {
                    (activity as InfoActivity).mostrarFragment(fragment)
                }
            }
            .addOnFailureListener {
                Object.mensajeToast(requireContext(), R.string.error_anyadir_empresa)
            }
    }
}