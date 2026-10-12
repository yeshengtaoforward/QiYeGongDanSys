package com.example.qiyegongdansys.entity;

/**
 * 工单状态
 */
public enum WorkOrderStatus {
    /** 待处理 */
    PENDING("待处理"),
    /** 处理中 */
    PROCESSING("处理中"),
    /** 已解决 */
    RESOLVED("已解决"),
    /** 已关闭 */
    CLOSED("已关闭"),
    /** 已取消 */
    CANCELLED("已取消");

    private final String label;

    WorkOrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}