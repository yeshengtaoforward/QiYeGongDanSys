package com.example.qiyegongdansys.dto.request;

import com.example.qiyegongdansys.entity.WorkOrderStatus;
import jakarta.validation.constraints.NotNull;

public record AssignRequest(
        /** 空为自动分配 */
        Long assigneeId
) {
}