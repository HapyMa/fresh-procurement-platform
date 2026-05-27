package com.fresh.procurement.controller;

import com.fresh.procurement.dto.ApiResponse;
import com.fresh.procurement.entity.User;
import com.fresh.procurement.entity.UserAddress;
import com.fresh.procurement.repository.UserAddressRepository;
import com.fresh.procurement.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;

    public UserController(UserRepository userRepository,
                          UserAddressRepository userAddressRepository) {
        this.userRepository = userRepository;
        this.userAddressRepository = userAddressRepository;
    }

    @GetMapping("/profile")
    public ApiResponse<User> getProfile(@AuthenticationPrincipal Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return ApiResponse.success(user);
    }

    @GetMapping("/addresses")
    public ApiResponse<List<UserAddress>> getAddresses(
            @RequestParam(required = false) Integer addressType,
            @AuthenticationPrincipal Long userId) {
        List<UserAddress> addresses;
        if (addressType != null) {
            addresses = userAddressRepository.findByUserIdAndAddressType(userId, addressType);
        } else {
            addresses = userAddressRepository.findAll().stream()
                    .filter(a -> a.getUserId().equals(userId))
                    .toList();
        }
        return ApiResponse.success(addresses);
    }

    @PostMapping("/addresses")
    public ApiResponse<UserAddress> addAddress(@RequestBody UserAddress address,
                                               @AuthenticationPrincipal Long userId) {
        address.setUserId(userId);
        UserAddress saved = userAddressRepository.save(address);
        return ApiResponse.success(saved);
    }

    @DeleteMapping("/addresses/{addressId}")
    public ApiResponse<Void> deleteAddress(@PathVariable Long addressId,
                                           @AuthenticationPrincipal Long userId) {
        // 权限校验：确保 address 的 userId 与当前用户匹配
        UserAddress address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("地址不存在"));
        if (!address.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该地址");
        }
        userAddressRepository.deleteById(addressId);
        return ApiResponse.success();
    }
}
