package com.fresh.procurement.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fresh.procurement.data.model.AdminDashboard
import com.fresh.procurement.data.model.AdminDemandItem
import com.fresh.procurement.data.model.AdminQuoteItem
import com.fresh.procurement.data.model.AdminUserItem
import com.fresh.procurement.ui.theme.*
import com.fresh.procurement.ui.viewmodel.AdminDashboardUiState
import com.fresh.procurement.ui.viewmodel.AdminDemandsUiState
import com.fresh.procurement.ui.viewmodel.AdminQuotesUiState
import com.fresh.procurement.ui.viewmodel.AdminUsersUiState
import com.fresh.procurement.ui.viewmodel.AdminViewModel

// ============================================================
// AdminHomeScreen - Main entry point
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    onNavigateToUserDetail: (Long) -> Unit = {},
    onNavigateToDemandDetail: (Long) -> Unit = {},
    viewModel: AdminViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val usersState by viewModel.usersState.collectAsStateWithLifecycle()
    val demandsState by viewModel.demandsState.collectAsStateWithLifecycle()
    val quotesState by viewModel.quotesState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("管理后台") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = {
                        when (selectedTab) {
                            0 -> viewModel.loadDashboard()
                            1 -> viewModel.loadUsers()
                            2 -> viewModel.loadDemands()
                            3 -> viewModel.loadQuotes()
                        }
                    }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "刷新",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("仪表盘") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.People, contentDescription = null) },
                    label = { Text("用户") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Description, contentDescription = null) },
                    label = { Text("需求") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    label = { Text("报价") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    label = { Text("订单") },
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 }
                )
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> DashboardTab(
                modifier = Modifier.padding(paddingValues),
                state = dashboardState,
                onRefresh = { viewModel.loadDashboard() },
                onUserClick = onNavigateToUserDetail
            )
            1 -> UsersTab(
                modifier = Modifier.padding(paddingValues),
                state = usersState,
                onRefresh = { viewModel.loadUsers() },
                onUserTypeFilter = { viewModel.setUserFilter(it) },
                onStatusFilter = { viewModel.setStatusFilter(it) },
                onUserClick = onNavigateToUserDetail
            )
            2 -> DemandsTab(
                modifier = Modifier.padding(paddingValues),
                state = demandsState,
                onRefresh = { viewModel.loadDemands() },
                onStatusFilter = { viewModel.setDemandStatusFilter(it) },
                onDemandClick = onNavigateToDemandDetail
            )
            3 -> QuotesTab(
                modifier = Modifier.padding(paddingValues),
                state = quotesState,
                onRefresh = { viewModel.loadQuotes() },
                onStatusFilter = { viewModel.setQuoteStatusFilter(it) }
            )
            4 -> OrdersTab(
                modifier = Modifier.padding(paddingValues),
                state = dashboardState
            )
        }
    }
}

// ============================================================
// DashboardTab
// ============================================================

