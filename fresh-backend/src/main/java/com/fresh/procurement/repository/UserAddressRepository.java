package com.fresh.procurement.repository;

import com.fresh.procurement.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

    List<UserAddress> findByUserIdAndAddressType(Long userId, Integer addressType);
}
