package com.fresh.procurement.controller;

import com.fresh.procurement.dto.*;
import com.fresh.procurement.entity.Demand;
import com.fresh.procurement.service.DemandService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/buyer")
public class BuyerController {

    private final DemandService demandService;

    public BuyerController(DemandService demandService) {
        this.demandService = demandService;
    }

    @PostMapping("/demands")
    public ApiResponse<Demand> createDemand(@RequestBody CreateDemandRequest request,
                                            @AuthenticationPrincipal Long userId) {
        return ApiResponse.success(demandService.createDemand(request, userId));
    }

    @GetMapping("/demands")
    public ApiResponse<DemandListResponse> getDemands(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status,
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.success(demandService.getBuyerDemands(userId, status, page, size));
    }

    @GetMapping("/demands/{demandId}")
    public ApiResponse<Map<String, Object>> getDetail(@PathVariable Long demandId) {
        return ApiResponse.success(demandService.getDemandDetailWithGroupAndQuotes(demandId));
    }

    @PostMapping("/demands/{demandId}/select-quote")
    public ApiResponse<Demand> selectQuote(@PathVariable Long demandId,
                                           @RequestBody SelectQuoteRequest request) {
        return ApiResponse.success(demandService.selectQuote(demandId, request));
    }

    @PostMapping("/demands/{demandId}/confirm-receipt")
    public ApiResponse<Void> confirmReceipt(@PathVariable Long demandId,
                                            @RequestBody Map<String, Object> body) {
        demandService.confirmReceipt(demandId);
        return ApiResponse.success();
    }
}
