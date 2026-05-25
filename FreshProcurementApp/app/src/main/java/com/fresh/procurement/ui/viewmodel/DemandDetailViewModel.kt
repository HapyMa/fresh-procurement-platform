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

data class DemandDetailUiState(
    val demand: Demand? = null,
    val quotes: List<Quote> = emptyList(),
    val groupInfo: DemandGroup? = null,
    val isLoading: Boolean = false,
    val isSelecting: Boolean = false,
    val error: String? = null,
    val selectSuccess: Boolean = false
)

@HiltViewModel
class DemandDetailViewModel @Inject constructor(
    private val demandRepository: DemandRepository,
    private val quoteRepository: QuoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DemandDetailUiState())
    val uiState: StateFlow<DemandDetailUiState> = _uiState.asStateFlow()

    fun loadDemandDetail(demandId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            demandRepository.getDemandDetail(demandId)
                .onSuccess { demand ->
                    _uiState.value = _uiState.value.copy(
                        demand = demand,
                        isLoading = false
                    )
                    // 如果需求已合并，加载合并组信息
                    demand.groupId?.let { loadGroupInfo(it) }
                    // 如果在报价中状态，加载报价列表
                    if (demand.status == 3 || demand.status == 4) {
                        loadQuotes(demand.groupId ?: 0)
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = error.message)
                }
        }
    }

    private fun loadGroupInfo(groupId: Long) {
        viewModelScope.launch {
            demandRepository.getDemandGroupDetail(groupId)
                .onSuccess { detail ->
                    _uiState.value = _uiState.value.copy(
                        groupInfo = DemandGroup(
                            groupId = detail.groupId,
                            categoryId = 0,
                            productName = detail.productName,
                            city = detail.city,
                            totalQuantity = detail.totalQuantity,
                            unit = null,
                            demandCount = detail.demands.size,
                            mergeDeadline = null,
                            quoteCount = null,
                            status = 2
                        )
                    )
                }
        }
    }

    private fun loadQuotes(groupId: Long) {
        viewModelScope.launch {
            quoteRepository.getMyQuotes(page = 1, size = 20, status = 1)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        quotes = response.list.filter { it.groupId == groupId }
                    )
                }
        }
    }

    fun selectQuote(demandId: Long, quoteId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSelecting = true)
            demandRepository.selectQuote(demandId, quoteId)
                .onSuccess { updatedDemand ->
                    _uiState.value = _uiState.value.copy(
                        demand = updatedDemand,
                        isSelecting = false,
                        selectSuccess = true
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSelecting = false,
                        error = error.message
                    )
                }
        }
    }

    fun confirmReceipt(demandId: Long, actualWeight: Double?, remark: String?) {
        viewModelScope.launch {
            demandRepository.confirmReceipt(demandId, actualWeight, remark)
                .onSuccess {
                    loadDemandDetail(demandId)
                }
        }
    }
}
