package com.jjas.proyectofinalciclo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.jjas.proyectofinalciclo.databinding.ItemRvEmpresaBinding

class EmpresaAdapter(listaEmpresas: MutableList<Empresa>, context: Context, private val listener: OnItemClickListener) :
      RecyclerView.Adapter<EmpresaAdapter.EmpresaViewHolder>() {
          private var empresas: MutableList<Empresa>
          private var contexto: Context
          init {
              empresas = listaEmpresas
              contexto = context
          }

    private var actionMode: ActionMode? = null
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var colRef: CollectionReference = db.collection("Empresas")
    private var id: String? = null

    override fun onCreateViewHolder (parent: ViewGroup, viewType: Int) : EmpresaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rv_empresa, parent, false)
        return EmpresaViewHolder(view)
    }

    override fun onBindViewHolder (holder: EmpresaViewHolder, position: Int) {
        val item = empresas[position]
        holder.bind(item, contexto, listener)
    }

    override fun getItemCount(): Int {
        return empresas.size
    }

    inner class EmpresaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ItemRvEmpresaBinding.bind(view)

        fun bind(empresa: Empresa, context: Context, listener: OnItemClickListener) {
            binding.tvItemNombreEmpresa.text = empresa.nombre
            binding.tvItemTelefonoEmpresa.text = empresa.telefono
            binding.tvItemEmailEmpresa.text = empresa.correo
            binding.tvItemUrlEmpresa.text = empresa.web
            val logo = empresa.logo
            if (logo.isNotEmpty()) {
                Glide.with(context).load(Uri.parse(logo)).into(binding.ivItemLogo)
            }

            itemView.setOnClickListener {
                val argumentos = Bundle()
                argumentos.putString("id", empresa.id)
                argumentos.putBoolean("nuevo", false)
                val fragment = FragmentVerEmpresa()
                fragment.arguments = argumentos

                listener.onItemClick(fragment)
                actionMode?.finish()
            }

            itemView.setOnLongClickListener {
                id = empresa.id
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
                binding.itemIndividual.setBackgroundColor(contexto.getColor(R.color.azul_oscuro))
                binding.tvItemNombreEmpresa.setTextColor(contexto.getColor(R.color.white))
                binding.tvItemTelefonoEmpresa.setTextColor(contexto.getColor(R.color.white))
                binding.tvItemEmailEmpresa.setTextColor(contexto.getColor(R.color.white))
                binding.tvItemUrlEmpresa.setTextColor(contexto.getColor(R.color.white))
            } else {
                binding.itemIndividual.setBackgroundColor(contexto.getColor(R.color.azul_claro))
                binding.tvItemNombreEmpresa.setTextColor(contexto.getColor(R.color.azul_oscuro))
                binding.tvItemTelefonoEmpresa.setTextColor(contexto.getColor(R.color.azul_oscuro))
                binding.tvItemEmailEmpresa.setTextColor(contexto.getColor(R.color.azul_oscuro))
                binding.tvItemUrlEmpresa.setTextColor(contexto.getColor(R.color.azul_oscuro))
            }
        }

        // Pide confirmación al usuario antes de eliminar una empresa
        private fun mostrarDialog() {
            val builder = AlertDialog.Builder(contexto)
            builder.apply {
                setMessage("¿Desea eliminar la empresa?\nAtención: Tenga en cuenta que " +
                           "también se eliminarán sus empleados.")
                setPositiveButton(android.R.string.ok) {_, _ ->
                    val docRef = colRef.document(id!!)
                    docRef.delete()
                        .addOnSuccessListener {
                            // Informa al usuario
                            Object.mensajeToast(context, R.string.empresa_eliminada)
                        }
                        .addOnFailureListener {
                            // Informa al usuario
                            Object.mensajeToast(context, R.string.error_borrar_empresa)
                        }
                }
                setNegativeButton(android.R.string.cancel) {_, _ ->
                    return@setNegativeButton
                }
            }.show()
        }

        // Lanza el fragment para editar la empresa
        private fun abrirEditor() {
            val args = Bundle()
            args.putString("id", id)
            val fragment = FragmentEditarEmpresa()
            fragment.arguments = args

            listener.onItemClick(fragment)
        }

        // Lanza un intent implícito para compartir los datos
        private fun compartir() {
            val docRef = colRef.document(id!!)
            docRef.get().apply {
                addOnSuccessListener {
                    val texto = "Nombre: ${it["nombre"]}\nCIF: ${it["cif"]}\nSector: ${it["sector"]}\n" +
                            "Domicilio: ${it["domicilio"]}\nTeléfono: ${it["telefono"]}\nCorreo: ${it["correo"]}\n" +
                            "Web: ${it["web"]}\nLogo: ${it["logo"]}"
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    intent.putExtra(Intent.EXTRA_TEXT, texto)

                    // Mostrar el selector de aplicaciones para compartir
                    contexto.startActivity(Intent.createChooser(intent, "Datos de la empresa"))
                }
            }
        }
    }
}