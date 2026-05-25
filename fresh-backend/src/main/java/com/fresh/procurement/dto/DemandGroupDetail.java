package com.fresh.procurement.dto;

import com.fresh.procurement.entity.Demand;
import lombok.Data;

import java.util.List;

@Data
public class DemandGroupDetail {
    private Long groupId;
    private String productName;
    private String city;
    private Double totalQuantity;
    private String unit;
    private int status;
    private List<Demand> demands;
}
