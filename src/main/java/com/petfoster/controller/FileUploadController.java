package com.petfoster.controller;

import com.petfoster.common.ApiResponse;
import com.petfoster.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Tag(name = "文件上传", description = "图片上传接口")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传单张图片", description = "需要JWT认证")
    public ApiResponse<String> uploadImage(@RequestPart("file") MultipartFile file) {
        String fileUrl = fileStorageService.uploadFile(file);
        return ApiResponse.success("上传成功", fileUrl);
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "批量上传图片", description = "需要JWT认证")
    public ApiResponse<List<String>> uploadImages(@RequestPart("files") MultipartFile[] files) {
        List<String> fileUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileUrl = fileStorageService.uploadFile(file);
            fileUrls.add(fileUrl);
        }
        return ApiResponse.success("上传成功", fileUrls);
    }
}
