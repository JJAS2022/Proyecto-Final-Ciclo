package com.jjas.proyectofinalciclo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.jjas.proyectofinalciclo.databinding.ItemRvEmpleadoBinding

class EmpleadoAdapter (listaEmpleados: MutableList<Empleado>, context: Context, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<EmpleadoAdapter.EmpleadoViewHolder>() {
    private var empleados: MutableList<Empleado>
    private var contexto: Context
    init {
        empleados = listaEmpleados
        contexto = context
    }

    private var actionMode: ActionMode? = null
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var colRef: CollectionReference = db.collection("Empleados")
    private var id: String? = null

    override fun onCreateViewHolder (parent: ViewGroup, viewType: Int) : EmpleadoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rv_empleado, parent, false)
        return EmpleadoViewHolder(view)
    }

    override fun onBindViewHolder (holder: EmpleadoViewHolder, position: Int) {
        val item = empleados[position]
        holder.bind(item, listener)
    }

    override fun getItemCount(): Int {
        return empleados.size
    }

    inner class EmpleadoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ItemRvEmpleadoBinding.bind(view)

        fun bind(empleado: Empleado, listener: OnItemClickListener) {
            binding.tvItemNombreEmpleado.text = empleado.nombre
            binding.tvItemTelefonoEmpleado.text = empleado.telefono
            binding.tvItemEmailEmpleado.text = empleado.correo
            binding.tvItemEmpresaEmpleado.text = empleado.empresa
            binding.tvItemPuestoEmpleado.text = empleado.puesto

            itemView.setOnClickListener {
                val argumentos = Bundle()
                argumentos.putString("id", empleado.id)
                argumentos.putBoolean("nuevo", false)
                val fragment = FragmentVerEmpleado()
                fragment.arguments = argumentos

                listener.onItemClick(fragment)
                actionMode?.finish()
            }

            itemView.setOnLongClickListener {
                id = empleado.id
                when(actionMode) {
                    null -> {
                        actionMode = it.startActionMode(actionModeCallback)
                        it.isSelected = true
                        cambiarColor(true)
                        true
                    }
                    else -> false
                }

                return@setOnLongClickListener true
            }
        }

        // Configura el ActionMode
        private val actionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                val inflater = mode.menuInflater
                inflater.inflate(R.menu.action_menu, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.menu_borrar -> {
                        mostrarDialog()
                        mode.finish()
                        return true
                    }
                    R.id.menu_editar -> {
                        abrirEditor()
                        mode.finish()
                        return true
                    }
                    R.id.menu_compartir-> {
                        compartir()
                        mode.finish()
                        return true
                    }
                    else -> false
                }
            }

            override fun onDestroyActionMode(mode: ActionMode) {
                actionMode = null
                cambiarColor(false)
            }
        }

        // Cambia el color del elemento seleccionado
        private fun cambiarColor(seleccionado: Boolean) {
            if (seleccionado) {
                binding.itemIndividualEmpleado.setBackgroundColor(contexto.getColor(R.color.azul_oscuro))
                binding.tvItemNombreEmpleado.setTextColor(contexto.getColor(R.color.white))
                binding.tvItemTelefonoEmpleado.setTextColor(contexto.getColor(R.color.white))
                binding.tvItemEmailEmpleado.setTextColor(contexto.getColor(R.color.white))
                binding.tvItemEmpresaEmpleado.setTextColor(contexto.getColor(R.color.white))
                binding.tvItemPuestoEmpleado.setTextColor(contexto.getColor(R.color.white))
            } else {
                binding.itemIndividualEmpleado.setBackgroundColor(contexto.getColor(R.color.azul_claro))
                binding.tvItemNombreEmpleado.setTextColor(contexto.getColor(R.color.azul_oscuro))
                binding.tvItemTelefonoEmpleado.setTextColor(contexto.getColor(R.color.azul_oscuro))
                binding.tvItemEmailEmpleado.setTextColor(contexto.getColor(R.color.azul_oscuro))
                binding.tvItemEmpresaEmpleado.setTextColor(contexto.getColor(R.color.azul_oscuro))
                binding.tvItemPuestoEmpleado.setTextColor(contexto.getColor(R.color.azul_oscuro))
            }
        }

        // Pide confirmación al usuario antes de eliminar un empleado
        private fun mostrarDialog() {
            val builder = AlertDialog.Builder(contexto)
            builder.apply {
                setMessage("¿Desea eliminar el empleado de la base de datos?")
                setPositiveButton(android.R.string.ok) {_, _ ->
                    val docRef = colRef.document(id!!)
                    docRef.delete()
                        .addOnSuccessListener {
                            // Informa al usuario
                            Object.mensajeToast(context, R.string.empleado_eliminado)
                        }
                        .addOnFailureListener {
                            // Informa al usuario
                            Object.mensajeToast(context, R.string.error_borrar_empleado)
                        }
                }
                setNegativeButton(android.R.string.cancel) {_, _ ->
                    return@setNegativeButton
                }
            }.show()
        }

        // Lanza el fragment para editar el empleado
        private fun abrirEditor() {
            val args = Bundle()
            args.putString("id", id)
            val fragment = FragmentEditarEmpleado()
            fragment.arguments = args

            listener.onItemClick(fragment)
        }

        // Lanza un intent implícito para compartir los datos
        private fun compartir() {
            val docRef = colRef.document(id!!)
            docRef.get().apply {
                addOnSuccessListener {
                    val texto = "Nombre: ${it["nombre"]}\nDNI: ${it["dni"]}\nPuesto: ${it["puesto"]}\n" +
                            "Dirección: ${it["direccion"]}\nTeléfono: ${it["telefono"]}\nCorreo: ${it["correo"]}\n" +
                            "Empresa: ${it["empresa"]}\nFecha de nacimiento: ${it["fecha"]}"
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    intent.putExtra(Intent.EXTRA_TEXT, texto)

                    // Mostrar el selector de aplicaciones para compartir
                    contexto.startActivity(Intent.createChooser(intent, "Datos del empleado"))
                }
            }
        }
    }
}