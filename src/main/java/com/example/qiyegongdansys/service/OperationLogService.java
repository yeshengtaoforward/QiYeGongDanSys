package com.example.qiyegongdansys.service;

import com.example.qiyegongdansys.dto.request.LogQueryRequest;
import com.example.qiyegongdansys.entity.OperationLog;
import com.example.qiyegongdansys.repository.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogRepository operationLogRepository;

    public Page<OperationLog> query(LogQueryRequest q) {
        PageRequest pageable = PageRequest.of(
                q.page() == null ? 0 : Math.max(0, q.page()),
                q.size() == null ? 10 : Math.min(Math.max(q.size(), 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<OperationLog> spec = (root, query, cb) -> {
            var p = cb.conjunction();
            if (StringUtils.hasText(q.username())) {
                p = cb.and(p, cb.like(root.get("username"), "%" + q.username() + "%"));
            }
            if (StringUtils.hasText(q.module())) {
                p = cb.and(p, cb.equal(root.get("module"), q.module()));
            }
            if (StringUtils.hasText(q.keyword())) {
                String like = "%" + q.keyword() + "%";
                p = cb.and(p, cb.or(cb.like(root.get("operation"), like), cb.like(root.get("detail"), like)));
            }
            return p;
        };
        return operationLogRepository.findAll(spec, pageable);
    }
}