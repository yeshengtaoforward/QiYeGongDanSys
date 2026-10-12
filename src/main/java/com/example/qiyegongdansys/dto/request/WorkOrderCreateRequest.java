package com.example.qiyegongdansys.dto.request;

import com.example.qiyegongdansys.entity.Priority;
import jakarta.validation.constraints.NotBlank;

public record WorkOrderCreateRequest(
        @NotBlank(message = "工单标题不能为空") String title,
        String description,
        Priority priority,
        Long typeId,
        String customData,
        /** 分配模式: AUTO 自动 / MANUAL 手动(需指定 assigneeId) */
        String assignMode,
        Long assigneeId
) {
}