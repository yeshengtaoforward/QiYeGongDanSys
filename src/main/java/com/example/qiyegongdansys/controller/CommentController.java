package com.example.qiyegongdansys.controller;

import com.example.qiyegongdansys.common.ApiResponse;
import com.example.qiyegongdansys.common.annotation.LogOperation;
import com.example.qiyegongdansys.dto.request.CommentRequest;
import com.example.qiyegongdansys.dto.response.CommentResponse;
import com.example.qiyegongdansys.repository.WorkOrderCommentRepository;
import com.example.qiyegongdansys.security.SecurityUtils;
import com.example.qiyegongdansys.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workorders/{workOrderId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final WorkOrderCommentRepository commentRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<CommentResponse>> list(@PathVariable Long workOrderId) {
        return ApiResponse.success(commentRepository.findByWorkOrderIdOrderByCreatedAtAsc(workOrderId)
                .stream().map(CommentResponse::from).toList());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @LogOperation(module = "工单评论", operation = "添加评论")
    public ApiResponse<CommentResponse> add(@PathVariable Long workOrderId,
                                            @Valid @RequestBody CommentRequest request) {
        return ApiResponse.success("评论成功",
                commentService.add(workOrderId, request, SecurityUtils.getCurrentUser().getId()));
    }
}