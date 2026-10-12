package com.example.qiyegongdansys.controller;

import com.example.qiyegongdansys.common.ApiResponse;
import com.example.qiyegongdansys.common.PageResult;
import com.example.qiyegongdansys.dto.request.LogQueryRequest;
import com.example.qiyegongdansys.entity.OperationLog;
import com.example.qiyegongdansys.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService logService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<PageResult<OperationLog>> list(@RequestParam(required = false) String username,
                                                      @RequestParam(required = false) String module,
                                                      @RequestParam(required = false) String keyword,
                                                      @RequestParam(defaultValue = "0") Integer page,
                                                      @RequestParam(defaultValue = "10") Integer size) {
        Page<OperationLog> result = logService.query(new LogQueryRequest(username, module, keyword, page, size));
        return ApiResponse.success(PageResult.of(result));
    }
}