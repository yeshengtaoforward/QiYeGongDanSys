package com.example.qiyegongdansys.controller;

import com.example.qiyegongdansys.common.ApiResponse;
import com.example.qiyegongdansys.common.annotation.LogOperation;
import com.example.qiyegongdansys.dto.response.TypeResponse;
import com.example.qiyegongdansys.service.WorkOrderTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/types")
@RequiredArgsConstructor
public class WorkOrderTypeController {

    private final WorkOrderTypeService typeService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<TypeResponse>> list(@RequestParam(required = false) Boolean enabledOnly) {
        return ApiResponse.success(typeService.list(enabledOnly));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<TypeResponse> get(@PathVariable Long id) {
        return ApiResponse.success(typeService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @LogOperation(module = "工单类型", operation = "创建类型")
    public ApiResponse<TypeResponse> create(@RequestBody Map<String, String> body) {
        return ApiResponse.success("创建成功", typeService.create(
                body.get("code"), body.get("name"), body.get("description"), body.get("customFields")));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @LogOperation(module = "工单类型", operation = "更新类型")
    public ApiResponse<TypeResponse> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ApiResponse.success("更新成功", typeService.update(
                id,
                (String) body.get("name"),
                (String) body.get("description"),
                (String) body.get("customFields"),
                body.get("enabled") == null ? null : Boolean.valueOf(body.get("enabled").toString())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @LogOperation(module = "工单类型", operation = "删除类型")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        typeService.delete(id);
        return ApiResponse.success("删除成功", null);
    }
}