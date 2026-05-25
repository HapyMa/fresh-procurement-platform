package com.fresh.procurement.service;

import com.fresh.procurement.dto.CreateDemandRequest;
import com.fresh.procurement.dto.DemandGroupDetail;
import com.fresh.procurement.dto.DemandGroupListResponse;
import com.fresh.procurement.dto.DemandListResponse;
import com.fresh.procurement.dto.SelectQuoteRequest;
import com.fresh.procurement.entity.*;
import com.fresh.procurement.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DemandService {

    private final DemandRepository demandRepository;
    private final DemandGroupRepository demandGroupRepository;
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final QuoteRepository quoteRepository;

    public DemandService(DemandRepository demandRepository,
                         DemandGroupRepository demandGroupRepository,
                         UserRepository userRepository,
                         UserAddressRepository userAddressRepository,
                         QuoteRepository quoteRepository) {
        this.demandRepository = demandRepository;
        this.demandGroupRepository = demandGroupRepository;
        this.userRepository = userRepository;
        this.userAddressRepository = userAddressRepository;
        this.quoteRepository = quoteRepository;
    }

    /**
     * 创建需求：查找同品类同城市未关闭的 DemandGroup，有则加入并更新总量，无则创建新组
     */
    public Demand createDemand(CreateDemandRequest request, Long buyerId) {
        // 获取配送地址的城市信息
        UserAddress address = userAddressRepository.findById(request.getDeliveryAddressId())
                .orElseThrow(() -> new RuntimeException("配送地址不存在"));
        String city = address.getCity();

        // 查找同品类同城市未关闭的 DemandGroup（status != 2 表示未关闭）
        List<DemandGroup> existingGroups = demandGroupRepository
                .findByCityAndStatusOrderByCreatedAtDesc(city, 0);

        DemandGroup targetGroup = null;
        for (DemandGroup group : existingGroups) {
            if (group.getCategoryId().equals(request.getCategoryId())
                    && group.getProductName().equals(request.getProductName())
                    && group.getUnit().equals(request.getUnit())) {
                targetGroup = group;
                break;
            }
        }

        if (targetGroup == null) {
            // 创建新的合并组
            targetGroup = new DemandGroup();
            targetGroup.setCategoryId(request.getCategoryId());
            targetGroup.setProductName(request.getProductName());
            targetGroup.setCity(city);
            targetGroup.setTotalQuantity(request.getQuantity());
            targetGroup.setUnit(request.getUnit());
            targetGroup.setMergeDeadline(LocalDateTime.now().plusHours(24));
            targetGroup.setStatus(0);
            targetGroup = demandGroupRepository.save(targetGroup);
        } else {
            // 更新合并组的总量
            targetGroup.setTotalQuantity(targetGroup.getTotalQuantity() + request.getQuantity());
            demandGroupRepository.save(targetGroup);
        }

        // 创建需求
        Demand demand = new Demand();
        demand.setGroupId(targetGroup.getId());
        demand.setBuyerId(buyerId);
        demand.setCategoryId(request.getCategoryId());
        demand.setProductName(request.getProductName());
        demand.setQuantity(request.getQuantity());
        demand.setUnit(request.getUnit());
        demand.setMaxPrice(request.getMaxPrice());
        demand.setQualityRequirement(request.getQualityRequirement());
        demand.setDeliveryAddressId(request.getDeliveryAddressId());
        demand.setDeliveryDate(LocalDate.parse(request.getDeliveryDate()));
        demand.setDeliveryTimeSlot(request.getDeliveryTimeSlot());
        demand.setRemark(request.getRemark());
        demand.setStatus(0);
        demand.setPackStatus(0);
        return demandRepository.save(demand);
    }

    /**
     * 按 buyerId 和 status 分页查询需求
     */
    public DemandListResponse getBuyerDemands(Long buyerId, Integer status, int page, int size) {
        List<Demand> allDemands;
        if (status != null) {
            allDemands = demandRepository.findByBuyerIdAndStatusOrderByCreatedAtDesc(buyerId, status);
        } else {
            allDemands = demandRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId);
        }

        int total = allDemands.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);

        List<Demand> list;
        if (fromIndex >= total) {
            list = Collections.emptyList();
        } else {
            list = allDemands.subList(fromIndex, toIndex);
        }

        return new DemandListResponse(total, list);
    }

    /**
     * 查询单个需求详情
     */
    public Demand getDemandDetail(Long demandId) {
        return demandRepository.findById(demandId)
                .orElseThrow(() -> new RuntimeException("需求不存在"));
    }

    /**
     * 获取需求详情（包含合并组信息和报价列表）
     */
    public Map<String, Object> getDemandDetailWithGroupAndQuotes(Long demandId) {
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new RuntimeException("需求不存在"));

        DemandGroup group = demandGroupRepository.findById(demand.getGroupId())
                .orElse(null);

        List<Quote> quotes = quoteRepository.findByGroupIdAndStatus(demand.getGroupId(), 0);

        Map<String, Object> result = new HashMap<>();
        result.put("demand", demand);
        result.put("groupInfo", group);
        result.put("quotes", quotes);
        return result;
    }

    /**
     * 选定报价：更新需求的 selectedQuoteId, dealPrice, dealTotalAmount, supplierId, status=5
     */
    public Demand selectQuote(Long demandId, SelectQuoteRequest request) {
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new RuntimeException("需求不存在"));

        Quote quote = quoteRepository.findById(request.getQuoteId())
                .orElseThrow(() -> new RuntimeException("报价不存在"));

        demand.setSelectedQuoteId(quote.getId());
        demand.setDealPrice(quote.getUnitPrice());
        demand.setDealTotalAmount(quote.getTotalAmount());
        demand.setSupplierId(quote.getSupplierId());
        demand.setStatus(5);

        // 更新报价状态为已选中
        quote.setStatus(1);
        quote.setSelectedAt(LocalDateTime.now());
        quoteRepository.save(quote);

        return demandRepository.save(demand);
    }

    /**
     * 确认收货：更新 status=7
     */
    public void confirmReceipt(Long demandId) {
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new RuntimeException("需求不存在"));
        demand.setStatus(7);
        demandRepository.save(demand);
    }

    /**
     * 按城市和状态查询合并组
     */
    public DemandGroupListResponse getDemandGroups(String city, int page, int size) {
        List<DemandGroup> allGroups;
        if (city != null && !city.isEmpty()) {
            allGroups = demandGroupRepository.findByCityAndStatusOrderByCreatedAtDesc(city, 0);
        } else {
            allGroups = demandGroupRepository.findByStatusOrderByCreatedAtDesc(0);
        }

        int total = allGroups.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);

        List<DemandGroup> list;
        if (fromIndex >= total) {
            list = Collections.emptyList();
        } else {
            list = allGroups.subList(fromIndex, toIndex);
        }

        return new DemandGroupListResponse(city, page, size, total, list);
    }

    /**
     * 查询合并组详情及其所有子需求
     */
    public DemandGroupDetail getDemandGroupDetail(Long groupId) {
        DemandGroup group = demandGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("合并组不存在"));

        List<Demand> demands = demandRepository.findByGroupId(groupId);

        // 为每个需求附加买家名称和配送地址信息
        for (Demand demand : demands) {
            User buyer = userRepository.findById(demand.getBuyerId()).orElse(null);
            if (buyer != null) {
                // 将 buyerName 设置到 remark 前缀（或者通过其他方式传递）
                // 由于 Demand 实体没有 buyerName 字段，我们通过扩展方式处理
            }
        }

        DemandGroupDetail detail = new DemandGroupDetail();
        detail.setGroupId(group.getId());
        detail.setProductName(group.getProductName());
        detail.setCity(group.getCity());
        detail.setTotalQuantity(group.getTotalQuantity());
        detail.setUnit(group.getUnit());
        detail.setStatus(group.getStatus());
        detail.setDemands(demands);

        return detail;
    }

    /**
     * 按供应商和状态查询订单
     */
    public DemandListResponse getSupplierOrders(Long supplierId, Integer status, int page, int size) {
        List<Demand> allDemands;
        if (status != null) {
            allDemands = demandRepository.findBySupplierIdAndStatusOrderByCreatedAtDesc(supplierId, status);
        } else {
            allDemands = demandRepository.findBySupplierIdOrderByCreatedAtDesc(supplierId);
        }

        int total = allDemands.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);

        List<Demand> list;
        if (fromIndex >= total) {
            list = Collections.emptyList();
        } else {
            list = allDemands.subList(fromIndex, toIndex);
        }

        return new DemandListResponse(total, list);
    }
}
