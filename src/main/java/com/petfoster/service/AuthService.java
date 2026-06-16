package com.petfoster.service;

import com.petfoster.common.BusinessException;
import com.petfoster.config.JwtTokenProvider;
import com.petfoster.dto.AuthDTO;
import com.petfoster.entity.User;
import com.petfoster.repository.UserRepository;
import com.petfoster.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthDTO.LoginResponse register(AuthDTO.RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw BusinessException.badRequest("用户名已存在");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw BusinessException.badRequest("邮箱已被注册");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .avatar(request.getAvatar())
                .build();

        user = userRepository.save(user);
        log.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        long expiresIn = jwtTokenProvider.getExpiration() / 1000;

        return new AuthDTO.LoginResponse(token, expiresIn, EntityMapper.toUserInfo(user));
    }

    public AuthDTO.LoginResponse login(AuthDTO.LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> BusinessException.unauthorized("用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw BusinessException.unauthorized("用户名或密码错误");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        long expiresIn = jwtTokenProvider.getExpiration() / 1000;

        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());
        return new AuthDTO.LoginResponse(token, expiresIn, EntityMapper.toUserInfo(user));
    }

    public AuthDTO.UserInfo getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));
        return EntityMapper.toUserInfo(user);
    }

    @Transactional
    public AuthDTO.UserInfo updateProfile(Long userId, AuthDTO.UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        if (StringUtils.hasText(request.getEmail())) {
            if (!request.getEmail().equals(user.getEmail())
                    && userRepository.existsByEmail(request.getEmail())) {
                throw BusinessException.badRequest("邮箱已被使用");
            }
            user.setEmail(request.getEmail());
        }

        if (StringUtils.hasText(request.getAvatar())) {
            user.setAvatar(request.getAvatar());
        }

        if (StringUtils.hasText(request.getOldPassword()) && StringUtils.hasText(request.getNewPassword())) {
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
                throw BusinessException.badRequest("原密码不正确");
            }
            if (request.getNewPassword().length() < 6) {
                throw BusinessException.badRequest("新密码长度不能小于6位");
            }
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }

        user = userRepository.save(user);
        log.info("用户资料更新成功: userId={}", userId);
        return EntityMapper.toUserInfo(user);
    }
}
