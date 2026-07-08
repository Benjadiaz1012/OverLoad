package com.pdm0126.overload.ui.screens.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SignInUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSignedIn: Boolean = false
)

class SignInViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun signInWithGoogleIdToken(idToken: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                _uiState.update { it.copy(isLoading = false, isSignedIn = true) }
            }
            .addOnFailureListener {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo iniciar sesión con Google"
                    )
                }
            }
    }

    fun onGoogleSignInCancelled() {
        _uiState.update { it.copy(isLoading = false) }
    }

    fun onGoogleSignInFailed() {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = "No se pudo iniciar sesión con Google"
            )
        }
    }

    // Se llama justo después de navegar en el LaunchedEffect de SignIn, para que
    // isSignedIn no quede "pegado" en true. Esta misma instancia de ViewModel sobrevive
    // entre pantallas (no hay ViewModelStore por entry), así que sin este reset, un
    // logout posterior volvería a disparar onNext() solo, sin acción del usuario.
    fun resetSignedInState() {
        _uiState.update { it.copy(isSignedIn = false) }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun friendlyError(e: Exception): String {
        val message = e.message ?: return "Error al iniciar sesión. Intenta de nuevo."
        return when {
            message.contains("password is invalid", ignoreCase = true) -> "Contraseña incorrecta"
            message.contains(
                "no user record",
                ignoreCase = true
            ) -> "No existe una cuenta con ese correo"

            message.contains(
                "already in use",
                ignoreCase = true
            ) -> "Ya existe una cuenta con ese correo"

            message.contains("badly formatted", ignoreCase = true) -> "Correo inválido"
            message.contains("network", ignoreCase = true) -> "Sin conexión a internet"
            else -> "Error al iniciar sesión. Intenta de nuevo."
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { SignInViewModel() }
        }
    }
}