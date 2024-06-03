package com.jjas.proyectofinalciclo

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AnyadirUsuarioTest {
    private lateinit var anyadirUsuario: AnyadirUsuario
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var colRef: CollectionReference

    // Condiguración inicial de inyección de dependencias
    @Before
    fun setUp() {
        // Mockeo de servicios de Firebase
        auth = mock(FirebaseAuth::class.java)
        db = mock(FirebaseFirestore::class.java)
        colRef = mock(CollectionReference::class.java)

        // Instanciación de la actividad a probar
        anyadirUsuario = AnyadirUsuario()

        // Inyección de los servicios mockeados en la actividad
        anyadirUsuario.auth = auth
        anyadirUsuario.db = db
        anyadirUsuario.colRef = colRef
    }

    // Prueba que comprueba el método con datos válidos
    @Test
    fun crearCuenta_usuarioExitoso() {
        // Arrange
        val task = mock(Task::class.java)
        `when`(task.isSuccessful).thenReturn(true)
        `when`(auth.createUserWithEmailAndPassword(anyString(), anyString()))
            .thenReturn(task as Task<AuthResult>?)

        // Act
        anyadirUsuario.crearCuenta("Nombre", "Apellido", "correo@ejemplo.com", "contraseña")

        // Assert
        verify(auth).createUserWithEmailAndPassword("correo@ejemplo.com", "contraseña")
        verify(task).isSuccessful
    }

    // Prueba que comprueba el método con datos de usuario repetidos
    @Test
    fun crearCuenta_usuarioRepetido() {
        // Arrange
        val task = mock(Task::class.java)
        `when`(task.isSuccessful).thenReturn(false)
        `when`(task.exception).thenReturn(FirebaseAuthUserCollisionException("errorCode", "Mensaje de error"))
        `when`(auth.createUserWithEmailAndPassword(anyString(), anyString()))
            .thenReturn(task as Task<AuthResult>?)

        // Act
        anyadirUsuario.crearCuenta("Nombre", "Apellido", "correo@ejemplo.com", "contraseña")

        // Assert
        verify(auth).createUserWithEmailAndPassword("correo@ejemplo.com", "contraseña")
        assert(task.exception is FirebaseAuthUserCollisionException)
    }

    // Prueba que comprueba el método con una contraseña débil
    @Test
    fun crearCuenta_contrasenaDebil() {
        // Arrange
        val task = mock(Task::class.java)
        `when`(task.isSuccessful).thenReturn(false)
        `when`(task.exception).thenReturn(FirebaseAuthWeakPasswordException("errorCode", "Mensaje de error", "contraseña"))
        `when`(auth.createUserWithEmailAndPassword(anyString(), anyString()))
            .thenReturn(task as Task<AuthResult>?)

        // Act
        anyadirUsuario.crearCuenta("Nombre", "Apellido", "correo@ejemplo.com", "contraseña")

        // Assert
        verify(auth).createUserWithEmailAndPassword("correo@ejemplo.com", "contraseña")
        assert(task.exception is FirebaseAuthWeakPasswordException)
    }
}