package com.example.qiyegongdansys.service;

import com.example.qiyegongdansys.common.exception.BusinessException;
import com.example.qiyegongdansys.dto.request.CommentRequest;
import com.example.qiyegongdansys.dto.response.CommentResponse;
import com.example.qiyegongdansys.entity.User;
import com.example.qiyegongdansys.entity.WorkOrder;
import com.example.qiyegongdansys.entity.WorkOrderComment;
import com.example.qiyegongdansys.repository.WorkOrderCommentRepository;
import com.example.qiyegongdansys.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final WorkOrderCommentRepository commentRepository;
    private final WorkOrderRepository workOrderRepository;
    private final UserService userService;

    @Transactional
    public CommentResponse add(Long workOrderId, CommentRequest req, Long authorId) {
        WorkOrder order = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new BusinessException("工单不存在"));
        User author = userService.finEntity(authorId);
        WorkOrderComment comment = new WorkOrderComment();
        comment.setWorkOrder(order);
        comment.setAuthor(author);
        comment.setContent(req.content());
        return CommentResponse.from(commentRepository.save(comment));
    }
}