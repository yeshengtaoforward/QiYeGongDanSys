package com.example.qiyegongdansys.service;

import com.example.qiyegongdansys.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工单统计服务
 */
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final WorkOrderRepository workOrderRepository;

    public Map<String, Object> overview() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", workOrderRepository.count());
        result.put("pending", workOrderRepository.countByStatus(com.example.qiyegongdansys.entity.WorkOrderStatus.PENDING));
        result.put("processing", workOrderRepository.countByStatus(com.example.qiyegongdansys.entity.WorkOrderStatus.PROCESSING));
        result.put("resolved", workOrderRepository.countByStatus(com.example.qiyegongdansys.entity.WorkOrderStatus.RESOLVED));
        result.put("closed", workOrderRepository.countByStatus(com.example.qiyegongdansys.entity.WorkOrderStatus.CLOSED));
        result.put("cancelled", workOrderRepository.countByStatus(com.example.qiyegongdansys.entity.WorkOrderStatus.CANCELLED));
        result.put("byStatus", group(workOrderRepository.countByStatus()));
        result.put("byPriority", group(workOrderRepository.countByPriority()));
        return result;
    }

    public Map<String, Object> byType() {
        return group(workOrderRepository.countByType());
    }

    public Map<String, Object> byAgent() {
        return group(workOrderRepository.countByAssignee());
    }

    public Map<String, Object> byDay(int days) {
        int n = days <= 0 ? 7 : Math.min(days, 90);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        LocalDateTime start = end.minusDays(n);
        Map<LocalDate, Long> daily = workOrderRepository.findByCreatedAtBetween(start, end).stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCreatedAt().toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.counting()));
        Map<String, Object> result = new LinkedHashMap<>();
        // 补齐空日期
        for (int i = n - 1; i >= 0; i--) {
            LocalDate d = end.toLocalDate().minusDays(i);
            result.put(d.toString(), daily.getOrDefault(d, 0L));
        }
        return result;
    }

    public Map<String, Object> assignModeDistribution() {
        return group(workOrderRepository.countByAssignMode());
    }

    private Map<String, Object> group(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(
                r -> String.valueOf(r[0]),
                r -> (Long) r[1],
                (a, b) -> a,
                LinkedHashMap::new));
    }
}