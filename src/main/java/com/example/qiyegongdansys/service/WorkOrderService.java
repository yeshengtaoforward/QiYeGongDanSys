package com.example.qiyegongdansys.service;

import com.example.qiyegongdansys.common.exception.BusinessException;
import com.example.qiyegongdansys.dto.request.AssignRequest;
import com.example.qiyegongdansys.dto.request.StatusChangeRequest;
import com.example.qiyegongdansys.dto.request.WorkOrderCreateRequest;
import com.example.qiyegongdansys.dto.request.WorkOrderQueryRequest;
import com.example.qiyegongdansys.dto.response.CommentResponse;
import com.example.qiyegongdansys.dto.response.AttachmentResponse;
import com.example.qiyegongdansys.dto.response.WorkOrderDetailResponse;
import com.example.qiyegongdansys.dto.response.WorkOrderResponse;
import com.example.qiyegongdansys.entity.*;
import com.example.qiyegongdansys.repository.AttachmentRepository;
import com.example.qiyegongdansys.repository.WorkOrderCommentRepository;
import com.example.qiyegongdansys.repository.WorkOrderRepository;
import com.example.qiyegongdansys.repository.WorkOrderTypeRepository;
import com.example.qiyegongdansys.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderTypeRepository typeRepository;
    private final WorkOrderCommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;
    private final AssignmentService assignmentService;
    private final UserService userService;

    // ===================== 创建与分配 =====================

    @Transactional
    public WorkOrderResponse create(WorkOrderCreateRequest req, UserPrincipal principal) {
        WorkOrder order = new WorkOrder();
        order.setTitle(req.title());
        order.setDescription(req.description());
        order.setPriority(req.priority() == null ? Priority.MEDIUM : req.priority());
        order.setCustomData(req.customData());
        order.setCreator(userService.finEntity(principal.getId()));
        order.setOrderNo(generateOrderNo());
        if (req.typeId() != null) {
            order.setType(typeRepository.findById(req.typeId())
                    .orElseThrow(() -> new BusinessException("工单类型不存在")));
        }

        // 分配
        String mode = req.assignMode() == null ? "MANUAL" : req.assignMode().toUpperCase();
        if ("AUTO".equals(mode)) {
            order.setAssignMode("AUTO");
            User assignee = assignmentService.autoAssign();
            assignmentService.assign(order, assignee, "AUTO");
        } else if ("MANUAL".equals(mode)) {
            if (req.assigneeId() != null) {
                order.setAssignMode("MANUAL");
                assignmentService.assign(order, userService.finEntity(req.assigneeId()), "MANUAL");
            }
            // 未指定处理人：先不分配，保持 PENDING
        } else {
            throw new BusinessException("分配模式不合法，仅支持 AUTO 或 MANUAL");
        }

        return WorkOrderResponse.from(workOrderRepository.save(order));
    }

    @Transactional
    public WorkOrderResponse assign(Long id, AssignRequest req) {
        WorkOrder order = find(id);
        if (req.assigneeId() == null) {
            User assignee = assignmentService.autoAssign();
            assignmentService.assign(order, assignee, "AUTO");
        } else {
            User assignee = userService.finEntity(req.assigneeId());
            assignmentService.assign(order, assignee, "MANUAL");
        }
        return WorkOrderResponse.from(workOrderRepository.save(order));
    }

    // ===================== 状态流转 =====================

    @Transactional
    public WorkOrderResponse changeStatus(Long id, StatusChangeRequest req, UserPrincipal principal) {
        WorkOrder order = find(id);
        validateAssignee(order, principal);
        WorkOrderStatus target = req.status();
        validateTransition(order, target);
        applyStatus(order, target);
        return WorkOrderResponse.from(workOrderRepository.save(order));
    }

    /**
     * 处理人归属校验：ADMIN/MANAGER 可处理任意工单；
     * 其他角色（AGENT）仅能流转分配给自己处理的工单。
     */
    private void validateAssignee(WorkOrder order, UserPrincipal principal) {
        String role = principal.getRoleCode();
        if ("ADMIN".equals(role) || "MANAGER".equals(role)) {
            return;
        }
        if (order.getAssignee() == null || !order.getAssignee().getId().equals(principal.getId())) {
            throw new BusinessException("只能处理分配给自己的工单");
        }
    }

    private void validateTransition(WorkOrder order, WorkOrderStatus target) {
        WorkOrderStatus current = order.getStatus();
        boolean ok = switch (current) {
            case PENDING -> target == WorkOrderStatus.PROCESSING || target == WorkOrderStatus.CANCELLED || target == WorkOrderStatus.CLOSED;
            case PROCESSING -> target == WorkOrderStatus.RESOLVED || target == WorkOrderStatus.CANCELLED;
            case RESOLVED -> target == WorkOrderStatus.CLOSED || target == WorkOrderStatus.PROCESSING;
            case CLOSED, CANCELLED -> false;
        };
        if (!ok) {
            throw new BusinessException("不允许从 " + current.getLabel() + " 流转到 " + target.getLabel());
        }
    }

    private void applyStatus(WorkOrder order, WorkOrderStatus target) {
        order.setStatus(target);
        LocalDateTime now = LocalDateTime.now();
        switch (target) {
            case PROCESSING -> { }
            case RESOLVED -> order.setResolvedAt(now);
            case CLOSED -> order.setClosedAt(now);
            default -> { }
        }
    }

    // ===================== 查询 =====================

    public WorkOrderDetailResponse detail(Long id) {
        WorkOrder order = find(id);
        List<CommentResponse> comments = commentRepository.findByWorkOrderIdOrderByCreatedAtAsc(id)
                .stream().map(CommentResponse::from).toList();
        List<AttachmentResponse> attachments = attachmentRepository.findByWorkOrderId(id)
                .stream().map(AttachmentResponse::from).toList();
        List<Map<String, String>> transitions = allowedTransitions(order.getStatus());
        return new WorkOrderDetailResponse(WorkOrderResponse.from(order), comments, attachments, transitions);
    }

    public Page<WorkOrderResponse> query(WorkOrderQueryRequest q) {
        PageRequest pageable = PageRequest.of(defaultPage(q.page()), defaultSize(q.size()),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<WorkOrder> spec = buildSpec(q);
        return workOrderRepository.findAll(spec, pageable).map(WorkOrderResponse::from);
    }

    private Specification<WorkOrder> buildSpec(WorkOrderQueryRequest q) {
        return (root, query, cb) -> {
            var p = cb.conjunction();
            if (StringUtils.hasText(q.keyword())) {
                String like = "%" + q.keyword().trim() + "%";
                p = cb.and(p, cb.or(cb.like(root.get("title"), like), cb.like(root.get("orderNo"), like)));
            }
            if (q.status() != null) {
                p = cb.and(p, cb.equal(root.get("status"), q.status()));
            }
            if (q.priority() != null) {
                p = cb.and(p, cb.equal(root.get("priority"), q.priority()));
            }
            if (q.typeId() != null) {
                p = cb.and(p, cb.equal(root.join("type").get("id"), q.typeId()));
            }
            if (q.creatorId() != null) {
                p = cb.and(p, cb.equal(root.join("creator").get("id"), q.creatorId()));
            }
            if (q.assigneeId() != null) {
                p = cb.and(p, cb.equal(root.join("assignee").get("id"), q.assigneeId()));
            }
            if (q.startTime() != null) {
                p = cb.and(p, cb.greaterThanOrEqualTo(root.get("createdAt"), q.startTime()));
            }
            if (q.endTime() != null) {
                p = cb.and(p, cb.lessThanOrEqualTo(root.get("createdAt"), q.endTime()));
            }
            return p;
        };
    }

    // ===================== 工具 =====================

    private List<Map<String, String>> allowedTransitions(WorkOrderStatus current) {
        return switch (current) {
            case PENDING -> List.of(
                    Map.of("to", "PROCESSING", "label", "开始处理"),
                    Map.of("to", "CANCELLED", "label", "取消"));
            case PROCESSING -> List.of(
                    Map.of("to", "RESOLVED", "label", "标记已解决"),
                    Map.of("to", "CANCELLED", "label", "取消"));
            case RESOLVED -> List.of(
                    Map.of("to", "CLOSED", "label", "确认关闭"),
                    Map.of("to", "PROCESSING", "label", "重新打开"));
            default -> List.of();
        };
    }

    private WorkOrder find(Long id) {
        return workOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("工单不存在"));
    }

    private synchronized String generateOrderNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int rand = ThreadLocalRandom.current().nextInt(1000, 9999);
        String no = "WO" + date + rand;
        while (workOrderRepository.existsByOrderNo(no)) {
            rand = ThreadLocalRandom.current().nextInt(1000, 9999);
            no = "WO" + date + rand;
        }
        return no;
    }

    private int defaultPage(Integer v) {
        return v == null ? 0 : Math.max(0, v);
    }

    private int defaultSize(Integer v) {
        int size = v == null ? 10 : v;
        return Math.min(Math.max(size, 1), 100);
    }
}