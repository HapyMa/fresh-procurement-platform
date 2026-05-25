package com.fresh.procurement.service;

import com.fresh.procurement.dto.CreateQuoteRequest;
import com.fresh.procurement.dto.QuoteListResponse;
import com.fresh.procurement.entity.DemandGroup;
import com.fresh.procurement.entity.Quote;
import com.fresh.procurement.repository.DemandGroupRepository;
import com.fresh.procurement.repository.QuoteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final DemandGroupRepository demandGroupRepository;

    public QuoteService(QuoteRepository quoteRepository,
                        DemandGroupRepository demandGroupRepository) {
        this.quoteRepository = quoteRepository;
        this.demandGroupRepository = demandGroupRepository;
    }

    /**
     * 创建报价：计算 totalAmount = unitPrice * group.totalQuantity，设置 expireAt
     */
    public Quote createQuote(Long groupId, CreateQuoteRequest request, Long supplierId) {
        DemandGroup group = demandGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("合并组不存在"));

        Quote quote = new Quote();
        quote.setGroupId(groupId);
        quote.setSupplierId(supplierId);
        quote.setUnitPrice(request.getUnitPrice());
        quote.setTotalAmount(request.getUnitPrice() * group.getTotalQuantity());
        quote.setValidHours(request.getValidHours());
        quote.setExpireAt(LocalDateTime.now().plusHours(request.getValidHours()));
        quote.setRemark(request.getRemark());
        quote.setStatus(0);

        return quoteRepository.save(quote);
    }

    /**
     * 查询某合并组的所有有效报价
     */
    public List<Quote> getGroupQuotes(Long groupId) {
        return quoteRepository.findByGroupIdAndStatus(groupId, 0);
    }

    /**
     * 按供应商和状态查询报价
     */
    public QuoteListResponse getMyQuotes(Long supplierId, Integer status, int page, int size) {
        List<Quote> allQuotes;
        if (status != null) {
            allQuotes = quoteRepository.findBySupplierIdAndStatusOrderByCreatedAtDesc(supplierId, status);
        } else {
            allQuotes = quoteRepository.findBySupplierIdOrderByCreatedAtDesc(supplierId);
        }

        int total = allQuotes.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);

        List<Quote> list;
        if (fromIndex >= total) {
            list = Collections.emptyList();
        } else {
            list = allQuotes.subList(fromIndex, toIndex);
        }

        return new QuoteListResponse(total, list);
    }
}
