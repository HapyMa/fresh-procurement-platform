package com.fresh.procurement.dto;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class CreateDemandRequest {
    
    @NotNull(message = "品类ID不能为空")
    private Long categoryId;
    
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称不能超过100个字符")
    private String productName;
    
    @NotNull(message = "数量不能为空")
    @Positive(message = "数量必须大于0")
    private Double quantity;
    
    @NotBlank(message = "单位不能为空")
    @Size(max = 20, message = "单位不能超过20个字符")
    private String unit;
    
    @Positive(message = "最高价格必须大于0")
    private Double maxPrice;
    
    @Size(max = 500, message = "质量要求不能超过500个字符")
    private String qualityRequirement;
    
    @NotNull(message = "配送地址ID不能为空")
    private Long deliveryAddressId;
    
    @NotBlank(message = "配送日期不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "配送日期格式不正确")
    private String deliveryDate;
    
    @Size(max = 50, message = "配送时段不能超过50个字符")
    private String deliveryTimeSlot;
    
    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;
}
