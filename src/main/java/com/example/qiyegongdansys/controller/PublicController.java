package com.example.qiyegongdansys.controller;

import com.example.qiyegongdansys.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 公开信息接口：无需认证，用于健康检查与 API 概览
 */
@RestController
public class PublicController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping("/public/info")
    public ApiResponse<Map<String, Object>> info() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("app", "qiyegongdansys");
        info.put("name", "企业工单管理系统");
        info.put("version", "1.0.0");
        info.put("status", "up");
        info.put("serverTime", LocalDateTime.now().format(FMT));
        info.put("authentication", "POST /api/auth/login");
        info.put("defaultAccount", "admin / admin123");
        return ApiResponse.success(info);
    }

    @GetMapping("/public/health")
    public Map<String, Object> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("time", LocalDateTime.now().format(FMT));
        return body;
    }
}