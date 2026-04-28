package com.reams.common.result;

import lombok.Data;

/**
 * 文件上传结果封装
 */
@Data
public class FileUploadResult {
    
    /**
     * 文件 URL
     */
    private String url;
    
    /**
     * 文件原始名称
     */
    private String originalName;
    
    /**
     * 文件大小 (字节)
     */
    private Long size;
    
    /**
     * 文件类型
     */
    private String contentType;
    
    /**
     * 文件 ID(可用于删除)
     */
    private String fileId;
    
    public static FileUploadResult of(String url, String originalName, Long size, String contentType) {
        FileUploadResult result = new FileUploadResult();
        result.setUrl(url);
        result.setOriginalName(originalName);
        result.setSize(size);
        result.setContentType(contentType);
        return result;
    }
}
