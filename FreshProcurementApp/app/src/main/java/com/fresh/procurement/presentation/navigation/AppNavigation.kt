package com.fresh.procurement.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fresh.procurement.presentation.auth.LoginScreen
import com.fresh.procurement.presentation.auth.RegisterScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // Auth
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

        // Buyer
        composable(Screen.BuyerHome.route) {
            // TODO: 实现 BuyerHomeScreen
        }

        composable(Screen.CreateDemand.route) {
            // TODO: 实现 CreateDemandScreen
        }

        composable(Screen.DemandDetail.route) { backStackEntry ->
            val demandId = backStackEntry.arguments?.getString("demandId")?.toLongOrNull() ?: 0L
            // TODO: 实现 DemandDetailScreen
        }

        // Supplier
        composable(Screen.SupplierHome.route) {
            // TODO: 实现 SupplierHomeScreen
        }

        composable(Screen.DemandGroupDetail.route) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")?.toLongOrNull() ?: 0L
            // TODO: 实现 DemandGroupDetailScreen
        }

        composable(Screen.PackOrder.route) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")?.toLongOrNull() ?: 0L
            // TODO: 实现 PackOrderScreen
        }

        composable(Screen.ShipOrder.route) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")?.toLongOrNull() ?: 0L
            // TODO: 实现 ShipOrderScreen
        }

        // Admin
        composable(Screen.AdminHome.route) {
            // TODO: 实现 AdminHomeScreen
        }

        composable(Screen.AdminUsers.route) {
            // TODO: 实现 AdminUsersScreen
        }

        composable(Screen.AdminUserDetail.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toLongOrNull() ?: 0L
            // TODO: 实现 AdminUserDetailScreen
        }

        composable(Screen.AdminDemands.route) {
            // TODO: 实现 AdminDemandsScreen
        }

        composable(Screen.AdminDemandDetail.route) { backStackEntry ->
            val demandId = backStackEntry.arguments?.getString("demandId")?.toLongOrNull() ?: 0L
            // TODO: 实现 AdminDemandDetailScreen
        }
    }
}
