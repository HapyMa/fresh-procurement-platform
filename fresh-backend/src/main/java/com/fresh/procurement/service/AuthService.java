package com.fresh.procurement.service;

import com.fresh.procurement.dto.LoginRequest;
import com.fresh.procurement.dto.LoginResponse;
import com.fresh.procurement.dto.RegisterRequest;
import com.fresh.procurement.entity.User;
import com.fresh.procurement.repository.UserRepository;
import com.fresh.procurement.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse register(RegisterRequest request) {
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

        // 计算过期时间
        String expireAt = Instant.now()
                .plusMillis(86400000) // 24小时后过期
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        return new LoginResponse(user.getId(), token, expireAt, user.getUserType());
    }

    public LoginResponse login(LoginRequest request) {
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

        // 计算过期时间
        String expireAt = Instant.now()
                .plusMillis(86400000)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        return new LoginResponse(user.getId(), token, expireAt, user.getUserType());
    }
}
