package com.fresh.procurement.dto;

import com.fresh.procurement.entity.Demand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DemandListResponse {
    private int total;
    private List<Demand> list;
}
