package com.fresh.procurement.dto;

import com.fresh.procurement.entity.DemandGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DemandGroupListResponse {
    private String city;
    private int page;
    private int size;
    private int total;
    private List<DemandGroup> demandGroups;
}
