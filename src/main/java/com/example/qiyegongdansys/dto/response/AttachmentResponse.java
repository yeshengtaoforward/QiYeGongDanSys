package com.example.qiyegongdansys.dto.response;

import com.example.qiyegongdansys.entity.Attachment;

import java.time.LocalDateTime;

public record AttachmentResponse(
        Long id,
        Long workOrderId,
        String fileName,
        String fileType,
        Long fileSize,
        LocalDateTime createdAt
) {
    public static AttachmentResponse from(Attachment a) {
        return new AttachmentResponse(
                a.getId(),
                a.getWorkOrder() != null ? a.getWorkOrder().getId() : null,
                a.getFileName(),
                a.getFileType(),
                a.getFileSize(),
                a.getCreatedAt()
        );
    }
}