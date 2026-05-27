package com.fresh.procurement.dto;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class RegisterRequest {
    
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$", 
             message = "密码强度不足：至少8位，需包含大小写字母和数字")
    private String password;
    
    @Min(value = 1, message = "用户类型无效")
    @Max(value = 3, message = "用户类型无效")
    private int userType;
    
    @Size(max = 50, message = "昵称不能超过50个字符")
    private String nickname;
}
