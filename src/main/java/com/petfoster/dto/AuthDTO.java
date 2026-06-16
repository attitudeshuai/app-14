package com.petfoster.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDTO {

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度应在3-50之间")
        private String username;

        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 100, message = "密码长度应在6-100之间")
        private String password;

        private String avatar;
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @Data
    public static class UpdateProfileRequest {
        private String email;
        private String avatar;
        private String oldPassword;
        private String newPassword;
    }

    @Data
    public static class LoginResponse {
        private String token;
        private String tokenType;
        private Long expiresIn;
        private UserInfo user;

        public LoginResponse(String token, Long expiresIn, UserInfo user) {
            this.token = token;
            this.tokenType = "Bearer";
            this.expiresIn = expiresIn;
            this.user = user;
        }
    }

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String avatar;
        private String createdAt;

        public UserInfo(Long id, String username, String email, String avatar, String createdAt) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.avatar = avatar;
            this.createdAt = createdAt;
        }
    }
}
