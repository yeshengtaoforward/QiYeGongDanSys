package com.example.qiyegongdansys.controller;

import com.example.qiyegongdansys.common.ApiResponse;
import com.example.qiyegongdansys.common.annotation.LogOperation;
import com.example.qiyegongdansys.dto.response.AttachmentResponse;
import com.example.qiyegongdansys.entity.Attachment;
import com.example.qiyegongdansys.security.SecurityUtils;
import com.example.qiyegongdansys.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/workorders/{workOrderId}/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @LogOperation(module = "附件管理", operation = "上传附件")
    public ApiResponse<AttachmentResponse> upload(@PathVariable Long workOrderId,
                                                  @RequestParam("file") MultipartFile file) {
        return ApiResponse.success("上传成功",
                attachmentService.upload(workOrderId, file, SecurityUtils.getCurrentUser().getId()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<AttachmentResponse>> list(@PathVariable Long workOrderId) {
        return ApiResponse.success(attachmentService.listByWorkOrder(workOrderId));
    }

    @GetMapping("/{attachmentId}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> download(@PathVariable Long workOrderId,
                                             @PathVariable Long attachmentId) {
        Attachment attachment = attachmentService.getEntity(attachmentId);
        try {
            Path file = Paths.get(uploadDir).resolve(attachment.getStoredPath());
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            String encoded = URLEncoder.encode(attachment.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename*=UTF-8''" + encoded)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{attachmentId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @LogOperation(module = "附件管理", operation = "删除附件")
    public ApiResponse<Void> delete(@PathVariable Long workOrderId, @PathVariable Long attachmentId) {
        attachmentService.delete(attachmentId);
        return ApiResponse.success("删除成功", null);
    }
}