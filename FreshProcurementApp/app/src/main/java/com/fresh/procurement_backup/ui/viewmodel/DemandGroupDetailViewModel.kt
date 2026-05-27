package com.fresh.procurement.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fresh.procurement.data.model.*
import com.fresh.procurement.data.repository.DemandRepository
import com.fresh.procurement.data.repository.QuoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DemandGroupDetailUiState(
    val groupDetail: DemandGroupDetail? = null,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val quoteSuccess: Boolean = false
)

@HiltViewModel
class DemandGroupDetailViewModel @Inject constructor(
    private val demandRepository: DemandRepository,
    private val quoteRepository: QuoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DemandGroupDetailUiState())
    val uiState: StateFlow<DemandGroupDetailUiState> = _uiState.asStateFlow()

    fun loadGroupDetail(groupId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            demandRepository.getDemandGroupDetail(groupId)
                .onSuccess { detail ->
                    _uiState.value = _uiState.value.copy(
                        groupDetail = detail,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = error.message)
                }
        }
    }

    fun submitQuote(groupId: Long, unitPrice: Double, validHours: Int, remark: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            quoteRepository.createQuote(groupId, unitPrice, validHours, remark)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        quoteSuccess = true
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        error = error.message
                    )
                }
        }
    }

    fun resetQuoteSuccess() {
        _uiState.value = _uiState.value.copy(quoteSuccess = false)
    }
}
