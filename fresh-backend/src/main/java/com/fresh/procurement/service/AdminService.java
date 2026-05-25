package com.fresh.procurement.service;

import com.fresh.procurement.dto.AdminDTOs.*;
import com.fresh.procurement.entity.*;
import com.fresh.procurement.repository.*;
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
     * 分页查询用户列表
     */
    public AdminUserListResponse getUsers(Integer userType, Integer status, int page, int size) {
        List<User> allUsers;

        if (userType != null && status != null) {
            allUsers = userRepository.findByUserTypeAndStatus(userType, status);
        } else if (userType != null) {
            allUsers = userRepository.findByUserTypeOrderByCreatedAtDesc(userType);
        } else if (status != null) {
            allUsers = userRepository.findAll().stream()
                    .filter(u -> status.equals(u.getStatus()))
                    .sorted(Comparator.comparing(User::getCreatedAt).reversed())
                    .collect(Collectors.toList());
        } else {
            allUsers = userRepository.findAll().stream()
                    .sorted(Comparator.comparing(User::getCreatedAt).reversed())
                    .collect(Collectors.toList());
        }

        int total = allUsers.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);

        List<User> users;
        if (fromIndex >= total) {
            users = Collections.emptyList();
        } else {
            users = allUsers.subList(fromIndex, toIndex);
        }

        return new AdminUserListResponse(total, users);
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
     * 分页查询需求列表（附带买家名称、供应商名称、品类名称、城市名称）
     */
    public AdminDemandListResponse getDemands(Integer status, int page, int size) {
        List<Demand> allDemands;
        if (status != null) {
            allDemands = demandRepository.findByStatusOrderByCreatedAtDesc(status);
        } else {
            allDemands = demandRepository.findAllByOrderByCreatedAtDesc();
        }

        List<AdminDemandItem> allItems = allDemands.stream()
                .map(this::enrichDemandItem)
                .collect(Collectors.toList());

        int total = allItems.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);

        List<AdminDemandItem> items;
        if (fromIndex >= total) {
            items = Collections.emptyList();
        } else {
            items = allItems.subList(fromIndex, toIndex);
        }

        return new AdminDemandListResponse(total, items);
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
     * 分页查询报价列表（附带供应商名称、商品名称、城市名称）
     */
    public AdminQuoteListResponse getQuotes(Integer status, int page, int size) {
        List<Quote> allQuotes = quoteRepository.findAllByOrderByCreatedAtDesc();

        List<AdminQuoteItem> allItems = allQuotes.stream()
                .map(this::enrichQuoteItem)
                .collect(Collectors.toList());

        // 按状态过滤
        if (status != null) {
            allItems = allItems.stream()
                    .filter(item -> status.equals(item.getStatus()))
                    .collect(Collectors.toList());
        }

        int total = allItems.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);

        List<AdminQuoteItem> items;
        if (fromIndex >= total) {
            items = Collections.emptyList();
        } else {
            items = allItems.subList(fromIndex, toIndex);
        }

        return new AdminQuoteListResponse(total, items);
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
     * 分页查询需求合并组
     */
    public List<DemandGroup> getDemandGroups(int page, int size) {
        List<DemandGroup> allGroups = demandGroupRepository.findAll().stream()
                .sorted(Comparator.comparing(DemandGroup::getCreatedAt).reversed())
                .collect(Collectors.toList());

        int total = allGroups.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);

        if (fromIndex >= total) {
            return Collections.emptyList();
        }
        return allGroups.subList(fromIndex, toIndex);
    }

    // ========== 辅助方法 ==========

    /**
     * 为 DemandItem 附加买家名称、供应商名称、品类名称、城市名称
     */
    private AdminDemandItem enrichDemandItem(Demand demand) {
        AdminDemandItem item = AdminDemandItem.fromDemand(demand);

        // 买家名称
        userRepository.findById(demand.getBuyerId())
                .ifPresent(user -> item.setBuyerName(user.getNickname()));

        // 供应商名称
        if (demand.getSupplierId() != null) {
            userRepository.findById(demand.getSupplierId())
                    .ifPresent(user -> item.setSupplierName(user.getNickname()));
        }

        // 品类名称
        if (demand.getCategoryId() != null) {
            categoryRepository.findById(demand.getCategoryId())
                    .ifPresent(category -> item.setCategoryName(category.getName()));
        }

        // 城市名称（从配送地址获取）
        if (demand.getDeliveryAddressId() != null) {
            userAddressRepository.findById(demand.getDeliveryAddressId())
                    .ifPresent(address -> item.setCityName(address.getCity()));
        }

        return item;
    }

    /**
     * 为 QuoteItem 附加供应商名称、商品名称、城市名称
     */
    private AdminQuoteItem enrichQuoteItem(Quote quote) {
        AdminQuoteItem item = AdminQuoteItem.fromQuote(quote);

        // 供应商名称
        userRepository.findById(quote.getSupplierId())
                .ifPresent(user -> item.setSupplierName(user.getNickname()));

        // 商品名称和城市名称（从合并组获取）
        if (quote.getGroupId() != null) {
            demandGroupRepository.findById(quote.getGroupId())
                    .ifPresent(group -> {
                        item.setProductName(group.getProductName());
                        item.setCityName(group.getCity());
                    });
        }

        return item;
    }
}
