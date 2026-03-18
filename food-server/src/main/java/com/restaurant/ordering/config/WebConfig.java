package com.restaurant.ordering.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将上传目录映射为静态资源访问路径
        String uploadPath = new File(uploadDir).getAbsolutePath() + File.separator;

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);

        // 添加 /api/uploads/** 映射（解决 baseUrl 包含 /api 时的访问问题）
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations("file:" + uploadPath);

        // 如果上传目录在类路径下，也添加类路径映射
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}