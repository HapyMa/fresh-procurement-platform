package com.fresh.procurement.repository;

import com.fresh.procurement.entity.Demand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // ========== 分页查询方法 ==========

    /**
     * 按买家ID分页查询需求
     */
    Page<Demand> findByBuyerIdOrderByCreatedAtDesc(Long buyerId, Pageable pageable);

    /**
     * 按买家ID和状态分页查询需求
     */
    Page<Demand> findByBuyerIdAndStatusOrderByCreatedAtDesc(Long buyerId, Integer status, Pageable pageable);

    /**
     * 按供应商ID分页查询需求
     */
    Page<Demand> findBySupplierIdOrderByCreatedAtDesc(Long supplierId, Pageable pageable);

    /**
     * 按供应商ID和状态分页查询需求
     */
    Page<Demand> findBySupplierIdAndStatusOrderByCreatedAtDesc(Long supplierId, Integer status, Pageable pageable);

    /**
     * 分页查询所有需求（按创建时间倒序）
     */
    Page<Demand> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 按状态分页查询需求（按创建时间倒序）
     */
    Page<Demand> findByStatusOrderByCreatedAtDesc(Integer status, Pageable pageable);

    /**
     * 按供应商ID和状态和打包状态分页查询需求
     */
    @Query("SELECT d FROM Demand d WHERE d.supplierId = :supplierId AND d.status = :status AND d.packStatus = :packStatus ORDER BY d.createdAt DESC")
    Page<Demand> findBySupplierIdAndStatusAndPackStatusOrderByCreatedAtDesc(
            @Param("supplierId") Long supplierId,
            @Param("status") Integer status,
            @Param("packStatus") Integer packStatus,
            Pageable pageable);

    /**
     * 统计供应商ID和状态和打包状态的需求数量
     */
    @Query("SELECT COUNT(d) FROM Demand d WHERE d.supplierId = :supplierId AND d.status = :status AND d.packStatus = :packStatus")
    long countBySupplierIdAndStatusAndPackStatus(
            @Param("supplierId") Long supplierId,
            @Param("status") Integer status,
            @Param("packStatus") Integer packStatus);

    // ========== JOIN 查询方法（N+1 优化）==========

    /**
     * 带 JOIN 的分页查询需求列表（用于管理后台）
     */
    @Query("SELECT d FROM Demand d " +
           "LEFT JOIN FETCH User u1 ON u1.id = d.buyerId " +
           "LEFT JOIN FETCH User u2 ON u2.id = d.supplierId " +
           "LEFT JOIN FETCH Category c ON c.id = d.categoryId " +
           "LEFT JOIN FETCH UserAddress a ON a.id = d.deliveryAddressId " +
           "WHERE d.status = :status " +
           "ORDER BY d.createdAt DESC")
    List<Demand> findAllWithJoinByStatus(@Param("status") Integer status);

    @Query("SELECT d FROM Demand d " +
           "LEFT JOIN FETCH User u1 ON u1.id = d.buyerId " +
           "LEFT JOIN FETCH User u2 ON u2.id = d.supplierId " +
           "LEFT JOIN FETCH Category c ON c.id = d.categoryId " +
           "LEFT JOIN FETCH UserAddress a ON a.id = d.deliveryAddressId " +
           "ORDER BY d.createdAt DESC")
    List<Demand> findAllWithJoin();

    /**
     * 统计指定状态的需求数量（用于分页计算总数）
     */
    long countByStatus(Integer status);
}
