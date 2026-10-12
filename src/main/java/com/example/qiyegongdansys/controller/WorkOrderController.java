package com.example.qiyegongdansys.controller;

import com.example.qiyegongdansys.common.ApiResponse;
import com.example.qiyegongdansys.common.PageResult;
import com.example.qiyegongdansys.common.annotation.LogOperation;
import com.example.qiyegongdansys.dto.request.AssignRequest;
import com.example.qiyegongdansys.dto.request.StatusChangeRequest;
import com.example.qiyegongdansys.dto.request.WorkOrderCreateRequest;
import com.example.qiyegongdansys.dto.request.WorkOrderQueryRequest;
import com.example.qiyegongdansys.dto.response.WorkOrderDetailResponse;
import com.example.qiyegongdansys.dto.response.WorkOrderResponse;
import com.example.qiyegongdansys.security.SecurityUtils;
import com.example.qiyegongdansys.service.WorkOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/workorders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT','CUSTOMER')")
    @LogOperation(module = "工单管理", operation = "创建工单")
    public ApiResponse<WorkOrderResponse> create(@Valid @RequestBody WorkOrderCreateRequest request) {
        return ApiResponse.success("创建成功", workOrderService.create(request, SecurityUtils.getCurrentUser()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResult<WorkOrderResponse>> query(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) Long creatorId,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        WorkOrderQueryRequest query = new WorkOrderQueryRequest(
                keyword,
                status == null ? null : com.example.qiyegongdansys.entity.WorkOrderStatus.valueOf(status),
                priority == null ? null : com.example.qiyegongdansys.entity.Priority.valueOf(priority),
                typeId, creatorId, assigneeId, startTime, endTime, page, size);
        // 客户角色仅能看自己创建的工单
        if ("CUSTOMER".equals(SecurityUtils.getCurrentUser().getRoleCode())) {
            query = new WorkOrderQueryRequest(keyword, query.status(), query.priority(),
                    typeId, SecurityUtils.getCurrentUser().getId(), assigneeId, startTime, endTime, page, size);
        }
        Page<WorkOrderResponse> result = workOrderService.query(query);
        return ApiResponse.success(PageResult.of(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<WorkOrderDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(workOrderService.detail(id));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @LogOperation(module = "工单管理", operation = "分配工单")
    public ApiResponse<WorkOrderResponse> assign(@PathVariable Long id,
                                                 @RequestBody(required = false) AssignRequest request) {
        AssignRequest req = request == null ? new AssignRequest(null) : request;
        return ApiResponse.success("分配成功", workOrderService.assign(id, req));
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT')")
    @LogOperation(module = "工单管理", operation = "状态流转")
    public ApiResponse<WorkOrderResponse> changeStatus(@PathVariable Long id,
                                                       @Valid @RequestBody StatusChangeRequest request) {
        return ApiResponse.success("状态更新成功",
                workOrderService.changeStatus(id, request, SecurityUtils.getCurrentUser()));
    }
}