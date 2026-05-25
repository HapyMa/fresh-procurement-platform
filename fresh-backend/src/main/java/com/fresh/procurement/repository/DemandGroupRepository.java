package com.fresh.procurement.repository;

import com.fresh.procurement.entity.DemandGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DemandGroupRepository extends JpaRepository<DemandGroup, Long> {

    List<DemandGroup> findByCityAndStatusOrderByCreatedAtDesc(String city, Integer status);

    List<DemandGroup> findByStatusOrderByCreatedAtDesc(Integer status);
}
