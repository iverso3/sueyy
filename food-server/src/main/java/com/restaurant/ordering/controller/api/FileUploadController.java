package com.restaurant.ordering.controller.api;

import com.restaurant.ordering.model.dto.response.ApiResponse;
import com.restaurant.ordering.model.dto.response.FileUploadResponse;
import com.restaurant.ordering.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {

    private final FileUploadService fileUploadService;

    /**
     * 上传文件（通用）
     */
    @PostMapping("/file")
    public ApiResponse<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("收到文件上传请求: {} ({} bytes)",
                file.getOriginalFilename(), file.getSize());

        FileUploadResponse response = fileUploadService.uploadFile(file);
        return ApiResponse.success(response);
    }

    /**
     * 上传图片（限制图片格式）
     */
    @PostMapping("/image")
    public ApiResponse<FileUploadResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("收到图片上传请求: {} ({} bytes)",
                file.getOriginalFilename(), file.getSize());

        FileUploadResponse response = fileUploadService.uploadImage(file);
        return ApiResponse.success(response);
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/file")
    public ApiResponse<Boolean> deleteFile(@RequestParam("fileUrl") String fileUrl) {
        log.info("收到文件删除请求: {}", fileUrl);

        boolean deleted = fileUploadService.deleteFile(fileUrl);
        if (deleted) {
            return ApiResponse.success("文件删除成功", true);
        } else {
            return ApiResponse.error("文件删除失败");
        }
    }

    /**
     * 批量上传图片
     */
    @PostMapping("/images")
    public ApiResponse<?> uploadImages(@RequestParam("files") MultipartFile[] files) {
        log.info("收到批量图片上传请求: {} 个文件", files.length);

        if (files == null || files.length == 0) {
            return ApiResponse.error("请选择要上传的图片");
        }

        if (files.length > 10) {
            return ApiResponse.error("一次最多上传10张图片");
        }

        FileUploadResponse[] responses = new FileUploadResponse[files.length];
        for (int i = 0; i < files.length; i++) {
            responses[i] = fileUploadService.uploadImage(files[i]);
        }

        return ApiResponse.success("批量上传完成", responses);
    }
}