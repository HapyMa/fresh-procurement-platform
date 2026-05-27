package com.fresh.procurement.ui.screens.buyer

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
import com.fresh.procurement.data.model.Demand
import com.fresh.procurement.data.model.DemandGroup
import com.fresh.procurement.data.model.Quote
import com.fresh.procurement.ui.viewmodel.DemandDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemandDetailScreen(
    demandId: Long,
    onNavigateBack: () -> Unit,
    viewModel: DemandDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showQuoteDialog by remember { mutableStateOf(false) }
    var selectedQuote by remember { mutableStateOf<Quote?>(null) }

    // 加载需求详情
    LaunchedEffect(demandId) {
        viewModel.loadDemandDetail(demandId)
    }

    // 监听选择报价成功
    LaunchedEffect(uiState.selectSuccess) {
        if (uiState.selectSuccess) {
            showQuoteDialog = false
            selectedQuote = null
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
        }
    ) { paddingValues ->
        when {
            // 加载中状态
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
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val detailError = uiState.error
                    Text(
                        text = detailError ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    val nonBlockError = uiState.error
                    if (nonBlockError != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = nonBlockError,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // 状态卡片
                    StatusCard(
                        status = demand.getStatusText(),
                        statusCode = demand.status
                    )

                    // 需求信息
                    DemandInfoCard(demand = demand)

                    // 合并组信息
                    if (uiState.groupInfo != null) {
                        GroupInfoCard(groupInfo = uiState.groupInfo!!)
                    }

                    // 报价列表
                    if (uiState.quotes.isNotEmpty()) {
                        QuotesCard(
                            quotes = uiState.quotes,
                            isSelecting = uiState.isSelecting,
                            onSelectQuote = { quote ->
                                selectedQuote = quote
                                showQuoteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // 选择报价确认对话框
    if (showQuoteDialog && selectedQuote != null) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isSelecting) {
                    showQuoteDialog = false
                }
            },
            title = { Text("确认选择") },
            text = {
                Column {
                    Text("供应商: ${selectedQuote!!.supplierName}")
                    Text("报价: ${selectedQuote!!.unitPrice} 元/斤")
                    selectedQuote!!.totalAmount?.let {
                        Text("总价: $it 元")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "确认后将生成订单，供应商将开始备货发货。",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.selectQuote(demandId, selectedQuote!!.quoteId)
                    },
                    enabled = !uiState.isSelecting
                ) {
                    if (uiState.isSelecting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("确认")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showQuoteDialog = false },
                    enabled = !uiState.isSelecting
                ) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun StatusCard(status: String, statusCode: Int) {
    val (containerColor, contentColor) = when (statusCode) {
        3 -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        4 -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        5 -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
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
                imageVector = when (statusCode) {
                    3 -> Icons.Default.Timer
                    4 -> Icons.Default.CheckCircle
                    5 -> Icons.Default.Inventory
                    else -> Icons.Default.Info
                },
                contentDescription = null,
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "当前状态: $status",
                style = MaterialTheme.typography.titleMedium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun DemandInfoCard(demand: Demand) {
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
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoRow(label = "商品", value = demand.productName)
            InfoRow(label = "数量", value = "${demand.quantity.toInt()} ${demand.unit ?: ""}")
            demand.maxPrice?.let {
                InfoRow(label = "期望单价", value = "$it 元/${demand.unit ?: ""}")
            }
            demand.qualityRequirement?.let {
                InfoRow(label = "品质要求", value = it)
            }
            demand.deliveryDate?.let {
                InfoRow(label = "配送日期", value = it)
            }
            demand.deliveryTimeSlot?.let {
                InfoRow(label = "配送时间", value = it)
            }
        }
    }
}

@Composable
private fun GroupInfoCard(groupInfo: DemandGroup) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
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
                    text = "合并组信息",
                    style = MaterialTheme.typography.titleMedium
                )
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "已合并",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(label = "城市", value = groupInfo.city)
            InfoRow(label = "合并总量", value = "${groupInfo.totalQuantity.toInt()} ${groupInfo.unit ?: "斤"}")
            InfoRow(label = "采购单数", value = "${groupInfo.demandCount} 个")
        }
    }
}

@Composable
private fun QuotesCard(
    quotes: List<Quote>,
    isSelecting: Boolean,
    onSelectQuote: (Quote) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "供应商报价 (${quotes.size})",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        quotes.forEach { quote ->
            QuoteCard(
                quote = quote,
                onSelect = { onSelectQuote(quote) },
                isSelecting = isSelecting
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuoteCard(
    quote: Quote,
    onSelect: () -> Unit,
    isSelecting: Boolean
) {
    Card(
        onClick = onSelect,
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
                    text = quote.supplierName ?: "",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${quote.supplierScore}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${quote.unitPrice}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "元/斤",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (!quote.remark.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = quote.remark,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSelecting
            ) {
                if (isSelecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("选择此报价")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
