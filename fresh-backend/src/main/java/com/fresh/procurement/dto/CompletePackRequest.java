package com.fresh.procurement.dto;

import lombok.Data;

@Data
public class CompletePackRequest {
    private Double actualQuantity;
    private Double actualWeight;
    private String grade;
    private int qualityCheck;
    private int packageCount;
    private String packageType;
    private String labelCode;
    private String remark;
}
