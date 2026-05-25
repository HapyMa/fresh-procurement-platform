package com.fresh.procurement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private Long userId;
    private String token;
    private String expireAt;
    private Integer userType;

    public LoginResponse(Long userId, String token, String expireAt, Integer userType) {
        this.userId = userId;
        this.token = token;
        this.expireAt = expireAt;
        this.userType = userType;
    }
}
