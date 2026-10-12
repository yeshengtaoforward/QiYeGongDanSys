package com.example.qiyegongdansys.repository;

import com.example.qiyegongdansys.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByWorkOrderId(Long workOrderId);
}