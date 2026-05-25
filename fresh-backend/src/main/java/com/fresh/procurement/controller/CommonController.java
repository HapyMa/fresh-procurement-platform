package com.fresh.procurement.controller;

import com.fresh.procurement.dto.ApiResponse;
import com.fresh.procurement.entity.Category;
import com.fresh.procurement.entity.DemandGroup;
import com.fresh.procurement.repository.CategoryRepository;
import com.fresh.procurement.repository.DemandGroupRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class CommonController {

    private final CategoryRepository categoryRepository;
    private final DemandGroupRepository demandGroupRepository;

    public CommonController(CategoryRepository categoryRepository,
                            DemandGroupRepository demandGroupRepository) {
        this.categoryRepository = categoryRepository;
        this.demandGroupRepository = demandGroupRepository;
    }

    @GetMapping("/categories")
    public ApiResponse<List<Category>> getCategories(
            @RequestParam(defaultValue = "0") Long parentId) {
        List<Category> categories;
        if (parentId == 0) {
            categories = categoryRepository.findByStatusOrderBySortOrder(1);
        } else {
            categories = categoryRepository.findByParentIdAndStatusOrderBySortOrder(parentId, 1);
        }
        return ApiResponse.success(categories);
    }

    @GetMapping("/cities")
    public ApiResponse<List<String>> getCities() {
        // 返回所有有需求的城市（从 demand_group 表中获取不重复的城市）
        List<DemandGroup> groups = demandGroupRepository.findByStatusOrderByCreatedAtDesc(0);
        List<String> cities = groups.stream()
                .map(DemandGroup::getCity)
                .distinct()
                .collect(Collectors.toList());
        return ApiResponse.success(cities);
    }
}
