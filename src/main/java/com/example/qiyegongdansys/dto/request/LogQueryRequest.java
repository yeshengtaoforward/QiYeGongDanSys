package com.example.qiyegongdansys.dto.request;

public record LogQueryRequest(
        String username,
        String module,
        String keyword,
        Integer page,
        Integer size
) {
}