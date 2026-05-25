package com.fresh.procurement.repository;

import com.fresh.procurement.entity.PackRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PackRecordRepository extends JpaRepository<PackRecord, Long> {

    Optional<PackRecord> findByDemandId(Long demandId);
}
