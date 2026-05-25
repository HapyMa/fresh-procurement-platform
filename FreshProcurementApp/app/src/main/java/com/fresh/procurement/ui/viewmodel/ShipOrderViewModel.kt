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

data class ShipOrderUiState(
    val demand: Demand? = null,
    val packRecord: PackRecord? = null,
    val isLoading: Boolean = false,
    val isShipping: Boolean = false,
    val error: String? = null,
    val shipSuccess: Boolean = false
)

@HiltViewModel
class ShipOrderViewModel @Inject constructor(
    private val demandRepository: DemandRepository,
    private val packRepository: PackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShipOrderUiState())
    val uiState: StateFlow<ShipOrderUiState> = _uiState.asStateFlow()

    fun loadOrder(demandId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            demandRepository.getDemandDetail(demandId)
                .onSuccess { demand ->
                    _uiState.value = _uiState.value.copy(demand = demand, isLoading = false)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = error.message)
                }
            // 同时加载打包记录
            loadPackRecord(demandId)
        }
    }

    private fun loadPackRecord(demandId: Long) {
        viewModelScope.launch {
            packRepository.getPackRecord(demandId)
                .onSuccess { record ->
                    _uiState.value = _uiState.value.copy(packRecord = record)
                }
        }
    }

    fun shipOrder(
        demandId: Long,
        packageIds: List<Long>,
        logisticsType: Int,
        logisticsCompany: String?,
        trackingNo: String?,
        estimatedArrival: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isShipping = true, error = null)
            packRepository.shipOrder(
                demandId = demandId,
                packageIds = packageIds,
                logisticsType = logisticsType,
                logisticsCompany = logisticsCompany,
                trackingNo = trackingNo,
                estimatedArrival = estimatedArrival
            )
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isShipping = false,
                        shipSuccess = true
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isShipping = false,
                        error = error.message
                    )
                }
        }
    }
}
