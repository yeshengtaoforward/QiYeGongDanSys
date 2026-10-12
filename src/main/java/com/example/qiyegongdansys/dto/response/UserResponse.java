package com.example.qiyegongdansys.dto.response;

import com.example.qiyegongdansys.entity.Role;
import com.example.qiyegongdansys.entity.User;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record UserResponse(
        Long id,
        String username,
        String realName,
        String phone,
        String email,
        String department,
        Boolean enabled,
        Set<String> roles,
        Set<Long> roleIds,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getPhone(),
                user.getEmail(),
                user.getDepartment(),
                user.getEnabled(),
                user.getRoles().stream().map(Role::getCode).collect(Collectors.toSet()),
                user.getRoles().stream().map(Role::getId).collect(Collectors.toSet()),
                user.getCreatedAt()
        );
    }
}