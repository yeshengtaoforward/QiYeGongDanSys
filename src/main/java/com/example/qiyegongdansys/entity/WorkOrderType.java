package com.example.qiyegongdansys.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "sys_work_order_type")
public class WorkOrderType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 64)
    private String name;

    /** 是否为系统内置类型 */
    private Boolean builtin = false;

    /** 是否启用 */
    private Boolean enabled = true;

    @Column(length = 255)
    private String description;

    /** 自定义字段定义 (JSON), 例如 [{"field":"serverIp","label":"服务器IP","type":"text","required":false}] */
    @Column(columnDefinition = "TEXT")
    private String customFields;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}