package com.example.qiyegongdansys.dto.response;

import com.example.qiyegongdansys.common.constant.RoleConstants;
import com.example.qiyegongdansys.security.UserPrincipal;

import java.util.Set;

public record CurrentUserResponse(
        Long id,
        String username,
        String realName,
        String role,
        Set<String> permissions
) {
    public static CurrentUserResponse from(UserPrincipal principal) {
        return new CurrentUserResponse(
                principal.getId(),
                principal.getUsername(),
                principal.getRealName(),
                principal.getRoleCode(),
                RoleConstants.permissionsFor(principal.getRoleCode())
        );
    }
}