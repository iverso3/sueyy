package com.restaurant.ordering.service;

import com.restaurant.ordering.model.dto.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {

    /**
     * 上传文件
     * @param file 文件
     * @return 上传响应
     */
    FileUploadResponse uploadFile(MultipartFile file);

    /**
     * 上传图片（限制图片格式）
     * @param file 图片文件
     * @return 上传响应
     */
    FileUploadResponse uploadImage(MultipartFile file);

    /**
     * 删除文件
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    boolean deleteFile(String fileUrl);
}