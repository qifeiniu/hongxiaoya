package com.hongxiaoya.api.config;

import com.hongxiaoya.api.common.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    // 允许游客访问的路径，如果携带Token也会解析，不携带也不会拦截
    private static final List<String> GUEST_PATHS = Arrays.asList(
            "/api/v1/auth/login",
            "/api/v1/auth/login/wechat",
            "/api/v1/auth/sms/send",
            "/api/v1/home/recommend",
            "/api/v1/discover/list"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        String token = request.getHeader("Authorization");
        
        String uri = request.getRequestURI();
        
        if (StringUtils.hasText(token)) {
            try {
                Long userId = JwtUtils.parseToken(token);
                UserContext.setUserId(userId);
                return true;
            } catch (Exception e) {}
        }
        
        // 判断是否允许游客访问
        boolean isGuestAllowed = GUEST_PATHS.stream().anyMatch(uri::startsWith);
        if (isGuestAllowed) {
            return true;
        }

        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write("{\"code\":401, \"msg\":\"未登录或Token失效\"}");
        return false;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
