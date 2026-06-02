package com.jiangong.nmb.controller;

import com.jiangong.nmb.common.ApiResponse;
import com.jiangong.nmb.dto.auth.LoginDTO;
import com.jiangong.nmb.dto.auth.RegisterDTO;
import com.jiangong.nmb.dto.auth.SendCodeDTO;
import com.jiangong.nmb.entity.Role;
import com.jiangong.nmb.entity.User;
import com.jiangong.nmb.mapper.AuthMapper;
import com.jiangong.nmb.service.EmailService;
import com.jiangong.nmb.service.RoleService;
import com.jiangong.nmb.service.UserService;
import com.jiangong.nmb.vo.auth.LoginVO;
import com.jiangong.nmb.vo.auth.RegisterVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private UserService userService;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private AuthController authController;

    private User testUser;
    private Role testRole;
    private LoginVO loginVO;
    private RegisterVO registerVO;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("student");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(testRole);
        testUser.setEnabled(true);

        loginVO = new LoginVO();
        loginVO.setId(1L);
        loginVO.setUsername("testuser");
        loginVO.setToken("testToken");

        registerVO = new RegisterVO();
        registerVO.setId(1L);
        registerVO.setUsername("testuser");
        registerVO.setToken("testToken");
    }

    @Test
    void testLogin_Success() {
        // Given
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("password123");

        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(authMapper.toLoginVO(eq(testUser), anyString())).thenReturn(loginVO);

        // Mock static methods
        try (MockedStatic<com.jiangong.nmb.utils.PasswordUtil> passwordUtil = mockStatic(
                com.jiangong.nmb.utils.PasswordUtil.class);
                MockedStatic<com.jiangong.nmb.utils.JWTUtil> jwtUtil = mockStatic(
                        com.jiangong.nmb.utils.JWTUtil.class)) {

            passwordUtil.when(() -> com.jiangong.nmb.utils.PasswordUtil.matches("password123", "encodedPassword"))
                    .thenReturn(true);
            jwtUtil.when(() -> com.jiangong.nmb.utils.JWTUtil.generateToken(1L, "testuser"))
                    .thenReturn("testToken");

            // When
            ApiResponse<LoginVO> result = authController.login(loginDTO);

            // Then
            assertNotNull(result);
            assertTrue(result.getSuccess());
            assertEquals("testuser", result.getData().getUsername());
            assertEquals("testToken", result.getData().getToken());

            verify(userService).findByUsername("testuser");
            verify(authMapper).toLoginVO(testUser, "testToken");
        }
    }

    @Test
    void testSendCode() {
        // Given
        SendCodeDTO dto = new SendCodeDTO();
        dto.setEmail("test@example.com");

        doNothing().when(emailService).sendVerifyCode("test@example.com");

        // When
        ApiResponse<Void> result = authController.sendCode(dto);

        // Then
        assertNotNull(result);
        assertTrue(result.getSuccess());

        verify(emailService).sendVerifyCode("test@example.com");
    }

    @Test
    void testRegister_Success() {
        // Given
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser");
        dto.setEmail("new@example.com");
        dto.setPassword("password123");
        dto.setNickname("nickname");
        dto.setPhone("12345678901");
        dto.setCode("123456");

        when(emailService.verifyCode("new@example.com", "123456")).thenReturn(true);
        when(userService.findByUsername("newuser")).thenReturn(null);
        when(roleService.findRoleByName("student")).thenReturn(testRole);
        when(userService.save(any(User.class))).thenReturn(testUser);
        when(userService.findById(testUser.getId())).thenReturn(testUser);
        when(authMapper.toRegisterVO(eq(testUser), anyString())).thenReturn(registerVO);

        // Mock static methods
        try (MockedStatic<com.jiangong.nmb.utils.PasswordUtil> passwordUtil = mockStatic(
                com.jiangong.nmb.utils.PasswordUtil.class);
                MockedStatic<com.jiangong.nmb.utils.JWTUtil> jwtUtil = mockStatic(
                        com.jiangong.nmb.utils.JWTUtil.class)) {

            passwordUtil.when(() -> com.jiangong.nmb.utils.PasswordUtil.encode("password123"))
                    .thenReturn("encodedPassword");
            jwtUtil.when(() -> com.jiangong.nmb.utils.JWTUtil.generateToken(1L, "testuser"))
                    .thenReturn("testToken");

            // When
            ApiResponse<RegisterVO> result = authController.register(dto);

            // Then
            assertNotNull(result);
            assertTrue(result.getSuccess());
            assertEquals("testuser", result.getData().getUsername());

            verify(emailService).verifyCode("new@example.com", "123456");
            verify(userService).findByUsername("newuser");
            verify(roleService).findRoleByName("student");
            verify(userService).save(any(User.class));
        }
    }
}
