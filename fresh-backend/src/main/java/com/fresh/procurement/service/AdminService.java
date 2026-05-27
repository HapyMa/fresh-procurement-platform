package com.fresh.procurement.service;

import com.fresh.procurement.dto.AdminDTOs.*;
import com.fresh.procurement.entity.*;
import com.fresh.procurement.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final DemandRepository demandRepository;
    private final DemandGroupRepository demandGroupRepository;
    private final QuoteRepository quoteRepository;
    private final CategoryRepository categoryRepository;
    private final UserAddressRepository userAddressRepository;

    public AdminService(UserRepository userRepository,
                        DemandRepository demandRepository,
                        DemandGroupRepository demandGroupRepository,
                        QuoteRepository quoteRepository,
                        CategoryRepository categoryRepository,
                        UserAddressRepository userAddressRepository) {
        this.userRepository = userRepository;
        this.demandRepository = demandRepository;
        this.demandGroupRepository = demandGroupRepository;
        this.quoteRepository = quoteRepository;
        this.categoryRepository = categoryRepository;
        this.userAddressRepository = userAddressRepository;
    }

    /**
     * 获取管理后台仪表盘数据
     */
    public AdminDashboardResponse getDashboard() {
        AdminDashboardResponse response = new AdminDashboardResponse();
        response.setTotalUsers(userRepository.count());
        response.setTotalBuyers(userRepository.countByUserType(1));
        response.setTotalSuppliers(userRepository.countByUserType(2));
        response.setTotalDemands(demandRepository.count());
        response.setTotalOrders(demandRepository.countByStatus(5) + demandRepository.countByStatus(6) + demandRepository.countByStatus(7));
        response.setTotalQuotes(quoteRepository.count());
        Double totalAmount = demandRepository.sumDealTotalAmountByStatusGreaterThanEqual(5);
        response.setTotalAmount(totalAmount != null ? totalAmount : 0.0);
        response.setPendingDemands(demandRepository.countByStatus(0));
        response.setRecentUsers(userRepository.findTop10ByOrderByCreatedAtDesc());
        return response;
    }

    /**
     * 分页查询用户列表（数据库分页）
     */
    public AdminUserListResponse getUsers(Integer userType, Integer status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<User> userPage;

        if (userType != null && status != null) {
            userPage = userRepository.findByUserTypeAndStatusOrderByCreatedAtDesc(userType, status, pageable);
        } else if (userType != null) {
            userPage = userRepository.findByUserTypeOrderByCreatedAtDesc(userType, pageable);
        } else if (status != null) {
            userPage = userRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            userPage = userRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return new AdminUserListResponse((int) userPage.getTotalElements(), userPage.getContent());
    }

    /**
     * 获取用户详情
     */
    public User getUserDetail(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * 切换用户状态（启用/禁用）
     */
    public User toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        return userRepository.save(user);
    }

    /**
     * 分页查询需求列表（数据库分页 + JOIN 查询优化）
     */
    public AdminDemandListResponse getDemands(Integer status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Demand> demandPage;

        if (status != null) {
            demandPage = demandRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            demandPage = demandRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        // 批量获取关联数据，避免 N+1 查询
        Set<Long> buyerIds = demandPage.getContent().stream()
                .map(Demand::getBuyerId)
                .collect(Collectors.toSet());
        Set<Long> supplierIds = demandPage.getContent().stream()
                .map(Demand::getSupplierId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> categoryIds = demandPage.getContent().stream()
                .map(Demand::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> addressIds = demandPage.getContent().stream()
                .map(Demand::getDeliveryAddressId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 批量查询关联数据
        Map<Long, User> userMap = new HashMap<>();
        userRepository.findAllById(buyerIds).forEach(u -> userMap.put(u.getId(), u));
        userRepository.findAllById(supplierIds).forEach(u -> userMap.put(u.getId(), u));

        Map<Long, Category> categoryMap = new HashMap<>();
        categoryRepository.findAllById(categoryIds).forEach(c -> categoryMap.put(c.getId(), c));

        Map<Long, UserAddress> addressMap = new HashMap<>();
        userAddressRepository.findAllById(addressIds).forEach(a -> addressMap.put(a.getId(), a));

        // 转换为 AdminDemandItem
        List<AdminDemandItem> items = demandPage.getContent().stream()
                .map(demand -> enrichDemandItem(demand, userMap, categoryMap, addressMap))
                .collect(Collectors.toList());

        return new AdminDemandListResponse((int) demandPage.getTotalElements(), items);
    }

    /**
     * 获取需求详情
     */
    public Demand getDemandDetail(Long demandId) {
        return demandRepository.findById(demandId)
                .orElseThrow(() -> new RuntimeException("需求不存在"));
    }

    /**
     * 取消需求（设置 status=9）
     */
    public Demand cancelDemand(Long demandId) {
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new RuntimeException("需求不存在"));
        demand.setStatus(9);
        return demandRepository.save(demand);
    }

    /**
     * 分页查询报价列表（数据库分页 + JOIN 查询优化）
     */
    public AdminQuoteListResponse getQuotes(Integer status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Quote> quotePage;

        if (status != null) {
            quotePage = quoteRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            quotePage = quoteRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        // 批量获取关联数据，避免 N+1 查询
        Set<Long> supplierIds = quotePage.getContent().stream()
                .map(Quote::getSupplierId)
                .collect(Collectors.toSet());
        Set<Long> groupIds = quotePage.getContent().stream()
                .map(Quote::getGroupId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 批量查询关联数据
        Map<Long, User> userMap = new HashMap<>();
        userRepository.findAllById(supplierIds).forEach(u -> userMap.put(u.getId(), u));

        Map<Long, DemandGroup> groupMap = new HashMap<>();
        demandGroupRepository.findAllById(groupIds).forEach(g -> groupMap.put(g.getId(), g));

        // 转换为 AdminQuoteItem
        List<AdminQuoteItem> items = quotePage.getContent().stream()
                .map(quote -> enrichQuoteItem(quote, userMap, groupMap))
                .collect(Collectors.toList());

        return new AdminQuoteListResponse((int) quotePage.getTotalElements(), items);
    }

    /**
     * 获取订单状态分布统计
     */
    public AdminOrderStatsResponse getOrderStats() {
        List<Demand> allDemands = demandRepository.findAll();
        Map<String, Long> statusDistribution = new LinkedHashMap<>();

        // 定义状态名称映射
        Map<Integer, String> statusNames = new LinkedHashMap<>();
        statusNames.put(0, "待合并");
        statusNames.put(1, "已合并");
        statusNames.put(2, "已发布");
        statusNames.put(3, "报价中");
        statusNames.put(4, "已选标");
        statusNames.put(5, "待发货");
        statusNames.put(6, "已发货");
        statusNames.put(7, "已完成");
        statusNames.put(8, "已评价");
        statusNames.put(9, "已取消");

        for (Map.Entry<Integer, String> entry : statusNames.entrySet()) {
            long count = allDemands.stream()
                    .filter(d -> entry.getKey().equals(d.getStatus()))
                    .count();
            statusDistribution.put(entry.getValue(), count);
        }

        return new AdminOrderStatsResponse(statusDistribution);
    }

    /**
     * 分页查询需求合并组（数据库分页）
     */
    public List<DemandGroup> getDemandGroups(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<DemandGroup> groupPage = demandGroupRepository.findAllByOrderByCreatedAtDesc(pageable);
        return groupPage.getContent();
    }

    // ========== 辅助方法 ==========

    /**
     * 为 DemandItem 附加买家名称、供应商名称、品类名称、城市名称（使用预加载的 Map 避免多次查询）
     */
    private AdminDemandItem enrichDemandItem(Demand demand, 
                                             Map<Long, User> userMap, 
                                             Map<Long, Category> categoryMap, 
                                             Map<Long, UserAddress> addressMap) {
        AdminDemandItem item = AdminDemandItem.fromDemand(demand);

        // 买家名称
        User buyer = userMap.get(demand.getBuyerId());
        if (buyer != null) {
            item.setBuyerName(buyer.getNickname());
        }

        // 供应商名称
        if (demand.getSupplierId() != null) {
            User supplier = userMap.get(demand.getSupplierId());
            if (supplier != null) {
                item.setSupplierName(supplier.getNickname());
            }
        }

        // 品类名称
        if (demand.getCategoryId() != null) {
            Category category = categoryMap.get(demand.getCategoryId());
            if (category != null) {
                item.setCategoryName(category.getName());
            }
        }

        // 城市名称（从配送地址获取）
        if (demand.getDeliveryAddressId() != null) {
            UserAddress address = addressMap.get(demand.getDeliveryAddressId());
            if (address != null) {
                item.setCityName(address.getCity());
            }
        }

        return item;
    }

    /**
     * 为 QuoteItem 附加供应商名称、商品名称、城市名称（使用预加载的 Map 避免多次查询）
     */
    private AdminQuoteItem enrichQuoteItem(Quote quote, 
                                           Map<Long, User> userMap, 
                                           Map<Long, DemandGroup> groupMap) {
        AdminQuoteItem item = AdminQuoteItem.fromQuote(quote);

        // 供应商名称
        User supplier = userMap.get(quote.getSupplierId());
        if (supplier != null) {
            item.setSupplierName(supplier.getNickname());
        }

        // 商品名称和城市名称（从合并组获取）
        if (quote.getGroupId() != null) {
            DemandGroup group = groupMap.get(quote.getGroupId());
            if (group != null) {
                item.setProductName(group.getProductName());
                item.setCityName(group.getCity());
            }
        }

        return item;
    }
}
