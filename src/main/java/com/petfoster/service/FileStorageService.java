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

    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] GIF_MAGIC = {0x47, 0x49, 0x46, 0x38};
    private static final byte[] WEBP_RIFF = {0x52, 0x49, 0x46, 0x46};
    private static final byte[] WEBP_FORMAT = {0x57, 0x45, 0x42, 0x50};

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

        validateRealFileType(file, extension);

        String newFilename = generateFileName(extension);
        Path targetPath = uploadPath.resolve(newFilename).normalize();

        if (!targetPath.startsWith(uploadPath)) {
            throw BusinessException.badRequest("非法的文件路径");
        }

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
            String filename = extractFileName(fileUrl);
            if (!isValidFileName(filename)) {
                log.warn("检测到潜在的目录逃逸攻击: {}", fileUrl);
                return false;
            }

            Path filePath = uploadPath.resolve(filename).normalize();

            if (!filePath.startsWith(uploadPath)) {
                log.warn("检测到目录逃逸攻击，已拒绝删除: {}", fileUrl);
                return false;
            }

            if (!Files.exists(filePath)) {
                return false;
            }

            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("文件删除成功: {}", filename);
            }
            return deleted;
        } catch (Exception e) {
            log.warn("删除文件失败: {}", fileUrl, e);
            return false;
        }
    }

    private void validateRealFileType(MultipartFile file, String extension) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[12];
            int bytesRead = is.read(header);

            if (bytesRead < 4) {
                throw BusinessException.badRequest("文件内容异常，无法识别文件类型");
            }

            boolean isValidType = switch (extension.toLowerCase()) {
                case ".jpg", ".jpeg" -> matchesMagic(header, JPEG_MAGIC);
                case ".png" -> matchesMagic(header, PNG_MAGIC);
                case ".gif" -> matchesMagic(header, GIF_MAGIC);
                case ".webp" -> isWebp(header);
                default -> false;
            };

            if (!isValidType) {
                throw BusinessException.badRequest(
                    "文件真实类型与后缀名不匹配，请上传有效的图片文件");
            }

            log.debug("文件类型校验通过: {}", extension);
        } catch (IOException e) {
            throw BusinessException.badRequest("读取文件内容失败");
        }
    }

    private boolean matchesMagic(byte[] header, byte[] magic) {
        if (header.length < magic.length) {
            return false;
        }
        for (int i = 0; i < magic.length; i++) {
            if (header[i] != magic[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean isWebp(byte[] header) {
        if (header.length < 12) {
            return false;
        }
        if (!matchesMagic(header, WEBP_RIFF)) {
            return false;
        }
        for (int i = 0; i < WEBP_FORMAT.length; i++) {
            if (header[8 + i] != WEBP_FORMAT[i]) {
                return false;
            }
        }
        return true;
    }

    private String extractFileName(String fileUrl) {
        String path = fileUrl;
        if (path.contains("?")) {
            path = path.substring(0, path.indexOf("?"));
        }
        if (path.contains("#")) {
            path = path.substring(0, path.indexOf("#"));
        }
        if (path.contains("/")) {
            path = path.substring(path.lastIndexOf("/") + 1);
        }
        if (path.contains("\\")) {
            path = path.substring(path.lastIndexOf("\\") + 1);
        }
        return path;
    }

    private boolean isValidFileName(String filename) {
        if (!StringUtils.hasText(filename)) {
            return false;
        }
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return false;
        }
        if (filename.startsWith(".") || filename.endsWith(".")) {
            return false;
        }
        if (filename.length() > 255) {
            return false;
        }
        return true;
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

    public Path getUploadPath() {
        return uploadPath;
    }
}
