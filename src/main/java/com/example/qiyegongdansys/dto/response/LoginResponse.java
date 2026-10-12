package com.example.qiyegongdansys.dto.response;

import java.util.Set;

public record LoginResponse(
        String token,
        String tokenType,
        Long userId,
        String username,
        String realName,
        String role,
        Set<String> permissions
) {
}