package com.fresh.procurement.dto;

import com.fresh.procurement.entity.Demand;
import com.fresh.procurement.entity.Quote;
import com.fresh.procurement.entity.User;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AdminDTOs {

    @Data
    public static class AdminDashboardResponse {
        private long totalUsers;
        private long totalBuyers;
        private long totalSuppliers;
        private long totalDemands;
        private long totalOrders;
        private long totalQuotes;
        private double totalAmount;
        private long pendingDemands;
        private List<User> recentUsers;
    }

    @Data
    public static class AdminUserListResponse {
        private int total;
        private List<User> users;

        public AdminUserListResponse(int total, List<User> users) {
            this.total = total;
            this.users = users;
        }
    }

    @Data
    public static class AdminDemandItem {
        private Long id;
        private Long groupId;
        private Long buyerId;
        private Long categoryId;
        private String productName;
        private Double quantity;
        private String unit;
        private Double maxPrice;
        private String qualityRequirement;
        private Long deliveryAddressId;
        private LocalDate deliveryDate;
        private String deliveryTimeSlot;
        private String remark;
        private Integer status;
        private Long selectedQuoteId;
        private Double dealPrice;
        private Double dealTotalAmount;
        private Long supplierId;
        private Integer packStatus;
        private Double actualWeight;
        private String packRemark;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String buyerName;
        private String supplierName;
        private String categoryName;
        private String cityName;

        public static AdminDemandItem fromDemand(Demand demand) {
            AdminDemandItem item = new AdminDemandItem();
            item.setId(demand.getId());
            item.setGroupId(demand.getGroupId());
            item.setBuyerId(demand.getBuyerId());
            item.setCategoryId(demand.getCategoryId());
            item.setProductName(demand.getProductName());
            item.setQuantity(demand.getQuantity());
            item.setUnit(demand.getUnit());
            item.setMaxPrice(demand.getMaxPrice());
            item.setQualityRequirement(demand.getQualityRequirement());
            item.setDeliveryAddressId(demand.getDeliveryAddressId());
            item.setDeliveryDate(demand.getDeliveryDate());
            item.setDeliveryTimeSlot(demand.getDeliveryTimeSlot());
            item.setRemark(demand.getRemark());
            item.setStatus(demand.getStatus());
            item.setSelectedQuoteId(demand.getSelectedQuoteId());
            item.setDealPrice(demand.getDealPrice());
            item.setDealTotalAmount(demand.getDealTotalAmount());
            item.setSupplierId(demand.getSupplierId());
            item.setPackStatus(demand.getPackStatus());
            item.setActualWeight(demand.getActualWeight());
            item.setPackRemark(demand.getPackRemark());
            item.setCreatedAt(demand.getCreatedAt());
            item.setUpdatedAt(demand.getUpdatedAt());
            return item;
        }
    }

    @Data
    public static class AdminDemandListResponse {
        private int total;
        private List<AdminDemandItem> demands;

        public AdminDemandListResponse(int total, List<AdminDemandItem> demands) {
            this.total = total;
            this.demands = demands;
        }
    }

    @Data
    public static class AdminQuoteItem {
        private Long id;
        private Long groupId;
        private Long supplierId;
        private Double unitPrice;
        private Double totalAmount;
        private Integer validHours;
        private LocalDateTime expireAt;
        private String remark;
        private Integer status;
        private LocalDateTime selectedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String supplierName;
        private String productName;
        private String cityName;

        public static AdminQuoteItem fromQuote(Quote quote) {
            AdminQuoteItem item = new AdminQuoteItem();
            item.setId(quote.getId());
            item.setGroupId(quote.getGroupId());
            item.setSupplierId(quote.getSupplierId());
            item.setUnitPrice(quote.getUnitPrice());
            item.setTotalAmount(quote.getTotalAmount());
            item.setValidHours(quote.getValidHours());
            item.setExpireAt(quote.getExpireAt());
            item.setRemark(quote.getRemark());
            item.setStatus(quote.getStatus());
            item.setSelectedAt(quote.getSelectedAt());
            item.setCreatedAt(quote.getCreatedAt());
            item.setUpdatedAt(quote.getUpdatedAt());
            return item;
        }
    }

    @Data
    public static class AdminQuoteListResponse {
        private int total;
        private List<AdminQuoteItem> quotes;

        public AdminQuoteListResponse(int total, List<AdminQuoteItem> quotes) {
            this.total = total;
            this.quotes = quotes;
        }
    }

    @Data
    public static class AdminOrderStatsResponse {
        private Map<String, Long> statusDistribution;

        public AdminOrderStatsResponse(Map<String, Long> statusDistribution) {
            this.statusDistribution = statusDistribution;
        }
    }
}
