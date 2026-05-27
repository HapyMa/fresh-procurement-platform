package com.fresh.procurement.presentation.navigation

sealed class Screen(val route: String) {
    // Auth
    object Login : Screen("login")
    object Register : Screen("register")

    // Buyer
    object BuyerHome : Screen("buyer_home")
    object CreateDemand : Screen("create_demand")
    object DemandDetail : Screen("demand_detail/{demandId}") {
        fun createRoute(demandId: Long) = "demand_detail/$demandId"
    }

    // Supplier
    object SupplierHome : Screen("supplier_home")
    object DemandGroupDetail : Screen("demand_group_detail/{groupId}") {
        fun createRoute(groupId: Long) = "demand_group_detail/$groupId"
    }
    object PackOrder : Screen("pack_order/{groupId}") {
        fun createRoute(groupId: Long) = "pack_order/$groupId"
    }
    object ShipOrder : Screen("ship_order/{groupId}") {
        fun createRoute(groupId: Long) = "ship_order/$groupId"
    }

    // Admin
    object AdminHome : Screen("admin_home")
    object AdminUsers : Screen("admin_users")
    object AdminUserDetail : Screen("admin_user_detail/{userId}") {
        fun createRoute(userId: Long) = "admin_user_detail/$userId"
    }
    object AdminDemands : Screen("admin_demands")
    object AdminDemandDetail : Screen("admin_demand_detail/{demandId}") {
        fun createRoute(demandId: Long) = "admin_demand_detail/$demandId"
    }
}
