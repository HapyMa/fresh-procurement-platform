package com.fresh.procurement.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fresh.procurement.ui.screens.admin.AdminDemandDetailScreen
import com.fresh.procurement.ui.screens.admin.AdminHomeScreen
import com.fresh.procurement.ui.screens.admin.AdminUserDetailScreen
import com.fresh.procurement.ui.screens.auth.LoginScreen
import com.fresh.procurement.ui.screens.auth.RegisterScreen
import com.fresh.procurement.ui.screens.buyer.BuyerHomeScreen
import com.fresh.procurement.ui.screens.buyer.CreateDemandScreen
import com.fresh.procurement.ui.screens.buyer.DemandDetailScreen
import com.fresh.procurement.ui.screens.supplier.DemandGroupDetailScreen
import com.fresh.procurement.ui.screens.supplier.PackOrderScreen
import com.fresh.procurement.ui.screens.supplier.ShipOrderScreen
import com.fresh.procurement.ui.screens.supplier.SupplierHomeScreen

sealed class Screen(val route: String) {
    // 认证
    object Login : Screen("login")
    object Register : Screen("register")

    // 采购商
    object BuyerHome : Screen("buyer_home")
    object CreateDemand : Screen("create_demand")
    object DemandDetail : Screen("demand_detail/{demandId}") {
        fun createRoute(demandId: Long) = "demand_detail/$demandId"
    }

    // 供应商
    object SupplierHome : Screen("supplier_home")
    object DemandGroupDetail : Screen("demand_group_detail/{groupId}") {
        fun createRoute(groupId: Long) = "demand_group_detail/$groupId"
    }
    object PackOrder : Screen("pack_order/{demandId}") {
        fun createRoute(demandId: Long) = "pack_order/$demandId"
    }
    object ShipOrder : Screen("ship_order/{demandId}") {
        fun createRoute(demandId: Long) = "ship_order/$demandId"
    }

    // 管理员
    object AdminHome : Screen("admin_home")
    object AdminUserDetail : Screen("admin_user_detail/{userId}") {
        fun createRoute(userId: Long) = "admin_user_detail/$userId"
    }
    object AdminDemandDetail : Screen("admin_demand_detail/{demandId}") {
        fun createRoute(demandId: Long) = "admin_demand_detail/$demandId"
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // 认证
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = { userType ->
                    // 根据用户类型导航到不同首页
                    val route = when (userType) {
                        3 -> Screen.AdminHome.route
                        1 -> Screen.BuyerHome.route
                        else -> Screen.SupplierHome.route
                    }
                    navController.navigate(route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = { navController.navigate(Screen.Login.route) }
            )
        }

        // 采购商
        composable(Screen.BuyerHome.route) {
            BuyerHomeScreen(
                onNavigateToCreateDemand = { navController.navigate(Screen.CreateDemand.route) },
                onNavigateToDemandDetail = { demandId ->
                    navController.navigate(Screen.DemandDetail.createRoute(demandId))
                }
            )
        }

        composable(Screen.CreateDemand.route) {
            CreateDemandScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.DemandDetail.route) { backStackEntry ->
            val demandId = backStackEntry.arguments?.getString("demandId")?.toLongOrNull() ?: 0L
            DemandDetailScreen(
                demandId = demandId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 供应商
        composable(Screen.SupplierHome.route) {
            SupplierHomeScreen(
                onNavigateToGroupDetail = { groupId ->
                    navController.navigate(Screen.DemandGroupDetail.createRoute(groupId))
                },
                onNavigateToPackOrder = { demandId ->
                    navController.navigate(Screen.PackOrder.createRoute(demandId))
                }
            )
        }

        composable(Screen.DemandGroupDetail.route) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")?.toLongOrNull() ?: 0L
            DemandGroupDetailScreen(
                groupId = groupId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PackOrder.route) { backStackEntry ->
            val demandId = backStackEntry.arguments?.getString("demandId")?.toLongOrNull() ?: 0L
            PackOrderScreen(
                demandId = demandId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToShip = { navController.navigate(Screen.ShipOrder.createRoute(demandId)) }
            )
        }

        composable(Screen.ShipOrder.route) { backStackEntry ->
            val demandId = backStackEntry.arguments?.getString("demandId")?.toLongOrNull() ?: 0L
            ShipOrderScreen(
                demandId = demandId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 管理员
        composable(Screen.AdminHome.route) {
            AdminHomeScreen(
                onNavigateToUserDetail = { userId ->
                    navController.navigate(Screen.AdminUserDetail.createRoute(userId))
                },
                onNavigateToDemandDetail = { demandId ->
                    navController.navigate(Screen.AdminDemandDetail.createRoute(demandId))
                }
            )
        }

        composable(Screen.AdminUserDetail.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toLongOrNull() ?: 0L
            AdminUserDetailScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminDemandDetail.route) { backStackEntry ->
            val demandId = backStackEntry.arguments?.getString("demandId")?.toLongOrNull() ?: 0L
            AdminDemandDetailScreen(
                demandId = demandId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
