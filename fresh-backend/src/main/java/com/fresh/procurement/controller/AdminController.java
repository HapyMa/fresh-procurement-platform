package com.fresh.procurement.controller;

import com.fresh.procurement.dto.AdminDTOs.*;
import com.fresh.procurement.dto.ApiResponse;
import com.fresh.procurement.entity.Demand;
import com.fresh.procurement.entity.DemandGroup;
import com.fresh.procurement.entity.User;
import com.fresh.procurement.service.AdminService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardResponse> getDashboard(@AuthenticationPrincipal Long adminId) {
        return ApiResponse.success(adminService.getDashboard());
    }

    @GetMapping("/users")
    public ApiResponse<AdminUserListResponse> getUsers(
            @RequestParam(required = false) Integer userType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Long adminId) {
        return ApiResponse.success(adminService.getUsers(userType, status, page, size));
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<User> getUserDetail(@PathVariable Long userId,
                                           @AuthenticationPrincipal Long adminId) {
        return ApiResponse.success(adminService.getUserDetail(userId));
    }

    @PutMapping("/users/{userId}/toggle-status")
    public ApiResponse<User> toggleUserStatus(@PathVariable Long userId,
                                              @AuthenticationPrincipal Long adminId) {
        return ApiResponse.success(adminService.toggleUserStatus(userId));
    }

    @GetMapping("/demands")
    public ApiResponse<AdminDemandListResponse> getDemands(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Long adminId) {
        return ApiResponse.success(adminService.getDemands(status, page, size));
    }

    @GetMapping("/demands/{demandId}")
    public ApiResponse<Demand> getDemandDetail(@PathVariable Long demandId,
                                               @AuthenticationPrincipal Long adminId) {
        return ApiResponse.success(adminService.getDemandDetail(demandId));
    }

    @PutMapping("/demands/{demandId}/cancel")
    public ApiResponse<Demand> cancelDemand(@PathVariable Long demandId,
                                            @AuthenticationPrincipal Long adminId) {
        return ApiResponse.success(adminService.cancelDemand(demandId));
    }

    @GetMapping("/quotes")
    public ApiResponse<AdminQuoteListResponse> getQuotes(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Long adminId) {
        return ApiResponse.success(adminService.getQuotes(status, page, size));
    }

    @GetMapping("/order-stats")
    public ApiResponse<AdminOrderStatsResponse> getOrderStats(@AuthenticationPrincipal Long adminId) {
        return ApiResponse.success(adminService.getOrderStats());
    }

    @GetMapping("/demand-groups")
    public ApiResponse<java.util.List<DemandGroup>> getDemandGroups(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Long adminId) {
        return ApiResponse.success(adminService.getDemandGroups(page, size));
    }
}
