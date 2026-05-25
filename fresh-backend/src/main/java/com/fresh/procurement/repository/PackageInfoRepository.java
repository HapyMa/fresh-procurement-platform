package com.fresh.procurement.repository;

import com.fresh.procurement.entity.PackageInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PackageInfoRepository extends JpaRepository<PackageInfo, Long> {

    List<PackageInfo> findByDemandId(Long demandId);

    List<PackageInfo> findByPackRecordId(Long packRecordId);
}
