package com.reams.service.impl;

import com.reams.common.result.FileUploadResult;
import com.reams.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 本地文件存储服务，统一复用 FileUploadUtil 管理上传目录与路径。
 */
@Service
public class LocalFileStorageService {

    @Autowired
    private FileUploadUtil fileUploadUtil;

    public FileUploadResult uploadImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("只能上传图片文件");
        }

        List<String> allowedTypes = Arrays.asList(
                "image/jpeg", "image/png", "image/gif", "image/webp", "image/jpg"
        );
        if (!allowedTypes.contains(contentType)) {
            throw new IllegalArgumentException("不支持的图片格式，仅支持 JPG、PNG、GIF、WebP");
        }

        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("图片大小不能超过 10MB");
        }

        try {
            String fileUrl = fileUploadUtil.uploadFile(file, FileUploadUtil.FileType.HOUSE);
            return FileUploadResult.of(fileUrl, file.getOriginalFilename(), file.getSize(), contentType);
        } catch (IOException e) {
            throw new IOException("图片上传失败: " + e.getMessage(), e);
        }
    }

    public List<FileUploadResult> uploadImages(MultipartFile[] files) throws IOException {
        List<FileUploadResult> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                results.add(uploadImage(file));
            } catch (Exception e) {
                errors.add("文件 " + file.getOriginalFilename() + " 上传失败: " + e.getMessage());
            }
        }

        if (!errors.isEmpty() && results.isEmpty()) {
            throw new IOException(String.join("; ", errors));
        }

        return results;
    }

    public String uploadAvatar(MultipartFile file, String userType) throws IOException {
        FileUploadUtil.FileType fileType;
        switch (userType.toLowerCase()) {
            case "admin":
                fileType = FileUploadUtil.FileType.AVATAR_ADMIN;
                break;
            case "agent":
                fileType = FileUploadUtil.FileType.AVATAR_AGENT;
                break;
            case "customer":
            default:
                fileType = FileUploadUtil.FileType.AVATAR_CUSTOMER;
                break;
        }

        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("只能上传图片文件");
        }

        return fileUploadUtil.uploadFile(file, fileType);
    }

    public String uploadContract(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("application/pdf") || contentType.startsWith("image/"))) {
            throw new IllegalArgumentException("合同文件必须是 PDF 或图片格式");
        }

        return fileUploadUtil.uploadFile(file, FileUploadUtil.FileType.CONTRACT);
    }

    public boolean deleteFile(String filePath) {
        return fileUploadUtil.deleteFile(filePath);
    }
}
