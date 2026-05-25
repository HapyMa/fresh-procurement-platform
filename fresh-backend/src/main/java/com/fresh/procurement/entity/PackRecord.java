package com.fresh.procurement.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pack_record")
public class PackRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "demand_id")
    private Long demandId;

    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "planned_quantity")
    private Double plannedQuantity;

    @Column(name = "actual_quantity")
    private Double actualQuantity;

    @Column(name = "actual_weight")
    private Double actualWeight;

    @Column(name = "weight_deviation")
    private Double weightDeviation;

    private String grade;

    @Column(name = "quality_check")
    private Integer qualityCheck;

    @Column(name = "package_count")
    private Integer packageCount;

    @Column(name = "package_type")
    private String packageType;

    @Column(name = "label_code")
    private String labelCode;

    private String photos;

    private Integer status;

    @Column(name = "packed_at")
    private LocalDateTime packedAt;

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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
