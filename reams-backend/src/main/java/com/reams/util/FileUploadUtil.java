package com.reams.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传工具类 - 统一管理文件存储目录结构
 */
@Slf4j
@Component
public class FileUploadUtil {

    @Value("${file.upload-dir:./uploads}")
    private String baseUploadDir;

    // 日期格式化
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM");

    /**
     * 应用启动时初始化上传目录
     */
    @PostConstruct
    public void init() {
        try {
            // 转换为绝对路径
            String absolutePath = baseUploadDir;
            if (!Paths.get(absolutePath).isAbsolute()) {
                absolutePath = System.getProperty("user.dir") + File.separator + baseUploadDir.replace("./", "").replace("/", File.separator);
            }

            Path basePath = Paths.get(absolutePath);
            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
                System.out.println("✅ 创建基础上传目录: " + absolutePath);
            }

            // 预创建所有子目录
            for (FileType type : FileType.values()) {
                Path typePath = basePath.resolve(type.getPath());
                if (!Files.exists(typePath)) {
                    Files.createDirectories(typePath);
                    System.out.println("✅ 创建子目录: " + typePath);
                }
            }

            System.out.println("✅ 文件上传目录初始化完成");
        } catch (IOException e) {
            log.error("❌ 初始化上传目录失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 文件类型枚举
     */
    public enum FileType {
        HOUSE("houses"),           // 房源图片
        AVATAR_ADMIN("avatars/admin"),      // 管理员头像
        AVATAR_AGENT("avatars/agent"),      // 中介头像
        AVATAR_CUSTOMER("avatars/customer"),// 客户头像
        CONTRACT("contracts"),     // 合同文件
        IDCARD("idcards");         // 身份证件

        private final String path;

        FileType(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }

    /**
     * 上传文件 (按日期归档)
     * @param file 上传的文件
     * @param fileType 文件类型
     * @return 文件访问路径
     */
    public String uploadFile(MultipartFile file, FileType fileType) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 1. 生成文件名 (UUID + 原扩展名)
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

        // 2. 创建目录 (按年月归档)
        String datePath = DATE_FORMAT.format(new Date());

        // 使用配置的绝对路径或转换为绝对路径
        String uploadBasePath = baseUploadDir;
        if (!Paths.get(uploadBasePath).isAbsolute()) {
            // 如果是相对路径，转换为项目根目录的绝对路径
            uploadBasePath = System.getProperty("user.dir") + File.separator + uploadBasePath.replace("./", "").replace("/", File.separator);
        }

        String dirPath = uploadBasePath + File.separator + fileType.getPath() + File.separator + datePath.replace("/", File.separator);

        Path dir = Paths.get(dirPath);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
            System.out.println("创建上传目录: " + dirPath);
        }

        // 3. 保存文件
        Path filePath = dir.resolve(fileName);
        file.transferTo(filePath.toFile());

        // 4. 返回访问路径 (用于前端访问，统一使用正斜杠)
        return "/uploads/" + fileType.getPath() + "/" + datePath + "/" + fileName;
    }

    /**
     * 上传通用文件 (默认存储到 houses 目录)
     * @param file 上传的文件
     * @return 文件访问路径
     */
    public String uploadFile(MultipartFile file) throws IOException {
        return uploadFile(file, FileType.HOUSE);
    }

    /**
     * 删除文件
     * @param filePath 文件路径 (相对路径，如：/uploads/houses/2026/04/xxx.jpg)
     * @return 是否删除成功
     */
    public boolean deleteFile(String filePath) {
        try {
            // 去掉开头的 /
            if (filePath.startsWith("/")) {
                filePath = filePath.substring(1);
            }

            Path path = Paths.get(baseUploadDir + filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                return true;
            }
            return false;
        } catch (IOException e) {
            log.error("删除文件失败: {}, Error: {}", filePath, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取文件的完整物理路径
     * @param accessPath 访问路径 (如：/uploads/houses/2026/04/xxx.jpg)
     * @return 物理路径
     */
    public String getPhysicalPath(String accessPath) {
        if (accessPath.startsWith("/")) {
            accessPath = accessPath.substring(1);
        }
        return baseUploadDir + "/" + accessPath;
    }
}
