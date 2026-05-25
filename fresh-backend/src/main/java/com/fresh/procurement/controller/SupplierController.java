package com.fresh.procurement.controller;

import com.fresh.procurement.dto.*;
import com.fresh.procurement.entity.Quote;
import com.fresh.procurement.service.DemandService;
import com.fresh.procurement.service.QuoteService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/supplier")
public class SupplierController {

    private final DemandService demandService;
    private final QuoteService quoteService;

    public SupplierController(DemandService demandService,
                              QuoteService quoteService) {
        this.demandService = demandService;
        this.quoteService = quoteService;
    }

    @GetMapping("/demand-groups")
    public ApiResponse<DemandGroupListResponse> getDemandGroups(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String city) {
        return ApiResponse.success(demandService.getDemandGroups(city, page, size));
    }

    @GetMapping("/demand-groups/{groupId}")
    public ApiResponse<DemandGroupDetail> getGroupDetail(@PathVariable Long groupId) {
        return ApiResponse.success(demandService.getDemandGroupDetail(groupId));
    }

    @PostMapping("/demand-groups/{groupId}/quotes")
    public ApiResponse<Quote> createQuote(@PathVariable Long groupId,
                                          @RequestBody CreateQuoteRequest request,
                                          @AuthenticationPrincipal Long userId) {
        return ApiResponse.success(quoteService.createQuote(groupId, request, userId));
    }

    @GetMapping("/quotes")
    public ApiResponse<QuoteListResponse> getMyQuotes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status,
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.success(quoteService.getMyQuotes(userId, status, page, size));
    }

    @GetMapping("/orders")
    public ApiResponse<DemandListResponse> getOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status,
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.success(demandService.getSupplierOrders(userId, status, page, size));
    }
}
