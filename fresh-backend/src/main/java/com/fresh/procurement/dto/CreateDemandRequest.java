package com.fresh.procurement.dto;

import lombok.Data;

@Data
public class CreateDemandRequest {
    private Long categoryId;
    private String productName;
    private Double quantity;
    private String unit;
    private Double maxPrice;
    private String qualityRequirement;
    private Long deliveryAddressId;
    private String deliveryDate;
    private String deliveryTimeSlot;
    private String remark;
}
