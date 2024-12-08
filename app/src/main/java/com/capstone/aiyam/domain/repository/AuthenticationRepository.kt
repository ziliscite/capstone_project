package com.capstone.aiyam.domain.repository

import com.capstone.aiyam.domain.model.AuthenticationResponse
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.Flow

interface AuthenticationRepository {
    fun createAccountWithEmail(username: String, email: String, password: String): Flow<AuthenticationResponse>
    fun loginWithEmail(email: String, password: String): Flow<AuthenticationResponse>
    fun signInWithGoogle(): Flow<AuthenticationResponse>
}
