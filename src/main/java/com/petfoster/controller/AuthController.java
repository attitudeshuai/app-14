package com.petfoster.controller;

import com.petfoster.common.ApiResponse;
import com.petfoster.dto.AuthDTO;
import com.petfoster.entity.User;
import com.petfoster.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "用户认证", description = "用户注册、登录、个人信息管理接口")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户，返回JWT Token和用户信息")
    public ApiResponse<AuthDTO.LoginResponse> register(
            @Valid @RequestBody AuthDTO.RegisterRequest request) {
        return ApiResponse.success("注册成功", authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名密码登录，返回JWT Token")
    public ApiResponse<AuthDTO.LoginResponse> login(
            @Valid @RequestBody AuthDTO.LoginRequest request) {
        return ApiResponse.success("登录成功", authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "需要JWT认证")
    public ApiResponse<AuthDTO.UserInfo> getCurrentUser(@AuthenticationPrincipal User user) {
        return ApiResponse.success(authService.getCurrentUser(user.getId()));
    }

    @PutMapping("/me")
    @Operation(summary = "更新个人信息", description = "需要JWT认证，可更新邮箱、头像、密码")
    public ApiResponse<AuthDTO.UserInfo> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody AuthDTO.UpdateProfileRequest request) {
        return ApiResponse.success("更新成功", authService.updateProfile(user.getId(), request));
    }
}
