package com.fresh.procurement.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String phone;
    private String password;
    private int userType;
    private String nickname;
}
