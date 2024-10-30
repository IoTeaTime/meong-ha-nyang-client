package com.example.mhnfe.ui.screens.qr

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class QRScanningViewModel(application: Application) : AndroidViewModel(application) {
    private val _scanResult = MutableLiveData<String?>()
    val scanResult: LiveData<String?> get() = _scanResult

    fun setScanResult(result: String) {
        viewModelScope.launch {
            _scanResult.value = result
        }
    }
}
