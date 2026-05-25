package com.fresh.procurement.repository;

import com.fresh.procurement.entity.Demand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DemandRepository extends JpaRepository<Demand, Long> {

    List<Demand> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    List<Demand> findByBuyerIdAndStatusOrderByCreatedAtDesc(Long buyerId, Integer status);

    List<Demand> findByGroupId(Long groupId);

    List<Demand> findBySupplierIdAndStatusOrderByCreatedAtDesc(Long supplierId, Integer status);

    List<Demand> findBySupplierIdOrderByCreatedAtDesc(Long supplierId);

    Long countByStatus(Integer status);

    List<Demand> findAllByOrderByCreatedAtDesc();

    List<Demand> findByStatusOrderByCreatedAtDesc(Integer status);

    @Query("SELECT COALESCE(SUM(d.dealTotalAmount), 0) FROM Demand d WHERE d.status >= ?1")
    Double sumDealTotalAmountByStatusGreaterThanEqual(Integer status);

    @Override
    long count();
}
