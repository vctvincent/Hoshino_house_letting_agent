package com.reams.controller;

import com.reams.common.result.FileUploadResult;
import com.reams.common.result.Result;
import com.reams.entity.SysAgent;
import com.reams.entity.SysCustomer;
import com.reams.mapper.SysAgentMapper;
import com.reams.mapper.SysCustomerMapper;
import com.reams.service.impl.LocalFileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/file")
public class FileUploadController {

    @Autowired
    private LocalFileStorageService localFileStorageService;

    @Autowired
    private SysAgentMapper agentMapper;

    @Autowired
    private SysCustomerMapper customerMapper;

    @PostMapping("/upload/image")
    public Result<FileUploadResult> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只能上传图片文件");
        }

        List<String> allowedTypes = Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp", "image/jpg");
        if (!allowedTypes.contains(contentType)) {
            return Result.error("不支持的图片格式，仅支持 JPG、PNG、GIF、WebP");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.error("图片大小不能超过 10MB");
        }

        try {
            FileUploadResult result = localFileStorageService.uploadImage(file);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("图片上传失败：" + e.getMessage());
        }
    }

    @PostMapping("/upload/avatar")
    public Result<FileUploadResult> uploadAvatar(Authentication authentication, @RequestParam("file") MultipartFile file) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error(401, "请先登录");
        }
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        String role = authentication.getAuthorities().iterator().next().getAuthority();
        Long userId = (Long) authentication.getPrincipal();

        if ("ROLE_ADMIN".equals(role)) {
            return Result.error("管理员使用默认头像，不支持上传自定义头像");
        }

        try {
            String userType = "ROLE_AGENT".equals(role) ? "agent" : "customer";
            String avatarUrl = localFileStorageService.uploadAvatar(file, userType);

            if ("ROLE_AGENT".equals(role)) {
                SysAgent agent = new SysAgent();
                agent.setId(userId);
                agent.setAvatar(avatarUrl);
                agentMapper.update(agent);
            } else {
                SysCustomer customer = new SysCustomer();
                customer.setId(userId);
                customer.setAvatar(avatarUrl);
                customerMapper.update(customer);
            }

            FileUploadResult result = FileUploadResult.of(avatarUrl, file.getOriginalFilename(), file.getSize(), file.getContentType());
            return Result.success("头像上传成功", result);
        } catch (Exception e) {
            return Result.error("头像上传失败：" + e.getMessage());
        }
    }

    @PostMapping("/upload/images")
    public Result<List<FileUploadResult>> uploadImages(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Result.error("请选择要上传的图片");
        }

        List<FileUploadResult> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                errors.add("文件 " + file.getOriginalFilename() + " 不能为空");
                continue;
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                errors.add("文件 " + file.getOriginalFilename() + " 不是图片");
                continue;
            }

            if (file.getSize() > 10 * 1024 * 1024) {
                errors.add("文件 " + file.getOriginalFilename() + " 超过 10MB");
                continue;
            }

            try {
                results.add(localFileStorageService.uploadImage(file));
            } catch (Exception e) {
                errors.add("文件 " + file.getOriginalFilename() + " 上传失败：" + e.getMessage());
            }
        }

        if (results.isEmpty()) {
            return Result.error("所有图片上传失败：" + String.join("；", errors));
        }

        return Result.success(results);
    }

    @PostMapping("/upload/file")
    public Result<FileUploadResult> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        String contentType = file.getContentType();
        List<String> allowedTypes = Arrays.asList(
                "application/pdf",
                "image/jpeg", "image/png", "image/gif", "image/webp", "image/jpg"
        );
        if (contentType == null || !allowedTypes.contains(contentType)) {
            return Result.error("不支持的文件格式，仅支持 PDF、JPG、PNG、GIF、WebP");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.error("文件大小不能超过 10MB");
        }

        try {
            String filePath;
            if ("application/pdf".equals(contentType)) {
                filePath = localFileStorageService.uploadContract(file);
            } else {
                filePath = localFileStorageService.uploadImage(file).getUrl();
            }

            FileUploadResult result = new FileUploadResult();
            result.setUrl(filePath);
            result.setOriginalName(file.getOriginalFilename());
            result.setSize(file.getSize());
            result.setContentType(contentType);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/delete/image")
    public Result<?> deleteImage(@RequestParam String url) {
        if (url == null || url.isEmpty()) {
            return Result.error("图片 URL 不能为空");
        }

        try {
            boolean deleted = localFileStorageService.deleteFile(url);
            if (!deleted) {
                return Result.error("删除图片失败");
            }
            return Result.success("图片已删除");
        } catch (Exception e) {
            return Result.error("删除图片失败：" + e.getMessage());
        }
    }
}
