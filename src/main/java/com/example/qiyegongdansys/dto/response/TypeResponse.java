package com.example.qiyegongdansys.dto.response;

import com.example.qiyegongdansys.entity.WorkOrderType;

import java.time.LocalDateTime;

public record TypeResponse(
        Long id,
        String code,
        String name,
        Boolean builtin,
        Boolean enabled,
        String description,
        String customFields,
        LocalDateTime createdAt
) {
    public static TypeResponse from(WorkOrderType t) {
        return new TypeResponse(
                t.getId(), t.getCode(), t.getName(), t.getBuiltin(),
                t.getEnabled(), t.getDescription(), t.getCustomFields(), t.getCreatedAt()
        );
    }
}