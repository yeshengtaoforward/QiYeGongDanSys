package com.example.qiyegongdansys.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UserCreateRequest(
        @NotBlank(message = "用户名不能为空") String username,
        @NotBlank(message = "密码不能为空") String password,
        @NotBlank(message = "姓名不能为空") String realName,
        String phone,
        @Email(message = "邮箱格式不正确") String email,
        String department,
        @NotNull(message = "角色不能为空") Set<String> roles
) {
}