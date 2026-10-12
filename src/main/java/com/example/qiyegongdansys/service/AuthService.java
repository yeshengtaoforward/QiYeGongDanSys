package com.example.qiyegongdansys.service;

import com.example.qiyegongdansys.common.constant.RoleConstants;
import com.example.qiyegongdansys.common.exception.BusinessException;
import com.example.qiyegongdansys.dto.request.LoginRequest;
import com.example.qiyegongdansys.dto.response.LoginResponse;
import com.example.qiyegongdansys.entity.User;
import com.example.qiyegongdansys.repository.UserRepository;
import com.example.qiyegongdansys.security.CustomUserDetailsService;
import com.example.qiyegongdansys.security.JwtUtil;
import com.example.qiyegongdansys.security.SecurityUtils;
import com.example.qiyegongdansys.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));
        if (!user.getEnabled()) {
            throw new BusinessException("账号已被禁用");
        }
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserByUsername(user.getUsername());
        String role = principal.getRoleCode();
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), role);
        return new LoginResponse(token, "Bearer", user.getId(), user.getUsername(),
                user.getRealName(), role, RoleConstants.permissionsFor(role));
    }

    public UserPrincipal currentUser() {
        return SecurityUtils.getCurrentUser();
    }
}