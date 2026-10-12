package com.example.qiyegongdansys.common.constant;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 角色与权限定义（RBAC）
 */
public final class RoleConstants {

    public static final String ADMIN = "ADMIN";
    public static final String MANAGER = "MANAGER";
    public static final String AGENT = "AGENT";
    public static final String CUSTOMER = "CUSTOMER";

    private RoleConstants() {
    }

    /** 角色 -> 权限点 映射，用于前端按钮级权限控制 */
    private static final Map<String, Set<String>> PERMISSIONS = Map.of(
            ADMIN, Set.of("user:manage", "workorder:all", "workorder:assign", "workorder:process",
                    "workorder:create", "workorder:view", "stats:view", "log:view", "type:manage"),
            MANAGER, Set.of("workorder:all", "workorder:assign", "workorder:process",
                    "workorder:create", "workorder:view", "stats:view", "log:view", "type:manage"),
            AGENT, Set.of("workorder:process", "workorder:create", "workorder:view"),
            CUSTOMER, Set.of("workorder:create", "workorder:mine")
    );

    public static Set<String> permissionsFor(String role) {
        return PERMISSIONS.getOrDefault(role, Set.of());
    }

    public static List<String> allRoles() {
        return List.of(ADMIN, MANAGER, AGENT, CUSTOMER);
    }
}