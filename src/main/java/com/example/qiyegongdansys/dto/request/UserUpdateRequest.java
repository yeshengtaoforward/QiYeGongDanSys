package com.example.qiyegongdansys.dto.request;

import java.util.Set;

public record UserUpdateRequest(
        String password,
        String realName,
        String phone,
        @jakarta.validation.constraints.Email(message = "邮箱格式不正确") String email,
        String department,
        Boolean enabled,
        Set<String> roles
) {
}