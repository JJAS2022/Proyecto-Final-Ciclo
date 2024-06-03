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
import com.jjas.proyectofinalciclo.databinding.FragmentRecyclerEmpresasBinding

class FragmentRecyclerEmpresas: Fragment(), OnItemClickListener {
    private lateinit var binding: FragmentRecyclerEmpresasBinding
    private lateinit var adapter: EmpresaAdapter
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
        binding = FragmentRecyclerEmpresasBinding.inflate(inflater)

        // Configura el Recycler para mostrar las empresas
        montarRecyclerView()

        // Funcionalidad del FAB para añadir empresas
        binding.btAnyadirEmpresaRecycler.setOnClickListener {
            if (activity is InfoActivity) {
                (activity as InfoActivity).mostrarFragment(FragmentAnyadirEmpresa())
            }
        }

        // Funcionalidad del FAB para buscar empresas
        binding.btBuscarEmpresa.setOnClickListener {
            mostrarDialog()
        }

        return binding.root
    }

    // Configura el RecyclerView
    private fun montarRecyclerView() {
        val empresas: MutableList<Empresa> = arrayListOf()

        // Configura el adapter
        binding.rvElementos.setHasFixedSize(true)
        binding.rvElementos.layoutManager = LinearLayoutManager(requireContext())
        adapter = EmpresaAdapter(empresas, requireContext(), this)
        binding.rvElementos.adapter = adapter

        colRef.addSnapshotListener { query, exception ->
            if (exception != null) {
                return@addSnapshotListener
            }

            // Limpia los datos del array
            empresas.clear()

            for (document in query!!) {
                val empresa = Empresa(
                    document.id,
                    document["nombre"].toString(),
                    document["cif"].toString(),
                    document["sector"].toString(),
                    document["domicilio"].toString(),
                    document["telefono"].toString(),
                    document["correo"].toString(),
                    document["web"].toString(),
                    document["logo"].toString()
                )
                empresas.add(empresa)
            }

            // Ordena las empresas por nombre y actualiza el adapter
            empresas.sortBy {
                it.nombre
            }
            adapter.notifyDataSetChanged()
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
                    buscarEmpresa(nombre)
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
    private fun buscarEmpresa(nombre: String) {
        val empresas: MutableList<Empresa> = arrayListOf()

        // Reconfigura el adapter
        binding.rvElementos.setHasFixedSize(true)
        binding.rvElementos.layoutManager = LinearLayoutManager(requireContext())
        adapter = EmpresaAdapter(empresas, requireContext(), this)
        binding.rvElementos.adapter = adapter

        colRef.whereGreaterThanOrEqualTo("nombre", nombre)
            .whereLessThanOrEqualTo("nombre", nombre + "\uf8ff")
            .addSnapshotListener { query, exception ->
            if (exception != null) {
                return@addSnapshotListener
            }

            // Limpia los datos del array
            empresas.clear()

            for (document in query!!) {
                val empresa = Empresa(
                    document.id,
                    document["nombre"].toString(),
                    document["cif"].toString(),
                    document["sector"].toString(),
                    document["domicilio"].toString(),
                    document["telefono"].toString(),
                    document["email"].toString(),
                    document["web"].toString(),
                    document["logo"].toString()
                )
                empresas.add(empresa)
            }

           if(empresas.isEmpty()) {
               montarRecyclerView()
               Object.mensajeToast(requireContext(), R.string.sin_coincidencias)
           } else {
               // Ordena las empresas por nombre y actualiza el adapter
               empresas.sortBy {
                   it.nombre
               }
               adapter.notifyDataSetChanged()
           }
        }
    }
}