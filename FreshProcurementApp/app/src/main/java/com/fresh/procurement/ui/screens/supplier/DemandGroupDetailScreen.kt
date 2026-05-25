package com.fresh.procurement.ui.screens.supplier

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fresh.procurement.data.model.DemandGroupDetail
import com.fresh.procurement.data.model.GroupDemandItem
import com.fresh.procurement.ui.viewmodel.DemandGroupDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemandGroupDetailScreen(
    groupId: Long,
    onNavigateBack: () -> Unit,
    viewModel: DemandGroupDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showQuoteDialog by remember { mutableStateOf(false) }
    var quotePrice by remember { mutableStateOf("") }
    var quoteHours by remember { mutableStateOf("24") }
    var quoteRemark by remember { mutableStateOf("") }

    // 加载组详情
    LaunchedEffect(groupId) {
        viewModel.loadGroupDetail(groupId)
    }

    // 监听报价成功
    LaunchedEffect(uiState.quoteSuccess) {
        if (uiState.quoteSuccess) {
            viewModel.resetQuoteSuccess()
            onNavigateBack()
        }
    }

    val groupDetail = uiState.groupDetail

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("合并需求详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp
            ) {
                Button(
                    onClick = { showQuoteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    enabled = groupDetail != null && !uiState.isSubmitting
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("提交报价")
                }
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null && groupDetail == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { viewModel.loadGroupDetail(groupId) }) {
                            Text("重试")
                        }
                    }
                }
            }
            groupDetail != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    // 合并组概览
                    GroupOverviewCard(groupDetail = groupDetail)

                    // 需求明细列表
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "包含需求 (${groupDetail.demands.size})",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        groupDetail.demands.forEach { demand ->
                            DemandItemCard(demand = demand)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }

    // 报价对话框
    if (showQuoteDialog && groupDetail != null) {
        AlertDialog(
            onDismissRequest = { showQuoteDialog = false },
            title = { Text("提交报价") },
            text = {
                Column {
                    Text(
                        text = "合并总量: ${groupDetail.totalQuantity.toInt()} ${groupDetail.unit ?: ""}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = quotePrice,
                        onValueChange = { quotePrice = it },
                        label = { Text("报价单价 (元/${groupDetail.unit ?: ""}) *") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = quoteHours,
                        onValueChange = { quoteHours = it },
                        label = { Text("报价有效时长 (小时) *") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = quoteRemark,
                        onValueChange = { quoteRemark = it },
                        label = { Text("报价说明") },
                        placeholder = { Text("如：量大从优，品质保证") },
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val price = quotePrice.toDoubleOrNull()
                        val hours = quoteHours.toIntOrNull()
                        if (price != null && hours != null) {
                            viewModel.submitQuote(
                                groupId = groupId,
                                unitPrice = price,
                                validHours = hours,
                                remark = quoteRemark.ifBlank { null }
                            )
                            showQuoteDialog = false
                        }
                    },
                    enabled = quotePrice.isNotBlank() && quoteHours.isNotBlank() && !uiState.isSubmitting
                ) {
                    Text("提交")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuoteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun GroupOverviewCard(groupDetail: DemandGroupDetail) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
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
                    text = groupDetail.productName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "报价中",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${groupDetail.totalQuantity.toInt()}",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${groupDetail.unit ?: ""} · ${groupDetail.demands.size}个采购单",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = groupDetail.city,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun DemandItemCard(demand: GroupDemandItem) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = demand.buyerName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${demand.quantity.toInt()} 斤",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            InfoRow(icon = Icons.Default.AttachMoney, text = "期望单价: ${demand.maxPrice ?: 0} 元/斤")
            InfoRow(icon = Icons.Default.Star, text = "品质: ${demand.qualityRequirement ?: ""}")
            InfoRow(
                icon = Icons.Default.LocationOn,
                text = demand.deliveryAddress?.let { "${it.city}${it.district ?: ""}${it.detail}" } ?: ""
            )
            InfoRow(
                icon = Icons.Default.CalendarToday,
                text = "${demand.deliveryDate ?: ""} ${demand.deliveryTimeSlot ?: ""}"
            )
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
