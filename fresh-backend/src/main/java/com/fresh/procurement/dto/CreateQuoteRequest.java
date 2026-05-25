package com.fresh.procurement.dto;

import lombok.Data;

@Data
public class CreateQuoteRequest {
    private Double unitPrice;
    private int validHours = 24;
    private String remark;
}
