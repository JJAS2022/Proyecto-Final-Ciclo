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
import com.jjas.proyectofinalciclo.databinding.FragmentAnyadirEmpleadoBinding
import java.util.Calendar

class FragmentAnyadirEmpleado: Fragment() {
    private lateinit var binding: FragmentAnyadirEmpleadoBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var colRefEmpresas: CollectionReference
    private lateinit var colRefEmpleados: CollectionReference
    private var fecha: Calendar? = null
    private var id: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Instancia las referencias a las bases de datos de Empresas y Empleados
        db = FirebaseFirestore.getInstance()
        colRefEmpresas = db.collection("Empresas")
        colRefEmpleados = db.collection("Empleados")

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
        binding = FragmentAnyadirEmpleadoBinding.inflate(inflater)

        with(binding) {
            // Funcionalidad del DatePicker
            if (fecha != null) {
                val textoFecha =
                    "${fecha!!.get(Calendar.DAY_OF_MONTH)}/${fecha!!.get(Calendar.MONTH)+1}/${fecha!!.get(Calendar.YEAR)}"
                binding.tvFecha.text = textoFecha
            }

            tvFecha.setOnClickListener {
                seleccionarFecha()
            }

            // Funcionalidad del Spinner de empresas
            configurarSpinner()

            // Funcionalidad del botón Registrar
            btRegistrarEmpleado.setOnClickListener {
                // Oculta el teclado
                Object.ocultarTeclado(requireContext(), it)

                // Almacena los valores de los campos
                val dni = etDni.text.toString().uppercase()
                val puesto = etPuesto.text.toString()
                val nombre = etNombreEmpleado.text.toString()
                val direccion = etDireccionEmpleado.text.toString()
                val telefono = etTelefonoEmpleado.text.toString()
                val correo = etCorreoEmpleado.text.toString()
                val fechaNacimiento = if (fecha != null) {
                    tvFecha.text.toString()
                } else {
                    ""
                }
                val empresa = spEmpresas.selectedItem.toString()

                if (dni.isNotEmpty() && puesto.isNotEmpty() && nombre.isNotEmpty() &&
                    direccion.isNotEmpty() && telefono.isNotEmpty() && correo.isNotEmpty() &&
                    fechaNacimiento.isNotEmpty()) {
                    if (Object.validarDni(dni)) {
                        Object.dniUnico(dni) {esUnico ->
                            if (esUnico) {
                                if (Object.validarTelefono(telefono)) {
                                    if (Object.validarEmail(correo)) {
                                        if (Object.validarFecha(fechaNacimiento)) {
                                            anyadirEmpleado(
                                                dni, puesto, nombre, direccion, telefono, correo,
                                                fechaNacimiento, empresa
                                            )
                                        } else {
                                            Object.mensajeToast(requireContext(), R.string.fecha_inválida)
                                        }
                                    } else {
                                        Object.mensajeToast(requireContext(), R.string.email_formato_invalido)
                                        etCorreoEmpleado.error = getString(R.string.incorrecto)
                                    }
                                } else {
                                    Object.mensajeToast(requireContext(), R.string.telefono_formato_invalido)
                                    etTelefonoEmpleado.error = getString(R.string.incorrecto)
                                }
                            } else {
                                Object.mensajeToast(requireContext(), R.string.dni_repetido)
                                etDni.error = getString(R.string.incorrecto)
                            }
                        }
                    } else {
                        Object.mensajeToast(requireContext(), R.string.dni_formato_invalido)
                        etDni.error = getString(R.string.incorrecto)
                    }
                } else {
                    Object.mensajeToast(requireContext(), R.string.datos_vacios)
                    if (dni.isEmpty()) {
                        etDni.error = getString(R.string.obligatorio)
                    }
                    if (puesto.isEmpty()) {
                        etPuesto.error = getString(R.string.obligatorio)
                    }
                    if (nombre.isEmpty()) {
                        etNombreEmpleado.error = getString(R.string.obligatorio)
                    }
                    if (direccion.isEmpty()) {
                        etDireccionEmpleado.error = getString(R.string.obligatorio)
                    }
                    if (telefono.isEmpty()) {
                        etTelefonoEmpleado.error = getString(R.string.obligatorio)
                    }
                    if (correo.isEmpty()) {
                        etCorreoEmpleado.error = getString(R.string.obligatorio)
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
    private fun anyadirEmpleado(
        dni: String, puesto: String, nombre: String, direccion: String, telefono: String,
        correo: String, fecha: String, empresa: String
    ) {
        val empleado = hashMapOf(
            "dni" to dni,
            "puesto" to puesto,
            "nombre" to nombre,
            "direccion" to direccion,
            "telefono" to telefono,
            "correo" to correo,
            "fecha" to fecha,
            "empresa" to empresa
        )

        colRefEmpleados.add(empleado)
            .addOnSuccessListener {
                val argumentos = Bundle()
                argumentos.putString("id", it.id)
                argumentos.putBoolean("nuevo", true)
                val fragment = FragmentVerEmpleado()
                fragment.arguments = argumentos

                if (activity is InfoActivity) {
                    (activity as InfoActivity).mostrarFragment(fragment)
                }
            }
            .addOnFailureListener {
                Object.mensajeToast(requireContext(), R.string.error_anyadir_empleado)
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
            binding.spEmpresas.adapter = adaptador

            // Coloca por defecto el nombre de la empresa que lanza el fragment
            if (empresas.isNotEmpty() && id != null) {
                var nombre= ""
                val docRef = colRefEmpresas.document(id!!)
                docRef.get().addOnSuccessListener {
                    nombre = it["nombre"].toString()
                    if (nombre.isNotEmpty()) {
                        val posicion = empresas.indexOf(nombre)
                        binding.spEmpresas.setSelection(posicion)
                    }
                }
            }

            adaptador.notifyDataSetChanged()
        }
    }

    // Permite seleccionar la fecha del DatePicker
    private fun seleccionarFecha() {
        val cal = fecha ?: Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener{ _, miAnyo, miMes, miDia ->
            cal.set(Calendar.YEAR, miAnyo)
            cal.set(Calendar.MONTH, miMes)
            cal.set(Calendar.DAY_OF_MONTH, miDia)

            fecha = cal

            val textoFecha =
                "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH)+1}/${cal.get(Calendar.YEAR)}"
            binding.tvFecha.text = textoFecha
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