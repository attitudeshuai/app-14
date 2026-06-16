package com.petfoster.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageConfig {

    private String uploadDir = "uploads";

    private long maxFileSize = 5 * 1024 * 1024;

    private String[] allowedExtensions = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
}
