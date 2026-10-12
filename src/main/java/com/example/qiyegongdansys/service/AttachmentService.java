package com.example.qiyegongdansys.service;

import com.example.qiyegongdansys.common.exception.BusinessException;
import com.example.qiyegongdansys.dto.response.AttachmentResponse;
import com.example.qiyegongdansys.entity.Attachment;
import com.example.qiyegongdansys.entity.User;
import com.example.qiyegongdansys.entity.WorkOrder;
import com.example.qiyegongdansys.repository.AttachmentRepository;
import com.example.qiyegongdansys.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final WorkOrderRepository workOrderRepository;
    private final UserService userService;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${app.upload.max-size-mb:20}")
    private long maxSizeMb;

    @Transactional
    public AttachmentResponse upload(Long workOrderId, MultipartFile file, Long uploaderId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        long maxBytes = maxSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BusinessException("文件大小超过限制 (" + maxSizeMb + "MB)");
        }

        WorkOrder order = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new BusinessException("工单不存在"));
        User uploader = userService.finEntity(uploaderId);

        String original = file.getOriginalFilename();
        String storedName = UUID.randomUUID().toString().replace("-", "")
                + "-" + safeName(original);
        String subDir = "wo-" + workOrderId;
        Path dir = Paths.get(uploadDir, subDir);
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(storedName);
            file.transferTo(target);

            Attachment attachment = new Attachment();
            attachment.setWorkOrder(order);
            attachment.setUploader(uploader);
            attachment.setFileName(original);
            attachment.setFileType(file.getContentType());
            attachment.setFileSize(file.getSize());
            attachment.setStoredPath(subDir + "/" + storedName);
            return AttachmentResponse.from(attachmentRepository.save(attachment));
        } catch (IOException ex) {
            throw new BusinessException("文件保存失败: " + ex.getMessage());
        }
    }

    @Transactional
    public void delete(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new BusinessException("附件不存在"));
        attachmentRepository.delete(attachment);
    }

    public Attachment getEntity(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new BusinessException("附件不存在"));
    }

    public List<AttachmentResponse> listByWorkOrder(Long workOrderId) {
        return attachmentRepository.findByWorkOrderId(workOrderId)
                .stream().map(AttachmentResponse::from).toList();
    }

    private String safeName(String original) {
        if (!StringUtils.hasText(original)) {
            return "file.bin";
        }
        // 仅保留文件名，防止路径穿越
        Path path = Paths.get(original);
        String name = path.getFileName().toString().replace(" ", "_");
        return name.length() > 100 ? name.substring(name.length() - 100) : name;
    }
}