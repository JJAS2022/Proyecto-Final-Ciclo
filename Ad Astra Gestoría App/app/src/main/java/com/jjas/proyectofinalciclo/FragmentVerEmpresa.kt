package com.jjas.proyectofinalciclo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.jjas.proyectofinalciclo.databinding.FragmentVerEmpresaBinding

class FragmentVerEmpresa: Fragment() {
    private lateinit var binding: FragmentVerEmpresaBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var colRefEmpresa: CollectionReference
    private lateinit var colRefEmpleados: CollectionReference
    private var id: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Instancia la referencia a las bases de datos de Empresas y empleados
        db = FirebaseFirestore.getInstance()
        colRefEmpresa = db.collection("Empresas")
        colRefEmpleados = db.collection("Empleados")

        // Recupera los argumentos enviados por el fragment que lo llama
        val argumentos = arguments
        if (argumentos != null) {
            id = argumentos.getString("id")
            val nuevo = argumentos.getBoolean("nuevo")
            if (nuevo) {
                Object.mensajeToast(requireContext(), R.string.empresa_anyadida)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVerEmpresaBinding.inflate(inflater)

        // Muestra los datos de la empresa
        mostrarDatos()

        // Funcionalidad de los botones
        with(binding) {
           btGmapsEmpresa.setOnClickListener {
               val uri = Uri.parse("geo:0,0?q=${tvDomicilioSocial.text}")
               val intent = Intent(Intent.ACTION_VIEW, uri)
               startActivity(intent)
           }

           btLlamarEmpresa.setOnClickListener {
               val uri = Uri.parse("tel:${tvTelefonoComercial.text}")
               val intent = Intent(Intent.ACTION_DIAL, uri)
               startActivity(intent)
           }

           btMandarCorreoEmpresa.setOnClickListener {
               val uri = Uri.parse("mailto: ${tvCorreoEmpresa.text}")
               val intent = Intent(Intent.ACTION_SENDTO, uri)
               startActivity(intent)
           }

           btVerUrl.setOnClickListener {
               val uri = Uri.parse("https://${tvWebEmpresa.text}")
               val intent = Intent(Intent.ACTION_VIEW, uri)
               startActivity(intent)
           }

            btMostrarEmpleados.setOnClickListener {
                val argumentos = Bundle()
                argumentos.putString("id", id)
                val fragment = FragmentRecyclerEmpleados()
                fragment.arguments = argumentos
                if (activity is InfoActivity) {
                    (activity as InfoActivity).mostrarFragment(fragment)
                }
            }

            btBorrar.setOnClickListener {
                mostrarDialog()
            }

            btEditar.setOnClickListener {
                val args = Bundle()
                args.putString("id", id)
                val fragment = FragmentEditarEmpresa()
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

    // Muestra en pantalla los datos de la empresa
    private fun mostrarDatos() {
        val docRef = colRefEmpresa.document(id ?: "")
        docRef.get().apply{
            addOnSuccessListener {
                if (!isAdded) {
                    return@addOnSuccessListener
                }

                with(binding) {
                    val logo = it["logo"].toString()
                    if (logo.isNotEmpty()) {
                        Glide.with(requireContext()).load(Uri.parse(logo)).into(ivLogo)
                    }
                    tvSector.text = it["sector"].toString()
                    tvCif.text = it["cif"].toString()
                    tvRazonSocial.text = it["nombre"].toString()
                    tvDomicilioSocial.text = it["domicilio"].toString()
                    tvTelefonoComercial.text = it["telefono"].toString()
                    tvCorreoEmpresa.text = it["correo"].toString()
                    tvWebEmpresa.text = it["web"].toString()
                }
            }
        }
    }

    // Pide confirmación al usuario antes de eliminar una empresa
    private fun mostrarDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.apply {
            setMessage("¿Desea eliminar la empresa?\nAtención: Tenga en cuenta que " +
                       "también se eliminarán sus empleados.")
            setPositiveButton(android.R.string.ok) {_, _ ->
                val docRef = colRefEmpresa.document(id!!)
                docRef.delete()
                    .addOnSuccessListener {
                        // Elimina los empleados asociados a la empresa
                        val empresa = binding.tvRazonSocial.text.toString()
                        eliminarEmpleados(empresa)

                        // Informa al usuario
                        Object.mensajeToast(requireContext(), R.string.empresa_eliminada)

                        // Vuelve al fragment anterior
                        val fragmentManager = requireActivity().supportFragmentManager
                        fragmentManager.popBackStack()
                    }
                    .addOnFailureListener {
                        // Informa al usuario
                        Object.mensajeToast(requireContext(), R.string.error_borrar_empresa)
                    }
            }
            setNegativeButton(android.R.string.cancel) {_, _ ->
                return@setNegativeButton
            }
        }.show()
    }

    // Elimina los empleados asociados a una empresa
    private fun eliminarEmpleados(empresa: String) {
        colRefEmpleados.whereEqualTo("empresa", empresa)
            .addSnapshotListener { query, exception ->
                if (exception != null) {
                    return@addSnapshotListener
                }

                for (document in query!!) {
                    val docRef = colRefEmpleados.document(document.id)
                    docRef.delete().apply {
                        addOnFailureListener {
                            return@addOnFailureListener
                        }
                    }
                }
            }
    }
}