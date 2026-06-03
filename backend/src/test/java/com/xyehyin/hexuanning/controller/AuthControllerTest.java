package com.xyehyin.hexuanning.controller;

import com.xyehyin.hexuanning.common.ApiResponse;
import com.xyehyin.hexuanning.dto.auth.LoginDTO;
import com.xyehyin.hexuanning.dto.auth.RegisterDTO;
import com.xyehyin.hexuanning.dto.auth.SendCodeDTO;
import com.xyehyin.hexuanning.entity.Role;
import com.xyehyin.hexuanning.entity.User;
import com.xyehyin.hexuanning.mapper.AuthMapper;
import com.xyehyin.hexuanning.service.EmailService;
import com.xyehyin.hexuanning.service.RoleService;
import com.xyehyin.hexuanning.service.UserService;
import com.xyehyin.hexuanning.vo.auth.LoginVO;
import com.xyehyin.hexuanning.vo.auth.RegisterVO;
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
        try (MockedStatic<com.xyehyin.hexuanning.security.PasswordUtil> passwordUtil = mockStatic(
                com.xyehyin.hexuanning.security.PasswordUtil.class);
                MockedStatic<com.xyehyin.hexuanning.security.JWTUtil> jwtUtil = mockStatic(
                        com.xyehyin.hexuanning.security.JWTUtil.class)) {

            passwordUtil.when(() -> com.xyehyin.hexuanning.security.PasswordUtil.matches("password123", "encodedPassword"))
                    .thenReturn(true);
            jwtUtil.when(() -> com.xyehyin.hexuanning.security.JWTUtil.generateToken(1L, "testuser"))
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
        try (MockedStatic<com.xyehyin.hexuanning.security.PasswordUtil> passwordUtil = mockStatic(
                com.xyehyin.hexuanning.security.PasswordUtil.class);
                MockedStatic<com.xyehyin.hexuanning.security.JWTUtil> jwtUtil = mockStatic(
                        com.xyehyin.hexuanning.security.JWTUtil.class)) {

            passwordUtil.when(() -> com.xyehyin.hexuanning.security.PasswordUtil.encode("password123"))
                    .thenReturn("encodedPassword");
            jwtUtil.when(() -> com.xyehyin.hexuanning.security.JWTUtil.generateToken(1L, "testuser"))
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
