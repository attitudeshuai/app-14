package com.petfoster.service;

import com.petfoster.common.BusinessException;
import com.petfoster.config.FileStorageConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileStorageConfig fileStorageConfig;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(fileStorageConfig.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
            log.info("文件上传目录初始化完成: {}", uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("无法创建文件上传目录", e);
        }
    }

    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest("文件不能为空");
        }

        if (file.getSize() > fileStorageConfig.getMaxFileSize()) {
            throw BusinessException.badRequest("文件大小超过限制，最大允许 " + 
                (fileStorageConfig.getMaxFileSize() / 1024 / 1024) + "MB");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(originalFilename);

        if (!isAllowedExtension(extension)) {
            throw BusinessException.badRequest("不支持的文件格式，仅支持: " + 
                String.join(", ", fileStorageConfig.getAllowedExtensions()));
        }

        String newFilename = generateFileName(extension);
        Path targetPath = uploadPath.resolve(newFilename);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("文件上传成功: {} -> {}", originalFilename, newFilename);
            return "/uploads/" + newFilename;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw BusinessException.badRequest("文件上传失败: " + e.getMessage());
        }
    }

    public boolean deleteFile(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return false;
        }

        try {
            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = uploadPath.resolve(filename);
            return Files.deleteIfExists(filePath);
        } catch (Exception e) {
            log.warn("删除文件失败: {}", fileUrl, e);
            return false;
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    private boolean isAllowedExtension(String extension) {
        return Arrays.asList(fileStorageConfig.getAllowedExtensions())
                .contains(extension.toLowerCase());
    }

    private String generateFileName(String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return timestamp + "_" + uuid + extension;
    }
}
