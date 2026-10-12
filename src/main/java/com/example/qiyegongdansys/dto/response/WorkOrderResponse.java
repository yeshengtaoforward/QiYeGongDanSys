package com.example.qiyegongdansys.dto.response;

import com.example.qiyegongdansys.entity.Priority;
import com.example.qiyegongdansys.entity.WorkOrder;
import com.example.qiyegongdansys.entity.WorkOrderStatus;
import com.example.qiyegongdansys.entity.WorkOrderType;

import java.time.LocalDateTime;

public record WorkOrderResponse(
        Long id,
        String orderNo,
        String title,
        String description,
        WorkOrderStatus status,
        String statusLabel,
        Priority priority,
        String priorityLabel,
        String assignMode,
        Long typeId,
        String typeName,
        String customData,
        Long creatorId,
        String creatorName,
        Long assigneeId,
        String assigneeName,
        LocalDateTime createdAt,
        LocalDateTime assignedAt,
        LocalDateTime resolvedAt,
        LocalDateTime closedAt
) {
    public static WorkOrderResponse from(WorkOrder o) {
        WorkOrderType type = o.getType();
        return new WorkOrderResponse(
                o.getId(),
                o.getOrderNo(),
                o.getTitle(),
                o.getDescription(),
                o.getStatus(),
                o.getStatus().getLabel(),
                o.getPriority(),
                o.getPriority().getLabel(),
                o.getAssignMode(),
                type != null ? type.getId() : null,
                type != null ? type.getName() : null,
                o.getCustomData(),
                o.getCreator() != null ? o.getCreator().getId() : null,
                o.getCreator() != null ? o.getCreator().getRealName() : null,
                o.getAssignee() != null ? o.getAssignee().getId() : null,
                o.getAssignee() != null ? o.getAssignee().getRealName() : null,
                o.getCreatedAt(),
                o.getAssignedAt(),
                o.getResolvedAt(),
                o.getClosedAt()
        );
    }
}