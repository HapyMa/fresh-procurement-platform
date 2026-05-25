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
import com.fresh.procurement.ui.viewmodel.PackOrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackOrderScreen(
    demandId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToShip: () -> Unit,
    viewModel: PackOrderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 分拣表单数据
    var actualQuantity by remember { mutableStateOf("") }
    var actualWeight by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("一级") }
    var packageCount by remember { mutableStateOf("1") }
    var packageType by remember { mutableStateOf("泡沫箱") }
    var labelCode by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }

    // 加载订单数据
    LaunchedEffect(demandId) {
        viewModel.loadOrder(demandId)
    }

    val currentStep = uiState.currentStep
    val demand = uiState.demand

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分拣打包") },
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
                    Text(
                        text = uiState.error,
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
                // 步骤指示器
                StepIndicator(currentStep = currentStep)

                HorizontalDivider()

                // 内容区域
                when (currentStep) {
                    0 -> OrderInfoStep(
                        demand = demand,
                        onNext = { viewModel.startPacking(demandId) }
                    )
                    1 -> PackingStep(
                        actualQuantity = actualQuantity,
                        onQuantityChange = { actualQuantity = it },
                        actualWeight = actualWeight,
                        onWeightChange = { actualWeight = it },
                        grade = grade,
                        onGradeChange = { grade = it },
                        packageCount = packageCount,
                        onPackageCountChange = { packageCount = it },
                        packageType = packageType,
                        onPackageTypeChange = { packageType = it },
                        labelCode = labelCode,
                        onLabelCodeChange = { labelCode = it },
                        remark = remark,
                        onRemarkChange = { remark = it },
                        isPacking = uiState.isPacking,
                        onBack = { viewModel.setStep(0) },
                        onComplete = {
                            val qty = actualQuantity.toDoubleOrNull() ?: 0.0
                            val weight = actualWeight.toDoubleOrNull() ?: 0.0
                            val count = packageCount.toIntOrNull() ?: 1
                            viewModel.completePacking(
                                demandId = demandId,
                                actualQuantity = qty,
                                actualWeight = weight,
                                grade = grade,
                                qualityCheck = 1,
                                packageCount = count,
                                packageType = packageType,
                                labelCode = labelCode,
                                remark = remark.ifBlank { null }
                            )
                        }
                    )
                    2 -> PackCompleteStep(
                        onViewShip = onNavigateToShip,
                        onBackToList = onNavigateBack
                    )
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StepItem(
            number = 1,
            title = "订单信息",
            isActive = currentStep >= 0,
            isCompleted = currentStep > 0
        )
        StepItem(
            number = 2,
            title = "分拣打包",
            isActive = currentStep >= 1,
            isCompleted = currentStep > 1
        )
        StepItem(
            number = 3,
            title = "完成",
            isActive = currentStep >= 2,
            isCompleted = currentStep > 2
        )
    }
}

@Composable
private fun StepItem(
    number: Int,
    title: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = when {
                isCompleted -> MaterialTheme.colorScheme.primary
                isActive -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = number.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isActive) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun OrderInfoStep(
    demand: com.fresh.procurement.data.model.Demand?,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 订单信息卡片
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

                Spacer(modifier = Modifier.height(16.dp))

                InfoRow(label = "商品", value = demand?.productName ?: "")
                InfoRow(label = "数量", value = "${demand?.quantity?.toInt() ?: 0} ${demand?.unit ?: ""}")
                InfoRow(label = "采购商", value = demand?.buyerName ?: "")
                InfoRow(label = "联系电话", value = "")
                InfoRow(
                    label = "收货地址",
                    value = demand?.deliveryAddress?.let { "${it.city}${it.district ?: ""}${it.detail}" } ?: ""
                )
                InfoRow(
                    label = "期望配送时间",
                    value = "${demand?.deliveryDate ?: ""} ${demand?.deliveryTimeSlot ?: ""}"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 品质要求
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "品质要求",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = demand?.qualityRequirement ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("开始分拣")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PackingStep(
    actualQuantity: String,
    onQuantityChange: (String) -> Unit,
    actualWeight: String,
    onWeightChange: (String) -> Unit,
    grade: String,
    onGradeChange: (String) -> Unit,
    packageCount: String,
    onPackageCountChange: (String) -> Unit,
    packageType: String,
    onPackageTypeChange: (String) -> Unit,
    labelCode: String,
    onLabelCodeChange: (String) -> Unit,
    remark: String,
    onRemarkChange: (String) -> Unit,
    isPacking: Boolean,
    onBack: () -> Unit,
    onComplete: () -> Unit
) {
    var showGradeMenu by remember { mutableStateOf(false) }
    var showPackageTypeMenu by remember { mutableStateOf(false) }

    val grades = listOf("特级", "一级", "二级", "三级")
    val packageTypes = listOf("泡沫箱", "纸箱", "编织袋", "网袋", "其他")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 实际分拣数量
        OutlinedTextField(
            value = actualQuantity,
            onValueChange = onQuantityChange,
            label = { Text("实际分拣数量") },
            suffix = { Text("斤") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 实际称重
        OutlinedTextField(
            value = actualWeight,
            onValueChange = onWeightChange,
            label = { Text("实际称重") },
            suffix = { Text("kg") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 品质等级
        ExposedDropdownMenuBox(
            expanded = showGradeMenu,
            onExpandedChange = { showGradeMenu = it }
        ) {
            OutlinedTextField(
                value = grade,
                onValueChange = {},
                readOnly = true,
                label = { Text("品质等级") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showGradeMenu) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = showGradeMenu,
                onDismissRequest = { showGradeMenu = false }
            ) {
                grades.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            onGradeChange(item)
                            showGradeMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 包裹数量
        OutlinedTextField(
            value = packageCount,
            onValueChange = onPackageCountChange,
            label = { Text("包裹数量") },
            suffix = { Text("个") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 包装类型
        ExposedDropdownMenuBox(
            expanded = showPackageTypeMenu,
            onExpandedChange = { showPackageTypeMenu = it }
        ) {
            OutlinedTextField(
                value = packageType,
                onValueChange = {},
                readOnly = true,
                label = { Text("包装类型") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPackageTypeMenu) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = showPackageTypeMenu,
                onDismissRequest = { showPackageTypeMenu = false }
            ) {
                packageTypes.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            onPackageTypeChange(item)
                            showPackageTypeMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 标签码
        OutlinedTextField(
            value = labelCode,
            onValueChange = onLabelCodeChange,
            label = { Text("标签码") },
            placeholder = { Text("扫描或输入标签码") },
            trailingIcon = {
                IconButton(onClick = { /* 扫码 */ }) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "扫码")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 备注
        OutlinedTextField(
            value = remark,
            onValueChange = onRemarkChange,
            label = { Text("备注") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 拍照区域
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = { /* 拍照 */ }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("点击拍摄打包照片")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                enabled = !isPacking
            ) {
                Text("上一步")
            }
            Button(
                onClick = onComplete,
                modifier = Modifier.weight(1f),
                enabled = !isPacking
            ) {
                if (isPacking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("完成打包")
            }
        }
    }
}

@Composable
private fun PackCompleteStep(
    onViewShip: () -> Unit,
    onBackToList: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "打包完成",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "该订单已打包完成，可以进行发货",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onViewShip,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("去发货")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBackToList,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("返回订单列表")
        }
    }
}
