package com.example.mhnfe.ui.screens.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

object AuthStateManager {
    private val _isLoggedOut = MutableLiveData<Boolean>()
    val isLoggedOut: LiveData<Boolean> get() = _isLoggedOut

    fun logout() {
        if (_isLoggedOut.value != true) {
            _isLoggedOut.postValue(true)
        }
    }
}


//class AuthStateViewModel : ViewModel() {
//    private val _isLoggedOut = MutableLiveData<Boolean>()
//    val isLoggedOut: LiveData<Boolean> get() = _isLoggedOut
//
//    fun logout() {
//        _isLoggedOut.value = true
//    }
//}