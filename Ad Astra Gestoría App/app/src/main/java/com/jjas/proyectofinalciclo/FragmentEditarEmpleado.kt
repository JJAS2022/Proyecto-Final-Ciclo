package com.jjas.proyectofinalciclo

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.jjas.proyectofinalciclo.databinding.FragmentEditarEmpleadoBinding
import java.util.Calendar

class FragmentEditarEmpleado: Fragment() {
    private lateinit var binding: FragmentEditarEmpleadoBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var colRefEmpresas: CollectionReference
    private lateinit var colRefEmpleados: CollectionReference
    private var empleadoId: String? = null
    private lateinit var nombre: String
    private lateinit var dni: String
    private lateinit var puesto: String
    private lateinit var domicilio: String
    private lateinit var telefono: String
    private lateinit var correo: String
    private lateinit var empresa: String
    private lateinit var fecha: String
    private var fechaSeleccionada: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Instancia las referencias a las bases de datos de Empresas y Empleados
        db = FirebaseFirestore.getInstance()
        colRefEmpresas = db.collection("Empresas")
        colRefEmpleados = db.collection("Empleados")

        // Recupera los argumentos enviados por el fragment que lo llama
        val argumentos = arguments
        if (argumentos != null) {
            empleadoId = argumentos.getString("id")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditarEmpleadoBinding.inflate(inflater)

        // Recupera los datos de la empresa a editar
        mostrarDatos()

        // Funcionalidad del DatePicker
        binding.tvFechaEditar.setOnClickListener {
            seleccionarFecha()
        }

        // Funcionalidad del botón Actualizar
        binding.btEditarEmpleado.setOnClickListener {
            val nuevoNombre = binding.etNombreEmpleadoEditar.text.toString()
            val nuevoDni = binding.etDniEditar.text.toString()
            val nuevoPuesto = binding.etPuestoEditar.text.toString()
            val nuevaDireccion = binding.etDireccionEditar.text.toString()
            val nuevoTelefono = binding.etTelefonoEditar.text.toString()
            val nuevoCorreo = binding.etCorreoEmpleadoEditar.text.toString()
            val nuevaEmpresa = binding.spEmpresasEditar.selectedItem.toString()
            val nuevaFecha = binding.tvFechaEditar.text.toString()

            if (nuevoNombre.isNotEmpty() && nuevoDni.isNotEmpty() && nuevoPuesto.isNotEmpty() &&
                nuevaDireccion.isNotEmpty() && nuevoTelefono.isNotEmpty() &&
                nuevoCorreo.isNotEmpty() && nuevaFecha.isNotEmpty()) {
                if (nuevoNombre != nombre || nuevoDni != dni || nuevoPuesto != puesto ||
                    nuevaDireccion != domicilio || nuevoTelefono != telefono || nuevoCorreo != correo
                    || nuevaEmpresa != empresa || nuevaFecha != fecha) {
                    if (Object.validarDni(nuevoDni)) {
                        Object.cifUnico(nuevoDni) {esUnico ->
                            if (esUnico || dni == nuevoDni) {
                                if (Object.validarTelefono(nuevoTelefono)) {
                                    if (Object.validarEmail(nuevoCorreo)) {
                                        if (Object.validarFecha(nuevaFecha)) {
                                            actualizarDatos(
                                                nuevoNombre, nuevoDni, nuevoPuesto, nuevaDireccion,
                                                nuevoTelefono, nuevoCorreo, nuevaEmpresa, nuevaFecha
                                            )
                                        } else {
                                            Object.mensajeToast(requireContext(),
                                                R.string.fecha_inválida)
                                        }
                                    } else {
                                        Object.mensajeToast(requireContext(),
                                            R.string.email_formato_invalido
                                        )
                                        binding.etCorreoEmpleadoEditar.error =
                                            getString(R.string.incorrecto)
                                    }
                                }
                                else {
                                    Object.mensajeToast(requireContext(), R.string.telefono_formato_invalido)
                                    binding.etTelefonoEditar.error = getString(R.string.incorrecto)
                                }
                            } else {
                                Object.mensajeToast(requireContext(), R.string.dni_repetido)
                                binding.etDniEditar.error = getString(R.string.incorrecto)
                            }
                        }
                    } else {
                        Object.mensajeToast(requireContext(), R.string.dni_formato_invalido)
                        binding.etDniEditar.error = getString(R.string.incorrecto)
                    }
                } else {
                    Object.mensajeToast(requireContext(), R.string.datos_iguales)
                }
            } else {
                Object.mensajeToast(requireContext(), R.string.datos_vacios)
                if (nuevoNombre.isEmpty()) {
                    binding.etNombreEmpleadoEditar.error = getString(R.string.obligatorio)
                }
                if (nuevoDni.isEmpty()) {
                    binding.etDniEditar.error = getString(R.string.obligatorio)
                }
                if (nuevoPuesto.isEmpty()) {
                    binding.etPuestoEditar.error = getString(R.string.obligatorio)
                }
                if (nuevaDireccion.isEmpty()) {
                    binding.etDireccionEditar.error = getString(R.string.obligatorio)
                }
                if (nuevoTelefono.isEmpty()) {
                    binding.etTelefonoEditar.error = getString(R.string.obligatorio)
                }
                if (nuevoCorreo.isEmpty()) {
                    binding.etCorreoEmpleadoEditar.error = getString(R.string.obligatorio)
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

    // Muestra los datos del empleado a editar
    private fun mostrarDatos() {
        val docRef = colRefEmpleados.document(empleadoId!!)
        docRef.get().apply {
            addOnSuccessListener {
                nombre = it["nombre"].toString()
                dni = it["dni"].toString()
                puesto = it["puesto"].toString()
                domicilio = it["direccion"].toString()
                telefono = it["telefono"].toString()
                correo = it["correo"].toString()
                empresa = it["empresa"].toString()
                fecha = it["fecha"].toString()

                with(binding) {
                    etNombreEmpleadoEditar.setText(nombre)
                    etDniEditar.setText(dni)
                    etPuestoEditar.setText(puesto)
                    etDireccionEditar.setText(domicilio)
                    etTelefonoEditar.setText(telefono)
                    etCorreoEmpleadoEditar.setText(correo)
                }

                // Mostrar fecha
                if (fechaSeleccionada == null) {
                    binding.tvFechaEditar.text = fecha
                    val arrayFecha = fecha.split("/")
                    val cal =  Calendar.getInstance()
                    cal.set(
                        arrayFecha[2].toInt(), (arrayFecha[1].toInt()-1), arrayFecha[0].toInt()
                    )
                    fechaSeleccionada = cal
                } else {
                    val textoFecha =
                        "${fechaSeleccionada!!.get(Calendar.DAY_OF_MONTH)}/${fechaSeleccionada!!.get(Calendar.MONTH)+1}/${fechaSeleccionada!!.get(Calendar.YEAR)}"
                    binding.tvFechaEditar.text = textoFecha
                }

                // Funcionalidad del Spinner de empresas
                configurarSpinner()
            }
        }

    }

    // Actualiza la base de datos Firestore con los datos
    private fun actualizarDatos(
        nombre: String, dni: String, puesto: String, direccion: String, telefono: String,
        correo: String, empresa: String, fecha: String
    ) {
        val docRef = colRefEmpleados.document(empleadoId!!)

        val datos = mapOf(
            "nombre" to nombre,
            "dni" to dni,
            "puesto" to puesto,
            "direccion" to direccion,
            "telefono" to telefono,
            "correo" to correo,
            "empresa" to empresa,
            "fecha" to fecha
        )

        docRef.update(datos)
            .addOnSuccessListener {
                Object.mensajeToast(requireContext(), R.string.empleado_actualizado)

                // Vuelve al fragment anterior
                val fragmentManager = requireActivity().supportFragmentManager
                fragmentManager.popBackStack()
            }
            .addOnFailureListener {
                Object.mensajeToast(requireContext(), R.string.error_actualizar_empleado)
            }
    }

    // Configura el Spinner de empresas
    private fun configurarSpinner() {
        val empresas: MutableList<String> = arrayListOf()
        colRefEmpresas.addSnapshotListener { query, exception ->
            if (exception != null || !isAdded) {
                return@addSnapshotListener
            }

            for (document in query!!) {
                empresas.add(document["nombre"].toString())
            }

            empresas.sort()
            val adaptador = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                empresas)
            adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spEmpresasEditar.adapter = adaptador

            // Coloca por defecto el nombre de la empresa que lanza el fragment
            if (empresas.isNotEmpty() && empresa.isNotEmpty()) {
                val posicion = empresas.indexOf(empresa)
                binding.spEmpresasEditar.setSelection(posicion)
            }

            adaptador.notifyDataSetChanged()
        }
    }

    // Permite seleccionar la fecha del DatePicker
    private fun seleccionarFecha() {
        val cal = fechaSeleccionada ?: Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener{ _, miAnyo, miMes, miDia ->
            cal.set(Calendar.YEAR, miAnyo)
            cal.set(Calendar.MONTH, miMes)
            cal.set(Calendar.DAY_OF_MONTH, miDia)

            fechaSeleccionada = cal

            val textoFecha =
                "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH)+1}/${cal.get(Calendar.YEAR)}"
            binding.tvFechaEditar.text = textoFecha
        }

        DatePickerDialog(
            requireContext(),
            dateSetListener,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}