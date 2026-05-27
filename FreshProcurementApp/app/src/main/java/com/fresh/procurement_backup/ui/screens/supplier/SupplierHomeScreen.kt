package com.fresh.procurement.ui.screens.supplier

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fresh.procurement.data.model.Demand
import com.fresh.procurement.data.model.DemandGroup
import com.fresh.procurement.data.model.GroupStatus
import com.fresh.procurement.ui.viewmodel.SupplierHomeUiState
import com.fresh.procurement.ui.viewmodel.SupplierViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierHomeScreen(
    onNavigateToGroupDetail: (Long) -> Unit,
    onNavigateToPackOrder: (Long) -> Unit,
    viewModel: SupplierViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("供应商首页") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("找需求") },
                    selected = uiState.currentTab == 0,
                    onClick = { viewModel.updateTab(0) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                    label = { Text("待打包") },
                    selected = uiState.currentTab == 1,
                    onClick = { viewModel.updateTab(1) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = null) },
                    label = { Text("配送") },
                    selected = uiState.currentTab == 2,
                    onClick = { viewModel.updateTab(2) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("我的") },
                    selected = uiState.currentTab == 3,
                    onClick = { viewModel.updateTab(3) }
                )
            }
        }
    ) { paddingValues ->
        when (uiState.currentTab) {
            0 -> DemandGroupListTab(
                modifier = Modifier.padding(paddingValues),
                onGroupClick = onNavigateToGroupDetail,
                groups = uiState.demandGroups,
                isLoading = uiState.isLoading,
                error = uiState.error,
                onRetry = { viewModel.loadDemandGroups() }
            )
            1 -> PendingPackTab(
                modifier = Modifier.padding(paddingValues),
                onOrderClick = onNavigateToPackOrder,
                orders = uiState.pendingPackOrders,
                isLoading = uiState.isLoading,
                error = uiState.error,
                onRetry = { viewModel.loadPendingPackOrders() }
            )
            2 -> DeliveryTab(
                modifier = Modifier.padding(paddingValues)
            )
            3 -> SupplierProfileTab(
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun DemandGroupListTab(
    modifier: Modifier = Modifier,
    onGroupClick: (Long) -> Unit,
    groups: List<DemandGroup>,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        if (isLoading && groups.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (error != null && groups.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onRetry) {
                    Text("重试")
                }
            }
        } else if (groups.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无需求",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(groups) { group ->
                    DemandGroupCard(
                        group = group,
                        onClick = { onGroupClick(group.groupId) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DemandGroupCard(
    group: DemandGroup,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = group.productName,
                    style = MaterialTheme.typography.titleLarge
                )
                GroupStatusChip(status = group.getStatusEnum())
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${group.totalQuantity.toInt()}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${group.unit ?: ""} · ${group.demandCount}个采购单",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = group.city,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (group.quoteCount != null && group.quoteCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "已有 ${group.quoteCount} 家报价",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun GroupStatusChip(status: GroupStatus) {
    val (containerColor, contentColor) = when (status) {
        GroupStatus.QUOTING -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status.text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun PendingPackTab(
    modifier: Modifier = Modifier,
    onOrderClick: (Long) -> Unit,
    orders: List<Demand>,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        if (isLoading && orders.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (error != null && orders.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onRetry) {
                    Text("重试")
                }
            }
        } else if (orders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无待打包订单",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders) { order ->
                    PackOrderCard(
                        order = order,
                        onClick = { onOrderClick(order.demandId) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PackOrderCard(
    order: Demand,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = order.productName,
                    style = MaterialTheme.typography.titleMedium
                )
                PackStatusBadge(status = order.packStatus ?: 0)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${order.quantity.toInt()} ${order.unit ?: ""}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "采购商: ${order.buyerName ?: ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = order.deliveryAddress?.let { "${it.city}${it.district ?: ""}${it.detail}" } ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PackStatusBadge(status: Int) {
    val (text, color) = when (status) {
        0 -> "待分拣" to MaterialTheme.colorScheme.error
        1 -> "分拣中" to MaterialTheme.colorScheme.tertiary
        2 -> "已打包" to MaterialTheme.colorScheme.primary
        else -> "未知" to MaterialTheme.colorScheme.outline
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun DeliveryTab(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("配送管理")
    }
}

@Composable
private fun SupplierProfileTab(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("供应商中心")
    }
}
