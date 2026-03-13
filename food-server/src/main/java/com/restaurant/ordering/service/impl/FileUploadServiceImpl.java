package com.restaurant.ordering.service.impl;

import com.restaurant.ordering.model.dto.response.FileUploadResponse;
import com.restaurant.ordering.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${file.base-url:http://localhost:${server.port}${server.servlet.context-path}}")
    private String baseUrl;

    // 允许的图片扩展名
    private static final String[] ALLOWED_IMAGE_EXTENSIONS = {
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    };

    // 最大文件大小：5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Override
    public FileUploadResponse uploadFile(MultipartFile file) {
        try {
            // 验证文件
            validateFile(file, false);

            // 生成文件名
            String fileName = generateFileName(file.getOriginalFilename());

            // 创建上传目录
            String filePath = createUploadDirectory();

            // 保存文件
            Path targetLocation = Paths.get(filePath).resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 构建访问URL
            String fileUrl = buildFileUrl(fileName);

            log.info("文件上传成功: {} -> {}", fileName, fileUrl);

            return FileUploadResponse.success(
                    file.getOriginalFilename(),
                    fileUrl,
                    file.getContentType(),
                    file.getSize()
            );

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return FileUploadResponse.error("文件上传失败: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("文件验证失败", e);
            return FileUploadResponse.error(e.getMessage());
        }
    }

    @Override
    public FileUploadResponse uploadImage(MultipartFile file) {
        try {
            // 验证图片
            validateFile(file, true);

            // 生成文件名
            String fileName = generateFileName(file.getOriginalFilename());

            // 创建上传目录（按日期分类）
            String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String filePath = createUploadDirectory(dateDir);

            // 保存文件
            Path targetLocation = Paths.get(filePath).resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 构建访问URL
            String fileUrl = buildImageUrl(dateDir, fileName);

            log.info("图片上传成功: {} -> {}", fileName, fileUrl);

            return FileUploadResponse.success(
                    file.getOriginalFilename(),
                    fileUrl,
                    file.getContentType(),
                    file.getSize()
            );

        } catch (IOException e) {
            log.error("图片上传失败", e);
            return FileUploadResponse.error("图片上传失败: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("图片验证失败", e);
            return FileUploadResponse.error(e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            // 从URL中提取文件路径
            String filePath = extractFilePathFromUrl(fileUrl);
            if (filePath == null) {
                return false;
            }

            File file = new File(filePath);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("文件删除成功: {}", filePath);
                }
                return deleted;
            }
            return false;
        } catch (Exception e) {
            log.error("文件删除失败: {}", fileUrl, e);
            return false;
        }
    }

    private void validateFile(MultipartFile file, boolean isImage) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过5MB");
        }

        // 检查文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        // 检查文件扩展名
        String fileExtension = getFileExtension(originalFilename);
        if (fileExtension == null || fileExtension.isEmpty()) {
            throw new IllegalArgumentException("不支持的文件类型");
        }

        // 如果是图片，检查扩展名
        if (isImage && !isAllowedImageExtension(fileExtension)) {
            throw new IllegalArgumentException("只允许上传图片文件 (jpg, jpeg, png, gif, bmp, webp)");
        }
    }

    private String generateFileName(String originalFilename) {
        String fileExtension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + "." + fileExtension;
    }

    private String createUploadDirectory() throws IOException {
        return createUploadDirectory("");
    }

    private String createUploadDirectory(String subDir) throws IOException {
        String fullPath = uploadDir;
        if (StringUtils.hasText(subDir)) {
            fullPath += File.separator + subDir;
        }

        File directory = new File(fullPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new IOException("无法创建上传目录: " + fullPath);
            }
            log.info("创建上传目录: {}", fullPath);
        }
        return fullPath;
    }

    private String buildFileUrl(String fileName) {
        return baseUrl + "/uploads/" + fileName;
    }

    private String buildImageUrl(String dateDir, String fileName) {
        return baseUrl + "/uploads/" + dateDir.replace("\\", "/") + "/" + fileName;
    }

    private String extractFilePathFromUrl(String fileUrl) {
        try {
            // 从URL中提取相对路径
            String relativePath = fileUrl.replace(baseUrl + "/uploads/", "");
            return uploadDir + File.separator + relativePath.replace("/", File.separator);
        } catch (Exception e) {
            log.error("无法从URL提取文件路径: {}", fileUrl, e);
            return null;
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }
        return null;
    }

    private boolean isAllowedImageExtension(String extension) {
        for (String allowed : ALLOWED_IMAGE_EXTENSIONS) {
            if (allowed.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
}