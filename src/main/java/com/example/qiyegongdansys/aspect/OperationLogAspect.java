package com.example.qiyegongdansys.aspect;

import com.example.qiyegongdansys.common.annotation.LogOperation;
import com.example.qiyegongdansys.entity.OperationLog;
import com.example.qiyegongdansys.repository.OperationLogRepository;
import com.example.qiyegongdansys.security.SecurityUtils;
import com.example.qiyegongdansys.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 操作日志切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogRepository operationLogRepository;

    @AfterReturning("@annotation(com.example.qiyegongdansys.common.annotation.LogOperation)")
    public void log(JoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            LogOperation annotation = method.getAnnotation(LogOperation.class);

            OperationLog op = new OperationLog();
            UserPrincipal principal = SecurityUtils.getCurrentUserOrNull();
            if (principal != null) {
                op.setUsername(principal.getUsername());
                op.setRole(principal.getRoleCode());
            }
            op.setModule(annotation.module());
            op.setOperation(annotation.operation());

            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                op.setIp(clientIp(request));
            }
            op.setDetail(signature.getMethod().getName());
            operationLogRepository.save(op);
        } catch (Exception ex) {
            log.warn("记录操作日志失败: {}", ex.getMessage());
        }
    }

    private String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}