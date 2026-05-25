package com.fresh.procurement.ui.screens.supplier

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fresh.procurement.ui.viewmodel.ShipOrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipOrderScreen(
    demandId: Long,
    onNavigateBack: () -> Unit,
    viewModel: ShipOrderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var logisticsType by remember { mutableIntStateOf(1) }
    var logisticsCompany by remember { mutableStateOf("") }
    var trackingNo by remember { mutableStateOf("") }
    var estimatedArrival by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }

    // 加载订单数据
    LaunchedEffect(demandId) {
        viewModel.loadOrder(demandId)
    }

    // 发货成功后自动返回
    LaunchedEffect(uiState.shipSuccess) {
        if (uiState.shipSuccess) {
            onNavigateBack()
        }
    }

    val demand = uiState.demand
    val packRecord = uiState.packRecord

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("发货") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && demand == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null && demand == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val shipError = uiState.error
                    Text(
                        text = shipError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { viewModel.loadOrder(demandId) }) {
                        Text("重试")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // 订单信息
                    OrderInfoSection(
                        demand = demand,
                        packRecord = packRecord
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 物流方式选择
                    Text(
                        text = "选择物流方式",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LogisticsTypeSelector(
                        selectedType = logisticsType,
                        onTypeSelected = { logisticsType = it }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 物流信息
                    when (logisticsType) {
                        1 -> ThirdPartyLogisticsForm(
                            company = logisticsCompany,
                            onCompanyChange = { logisticsCompany = it },
                            trackingNo = trackingNo,
                            onTrackingNoChange = { trackingNo = it }
                        )
                        2 -> SelfDeliveryForm(
                            estimatedArrival = estimatedArrival,
                            onArrivalChange = { estimatedArrival = it }
                        )
                        3 -> PickupInfo()
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 备注
                    OutlinedTextField(
                        value = remark,
                        onValueChange = { remark = it },
                        label = { Text("备注") },
                        placeholder = { Text("可选，如：请轻拿轻放") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 发货按钮
                Button(
                    onClick = {
                        // 获取包裹ID列表
                        val packageIds = packRecord?.packages?.map { it.packageId } ?: emptyList()
                        viewModel.shipOrder(
                            demandId = demandId,
                            packageIds = packageIds,
                            logisticsType = logisticsType,
                            logisticsCompany = logisticsCompany.ifBlank { null },
                            trackingNo = trackingNo.ifBlank { null },
                            estimatedArrival = estimatedArrival.ifBlank { null }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    enabled = when (logisticsType) {
                        1 -> logisticsCompany.isNotBlank() && trackingNo.isNotBlank() && !uiState.isShipping
                        2 -> estimatedArrival.isNotBlank() && !uiState.isShipping
                        else -> !uiState.isShipping
                    }
                ) {
                    if (uiState.isShipping) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("确认发货")
                }
            }
        }
    }
}

@Composable
private fun OrderInfoSection(
    demand: com.fresh.procurement.data.model.Demand?,
    packRecord: com.fresh.procurement.data.model.PackRecord?
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "订单信息",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("商品: ${demand?.productName ?: ""}")
                Text(
                    "${demand?.quantity?.toInt() ?: 0} ${demand?.unit ?: ""}",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "采购商: ${demand?.buyerName ?: ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "收货地址: ${demand?.deliveryAddress?.let { "${it.city}${it.district ?: ""}${it.detail}" } ?: ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 显示包裹数量
            if (packRecord != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "包裹数量: ${packRecord.packageCount ?: 0} 个",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun LogisticsTypeSelector(
    selectedType: Int,
    onTypeSelected: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LogisticsTypeItem(
            icon = Icons.Default.LocalShipping,
            title = "第三方物流",
            subtitle = "使用顺丰、京东等快递公司",
            selected = selectedType == 1,
            onClick = { onTypeSelected(1) }
        )

        LogisticsTypeItem(
            icon = Icons.Default.DriveEta,
            title = "供应商自配送",
            subtitle = "使用自有车辆配送",
            selected = selectedType == 2,
            onClick = { onTypeSelected(2) }
        )

        LogisticsTypeItem(
            icon = Icons.Default.Store,
            title = "采购商自提",
            subtitle = "采购商到仓库自提",
            selected = selectedType == 3,
            onClick = { onTypeSelected(3) }
        )
    }
}

@Composable
private fun LogisticsTypeItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else null
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
                tint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThirdPartyLogisticsForm(
    company: String,
    onCompanyChange: (String) -> Unit,
    trackingNo: String,
    onTrackingNoChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val companies = listOf("顺丰速运", "京东物流", "中通快递", "圆通速递", "韵达快递", "申通快递", "其他")

    Column {
        Text(
            text = "物流信息",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = company,
                onValueChange = {},
                readOnly = true,
                label = { Text("物流公司 *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                companies.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            onCompanyChange(item)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = trackingNo,
            onValueChange = onTrackingNoChange,
            label = { Text("物流单号 *") },
            trailingIcon = {
                IconButton(onClick = { /* 扫码 */ }) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "扫码")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SelfDeliveryForm(
    estimatedArrival: String,
    onArrivalChange: (String) -> Unit
) {
    Column {
        Text(
            text = "配送信息",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = estimatedArrival,
            onValueChange = onArrivalChange,
            label = { Text("预计到达时间 *") },
            placeholder = { Text("如：2024-01-20 10:00") },
            trailingIcon = {
                IconButton(onClick = { /* 选择时间 */ }) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 路线优化提示
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.small
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "建议：您今天还有2个订单需要配送，可以合并配送路线",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun PickupInfo() {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "自提信息",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "请通知采购商到以下地址自提：",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "成都市双流区XX批发市场A区3号仓库",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "联系人: 张经理 13800138001",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
