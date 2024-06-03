package com.jjas.proyectofinalciclo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.jjas.proyectofinalciclo.databinding.FragmentEditarEmpresaBinding

class FragmentEditarEmpresa: Fragment() {
    private lateinit var binding: FragmentEditarEmpresaBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var colRef: CollectionReference
    private var id: String? = null
    private lateinit var nombre: String
    private lateinit var cif: String
    private lateinit var sector: String
    private lateinit var domicilio: String
    private lateinit var telefono: String
    private lateinit var correo: String
    private lateinit var web: String
    private lateinit var logo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Instancia la referencia a la base de datos de Empresas
        db = FirebaseFirestore.getInstance()
        colRef = db.collection("Empresas")

        // Recupera los argumentos enviados por el fragment que lo llama
        val argumentos = arguments
        if (argumentos != null) {
            id = argumentos.getString("id")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditarEmpresaBinding.inflate(inflater)

        // Recupera los datos de la empresa a editar
        mostrarDatos()

        // Funcionalidad del botón Actualizar
        binding.btEditarEmpresa.setOnClickListener {
            val nuevoNombre = binding.etNombreEmpresaEditar.text.toString()
            val nuevoCif = binding.etCifEditar.text.toString()
            val nuevoSector = binding.etSectorEmpresaEditar.text.toString()
            val nuevoDomicilio = binding.etDireccionEmpresaEditar.text.toString()
            val nuevoTelefono = binding.etTelefonoEmpresaEditar.text.toString()
            val nuevoCorreo = binding.etCorreoEmpresaEditar.text.toString()
            val nuevaWeb = binding.etUrlEmpresaEditar.text.toString()
            val nuevoLogo = binding.etUrlLogoEditar.text.toString()

            if (nuevoNombre.isNotEmpty() && nuevoCif.isNotEmpty() && nuevoSector.isNotEmpty() &&
                nuevoDomicilio.isNotEmpty() && nuevoTelefono.isNotEmpty() &&
                nuevoCorreo.isNotEmpty() && nuevaWeb.isNotEmpty()) {
                if (nuevoNombre != nombre || nuevoCif != cif || nuevoSector != sector ||
                    nuevoDomicilio != domicilio || nuevoTelefono != telefono || nuevoCorreo != correo
                    || nuevaWeb != web || nuevoLogo != logo) {
                    if (Object.validarCif(nuevoCif)) {
                        Object.cifUnico(nuevoCif) {esUnico ->
                            if (esUnico || cif == nuevoCif) {
                                if (Object.validarTelefono(nuevoTelefono)) {
                                    if (Object.validarEmail(nuevoCorreo)) {
                                        if (Object.validarWeb(nuevaWeb)) {
                                            actualizarDatos(
                                                nuevoNombre, nuevoCif, nuevoSector, nuevoDomicilio,
                                                nuevoTelefono, nuevoCorreo, nuevaWeb, nuevoLogo
                                            )
                                        } else {
                                            Object.mensajeToast(
                                                requireContext(),
                                                R.string.web_formato_invalido
                                            )
                                            binding.etUrlEmpresaEditar.error =
                                                getString(R.string.incorrecto)
                                        }
                                    } else {
                                        Object.mensajeToast(
                                            requireContext(),
                                            R.string.email_formato_invalido
                                        )
                                        binding.etCorreoEmpresaEditar.error =
                                            getString(R.string.incorrecto)
                                    }
                                } else {
                                    Object.mensajeToast(requireContext(), R.string.telefono_formato_invalido)
                                    binding.etTelefonoEmpresaEditar.error = getString(R.string.incorrecto)
                                }
                            } else {
                                Object.mensajeToast(requireContext(), R.string.cif_repetido)
                                binding.etCifEditar.error = getString(R.string.incorrecto)
                            }
                        }
                    } else {
                        Object.mensajeToast(requireContext(), R.string.cif_formato_invalido)
                        binding.etCifEditar.error = getString(R.string.incorrecto)
                    }
                } else {
                    Object.mensajeToast(requireContext(), R.string.datos_iguales)
                }
            } else {
                Object.mensajeToast(requireContext(), R.string.datos_vacios)
                if (nuevoNombre.isEmpty()) {
                    binding.etNombreEmpresaEditar.error = getString(R.string.obligatorio)
                }
                if (nuevoCif.isEmpty()) {
                    binding.etCifEditar.error = getString(R.string.obligatorio)
                }
                if (nuevoSector.isEmpty()) {
                    binding.etSectorEmpresaEditar.error = getString(R.string.obligatorio)
                }
                if (nuevoDomicilio.isEmpty()) {
                    binding.etDireccionEmpresaEditar.error = getString(R.string.obligatorio)
                }
                if (nuevoTelefono.isEmpty()) {
                    binding.etTelefonoEmpresaEditar.error = getString(R.string.obligatorio)
                }
                if (nuevoCorreo.isEmpty()) {
                    binding.etCorreoEmpresaEditar.error = getString(R.string.obligatorio)
                }
                if (nuevaWeb.isEmpty()) {
                    binding.etUrlEmpresaEditar.error = getString(R.string.obligatorio)
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

    // Muestra los datos de la empresa a editar
    private fun mostrarDatos() {
        val docRef = colRef.document(id!!)
        docRef.get().apply {
            addOnSuccessListener {
                nombre = it["nombre"].toString()
                cif = it["cif"].toString()
                sector = it["sector"].toString()
                domicilio = it["domicilio"].toString()
                telefono = it["telefono"].toString()
                correo = it["email"].toString()
                web = it["web"].toString()
                logo = it["logo"].toString()

                with(binding) {
                    etNombreEmpresaEditar.setText(nombre)
                    etCifEditar.setText(cif)
                    etSectorEmpresaEditar.setText(sector)
                    etDireccionEmpresaEditar.setText(domicilio)
                    etTelefonoEmpresaEditar.setText(telefono)
                    etCorreoEmpresaEditar.setText(correo)
                    etUrlEmpresaEditar.setText(web)
                    etUrlLogoEditar.setText(logo)
                }
            }
        }

    }

    // Actualiza la base de datos Firestore con los datos
    private fun actualizarDatos(
        nombre: String, cif: String, sector: String, domicilio: String, telefono: String,
        correo: String, web: String, logo: String?
    ) {
        val docRef = colRef.document(id!!)

        val datos = mapOf(
            "nombre" to nombre,
            "cif" to cif,
            "sector" to sector,
            "domicilio" to domicilio,
            "telefono" to telefono,
            "correo" to correo,
            "web" to web,
            "logo" to logo
        )

        docRef.update(datos)
            .addOnSuccessListener {
                Object.mensajeToast(requireContext(), R.string.empresa_actualizada)

                // Vuelve al fragment anterior
                val fragmentManager = requireActivity().supportFragmentManager
                fragmentManager.popBackStack()
            }
            .addOnFailureListener {
                Object.mensajeToast(requireContext(), R.string.error_actualizar_empresa)
            }
    }
}