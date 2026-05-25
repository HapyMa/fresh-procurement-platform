package com.fresh.procurement.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fresh.procurement.data.model.*
import com.fresh.procurement.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminDashboardUiState(
    val dashboard: AdminDashboard? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class AdminUsersUiState(
    val users: List<AdminUserItem> = emptyList(),
    val total: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentFilter: Int = 0, // 0=全部, 1=采购商, 2=供应商
    val statusFilter: Int = -1  // -1=全部, 1=正常, 0=禁用
)

data class AdminDemandsUiState(
    val demands: List<AdminDemandItem> = emptyList(),
    val total: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val statusFilter: Int = -1, // -1=全部
    val cancelSuccess: Boolean = false
)

data class AdminQuotesUiState(
    val quotes: List<AdminQuoteItem> = emptyList(),
    val total: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val statusFilter: Int = -1
)

data class AdminDemandDetailUiState(
    val demand: AdminDemandItem? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val cancelSuccess: Boolean = false
)

data class AdminUserDetailUiState(
    val user: AdminUserItem? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val toggleSuccess: Boolean = false
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    // Dashboard
    private val _dashboardState = MutableStateFlow(AdminDashboardUiState())
    val dashboardState: StateFlow<AdminDashboardUiState> = _dashboardState.asStateFlow()

    fun loadDashboard() {
        viewModelScope.launch {
            _dashboardState.update { it.copy(isLoading = true, error = null) }
            adminRepository.getDashboard().onSuccess { data ->
                _dashboardState.update { it.copy(dashboard = data, isLoading = false) }
            }.onFailure { e ->
                _dashboardState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // Users
    private val _usersState = MutableStateFlow(AdminUsersUiState())
    val usersState: StateFlow<AdminUsersUiState> = _usersState.asStateFlow()

    fun loadUsers(page: Int = 1, size: Int = 20) {
        viewModelScope.launch {
            _usersState.update { it.copy(isLoading = true, error = null) }
            val userType = if (_usersState.value.currentFilter == 0) null else _usersState.value.currentFilter
            val status = if (_usersState.value.statusFilter == -1) null else _usersState.value.statusFilter
            adminRepository.getUsers(userType, status, page, size).onSuccess { data ->
                _usersState.update { it.copy(users = data.users, total = data.total, isLoading = false) }
            }.onFailure { e ->
                _usersState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun setUserFilter(filter: Int) {
        _usersState.update { it.copy(currentFilter = filter) }
        loadUsers()
    }

    fun setStatusFilter(filter: Int) {
        _usersState.update { it.copy(statusFilter = filter) }
        loadUsers()
    }

    // User Detail
    private val _userDetailState = MutableStateFlow(AdminUserDetailUiState())
    val userDetailState: StateFlow<AdminUserDetailUiState> = _userDetailState.asStateFlow()

    fun loadUserDetail(userId: Long) {
        viewModelScope.launch {
            _userDetailState.update { it.copy(isLoading = true, error = null, toggleSuccess = false) }
            adminRepository.getUserDetail(userId).onSuccess { data ->
                _userDetailState.update { it.copy(user = data, isLoading = false) }
            }.onFailure { e ->
                _userDetailState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun toggleUserStatus(userId: Long) {
        viewModelScope.launch {
            _userDetailState.update { it.copy(isLoading = true) }
            adminRepository.toggleUserStatus(userId).onSuccess { data ->
                _userDetailState.update { it.copy(user = data, isLoading = false, toggleSuccess = true) }
            }.onFailure { e ->
                _userDetailState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun resetToggleSuccess() {
        _userDetailState.update { it.copy(toggleSuccess = false) }
    }

    // Demands
    private val _demandsState = MutableStateFlow(AdminDemandsUiState())
    val demandsState: StateFlow<AdminDemandsUiState> = _demandsState.asStateFlow()

    fun loadDemands(page: Int = 1, size: Int = 20) {
        viewModelScope.launch {
            _demandsState.update { it.copy(isLoading = true, error = null, cancelSuccess = false) }
            val status = if (_demandsState.value.statusFilter == -1) null else _demandsState.value.statusFilter
            adminRepository.getDemands(status, page, size).onSuccess { data ->
                _demandsState.update { it.copy(demands = data.demands, total = data.total, isLoading = false) }
            }.onFailure { e ->
                _demandsState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun setDemandStatusFilter(filter: Int) {
        _demandsState.update { it.copy(statusFilter = filter) }
        loadDemands()
    }

    // Demand Detail
    private val _demandDetailState = MutableStateFlow(AdminDemandDetailUiState())
    val demandDetailState: StateFlow<AdminDemandDetailUiState> = _demandDetailState.asStateFlow()

    fun loadDemandDetail(demandId: Long) {
        viewModelScope.launch {
            _demandDetailState.update { it.copy(isLoading = true, error = null, cancelSuccess = false) }
            adminRepository.getDemandDetail(demandId).onSuccess { data ->
                _demandDetailState.update { it.copy(demand = data, isLoading = false) }
            }.onFailure { e ->
                _demandDetailState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun cancelDemand(demandId: Long) {
        viewModelScope.launch {
            _demandDetailState.update { it.copy(isLoading = true) }
            adminRepository.cancelDemand(demandId).onSuccess { data ->
                _demandDetailState.update { it.copy(demand = data, isLoading = false, cancelSuccess = true) }
            }.onFailure { e ->
                _demandDetailState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun resetCancelSuccess() {
        _demandDetailState.update { it.copy(cancelSuccess = false) }
    }

    // Quotes
    private val _quotesState = MutableStateFlow(AdminQuotesUiState())
    val quotesState: StateFlow<AdminQuotesUiState> = _quotesState.asStateFlow()

    fun loadQuotes(page: Int = 1, size: Int = 20) {
        viewModelScope.launch {
            _quotesState.update { it.copy(isLoading = true, error = null) }
            val status = if (_quotesState.value.statusFilter == -1) null else _quotesState.value.statusFilter
            adminRepository.getQuotes(status, page, size).onSuccess { data ->
                _quotesState.update { it.copy(quotes = data.quotes, total = data.total, isLoading = false) }
            }.onFailure { e ->
                _quotesState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun setQuoteStatusFilter(filter: Int) {
        _quotesState.update { it.copy(statusFilter = filter) }
        loadQuotes()
    }
}
