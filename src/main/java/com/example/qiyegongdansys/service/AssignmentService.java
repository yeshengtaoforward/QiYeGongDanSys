package com.example.qiyegongdansys.service;

import com.example.qiyegongdansys.common.constant.RoleConstants;
import com.example.qiyegongdansys.common.exception.BusinessException;
import com.example.qiyegongdansys.entity.User;
import com.example.qiyegongdansys.entity.WorkOrder;
import com.example.qiyegongdansys.entity.WorkOrderStatus;
import com.example.qiyegongdansys.repository.UserRepository;
import com.example.qiyegongdansys.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工单分配服务
 * 自动分配策略：优先选择处理中工单数最少（工作量最小）的 AGENT 进行分配
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final UserRepository userRepository;
    private final WorkOrderRepository workOrderRepository;

    /**
     * 自动分配：基于最小工作量策略挑选处理人
     */
    @Transactional
    public User autoAssign() {
        List<User> agents = userRepository.findUsersByRoleCode(RoleConstants.AGENT);
        if (agents.isEmpty()) {
            throw new BusinessException("当前没有可用的处理人(AGENT)，无法自动分配");
        }

        // 统计每个 AGENT 当前的未完结工单数（PENDING + PROCESSING）
        Map<Long, Long> workload = workOrderRepository.findAll().stream()
                .filter(o -> o.getAssignee() != null
                        && (o.getStatus() == WorkOrderStatus.PENDING || o.getStatus() == WorkOrderStatus.PROCESSING))
                .collect(Collectors.groupingBy(o -> o.getAssignee().getId(), Collectors.counting()));

        return agents.stream()
                .min(Comparator
                        .comparingLong((User u) -> workload.getOrDefault(u.getId(), 0L))
                        .thenComparing(User::getId))
                .orElseThrow(() -> new BusinessException("没有可用的处理人"));
    }

    @Transactional
    public WorkOrder assign(WorkOrder order, User assignee, String mode) {
        order.setAssignee(assignee);
        order.setAssignMode(mode);
        order.setStatus(WorkOrderStatus.PENDING);
        order.setAssignedAt(java.time.LocalDateTime.now());
        return workOrderRepository.save(order);
    }
}