package com.capstone.aiyam.presentation.auth.signin

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.capstone.aiyam.domain.model.AuthenticationResponse
import com.capstone.aiyam.domain.repository.AuthenticationRepository
import com.capstone.aiyam.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.PrivateKey
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    fun signInEmail(email: String, password: String) = authenticationRepository.loginWithEmail(email, password)

    fun signInGoogle(): Flow<AuthenticationResponse> {
        return authenticationRepository.signInWithGoogle()
    }

    fun googleIntent() = authenticationRepository.signInWithGoogleIntent()

    fun fallbackSignIn(intent: Intent): Flow<AuthenticationResponse> {
        return authenticationRepository.signInWithIntentGoogle(intent)
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    fun createTargetAlerts() { scope.launch {
        userRepository.createTargetAlerts().collect{}
    }}
}
