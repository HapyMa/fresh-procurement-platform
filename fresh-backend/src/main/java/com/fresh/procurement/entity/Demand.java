package com.fresh.procurement.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "demand")
public class Demand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "buyer_id")
    private Long buyerId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "product_name")
    private String productName;

    private Double quantity;

    private String unit;

    @Column(name = "max_price")
    private Double maxPrice;

    @Column(name = "quality_requirement")
    private String qualityRequirement;

    @Column(name = "delivery_address_id")
    private Long deliveryAddressId;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "delivery_time_slot")
    private String deliveryTimeSlot;

    private String remark;

    private Integer status;

    @Column(name = "selected_quote_id")
    private Long selectedQuoteId;

    @Column(name = "deal_price")
    private Double dealPrice;

    @Column(name = "deal_total_amount")
    private Double dealTotalAmount;

    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "pack_status")
    private Integer packStatus = 0;

    @Column(name = "actual_weight")
    private Double actualWeight;

    @Column(name = "pack_remark")
    private String packRemark;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (packStatus == null) {
            packStatus = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
