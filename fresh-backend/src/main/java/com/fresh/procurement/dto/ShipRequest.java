package com.fresh.procurement.dto;

import lombok.Data;

import java.util.List;

@Data
public class ShipRequest {
    private List<Long> packageIds;
    private int logisticsType;
    private String logisticsCompany;
    private String trackingNo;
    private String estimatedArrival;
}
