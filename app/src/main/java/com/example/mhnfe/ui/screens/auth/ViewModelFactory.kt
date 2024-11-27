package com.example.mhnfe.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mhnfe.data.repository.AuthRepository
import com.example.mhnfe.ui.screens.auth.login.LoginViewModel
import com.example.mhnfe.ui.screens.auth.signup.SignUpViewModel

//class SignUpViewModelFactory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
//            return SignUpViewModel(authRepository) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}
//
//class LoginViewModelFactory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
//            return LoginViewModel(authRepository) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}