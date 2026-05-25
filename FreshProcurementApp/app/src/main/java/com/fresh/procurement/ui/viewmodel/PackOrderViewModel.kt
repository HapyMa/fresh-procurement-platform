package com.fresh.procurement.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fresh.procurement.data.model.*
import com.fresh.procurement.data.repository.DemandRepository
import com.fresh.procurement.data.repository.PackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PackOrderUiState(
    val demand: Demand? = null,
    val packRecord: PackRecord? = null,
    val currentStep: Int = 0,
    val isLoading: Boolean = false,
    val isPacking: Boolean = false,
    val error: String? = null,
    val packSuccess: Boolean = false
)

@HiltViewModel
class PackOrderViewModel @Inject constructor(
    private val demandRepository: DemandRepository,
    private val packRepository: PackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PackOrderUiState())
    val uiState: StateFlow<PackOrderUiState> = _uiState.asStateFlow()

    fun loadOrder(demandId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            demandRepository.getDemandDetail(demandId)
                .onSuccess { demand ->
                    _uiState.value = _uiState.value.copy(
                        demand = demand,
                        isLoading = false
                    )
                    // 检查是否已有打包记录
                    loadPackRecord(demandId)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = error.message)
                }
        }
    }

    private fun loadPackRecord(demandId: Long) {
        viewModelScope.launch {
            packRepository.getPackRecord(demandId)
                .onSuccess { record ->
                    _uiState.value = _uiState.value.copy(
                        packRecord = record,
                        currentStep = when (record.status) {
                            1 -> 1 // 分拣中
                            2 -> 2 // 已打包
                            else -> 0
                        }
                    )
                }
                // 没有打包记录时忽略错误
        }
    }

    fun startPacking(demandId: Long) {
        viewModelScope.launch {
            packRepository.startPacking(demandId)
                .onSuccess { record ->
                    _uiState.value = _uiState.value.copy(
                        packRecord = record,
                        currentStep = 1
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }

    fun completePacking(
        demandId: Long,
        actualQuantity: Double,
        actualWeight: Double,
        grade: String?,
        qualityCheck: Int,
        packageCount: Int,
        packageType: String?,
        labelCode: String,
        remark: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPacking = true, error = null)
            packRepository.completePacking(
                demandId = demandId,
                actualQuantity = actualQuantity,
                actualWeight = actualWeight,
                grade = grade,
                qualityCheck = qualityCheck,
                packageCount = packageCount,
                packageType = packageType,
                labelCode = labelCode,
                remark = remark
            )
                .onSuccess { record ->
                    _uiState.value = _uiState.value.copy(
                        packRecord = record,
                        isPacking = false,
                        packSuccess = true,
                        currentStep = 2
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isPacking = false,
                        error = error.message
                    )
                }
        }
    }

    fun setStep(step: Int) {
        _uiState.value = _uiState.value.copy(currentStep = step)
    }
}
