package com.petfoster.service;

import com.petfoster.common.BusinessException;
import com.petfoster.dto.AuthDTO;
import com.petfoster.entity.User;
import com.petfoster.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private com.petfoster.config.JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private AuthDTO.RegisterRequest registerRequest;
    private AuthDTO.LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .avatar("avatar_url")
                .build();

        registerRequest = new AuthDTO.RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setAvatar("new_avatar");

        loginRequest = new AuthDTO.LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("用户注册 - 成功场景")
    void testRegister_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateToken(anyLong(), anyString())).thenReturn("test-token");
        when(jwtTokenProvider.getExpiration()).thenReturn(86400000L);

        AuthDTO.LoginResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("test-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(86400, response.getExpiresIn());
        assertNotNull(response.getUser());
        assertEquals("testuser", response.getUser().getUsername());

        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("用户注册 - 用户名已存在")
    void testRegister_UsernameExists() {
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.register(registerRequest));

        assertEquals("用户名已存在", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("用户注册 - 邮箱已被注册")
    void testRegister_EmailExists() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.register(registerRequest));

        assertEquals("邮箱已被注册", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("用户登录 - 成功场景")
    void testLogin_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(1L, "testuser")).thenReturn("test-token");
        when(jwtTokenProvider.getExpiration()).thenReturn(86400000L);

        AuthDTO.LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("test-token", response.getToken());
        assertEquals("testuser", response.getUser().getUsername());
    }

    @Test
    @DisplayName("用户登录 - 用户不存在")
    void testLogin_UserNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.login(loginRequest));

        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("用户登录 - 密码错误")
    void testLogin_WrongPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.login(loginRequest));

        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("获取当前用户信息 - 成功")
    void testGetCurrentUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        AuthDTO.UserInfo userInfo = authService.getCurrentUser(1L);

        assertNotNull(userInfo);
        assertEquals(1L, userInfo.getId());
        assertEquals("testuser", userInfo.getUsername());
        assertEquals("test@example.com", userInfo.getEmail());
    }

    @Test
    @DisplayName("获取当前用户信息 - 用户不存在")
    void testGetCurrentUser_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.getCurrentUser(999L));

        assertEquals("用户不存在", exception.getMessage());
    }
}
