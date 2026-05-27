package com.fresh.procurement.repository;

import com.fresh.procurement.entity.Quote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    List<Quote> findByGroupId(Long groupId);

    List<Quote> findByGroupIdAndStatus(Long groupId, Integer status);

    List<Quote> findBySupplierIdAndStatusOrderByCreatedAtDesc(Long supplierId, Integer status);

    List<Quote> findBySupplierIdOrderByCreatedAtDesc(Long supplierId);

    @Override
    long count();

    List<Quote> findAllByOrderByCreatedAtDesc();

    // ========== 分页查询方法 ==========

    /**
     * 按供应商ID分页查询报价
     */
    Page<Quote> findBySupplierIdOrderByCreatedAtDesc(Long supplierId, Pageable pageable);

    /**
     * 按供应商ID和状态分页查询报价
     */
    Page<Quote> findBySupplierIdAndStatusOrderByCreatedAtDesc(Long supplierId, Integer status, Pageable pageable);

    /**
     * 分页查询所有报价（按创建时间倒序）
     */
    Page<Quote> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 按状态分页查询报价
     */
    Page<Quote> findByStatusOrderByCreatedAtDesc(Integer status, Pageable pageable);

    // ========== JOIN 查询方法（N+1 优化）==========

    /**
     * 带 JOIN 的分页查询报价列表（用于管理后台）
     */
    @Query("SELECT q FROM Quote q " +
           "LEFT JOIN FETCH User u ON u.id = q.supplierId " +
           "LEFT JOIN FETCH DemandGroup dg ON dg.id = q.groupId " +
           "ORDER BY q.createdAt DESC")
    List<Quote> findAllWithJoin();

    @Query("SELECT q FROM Quote q " +
           "LEFT JOIN FETCH User u ON u.id = q.supplierId " +
           "LEFT JOIN FETCH DemandGroup dg ON dg.id = q.groupId " +
           "WHERE q.status = :status " +
           "ORDER BY q.createdAt DESC")
    List<Quote> findAllWithJoinByStatus(@Param("status") Integer status);
}
