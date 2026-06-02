package com.jiangong.nmb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiangong.nmb.common.ApiResponse;
import com.jiangong.nmb.common.PageResult;
import com.jiangong.nmb.dto.user.CreateUserDTO;
import com.jiangong.nmb.dto.user.UpdateUserDTO;
import com.jiangong.nmb.entity.Role;
import com.jiangong.nmb.entity.User;
import com.jiangong.nmb.mapper.UserMapper;
import com.jiangong.nmb.service.RoleService;
import com.jiangong.nmb.service.UserService;
import com.jiangong.nmb.vo.user.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private RoleService roleService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;
    private User testUser;
    private UserVO testUserVO;
    private Role testRole;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("teacher");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(testRole);
        testUser.setEnabled(true);

        testUserVO = new UserVO();
        testUserVO.setId(1L);
        testUserVO.setUsername("testuser");
        testUserVO.setEmail("test@example.com");
        testUserVO.setEnabled(true);
    }

    @Test
    void testList() {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userService.findAll()).thenReturn(users);
        when(userMapper.toUserVO(any(User.class))).thenReturn(testUserVO);

        // When
        ApiResponse<List<UserVO>> result = userController.list();

        // Then
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        assertEquals("testuser", result.getData().get(0).getUsername());

        verify(userService).findAll();
        verify(userMapper).toUserVO(testUser);
    }

    @Test
    void testPage() {
        // Given
        Page<User> userPage = new PageImpl<>(Arrays.asList(testUser));
        when(userService.findPage(anyInt(), anyInt(), any(), any())).thenReturn(userPage);
        when(userMapper.toUserVO(any(User.class))).thenReturn(testUserVO);

        // When
        ApiResponse<PageResult<UserVO>> result = userController.page(1, 10, null, 2);

        // Then
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getContent().size());
        assertEquals(1L, result.getData().getTotal());

        verify(userService).findPage(1, 10, null, null);
    }

    @Test
    void testListTeachers() {
        // Given
        List<User> teachers = Arrays.asList(testUser);
        when(userService.findByRoleName("teacher")).thenReturn(teachers);
        when(userMapper.toUserVO(any(User.class))).thenReturn(testUserVO);

        // When
        ApiResponse<List<UserVO>> result = userController.listTeachers();

        // Then
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertEquals(1, result.getData().size());

        verify(userService).findByRoleName("teacher");
    }

    @Test
    void testCreate() {
        // Given
        CreateUserDTO createDTO = new CreateUserDTO();
        createDTO.setUsername("newuser");
        createDTO.setEmail("new@example.com");
        createDTO.setPassword("password123");
        createDTO.setRoleId(1L);

        when(roleService.findById(1L)).thenReturn(testRole);
        when(userService.findByUsername("newuser")).thenReturn(null);
        when(userMapper.toUser(any(CreateUserDTO.class))).thenReturn(testUser);
        when(userService.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toUserVO(any(User.class))).thenReturn(testUserVO);

        // When
        ApiResponse<UserVO> result = userController.create(createDTO);

        // Then
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertNotNull(result.getData());

        verify(roleService).findById(1L);
        verify(userService).findByUsername("newuser");
        verify(userService).save(any(User.class));
    }

    @Test
    void testDelete() {
        // Given
        when(userService.delete(1L)).thenReturn(true);

        // When
        ApiResponse<Boolean> result = userController.delete(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertTrue(result.getData());

        verify(userService).delete(1L);
    }

    @Test
    void testUpdateProfile() {
        // Given
        UpdateUserDTO updateDTO = new UpdateUserDTO();
        updateDTO.setEmail("updated@example.com");

        when(userService.findById(1L)).thenReturn(testUser);
        when(userService.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toUserVO(any(User.class))).thenReturn(testUserVO);

        // When
        ApiResponse<UserVO> result = userController.updateProfile(1L, updateDTO);

        // Then
        assertNotNull(result);
        assertTrue(result.getSuccess());

        verify(userService).findById(1L);
        verify(userService).save(testUser);
        verify(userMapper).updateUserFromDto(updateDTO, testUser);
    }
}
