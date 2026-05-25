package com.fresh.procurement.repository;

import com.fresh.procurement.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    List<Quote> findByGroupId(Long groupId);

    List<Quote> findByGroupIdAndStatus(Long groupId, Integer status);

    List<Quote> findBySupplierIdAndStatusOrderByCreatedAtDesc(Long supplierId, Integer status);

    List<Quote> findBySupplierIdOrderByCreatedAtDesc(Long supplierId);
}
