package com.fresh.procurement.repository;

import com.fresh.procurement.entity.DemandGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DemandGroupRepository extends JpaRepository<DemandGroup, Long> {

    List<DemandGroup> findByCityAndStatusOrderByCreatedAtDesc(String city, Integer status);

    List<DemandGroup> findByStatusOrderByCreatedAtDesc(Integer status);

    // ========== 分页查询方法 ==========

    /**
     * 分页查询所有合并组（按创建时间倒序）
     */
    Page<DemandGroup> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 按城市和状态分页查询合并组
     */
    Page<DemandGroup> findByCityAndStatusOrderByCreatedAtDesc(String city, Integer status, Pageable pageable);

    /**
     * 按状态分页查询合并组
     */
    Page<DemandGroup> findByStatusOrderByCreatedAtDesc(Integer status, Pageable pageable);
}
