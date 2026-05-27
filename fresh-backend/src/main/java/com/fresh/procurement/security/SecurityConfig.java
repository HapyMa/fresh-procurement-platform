package com.fresh.procurement.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                // 公开接口
                auth.antMatchers("/api/v1/auth/**").permitAll();
                auth.antMatchers("/api/v1/categories").permitAll();
                auth.antMatchers("/api/v1/cities").permitAll();
                
                // H2 Console 仅在非生产环境启用
                if (!"prod".equals(activeProfile)) {
                    auth.antMatchers("/h2-console/**").permitAll();
                }
                
                // 静态资源
                auth.antMatchers(HttpMethod.GET, "/uploads/**").permitAll();
                // 管理员接口
                auth.antMatchers("/api/v1/admin/**").hasRole("ADMIN");
                // 其他接口需要认证
                auth.anyRequest().authenticated();
            })
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(rateLimitFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 简单的内存限流过滤器
     * 每个IP每分钟最多100次请求
     */
    @Bean
    public Filter rateLimitFilter() {
        return new OncePerRequestFilter() {
            private final Map<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();
            private static final int MAX_REQUESTS_PER_MINUTE = 100;
            private static final long WINDOW_SIZE_MS = 60000; // 1分钟

            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                
                String clientIp = getClientIp(request);
                long currentTime = System.currentTimeMillis();
                
                RateLimitInfo info = rateLimitMap.compute(clientIp, (key, existing) -> {
                    if (existing == null || currentTime - existing.windowStart > WINDOW_SIZE_MS) {
                        return new RateLimitInfo(currentTime);
                    }
                    return existing;
                });
                
                if (info.requestCount.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
                    response.setStatus(429);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}");
                    return;
                }
                
                // 清理过期的记录（简单清理，每次请求时检查）
                if (rateLimitMap.size() > 10000) {
                    rateLimitMap.entrySet().removeIf(entry -> 
                        currentTime - entry.getValue().windowStart > WINDOW_SIZE_MS);
                }
                
                filterChain.doFilter(request, response);
            }
            
            private String getClientIp(HttpServletRequest request) {
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                // 如果有多个IP，取第一个
                if (ip != null && ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        };
    }

    private static class RateLimitInfo {
        final long windowStart;
        final AtomicInteger requestCount = new AtomicInteger(0);
        
        RateLimitInfo(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}
