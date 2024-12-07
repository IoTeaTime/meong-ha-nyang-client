package com.example.mhnfe.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.data.remote.response.ImageInfo
import com.example.mhnfe.domain.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ReportDetailUiState(
    val images: List<ImageInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun loadImages(date: LocalDate) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val response = imageRepository.getImages(
                    year = date.year,
                    month = date.monthValue.toString().padStart(2, '0'),
                    day = date.dayOfMonth.toString().padStart(2, '0')
                )

                _uiState.update {
                    it.copy(
                        images = response.body.images,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "이미지를 불러오는데 실패했습니다."
                    )
                }
            }
        }
    }
}