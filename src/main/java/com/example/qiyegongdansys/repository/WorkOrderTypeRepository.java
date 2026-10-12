package com.example.qiyegongdansys.repository;

import com.example.qiyegongdansys.entity.WorkOrderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkOrderTypeRepository extends JpaRepository<WorkOrderType, Long> {

    Optional<WorkOrderType> findByCode(String code);
}