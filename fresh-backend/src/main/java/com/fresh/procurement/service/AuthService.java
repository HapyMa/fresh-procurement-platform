package com.fresh.procurement.service;

import com.fresh.procurement.dto.LoginRequest;
import com.fresh.procurement.dto.LoginResponse;
import com.fresh.procurement.dto.RegisterRequest;
import com.fresh.procurement.entity.User;
import com.fresh.procurement.repository.UserRepository;
import com.fresh.procurement.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    // 手机号格式正则：中国大陆手机号
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    
    // 密码强度正则：至少8位，包含大小写字母和数字
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse register(RegisterRequest request) {
        // 手机号格式校验
        if (request.getPhone() == null || !PHONE_PATTERN.matcher(request.getPhone()).matches()) {
            throw new RuntimeException("手机号格式不正确");
        }

        // 密码强度校验
        if (request.getPassword() == null || !PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            throw new RuntimeException("密码强度不足：至少8位，需包含大小写字母和数字");
        }

        // 检查手机号是否已注册
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new RuntimeException("手机号已注册");
        }

        // 创建用户
        User user = new User();
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setUserType(request.getUserType());
        user.setStatus(1);
        userRepository.save(user);

        // 生成JWT
        String token = jwtTokenProvider.generateToken(user.getId(), user.getPhone(), user.getUserType());

        // 计算过期时间（使用配置项）
        String expireAt = Instant.now()
                .plusMillis(jwtExpiration)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        return new LoginResponse(user.getId(), token, expireAt, user.getUserType());
    }

    public LoginResponse login(LoginRequest request) {
        // 手机号格式校验
        if (request.getPhone() == null || !PHONE_PATTERN.matcher(request.getPhone()).matches()) {
            throw new RuntimeException("手机号格式不正确");
        }

        // 查找用户
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("密码错误");
        }

        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }

        // 生成JWT
        String token = jwtTokenProvider.generateToken(user.getId(), user.getPhone(), user.getUserType());

        // 计算过期时间（使用配置项）
        String expireAt = Instant.now()
                .plusMillis(jwtExpiration)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        return new LoginResponse(user.getId(), token, expireAt, user.getUserType());
    }
}
