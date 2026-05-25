package com.fresh.procurement.controller;

import com.fresh.procurement.dto.*;
import com.fresh.procurement.entity.PackRecord;
import com.fresh.procurement.service.PackService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/supplier")
public class PackController {

    private final PackService packService;

    public PackController(PackService packService) {
        this.packService = packService;
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
        packService.shipOrder(demandId, request, userId);
        return ApiResponse.success();
    }
}
