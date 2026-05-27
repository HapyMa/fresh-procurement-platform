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

data class SupplierHomeUiState(
    val demandGroups: List<DemandGroup> = emptyList(),
    val pendingPackOrders: List<Demand> = emptyList(),
    val supplierOrders: List<Demand> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentTab: Int = 0
)

@HiltViewModel
class SupplierViewModel @Inject constructor(
    private val demandRepository: DemandRepository,
    private val packRepository: PackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupplierHomeUiState())
    val uiState: StateFlow<SupplierHomeUiState> = _uiState.asStateFlow()

    init {
        loadDemandGroups()
    }

    fun loadDemandGroups(city: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            demandRepository.getDemandGroups(page = 1, size = 20, city = city)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        demandGroups = response.list,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = error.message)
                }
        }
    }

    fun loadPendingPackOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            packRepository.getPendingPackList(page = 1, size = 20)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        pendingPackOrders = response.list,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = error.message)
                }
        }
    }

    fun loadSupplierOrders(status: Int? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            demandRepository.getSupplierOrders(page = 1, size = 20, status = status)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        supplierOrders = response.list,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = error.message)
                }
        }
    }

    fun updateTab(tab: Int) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
        when (tab) {
            0 -> loadDemandGroups()
            1 -> loadPendingPackOrders()
            2 -> loadSupplierOrders(status = 5)
        }
    }

    fun refresh() {
        when (_uiState.value.currentTab) {
            0 -> loadDemandGroups()
            1 -> loadPendingPackOrders()
            2 -> loadSupplierOrders(status = 5)
        }
    }
}