@Composable
private fun DashboardTab(
    modifier: Modifier = Modifier,
    state: AdminDashboardUiState,
    onRefresh: () -> Unit,
    onUserClick: (Long) -> Unit
) {
    LaunchedEffect(Unit) {
        onRefresh()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading && state.dashboard == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.error != null && state.dashboard == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "加载失败",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRefresh) {
                        Text("重试")
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Statistics grid
                    state.dashboard?.let { dashboard ->
                        item {
                            Text(
                                text = "数据概览",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        item {
                            StatCardRow(
                                items = listOf(
                                    StatCardData(Icons.Default.People, "总用户", "${dashboard.totalUsers}", FreshGreenLight, FreshGreenDark),
                                    StatCardData(Icons.Default.ShoppingCart, "采购商", "${dashboard.totalBuyers}", Color(0xFFE3F2FD), Color(0xFF1565C0))
                                )
                            )
                        }

                        item {
                            StatCardRow(
                                items = listOf(
                                    StatCardData(Icons.Default.LocalShipping, "供应商", "${dashboard.totalSuppliers}", Color(0xFFFFF3E0), OrangeDark),
                                    StatCardData(Icons.Default.Description, "总需求", "${dashboard.totalDemands}", Color(0xFFF3E5F5), Color(0xFF7B1FA2))
                                )
                            )
                        }

                        item {
                            StatCardRow(
                                items = listOf(
                                    StatCardData(Icons.Default.Receipt, "总订单", "${dashboard.totalOrders}", Color(0xFFE0F7FA), Color(0xFF00838F)),
                                    StatCardData(Icons.Default.AttachMoney, "总报价", "${dashboard.totalQuotes}", Color(0xFFFCE4EC), Color(0xFFC62828))
                                )
                            )
                        }

                        item {
                            StatCardRow(
                                items = listOf(
                                    StatCardData(Icons.Default.AccountBalanceWallet, "成交金额", "¥${formatAmount(dashboard.totalAmount)}", Color(0xFFFFF8E1), Color(0xFFF57F17)),
                                    StatCardData(Icons.Default.Pending, "待处理需求", "${dashboard.pendingDemands}", Color(0xFFFFEBEE), Color(0xFFD32F2F))
                                )
                            )
                        }

                        // Recent users section
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "最近注册用户",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (dashboard.recentUsers.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "暂无用户",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(dashboard.recentUsers) { user ->
                                RecentUserCard(
                                    user = user,
                                    onClick = { onUserClick(user.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class StatCardData(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val backgroundColor: Color,
    val iconColor: Color
)

@Composable
private fun StatCardRow(items: List<StatCardData>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { data ->
            StatCard(
                data = data,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    data: StatCardData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = data.backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Icon(
                imageVector = data.icon,
                contentDescription = null,
                tint = data.iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = data.value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = data.iconColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = data.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentUserCard(
    user: AdminUserItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar placeholder
            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.medium,
                color = FreshGreenLight
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = (user.nickname ?: user.phone).take(1),
                        style = MaterialTheme.typography.titleMedium,
                        color = FreshGreenDark
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.nickname ?: user.phone,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = user.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = user.getUserTypeText(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (user.isActive()) "正常" else "禁用",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (user.isActive()) FreshGreen else RedError
                )
            }
        }
    }
}

// ============================================================
// UsersTab
// ============================================================

@Composable
private fun UsersTab(
    modifier: Modifier = Modifier,
    state: AdminUsersUiState,
    onRefresh: () -> Unit,
    onUserTypeFilter: (Int) -> Unit,
    onStatusFilter: (Int) -> Unit,
    onUserClick: (Long) -> Unit
) {
    LaunchedEffect(Unit) {
        onRefresh()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading && state.users.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.error != null && state.users.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "加载失败",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRefresh) {
                        Text("重试")
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Total count
                    item {
                        Text(
                            text = "共 ${state.total} 位用户",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // User type filter chips
                    item {
                        FilterChipRow(
                            items = listOf(
                                FilterChipItem("全部", 0),
                                FilterChipItem("采购商", 1),
                                FilterChipItem("供应商", 2)
                            ),
                            selectedKey = state.currentFilter,
                            onSelect = onUserTypeFilter
                        )
                    }

                    // Status filter chips
                    item {
                        FilterChipRow(
                            items = listOf(
                                FilterChipItem("全部", -1),
                                FilterChipItem("正常", 1),
                                FilterChipItem("禁用", 0)
                            ),
                            selectedKey = state.statusFilter,
                            onSelect = onStatusFilter
                        )
                    }

                    // Loading indicator for refresh
                    if (state.isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }

                    if (state.users.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "暂无数据",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(state.users) { user ->
                            AdminUserCard(
                                user = user,
                                onClick = { onUserClick(user.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminUserCard(
    user: AdminUserItem,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar placeholder
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = FreshGreenLight
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = (user.nickname ?: user.phone).take(1),
                                style = MaterialTheme.typography.titleMedium,
                                color = FreshGreenDark
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = user.nickname ?: "未设置昵称",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = user.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status badge
                Surface(
                    color = if (user.isActive()) FreshGreenLight else Color(0xFFFFEBEE),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (user.isActive()) "正常" else "禁用",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (user.isActive()) FreshGreen else RedError,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "类型: ${user.getUserTypeText()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "注册: ${formatDate(user.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ============================================================
// DemandsTab
// ============================================================

@Composable
private fun DemandsTab(
    modifier: Modifier = Modifier,
    state: AdminDemandsUiState,
    onRefresh: () -> Unit,
    onStatusFilter: (Int) -> Unit,
    onDemandClick: (Long) -> Unit
) {
    LaunchedEffect(Unit) {
        onRefresh()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading && state.demands.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.error != null && state.demands.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "加载失败",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRefresh) {
                        Text("重试")
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Total count
                    item {
                        Text(
                            text = "共 ${state.total} 条需求",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Status filter chips
                    item {
                        FilterChipRow(
                            items = listOf(
                                FilterChipItem("全部", -1),
                                FilterChipItem("待报价", 0),
                                FilterChipItem("已报价", 3),
                                FilterChipItem("已选报价", 5),
                                FilterChipItem("已发货", 6),
                                FilterChipItem("已收货", 7),
                                FilterChipItem("已取消", 9)
                            ),
                            selectedKey = state.statusFilter,
                            onSelect = onStatusFilter
                        )
                    }

                    // Loading indicator for refresh
                    if (state.isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }

                    if (state.demands.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "暂无数据",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(state.demands) { demand ->
                            AdminDemandCard(
                                demand = demand,
                                onClick = { onDemandClick(demand.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminDemandCard(
    demand: AdminDemandItem,
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
            // Title row: product name + status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = demand.productName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                DemandStatusChip(status = demand.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quantity
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${demand.quantity.toInt()}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = demand.unit ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Deal info
            if (demand.dealPrice != null && demand.dealTotalAmount != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "成交: ¥${demand.dealPrice}/(${demand.unit ?: ""})  总计: ¥${formatAmount(demand.dealTotalAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OrangeDark
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Buyer / Supplier / City info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "采购商: ${demand.buyerName ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (demand.supplierName != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "供应商: ${demand.supplierName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (demand.cityName != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = demand.cityName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = formatDate(demand.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DemandStatusChip(status: Int) {
    val (text, containerColor, contentColor) = when (status) {
        0 -> Triple("待报价", Color(0xFFFFF3E0), OrangeDark)
        3 -> Triple("已报价", Color(0xFFE3F2FD), Color(0xFF1565C0))
        5 -> Triple("已选报价", FreshGreenLight, FreshGreenDark)
        6 -> Triple("已发货", Color(0xFFF3E5F5), Color(0xFF7B1FA2))
        7 -> Triple("已收货", Color(0xFFE0F2F1), Color(0xFF00695C))
        9 -> Triple("已取消", Color(0xFFFFEBEE), RedError)
        else -> Triple("未知", Gray200, Gray700)
    }

    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ============================================================
// QuotesTab
// ============================================================

@Composable
private fun QuotesTab(
    modifier: Modifier = Modifier,
    state: AdminQuotesUiState,
    onRefresh: () -> Unit,
    onStatusFilter: (Int) -> Unit
) {
    LaunchedEffect(Unit) {
        onRefresh()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading && state.quotes.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.error != null && state.quotes.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "加载失败",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRefresh) {
                        Text("重试")
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Total count
                    item {
                        Text(
                            text = "共 ${state.total} 条报价",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Status filter chips
                    item {
                        FilterChipRow(
                            items = listOf(
                                FilterChipItem("全部", -1),
                                FilterChipItem("待选中", 0),
                                FilterChipItem("已选中", 1)
                            ),
                            selectedKey = state.statusFilter,
                            onSelect = onStatusFilter
                        )
                    }

                    // Loading indicator for refresh
                    if (state.isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }

                    if (state.quotes.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "暂无数据",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(state.quotes) { quote ->
                            AdminQuoteCard(quote = quote)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminQuoteCard(
    quote: AdminQuoteItem
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title row: product name + status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = quote.productName ?: "未知商品",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                QuoteStatusChip(status = quote.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Price info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (quote.unitPrice != null) {
                    Column {
                        Text(
                            text = "单价",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "¥${formatAmount(quote.unitPrice)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (quote.totalAmount != null) {
                    Column {
                        Text(
                            text = "总价",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "¥${formatAmount(quote.totalAmount)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = OrangeDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Supplier and city
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "供应商: ${quote.supplierName ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (quote.cityName != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = quote.cityName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatDate(quote.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuoteStatusChip(status: Int) {
    val (text, containerColor, contentColor) = when (status) {
        0 -> Triple("待选中", Color(0xFFFFF3E0), OrangeDark)
        1 -> Triple("已选中", FreshGreenLight, FreshGreenDark)
        else -> Triple("未知", Gray200, Gray700)
    }

    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ============================================================
// OrdersTab
// ============================================================

@Composable
private fun OrdersTab(
    modifier: Modifier = Modifier,
    state: AdminDashboardUiState
) {
    val dashboard = state.dashboard

    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading && dashboard == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            dashboard != null -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "订单统计",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        // Total orders card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = FreshGreenLight)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = null,
                                    tint = FreshGreenDark,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${dashboard.totalOrders}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = FreshGreenDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "总订单数",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item {
                        // Total amount card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color(0xFFF57F17),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "¥${formatAmount(dashboard.totalAmount)}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF57F17)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "成交总金额",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item {
                        // Summary stats row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${dashboard.totalDemands}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1565C0)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "总需求数",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${dashboard.totalQuotes}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = OrangeDark
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "总报价数",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item {
                        // Pending demands card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pending,
                                    contentDescription = null,
                                    tint = RedError,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "${dashboard.pendingDemands}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = RedError
                                    )
                                    Text(
                                        text = "待处理需求",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item {
                        // Conversion rate card
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "数据摘要",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OrderSummaryRow(
                                    label = "采购商数量",
                                    value = "${dashboard.totalBuyers}"
                                )
                                Divider()
                                OrderSummaryRow(
                                    label = "供应商数量",
                                    value = "${dashboard.totalSuppliers}"
                                )
                                Divider()
                                OrderSummaryRow(
                                    label = "注册用户总数",
                                    value = "${dashboard.totalUsers}"
                                )
                                Divider()
                                OrderSummaryRow(
                                    label = "平均订单金额",
                                    value = if (dashboard.totalOrders > 0) {
                                        "¥${formatAmount(dashboard.totalAmount / dashboard.totalOrders)}"
                                    } else {
                                        "¥0.00"
                                    }
                                )
                            }
                        }
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无数据",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderSummaryRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

// ============================================================
// Shared Components
// ============================================================

private data class FilterChipItem(
    val label: String,
    val key: Int
)

@Composable
private fun FilterChipRow(
    items: List<FilterChipItem>,
    selectedKey: Int,
    onSelect: (Int) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            FilterChip(
                selected = item.key == selectedKey,
                onClick = { onSelect(item.key) },
                label = { Text(item.label) }
            )
        }
    }
}

// ============================================================
// Utility Functions
// ============================================================

private fun formatAmount(amount: Double): String {
    return if (amount >= 10000) {
        String.format("%.2f万", amount / 10000)
    } else {
        String.format("%.2f", amount)
    }
}

private fun formatDate(dateStr: String): String {
    return if (dateStr.length >= 10) {
        dateStr.substring(0, 10)
    } else {
        dateStr
    }
}
