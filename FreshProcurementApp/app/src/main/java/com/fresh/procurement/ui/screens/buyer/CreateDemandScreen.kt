package com.fresh.procurement.ui.screens.buyer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fresh.procurement.data.model.CreateDemandRequest
import com.fresh.procurement.ui.viewmodel.CreateDemandViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDemandScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateDemandViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var productName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("斤") }
    var maxPrice by remember { mutableStateOf("") }
    var qualityRequirement by remember { mutableStateOf("") }
    var deliveryDate by remember { mutableStateOf("") }
    var deliveryTimeSlot by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }

    // 监听提交成功状态，成功后自动返回
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("发布采购需求") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // 错误提示
                val createError = uiState.error
                if (createError != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = createError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 商品名称
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("商品名称 *") },
                    placeholder = { Text("如：土豆、西红柿") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 数量
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("数量 *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("单位") },
                        modifier = Modifier.width(80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 期望单价
                OutlinedTextField(
                    value = maxPrice,
                    onValueChange = { maxPrice = it },
                    label = { Text("期望最高单价") },
                    suffix = { Text("元/${unit}") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 品质要求
                OutlinedTextField(
                    value = qualityRequirement,
                    onValueChange = { qualityRequirement = it },
                    label = { Text("品质要求") },
                    placeholder = { Text("如：一级品，无损伤") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 配送日期
                OutlinedTextField(
                    value = deliveryDate,
                    onValueChange = { deliveryDate = it },
                    label = { Text("期望配送日期") },
                    trailingIcon = {
                        IconButton(onClick = { /* 打开日期选择器 */ }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 配送时间段
                OutlinedTextField(
                    value = deliveryTimeSlot,
                    onValueChange = { deliveryTimeSlot = it },
                    label = { Text("期望时间段") },
                    placeholder = { Text("如：上午9-12点") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 备注
                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("备注") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 提示信息
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "提示：系统将自动为您匹配同城的相同需求进行合并，以获得更优惠的报价。",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // 提交按钮
            Button(
                onClick = {
                    val request = CreateDemandRequest(
                        categoryId = 0,
                        productName = productName,
                        quantity = quantity.toDoubleOrNull() ?: 0.0,
                        unit = unit.ifBlank { null },
                        maxPrice = maxPrice.toDoubleOrNull(),
                        qualityRequirement = qualityRequirement.ifBlank { null },
                        deliveryAddressId = 0,
                        deliveryDate = deliveryDate.ifBlank { null },
                        deliveryTimeSlot = deliveryTimeSlot.ifBlank { null },
                        remark = remark.ifBlank { null }
                    )
                    viewModel.createDemand(request)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(50.dp),
                enabled = productName.isNotBlank() && quantity.isNotBlank() && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("发布需求")
                }
            }
        }
    }
}
