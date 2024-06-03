package com.jjas.proyectofinalciclo

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.jjas.proyectofinalciclo.databinding.ActivityInfoBinding

class InfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInfoBinding
    private var contadorFragments = 0
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Oculta el nombre de la aplicación en el ActionBar, cambia el color y muestra el logo
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setIcon(R.mipmap.app_icon_foreground)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(getColor(R.color.azul_oscuro)))
        window.statusBarColor = getColor(R.color.azul_oscuro)
    }

    // Muestra el fragment inicial si no hay otro fragment cargado
    override fun onStart() {
        super.onStart()
        if (currentFragment == null) {
            mostrarFragment(FragmentHome())
        }
    }

    // Muestra el fragment que se estaba mostrando antes de ocultar la aplicación
    override fun onResume() {
        super.onResume()
        if (currentFragment != null && currentFragment !is FragmentHome) {
            mostrarFragment(currentFragment!!)
            val fragmentManager = supportFragmentManager
            if(fragmentManager.backStackEntryCount > 0) {
                fragmentManager.popBackStack()
                cerrarFragment()
            } else {
                finish()
            }
        }
    }

    // Infla el menú principal
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflate = menuInflater
        inflate.inflate(R.menu.main_menu, menu)
        return true
    }

    // Funcionalidad de botones del menú principal
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home -> {
                mostrarFragment(FragmentHome())
                true
            }
            R.id.perfil -> {
                mostrarFragment(FragmentPerfil())
                true
            }
            R.id.cerrar_sesion -> {
                mostrarDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Muestra el fragment que recibe por parámetro y añade 1 al contador
    fun mostrarFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_holder, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
        currentFragment = fragment
        contadorFragments++
    }

    // Comprueba que estén todos los fragments cerrados y cierra la actividad
    fun cerrarFragment() {
        contadorFragments--
        if (contadorFragments == 0) {
            finish()
        }
    }

    // Pide confirmación al usuario antes de cerrar sesión
    private fun mostrarDialog() {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage("¿Desea cerrar la sesión del usuario actual?")
            setPositiveButton(android.R.string.ok) {_, _ ->
                Firebase.auth.signOut()
                val intent = Intent(context, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            setNegativeButton(android.R.string.cancel) {_, _ ->
                return@setNegativeButton
            }
        }.show()
    }
}