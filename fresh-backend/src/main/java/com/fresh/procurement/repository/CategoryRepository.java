package com.fresh.procurement.repository;

import com.fresh.procurement.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByParentIdAndStatusOrderBySortOrder(Long parentId, Integer status);

    List<Category> findByStatusOrderBySortOrder(Integer status);
}
