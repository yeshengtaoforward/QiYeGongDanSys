package com.example.qiyegongdansys.repository;

import com.example.qiyegongdansys.entity.WorkOrder;
import com.example.qiyegongdansys.entity.WorkOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long>, JpaSpecificationExecutor<WorkOrder> {

    boolean existsByOrderNo(String orderNo);

    // ===== 统计查询 =====
    @Query("select o.status, count(o) from WorkOrder o group by o.status")
    List<Object[]> countByStatus();

    @Query("select o.priority, count(o) from WorkOrder o group by o.priority")
    List<Object[]> countByPriority();

    @Query("select o.assignMode, count(o) from WorkOrder o group by o.assignMode")
    List<Object[]> countByAssignMode();

    @Query("select o.type.name, count(o) from WorkOrder o group by o.type.name")
    List<Object[]> countByType();

    // 处理人维度统计
    @Query("select o.assignee.realName, count(o) from WorkOrder o where o.assignee is not null group by o.assignee.realName")
    List<Object[]> countByAssignee();

    List<WorkOrder> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByStatus(WorkOrderStatus status);
}