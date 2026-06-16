package com.petfoster.controller;

import com.petfoster.common.ApiResponse;
import com.petfoster.common.PageResponse;
import com.petfoster.dto.PetDTO;
import com.petfoster.entity.User;
import com.petfoster.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
@Tag(name = "宠物管理", description = "宠物CRUD及搜索接口")
public class PetController {

    private final PetService petService;

    @GetMapping
    @Operation(summary = "获取宠物列表", description = "支持分页、搜索、筛选")
    public ApiResponse<PageResponse<PetDTO.PetResponse>> getPets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String species,
            @RequestParam(required = false) Long ownerId) {
        return ApiResponse.success(petService.getPets(page, size, sort, name, species, ownerId));
    }

    @GetMapping("/mine")
    @Operation(summary = "获取我的宠物", description = "获取当前登录用户的宠物列表，需要JWT认证")
    public ApiResponse<PageResponse<PetDTO.PetResponse>> getMyPets(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return ApiResponse.success(petService.getMyPets(user.getId(), page, size, sort));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取宠物详情")
    public ApiResponse<PetDTO.PetResponse> getPetById(@PathVariable Long id) {
        return ApiResponse.success(petService.getPetById(id));
    }

    @PostMapping
    @Operation(summary = "创建宠物(JSON格式)", description = "需要JWT认证，使用JSON格式提交数据，photoUrl为外部图片链接")
    public ApiResponse<PetDTO.PetResponse> createPet(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PetDTO.CreatePetRequest request) {
        return ApiResponse.success("创建成功", petService.createPet(user.getId(), request));
    }

    @PostMapping(value = "/with-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "创建宠物(带图片上传)", description = "需要JWT认证，支持直接上传本地图片文件，图片将自动保存并写入宠物信息")
    public ApiResponse<PetDTO.PetResponse> createPetWithPhoto(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String species,
            @RequestParam(required = false) String breed,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) String dietNotes,
            @RequestParam(required = false) String medicalNotes,
            @RequestParam(required = false) String photoUrl,
            @RequestPart(required = false) MultipartFile photo) {
        PetDTO.CreatePetRequest request = PetDTO.CreatePetRequest.builder()
                .name(name)
                .species(species)
                .breed(breed)
                .age(age)
                .dietNotes(dietNotes)
                .medicalNotes(medicalNotes)
                .photoUrl(photoUrl)
                .build();
        return ApiResponse.success("创建成功", petService.createPetWithPhoto(user.getId(), request, photo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新宠物信息(JSON格式)", description = "需要JWT认证，仅所有者可操作，使用JSON格式提交")
    public ApiResponse<PetDTO.PetResponse> updatePet(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestBody PetDTO.UpdatePetRequest request) {
        return ApiResponse.success("更新成功", petService.updatePet(user.getId(), id, request));
    }

    @PutMapping(value = "/{id}/with-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "更新宠物信息(带图片上传)", description = "需要JWT认证，仅所有者可操作，支持直接上传本地图片文件")
    public ApiResponse<PetDTO.PetResponse> updatePetWithPhoto(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String species,
            @RequestParam(required = false) String breed,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) String dietNotes,
            @RequestParam(required = false) String medicalNotes,
            @RequestParam(required = false) String photoUrl,
            @RequestPart(required = false) MultipartFile photo) {
        PetDTO.UpdatePetRequest request = PetDTO.UpdatePetRequest.builder()
                .name(name)
                .species(species)
                .breed(breed)
                .age(age)
                .dietNotes(dietNotes)
                .medicalNotes(medicalNotes)
                .photoUrl(photoUrl)
                .build();
        return ApiResponse.success("更新成功", petService.updatePetWithPhoto(user.getId(), id, request, photo));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除宠物", description = "需要JWT认证，仅所有者可操作")
    public ApiResponse<Void> deletePet(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        petService.deletePet(user.getId(), id);
        return ApiResponse.success("删除成功", null);
    }
}
