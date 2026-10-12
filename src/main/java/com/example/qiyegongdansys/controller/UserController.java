package com.example.qiyegongdansys.controller;

import com.example.qiyegongdansys.common.ApiResponse;
import com.example.qiyegongdansys.common.PageResult;
import com.example.qiyegongdansys.common.annotation.LogOperation;
import com.example.qiyegongdansys.common.constant.RoleConstants;
import com.example.qiyegongdansys.dto.request.UserCreateRequest;
import com.example.qiyegongdansys.dto.request.UserUpdateRequest;
import com.example.qiyegongdansys.dto.response.UserResponse;
import com.example.qiyegongdansys.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResult<UserResponse>> list(@RequestParam(required = false) String username,
                                                      @RequestParam(required = false) String realName,
                                                      @RequestParam(defaultValue = "0") Integer page,
                                                      @RequestParam(defaultValue = "10") Integer size) {
        Page<UserResponse> result = userService.list(username, realName, page, size);
        return ApiResponse.success(PageResult.of(result));
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<List<String>> roles() {
        return ApiResponse.success(RoleConstants.allRoles());
    }

    @GetMapping("/role/{roleCode}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<List<UserResponse>> byRole(@PathVariable String roleCode) {
        return ApiResponse.success(userService.listByRole(roleCode).stream().toList());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @LogOperation(module = "用户管理", operation = "创建用户")
    public ApiResponse<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        return ApiResponse.success("创建成功", userService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @LogOperation(module = "用户管理", operation = "更新用户")
    public ApiResponse<UserResponse> update(@PathVariable Long id,
                                            @Valid
                                            @RequestBody UserUpdateRequest request) {
        return ApiResponse.success("更新成功", userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @LogOperation(module = "用户管理", operation = "删除用户")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.success("删除成功", null);
    }
}