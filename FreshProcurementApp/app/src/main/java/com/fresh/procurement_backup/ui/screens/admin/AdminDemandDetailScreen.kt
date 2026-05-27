package com.fresh.procurement.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fresh.procurement.data.model.AdminDemandItem
import com.fresh.procurement.ui.theme.*
import com.fresh.procurement.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDemandDetailScreen(
    demandId: Long,
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.demandDetailState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCancelDialog by remember { mutableStateOf(false) }

    // 加载需求详情
    LaunchedEffect(demandId) {
        viewModel.loadDemandDetail(demandId)
    }

    // 监听取消成功
    LaunchedEffect(uiState.cancelSuccess) {
        if (uiState.cancelSuccess) {
            snackbarHostState.showSnackbar(
                message = "需求已取消"
            )
            viewModel.resetCancelSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("需求详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            // 加载中状态
            uiState.isLoading && uiState.demand == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            // 错误状态
            uiState.error != null && uiState.demand == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "加载失败",
                        style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.error),
                                            )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.error ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                            )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadDemandDetail(demandId) }) {
                        Text("重试")
                    }
                }
            }
            // 正常数据展示
            uiState.demand != null -> {
                val demand = uiState.demand!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    // 错误提示（非阻塞）
                    if (uiState.error != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = uiState.error ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onErrorContainer,),
                                                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // 状态卡片
                    DemandStatusCard(demand = demand)

                    // 需求信息卡片
                    DemandInfoCard(demand = demand)

                    // 买家信息卡片
                    if (!demand.buyerName.isNullOrBlank()) {
                        BuyerInfoCard(buyerName = demand.buyerName)
                    }

                    // 供应商信息卡片
                    if (!demand.supplierName.isNullOrBlank()) {
                        SupplierInfoCard(supplierName = demand.supplierName)
                    }

                    // 成交信息卡片
                    if (demand.dealPrice != null || demand.dealTotalAmount != null) {
                        DealInfoCard(demand = demand)
                    }

                    // 城市信息卡片
                    if (!demand.cityName.isNullOrBlank()) {
                        CityInfoCard(cityName = demand.cityName)
                    }

                    // 取消按钮（仅非已取消状态显示）
                    if (demand.status != 9) {
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = { showCancelDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            enabled = !uiState.isLoading,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    MaterialTheme.colorScheme.error
                                )
                            )
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.error,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("取消需求")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // 取消确认对话框
    if (showCancelDialog && uiState.demand != null) {
        val demand = uiState.demand!!
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("确认取消需求") },
            text = {
                Text(
                    "确定要取消需求「${demand.productName}」吗？\n" +
                        "数量: ${demand.quantity.toInt()} ${demand.unit ?: ""}\n\n" +
                        "取消后将通知相关采购商和供应商，此操作不可撤销。"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        viewModel.cancelDemand(demandId)
                    }
                ) {
                    Text(
                        "确认取消",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("返回")
                }
            }
        )
    }
}

@Composable
private fun DemandStatusCard(demand: AdminDemandItem) {
    val (containerColor, contentColor, icon) = when (demand.status) {
        0 -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Icons.Default.Timer)
        3 -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, Icons.Default.Info)
        5 -> Triple(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, Icons.Default.CheckCircle)
        6 -> Triple(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer, Icons.Default.LocalShipping)
        7 -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, Icons.Default.Inventory)
        9 -> Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, Icons.Default.Cancel)
        else -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Icons.Default.Info)
    }

    Surface(
        color = containerColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "当前状态: ${demand.getStatusText()}",
                style = MaterialTheme.typography.titleMedium.copy(color = contentColor)
            )
        }
    }
}

@Composable
private fun DemandInfoCard(demand: AdminDemandItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "需求信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            InfoRow(label = "商品名称", value = demand.productName)
            InfoRow(
                label = "数量",
                value = "${demand.quantity.toInt()} ${demand.unit ?: ""}"
            )
            demand.maxPrice?.let {
                InfoRow(
                    label = "最高单价",
                    value = "$it 元/${demand.unit ?: ""}"
                )
            }
            demand.qualityRequirement?.let {
                InfoRow(label = "品质要求", value = it)
            }
            demand.deliveryDate?.let {
                InfoRow(label = "配送日期", value = it)
            }
            demand.deliveryTimeSlot?.let {
                InfoRow(label = "配送时段", value = it)
            }
            demand.remark?.let {
                if (it.isNotBlank()) {
                    InfoRow(label = "备注", value = it)
                }
            }
        }
    }
}

@Composable
private fun BuyerInfoCard(buyerName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "采购商信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            InfoRow(label = "采购商", value = buyerName)
        }
    }
}

@Composable
private fun SupplierInfoCard(supplierName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "供应商信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            InfoRow(label = "供应商", value = supplierName)
        }
    }
}

@Composable
private fun DealInfoCard(demand: AdminDemandItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "成交信息",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Surface(
                    color = SuccessGreen,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "已成交",
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onPrimary,),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
            )

            demand.dealPrice?.let {
                InfoRow(
                    label = "成交单价",
                    value = "$it 元/${demand.unit ?: ""}",
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            demand.dealTotalAmount?.let {
                InfoRow(
                    label = "成交总额",
                    value = "$it 元",
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun CityInfoCard(cityName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "区域信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            InfoRow(label = "城市", value = cityName)
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
    }
}
