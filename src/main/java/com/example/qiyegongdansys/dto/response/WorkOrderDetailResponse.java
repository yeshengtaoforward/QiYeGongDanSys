package com.example.qiyegongdansys.dto.response;

import java.util.List;
import java.util.Map;

/**
 * 工单详情（含评论与附件）
 */
public record WorkOrderDetailResponse(
        WorkOrderResponse workOrder,
        List<CommentResponse> comments,
        List<AttachmentResponse> attachments,
        List<Map<String, String>> allowedTransitions
) {
}