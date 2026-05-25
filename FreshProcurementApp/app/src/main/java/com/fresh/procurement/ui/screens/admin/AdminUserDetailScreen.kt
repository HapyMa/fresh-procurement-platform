package com.fresh.procurement.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fresh.procurement.data.model.AdminUserItem
import com.fresh.procurement.ui.theme.SuccessGreen
import com.fresh.procurement.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserDetailScreen(
    userId: Long,
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.userDetailState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showToggleDialog by remember { mutableStateOf(false) }

    // 加载用户详情
    LaunchedEffect(userId) {
        viewModel.loadUserDetail(userId)
    }

    // 监听切换状态成功
    LaunchedEffect(uiState.toggleSuccess) {
        if (uiState.toggleSuccess) {
            snackbarHostState.showSnackbar(
                message = "用户状态已更新"
            )
            viewModel.resetToggleSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("用户详情") },
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
            uiState.isLoading && uiState.user == null -> {
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
            uiState.error != null && uiState.user == null -> {
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
                    Text(
                        text = uiState.error ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadUserDetail(userId) }) {
                        Text("重试")
                    }
                }
            }
            // 正常数据展示
            uiState.user != null -> {
                val user = uiState.user!!

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
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // 用户头像和状态区域
                    UserHeaderCard(user = user)

                    // 用户基本信息
                    UserInfoCard(user = user)

                    Spacer(modifier = Modifier.height(16.dp))

                    // 切换状态按钮
                    Button(
                        onClick = { showToggleDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (user.isActive()) {
                                MaterialTheme.colorScheme.error
                            } else {
                                SuccessGreen
                            }
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (user.isActive()) {
                                    Icons.Default.Block
                                } else {
                                    Icons.Default.CheckCircle
                                },
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (user.isActive()) "禁用用户" else "启用用户")
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // 切换状态确认对话框
    if (showToggleDialog && uiState.user != null) {
        val user = uiState.user!!
        AlertDialog(
            onDismissRequest = { showToggleDialog = false },
            icon = {
                Icon(
                    imageVector = if (user.isActive()) {
                        Icons.Default.Block
                    } else {
                        Icons.Default.CheckCircle
                    },
                    contentDescription = null,
                    tint = if (user.isActive()) {
                        MaterialTheme.colorScheme.error
                    } else {
                        SuccessGreen
                    }
                )
            },
            title = {
                Text(
                    if (user.isActive()) "确认禁用" else "确认启用"
                )
            },
            text = {
                Text(
                    if (user.isActive()) {
                        "确定要禁用用户「${user.nickname ?: user.phone}」吗？禁用后该用户将无法登录系统。"
                    } else {
                        "确定要启用用户「${user.nickname ?: user.phone}」吗？启用后该用户可以正常登录系统。"
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showToggleDialog = false
                        viewModel.toggleUserStatus(userId)
                    }
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { showToggleDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun UserHeaderCard(user: AdminUserItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像占位
            Surface(
                modifier = Modifier.size(64.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.nickname ?: "未设置昵称",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.phone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }

            // 状态标签
            StatusBadge(isActive = user.isActive())
        }
    }
}

@Composable
private fun StatusBadge(isActive: Boolean) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (isActive) {
            SuccessGreen
        } else {
            MaterialTheme.colorScheme.error
        }
    ) {
        Text(
            text = if (isActive) "正常" else "已禁用",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun UserInfoCard(user: AdminUserItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "用户信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            InfoRow(label = "手机号", value = user.phone)
            InfoRow(label = "昵称", value = user.nickname ?: "未设置")
            InfoRow(label = "用户类型", value = user.getUserTypeText())
            InfoRow(
                label = "账号状态",
                value = if (user.isActive()) "正常" else "已禁用"
            )
            InfoRow(label = "注册时间", value = user.createdAt)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
