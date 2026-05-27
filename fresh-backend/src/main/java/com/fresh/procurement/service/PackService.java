package com.fresh.procurement.service;

import com.fresh.procurement.dto.CompletePackRequest;
import com.fresh.procurement.dto.DemandListResponse;
import com.fresh.procurement.dto.ShipRequest;
import com.fresh.procurement.entity.Demand;
import com.fresh.procurement.entity.PackRecord;
import com.fresh.procurement.entity.PackageInfo;
import com.fresh.procurement.repository.DemandRepository;
import com.fresh.procurement.repository.PackageInfoRepository;
import com.fresh.procurement.repository.PackRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class PackService {

    private final DemandRepository demandRepository;
    private final PackRecordRepository packRecordRepository;
    private final PackageInfoRepository packageInfoRepository;

    public PackService(DemandRepository demandRepository,
                       PackRecordRepository packRecordRepository,
                       PackageInfoRepository packageInfoRepository) {
        this.demandRepository = demandRepository;
        this.packRecordRepository = packRecordRepository;
        this.packageInfoRepository = packageInfoRepository;
    }

    /**
     * 查询 supplierId 匹配且 status=5, packStatus=0 的需求（待打包列表）
     */
    public DemandListResponse getPendingPackList(Long supplierId, int page, int size) {
        // 查询供应商所有已选报价(status=5)的需求，然后过滤 packStatus=0
        List<Demand> allDemands = demandRepository.findBySupplierIdAndStatusOrderByCreatedAtDesc(supplierId, 5);
        List<Demand> pendingDemands = allDemands.stream()
                .filter(d -> d.getPackStatus() != null && d.getPackStatus() == 0)
                .toList();

        int total = pendingDemands.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);

        List<Demand> list;
        if (fromIndex >= total) {
            list = Collections.emptyList();
        } else {
            list = pendingDemands.subList(fromIndex, toIndex);
        }

        return new DemandListResponse(total, list);
    }

    /**
     * 开始打包：创建 PackRecord，更新 demand.packStatus=1
     */
    public PackRecord startPacking(Long demandId, Long supplierId) {
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new RuntimeException("需求不存在"));

        // 权限校验：确保 supplierId 与需求的 supplierId 匹配
        if (demand.getSupplierId() == null || !demand.getSupplierId().equals(supplierId)) {
            throw new RuntimeException("无权操作该需求");
        }

        // 检查是否已有打包记录
        if (packRecordRepository.findByDemandId(demandId).isPresent()) {
            throw new RuntimeException("打包记录已存在");
        }

        // 创建打包记录
        PackRecord packRecord = new PackRecord();
        packRecord.setDemandId(demandId);
        packRecord.setSupplierId(supplierId);
        packRecord.setPlannedQuantity(demand.getQuantity());
        packRecord.setStatus(0); // 打包中
        packRecord = packRecordRepository.save(packRecord);

        // 更新需求打包状态
        demand.setPackStatus(1);
        demandRepository.save(demand);

        return packRecord;
    }

    /**
     * 完成打包：更新 PackRecord，创建 PackageInfo 记录，更新 demand.packStatus=2
     */
    public PackRecord completePacking(Long demandId, CompletePackRequest request, Long supplierId) {
        PackRecord packRecord = packRecordRepository.findByDemandId(demandId)
                .orElseThrow(() -> new RuntimeException("打包记录不存在"));

        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new RuntimeException("需求不存在"));

        // 权限校验：确保 supplierId 与需求的 supplierId 匹配
        if (demand.getSupplierId() == null || !demand.getSupplierId().equals(supplierId)) {
            throw new RuntimeException("无权操作该需求");
        }

        // 更新打包记录
        packRecord.setActualQuantity(request.getActualQuantity());
        packRecord.setActualWeight(request.getActualWeight());
        
        // 修复偏差计算逻辑：计算实际重量与需求重量的偏差
        if (request.getActualWeight() != null && demand.getQuantity() != null) {
            packRecord.setWeightDeviation(request.getActualWeight() - demand.getQuantity());
        }
        
        packRecord.setGrade(request.getGrade());
        packRecord.setQualityCheck(request.getQualityCheck());
        packRecord.setPackageCount(request.getPackageCount());
        packRecord.setPackageType(request.getPackageType());
        packRecord.setLabelCode(request.getLabelCode());
        packRecord.setStatus(1); // 打包完成
        packRecord.setPackedAt(LocalDateTime.now());
        packRecord = packRecordRepository.save(packRecord);

        // 创建 PackageInfo 记录
        // 添加除零校验
        int packageCount = request.getPackageCount() != null ? request.getPackageCount() : 0;
        if (packageCount <= 0) {
            throw new RuntimeException("包裹数量必须大于0");
        }
        
        double weightPerPackage = 0.0;
        if (request.getActualWeight() != null && packageCount > 0) {
            weightPerPackage = request.getActualWeight() / packageCount;
        }
        
        for (int i = 0; i < packageCount; i++) {
            PackageInfo packageInfo = new PackageInfo();
            packageInfo.setPackRecordId(packRecord.getId());
            packageInfo.setDemandId(demandId);
            packageInfo.setPackageNo(UUID.randomUUID().toString().substring(0, 12).toUpperCase());
            packageInfo.setWeight(weightPerPackage);
            packageInfo.setItemsCount(1);
            packageInfo.setLabelCode(request.getLabelCode());
            packageInfo.setStatus(0); // 待发货
            packageInfoRepository.save(packageInfo);
        }

        // 更新需求打包状态
        demand.setPackStatus(2);
        demand.setActualWeight(request.getActualWeight());
        demand.setPackRemark(request.getRemark());
        demandRepository.save(demand);

        return packRecord;
    }

    /**
     * 查询打包记录
     */
    public PackRecord getPackRecord(Long demandId) {
        return packRecordRepository.findByDemandId(demandId)
                .orElseThrow(() -> new RuntimeException("打包记录不存在"));
    }

    /**
     * 发货：更新 demand.status=6, packStatus=3，更新 package 状态
     */
    public void shipOrder(Long demandId, ShipRequest request, Long supplierId) {
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new RuntimeException("需求不存在"));

        // 更新包裹状态
        if (request.getPackageIds() != null) {
            for (Long packageId : request.getPackageIds()) {
                PackageInfo packageInfo = packageInfoRepository.findById(packageId)
                        .orElseThrow(() -> new RuntimeException("包裹不存在: " + packageId));
                packageInfo.setStatus(1); // 已发货
                packageInfo.setLogisticsCompany(request.getLogisticsCompany());
                packageInfo.setTrackingNo(request.getTrackingNo());
                packageInfo.setShippedAt(LocalDateTime.now());
                packageInfoRepository.save(packageInfo);
            }
        }

        // 更新需求状态
        demand.setStatus(6); // 已发货
        demand.setPackStatus(3); // 已发货
        demandRepository.save(demand);
    }
}
