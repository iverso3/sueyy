package com.restaurant.ordering.model.dto.response;

import lombok.Data;

@Data
public class FileUploadResponse {
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long size;
    private String message;

    public static FileUploadResponse success(String fileName, String fileUrl, String fileType, Long size) {
        FileUploadResponse response = new FileUploadResponse();
        response.setFileName(fileName);
        response.setFileUrl(fileUrl);
        response.setFileType(fileType);
        response.setSize(size);
        response.setMessage("上传成功");
        return response;
    }

    public static FileUploadResponse error(String message) {
        FileUploadResponse response = new FileUploadResponse();
        response.setMessage(message);
        return response;
    }
}