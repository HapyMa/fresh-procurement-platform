package com.fresh.procurement.dto;

import com.fresh.procurement.entity.Quote;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuoteListResponse {
    private int total;
    private List<Quote> list;
}
