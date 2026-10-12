package com.example.qiyegongdansys.dto.request;

import com.example.qiyegongdansys.entity.WorkOrderStatus;
import jakarta.validation.constraints.NotNull;

public record StatusChangeRequest(
        @NotNull(message = "目标状态不能为空") WorkOrderStatus status,
        String reason
) {
}