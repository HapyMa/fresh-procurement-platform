package com.fresh.procurement.repository;

import com.fresh.procurement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhone(String phone);

    Long countByUserType(Integer userType);

    List<User> findByUserTypeOrderByCreatedAtDesc(Integer userType);

    List<User> findByUserTypeAndStatus(Integer userType, Integer status);

    List<User> findTop10ByOrderByCreatedAtDesc();

    Long countByStatus(Integer status);
}
