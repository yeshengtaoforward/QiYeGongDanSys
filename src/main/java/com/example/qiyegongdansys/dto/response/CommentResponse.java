package com.example.qiyegongdansys.dto.response;

import com.example.qiyegongdansys.entity.WorkOrderComment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long workOrderId,
        Long authorId,
        String authorName,
        String content,
        LocalDateTime createdAt
) {
    public static CommentResponse from(WorkOrderComment c) {
        return new CommentResponse(
                c.getId(),
                c.getWorkOrder().getId(),
                c.getAuthor().getId(),
                c.getAuthor().getRealName(),
                c.getContent(),
                c.getCreatedAt()
        );
    }
}