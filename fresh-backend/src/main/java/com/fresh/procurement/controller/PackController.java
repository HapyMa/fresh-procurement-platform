package com.fresh.procurement.controller;

import com.fresh.procurement.dto.*;
import com.fresh.procurement.entity.Demand;
import com.fresh.procurement.entity.PackRecord;
import com.fresh.procurement.service.DemandService;
import com.fresh.procurement.service.PackService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/supplier")
public class PackController {

    private final PackService packService;
    private final DemandService demandService;

    public PackController(PackService packService, DemandService demandService) {
        this.packService = packService;
        this.demandService = demandService;
    }

    @GetMapping("/pack/pending-list")
    public ApiResponse<DemandListResponse> getPendingList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.success(packService.getPendingPackList(userId, page, size));
    }

    @PostMapping("/pack/{demandId}/start")
    public ApiResponse<PackRecord> startPacking(@PathVariable Long demandId,
                                                @AuthenticationPrincipal Long userId) {
        return ApiResponse.success(packService.startPacking(demandId, userId));
    }

    @PostMapping("/pack/{demandId}/complete")
    public ApiResponse<PackRecord> completePacking(@PathVariable Long demandId,
                                                   @RequestBody CompletePackRequest request,
                                                   @AuthenticationPrincipal Long userId) {
        return ApiResponse.success(packService.completePacking(demandId, request, userId));
    }

    @GetMapping("/pack/{demandId}/record")
    public ApiResponse<PackRecord> getRecord(@PathVariable Long demandId) {
        return ApiResponse.success(packService.getPackRecord(demandId));
    }

    @PostMapping("/ship/{demandId}")
    public ApiResponse<Void> shipOrder(@PathVariable Long demandId,
                                       @RequestBody ShipRequest request,
                                       @AuthenticationPrincipal Long userId) {
        // 权限校验：确保 supplierId 与需求的 supplierId 匹配
        Demand demand = demandService.getDemandDetail(demandId);
        if (!demand.getSupplierId().equals(userId)) {
            throw new RuntimeException("无权操作该订单");
        }
        packService.shipOrder(demandId, request, userId);
        return ApiResponse.success();
    }
}
