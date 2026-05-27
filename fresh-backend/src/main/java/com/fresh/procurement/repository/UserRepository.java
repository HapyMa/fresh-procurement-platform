package com.fresh.procurement.repository;

import com.fresh.procurement.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhone(String phone);

    Long countByUserType(Integer userType);

    List<User> findByUserTypeOrderByCreatedAtDesc(Integer userType);

    List<User> findByUserTypeAndStatus(Integer userType, Integer status);

    List<User> findTop10ByOrderByCreatedAtDesc();

    Long countByStatus(Integer status);

    // ========== 分页查询方法 ==========

    /**
     * 分页查询所有用户（按创建时间倒序）
     */
    Page<User> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 按用户类型分页查询（按创建时间倒序）
     */
    Page<User> findByUserTypeOrderByCreatedAtDesc(Integer userType, Pageable pageable);

    /**
     * 按用户类型和状态分页查询
     */
    @Query("SELECT u FROM User u WHERE u.userType = :userType AND u.status = :status ORDER BY u.createdAt DESC")
    Page<User> findByUserTypeAndStatusOrderByCreatedAtDesc(
            @Param("userType") Integer userType,
            @Param("status") Integer status,
            Pageable pageable);

    /**
     * 按状态分页查询（按创建时间倒序）
     */
    @Query("SELECT u FROM User u WHERE u.status = :status ORDER BY u.createdAt DESC")
    Page<User> findByStatusOrderByCreatedAtDesc(@Param("status") Integer status, Pageable pageable);

    /**
     * 统计指定用户类型和状态的用户数量
     */
    long countByUserTypeAndStatus(Integer userType, Integer status);
}
