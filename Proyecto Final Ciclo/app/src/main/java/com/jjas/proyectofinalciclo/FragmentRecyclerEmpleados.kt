package com.jjas.proyectofinalciclo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.jjas.proyectofinalciclo.databinding.DialogBuscarBinding
import com.jjas.proyectofinalciclo.databinding.FragmentRecyclerEmpleadosBinding

class FragmentRecyclerEmpleados : Fragment(), OnItemClickListener {
    private lateinit var binding: FragmentRecyclerEmpleadosBinding
    private lateinit var adapter: EmpleadoAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var colRefEmpresas: CollectionReference
    private lateinit var colRefEmpleados: CollectionReference
    private var id: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Instancia la referencia a la base de datos de Empresas
        db = FirebaseFirestore.getInstance()
        colRefEmpleados = db.collection("Empleados")
        colRefEmpresas = db.collection("Empresas")

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
        binding = FragmentRecyclerEmpleadosBinding.inflate(inflater)

        // Configura el Recycler para mostrar las empresas
        montarRecyclerView()

        // Funcionalidad del FAB para añadir empleados
        binding.btAnyadirEmpleadoRecycler.setOnClickListener {
            val argumentos = Bundle()
            argumentos.putString("id", id)
            val fragment = FragmentAnyadirEmpleado()
            fragment.arguments = argumentos
            if (activity is InfoActivity) {
                (activity as InfoActivity).mostrarFragment(fragment)
            }
        }

        // Funcionalidad del FAB para buscar empleados
        binding.btBuscarEmpleado.setOnClickListener {
            mostrarDialog()
        }

        return binding.root
    }

    // Configura el RecyclerView
    private fun montarRecyclerView() {
        val empleados: MutableList<Empleado> = arrayListOf()

        // Configura el adapter
        binding.rvElementosEmpleados.setHasFixedSize(true)
        binding.rvElementosEmpleados.layoutManager = LinearLayoutManager(requireContext())
        adapter = EmpleadoAdapter(empleados, requireContext(), this)
        binding.rvElementosEmpleados.adapter = adapter

        if (id == null) {
            colRefEmpleados.addSnapshotListener { query, exception ->
                if (exception != null) {
                    return@addSnapshotListener
                }

                // Limpia los datos del array
                empleados.clear()

                for (document in query!!) {
                    val empleado = Empleado(
                        document.id,
                        document["nombre"].toString(),
                        document["dni"].toString(),
                        document["puesto"].toString(),
                        document["direccion"].toString(),
                        document["telefono"].toString(),
                        document["correo"].toString(),
                        document["empresa"].toString(),
                        document["fecha"].toString()
                    )
                    empleados.add(empleado)
                }

                // Ordena los empleados por nombre y actualiza el adapter
                empleados.sortBy {
                    it.nombre
                }
                adapter.notifyDataSetChanged()
            }
        } else {
            val docRefEmpresa = colRefEmpresas.document(id!!)
            var nombre: String

            docRefEmpresa.get().addOnSuccessListener {docSnapshot ->
                nombre = docSnapshot["nombre"].toString()
                colRefEmpleados.whereEqualTo("empresa", nombre)
                    .addSnapshotListener { query, exception ->
                        if (exception != null) {
                            return@addSnapshotListener
                        }

                        // Limpia los datos del array
                        empleados.clear()

                        for (document in query!!) {
                            val empleado = Empleado(
                                document.id,
                                document["nombre"].toString(),
                                document["dni"].toString(),
                                document["puesto"].toString(),
                                document["direccion"].toString(),
                                document["telefono"].toString(),
                                document["correo"].toString(),
                                document["empresa"].toString(),
                                document["fecha"].toString()
                            )
                            empleados.add(empleado)
                        }

                        if(empleados.isEmpty()) {
                            Object.mensajeToast(requireContext(), R.string.no_hay_empleados)
                        }

                        // Ordena los empleados por nombre y actualiza el adapter
                        empleados.sortBy {
                            it.nombre
                        }
                        adapter.notifyDataSetChanged()
                    }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activity is InfoActivity) {
            (activity as InfoActivity).cerrarFragment()
        }
    }

    override fun onItemClick(fragment: Fragment) {
        if (activity is InfoActivity) {
            (activity as InfoActivity).mostrarFragment(fragment)
        }
    }

    // Muestra un dialog para introducir el nombre a buscar
    private fun mostrarDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.apply {
            val bindingDialog: DialogBuscarBinding = DialogBuscarBinding.inflate(layoutInflater)
            setView(bindingDialog.root)
            setPositiveButton(R.string.buscar) {_, _ ->
                val nombre = bindingDialog.etBuscar.text.toString()
                if (nombre.isNotEmpty()) {
                    buscarEmpleado(nombre)
                } else {
                    Object.mensajeToast(requireContext(), R.string.campo_vacio)
                }
            }
            setNegativeButton(android.R.string.cancel) {_, _ ->
                return@setNegativeButton
            }
            setNeutralButton(R.string.restablecer) { _, _ ->
                montarRecyclerView()
            }
        }.show()
    }

    // Actualiza el Recycler View con las coincidencias de la búsqueda por nombre
    private fun buscarEmpleado(nombre: String) {
        val empleados: MutableList<Empleado> = arrayListOf()

        // Reconfigura el adapter
        binding.rvElementosEmpleados.setHasFixedSize(true)
        binding.rvElementosEmpleados.layoutManager = LinearLayoutManager(requireContext())
        adapter = EmpleadoAdapter(empleados, requireContext(), this)
        binding.rvElementosEmpleados.adapter = adapter

        colRefEmpleados.whereGreaterThanOrEqualTo("nombre", nombre)
            .whereLessThanOrEqualTo("nombre", nombre + "\uf8ff")
            .addSnapshotListener { query, exception ->
                if (exception != null) {
                    return@addSnapshotListener
                }

                // Limpia los datos del array
                empleados.clear()

                for (document in query!!) {
                    val empleado = Empleado(
                        document.id,
                        document["nombre"].toString(),
                        document["dni"].toString(),
                        document["puesto"].toString(),
                        document["direccion"].toString(),
                        document["telefono"].toString(),
                        document["correo"].toString(),
                        document["empresa"].toString(),
                        document["fecha"].toString()
                    )
                    empleados.add(empleado)
                }

                if(empleados.isEmpty()) {
                    montarRecyclerView()
                    Object.mensajeToast(requireContext(), R.string.sin_coincidencias)
                } else {
                    // Ordena las empresas por nombre y actualiza el adapter
                    empleados.sortBy {
                        it.nombre
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }
}