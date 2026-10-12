package com.example.qiyegongdansys.dto.request;

import com.example.qiyegongdansys.entity.Priority;
import com.example.qiyegongdansys.entity.WorkOrderStatus;

import java.time.LocalDateTime;

/**
 * 工单多维度查询条件
 */
public record WorkOrderQueryRequest(
        String keyword,
        WorkOrderStatus status,
        Priority priority,
        Long typeId,
        Long creatorId,
        Long assigneeId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer page,
        Integer size
) {
}