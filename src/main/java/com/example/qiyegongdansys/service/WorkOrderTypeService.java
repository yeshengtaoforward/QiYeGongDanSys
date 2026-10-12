package com.example.qiyegongdansys.service;

import com.example.qiyegongdansys.common.exception.BusinessException;
import com.example.qiyegongdansys.dto.response.TypeResponse;
import com.example.qiyegongdansys.entity.WorkOrder;
import com.example.qiyegongdansys.entity.WorkOrderType;
import com.example.qiyegongdansys.repository.WorkOrderRepository;
import com.example.qiyegongdansys.repository.WorkOrderTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkOrderTypeService {

    private final WorkOrderTypeRepository typeRepository;
    private final WorkOrderRepository workOrderRepository;

    public List<TypeResponse> list(Boolean enabledOnly) {
        return typeRepository.findAll().stream()
                .filter(t -> enabledOnly == null || Boolean.TRUE.equals(t.getEnabled()))
                .map(TypeResponse::from)
                .toList();
    }

    public TypeResponse get(Long id) {
        return TypeResponse.from(find(id));
    }

    @Transactional
    public TypeResponse create(String code, String name, String description, String customFields) {
        if (typeRepository.findByCode(code).isPresent()) {
            throw new BusinessException("类型编码已存在");
        }
        WorkOrderType type = new WorkOrderType();
        type.setCode(code);
        type.setName(name);
        type.setDescription(description);
        type.setCustomFields(customFields);
        type.setBuiltin(false);
        type.setEnabled(true);
        return TypeResponse.from(typeRepository.save(type));
    }

    @Transactional
    public TypeResponse update(Long id, String name, String description, String customFields, Boolean enabled) {
        WorkOrderType type = find(id);
        if (StringUtils.hasText(name)) {
            type.setName(name);
        }
        if (description != null) {
            type.setDescription(description);
        }
        if (customFields != null) {
            type.setCustomFields(customFields);
        }
        if (enabled != null) {
            type.setEnabled(enabled);
        }
        return TypeResponse.from(typeRepository.save(type));
    }

    @Transactional
    public void delete(Long id) {
        WorkOrderType type = find(id);
        long count = workOrderRepository.count();
        if (Boolean.TRUE.equals(type.getBuiltin())) {
            throw new BusinessException("内置类型不允许删除，可禁用");
        }
        typeRepository.delete(type);
    }

    private WorkOrderType find(Long id) {
        return typeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("工单类型不存在"));
    }
}