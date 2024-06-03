package com.jjas.proyectofinalciclo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.jjas.proyectofinalciclo.databinding.FragmentVerEmpleadoBinding

class FragmentVerEmpleado: Fragment() {
    private lateinit var binding: FragmentVerEmpleadoBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var colRefEmpresa: CollectionReference
    private lateinit var colRefEmpleado: CollectionReference
    private var empleadoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Instancia la referencia a la base de datos de Empresas
        db = FirebaseFirestore.getInstance()
        colRefEmpresa = db.collection("Empresas")
        colRefEmpleado = db.collection("Empleados")

        // Recupera los argumentos enviados por el fragment que lo llama
        val argumentos = arguments
        if (argumentos != null) {
            empleadoId= argumentos.getString("id")
            val nuevo = argumentos.getBoolean("nuevo")
            if (nuevo) {
                Object.mensajeToast(requireContext(), R.string.empleado_anyadido)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVerEmpleadoBinding.inflate(inflater)

        // Muestra los datos del empleado
        mostrarDatos()

        // Funcionalidad de los botones
        with(binding) {
            btGmapsEmpleado.setOnClickListener {
                val uri = Uri.parse("geo:0,0?q=${tvDireccionEmpleado.text}")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }

            btLlamarEmpleado.setOnClickListener {
                val uri = Uri.parse("tel:${tvTelefonoEmpleado.text}")
                val intent = Intent(Intent.ACTION_DIAL, uri)
                startActivity(intent)
            }

            btMandarCorreoEmpleado.setOnClickListener {
                val uri = Uri.parse("mailto: ${tvCorreoEmpleado.text}")
                val intent = Intent(Intent.ACTION_SENDTO, uri)
                startActivity(intent)
            }

            btMostrarEmpresa.setOnClickListener {
                mostrarEmpresa()
            }

            btBorrarEmpleado.setOnClickListener {
                mostrarDialog()
            }

            btEditarEmpleado.setOnClickListener {
                val args = Bundle()
                args.putString("id", empleadoId)
                val fragment = FragmentEditarEmpleado()
                fragment.arguments = args

                if (activity is InfoActivity) {
                    (activity as InfoActivity).mostrarFragment(fragment)
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

    // Muestra en pantalla los datos del empleado
    private fun mostrarDatos() {
        val docRef = colRefEmpleado.document(empleadoId ?: "")
        docRef.get().apply{
            addOnSuccessListener {
                if (!isAdded) {
                    return@addOnSuccessListener
                }
                if (it["nombre"] == null) {
                    // Vuelve al fragment anterior
                    val fragmentManager = requireActivity().supportFragmentManager
                    fragmentManager.popBackStack()
                } else {
                    with(binding) {
                        tvNombreEmpleado.text = it["nombre"].toString()
                        tvFechaNacimiento.text = it["fecha"].toString()
                        tvPuesto.text = it["puesto"].toString()
                        tvDni.text = it["dni"].toString()
                        tvDireccionEmpleado.text = it["direccion"].toString()
                        tvTelefonoEmpleado.text = it["telefono"].toString()
                        tvCorreoEmpleado.text = it["correo"].toString()
                        tvEmpresaEmpleado.text = it["empresa"].toString()
                    }
                }
            }
        }
    }

    // Pide confirmación al usuario antes de eliminar un empleado
    private fun mostrarDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.apply {
            setMessage("¿Desea eliminar el empleado de la base de datos?")
            setPositiveButton(android.R.string.ok) {_, _ ->
                val docRef = colRefEmpleado.document(empleadoId!!)
                docRef.delete()
                    .addOnSuccessListener {
                        // Informa al usuario
                        Object.mensajeToast(requireContext(), R.string.empleado_eliminado)

                        // Vuelve al fragment anterior
                        val fragmentManager = requireActivity().supportFragmentManager
                        fragmentManager.popBackStack()
                    }
                    .addOnFailureListener {
                        // Informa al usuario
                        Object.mensajeToast(requireContext(), R.string.error_borrar_empleado)
                    }
            }
            setNegativeButton(android.R.string.cancel) {_, _ ->
                return@setNegativeButton
            }
        }.show()
    }

    // Llama un fragment que muestra los datos de la empresa
    private fun mostrarEmpresa() {
        val nombreEmpresa = binding.tvEmpresaEmpleado.text.toString()
        colRefEmpresa.whereEqualTo("nombre", nombreEmpresa).get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val empresaID = result.documents[0].id
                    val args = Bundle()
                    args.putString("id", empresaID)
                    val fragment = FragmentVerEmpresa()
                    fragment.arguments = args

                    if (activity is InfoActivity) {
                        (activity as InfoActivity).mostrarFragment(fragment)
                    }
                }
            }
            .addOnFailureListener {
                Object.mensajeToast(requireContext(), R.string.error_mostrar_empresa)
            }
    }
}