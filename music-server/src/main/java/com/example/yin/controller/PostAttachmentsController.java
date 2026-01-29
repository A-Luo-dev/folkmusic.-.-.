package com.example.yin.controller;

import com.example.yin.model.domain.PostAttachments;
import com.example.yin.service.PostAttachmentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 帖子附件表 前端控制器
 * </p>
 *
 * @author Byterain
 * @since 2025-08-27
 */ 
@RestController
@RequestMapping("/attachments")
public class PostAttachmentsController {

    @Autowired
    private PostAttachmentsService attachmentsService;

    /**
     * 上传帖子附件（新版本，不需要postId）
     * POST /attachments/upload
     * Header: X-User-Id: <用户ID>
     * Body: MultipartFile
     */
    @PostMapping("/upload")
    public Map<String, Object> uploadAttachmentNew(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam("file") MultipartFile file) {
        
        try {
            System.out.println("收到附件上传请求，userId: " + userId + ", fileName: " + file.getOriginalFilename());

            if (file.isEmpty()) {
                throw new RuntimeException("文件不能为空");
            }

            if (userId == null) {
                throw new RuntimeException("用户未登录");
            }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFileName = System.currentTimeMillis() + "_" + userId + fileExtension;
        
        System.out.println("准备上传文件到MinIO: " + uniqueFileName);
        
        // 上传到MinIO的UploadFile文件夹
        String minioUrl = uploadToMinio(file, uniqueFileName);
        
        System.out.println("MinIO上传结果: " + minioUrl);
        
        // 获取文件类型
        String fileType = getFileType(originalFilename);
        
        // 先保存附件记录（临时postId为NULL，稍后在发帖时更新）
        PostAttachments attachment = new PostAttachments();
        attachment.setPostId(null); // 临时设置为NULL，发帖时更新
        attachment.setType(fileType);
        attachment.setUrl(minioUrl);
        attachment.setCreatedAt(LocalDateTime.now());
        
        System.out.println("准备保存附件记录到数据库: " + attachment);
        System.out.println("attachmentId: " + attachment.getAttachmentId());
        System.out.println("postId: " + attachment.getPostId());
        System.out.println("type: " + attachment.getType());
        System.out.println("url: " + attachment.getUrl());
        System.out.println("createdAt: " + attachment.getCreatedAt());
        
        try {
            // 测试数据库连接
            System.out.println("测试数据库连接...");
            long count = attachmentsService.count();
            System.out.println("当前附件表记录数: " + count);
            
            boolean saveResult = attachmentsService.save(attachment);
            System.out.println("附件记录保存结果: " + saveResult + ", ID: " + attachment.getAttachmentId());
            if (!saveResult) {
                System.err.println("保存失败！");
            }
            
            // 再次检查记录数
            long newCount = attachmentsService.count();
            System.out.println("保存后附件表记录数: " + newCount);
            
        } catch (Exception e) {
            System.err.println("保存附件时发生异常: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

            Map<String, Object> resp = new HashMap<>();
            resp.put("attachmentId", attachment.getAttachmentId());
            resp.put("url", minioUrl);
            resp.put("fileName", originalFilename);
            resp.put("type", fileType);
            resp.put("success", true);
            return resp;
            
        } catch (Exception e) {
            System.err.println("附件上传失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResp = new HashMap<>();
            errorResp.put("success", false);
            errorResp.put("error", e.getMessage());
            return errorResp;
        }
    }
    
    private String uploadToMinio(MultipartFile file, String fileName) throws Exception {
        // 使用MinioUploadController的专用附件上传方法
        String result = com.example.yin.controller.MinioUploadController.uploadAttachmentFile(file, fileName);
        if (result.contains("Error")) {
            throw new RuntimeException("文件上传失败: " + result);
        }
        // 返回MinIO访问URL
        return "http://localhost:9000/user01/UploadFile/" + fileName;
    }
    
    private String getFileType(String filename) {
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        switch (ext) {
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "webp":
                return "image";
            case "mp3":
            case "wav":
            case "ogg":
            case "m4a":
                return "audio";
            case "mp4":
            case "avi":
            case "mov":
            case "wmv":
                return "video";
            case "pdf":
                return "pdf";
            case "doc":
            case "docx":
                return "document";
            default:
                return "file";
        }
    }
}