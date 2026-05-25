package com.fresh.procurement.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fresh.procurement.data.model.*
import com.fresh.procurement.data.repository.DemandRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BuyerHomeUiState(
    val demands: List<Demand> = emptyList(),
    val total: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentTab: Int = 0
)

@HiltViewModel
class BuyerViewModel @Inject constructor(
    private val demandRepository: DemandRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BuyerHomeUiState())
    val uiState: StateFlow<BuyerHomeUiState> = _uiState.asStateFlow()

    init {
        loadDemands()
    }

    fun loadDemands(status: Int? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            demandRepository.getBuyerDemands(page = 1, size = 20, status = status)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        demands = response.list,
                        total = response.total,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    fun updateTab(tab: Int) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
        when (tab) {
            0 -> loadDemands()        // 需求列表
            1 -> loadDemands(status = 5) // 待发货订单
        }
    }

    fun refresh() {
        loadDemands()
    }
}
