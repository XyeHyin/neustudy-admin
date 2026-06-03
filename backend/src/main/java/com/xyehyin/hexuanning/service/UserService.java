package com.xyehyin.hexuanning.service;

import cn.hutool.core.exceptions.StatefulException;
import cn.hutool.http.HttpStatus;
import com.xyehyin.hexuanning.entity.User;
import com.xyehyin.hexuanning.repository.RoleRepository;
import com.xyehyin.hexuanning.repository.UserRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService extends BaseService<User, Long> {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    protected UserRepository getRepository() {
        return userRepository;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean delete(Long userId) {
        return deleteById(userId);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Page<User> findPage(int page, int size, String keyword, Boolean enabled) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id"));
        return userRepository.findByKeywordAndEnabled(
                (keyword == null || keyword.isBlank()) ? null : keyword,
                enabled,
                pageable
        );
    }

    public long countByRoleId(Long roleId) {
        return roleRepository.countRoleById(roleId);
    }

    /**
     * 根据角色名称查找用户
     */
    public List<User> findByRoleName(String roleName) {
        return userRepository.findByRolesName(roleName);
    }

    /**
     * 分页查询教师用户（支持关键字搜索）
     */
    public Page<User> findTeacherPageWithFilters(int page, int size, String keyword, Integer enabled) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Boolean enabledBoolean = null;
        if (enabled != null && enabled != 2) {
            enabledBoolean = enabled == 1;
        }
        return userRepository.findTeacherPageWithFilters(keyword, enabledBoolean, pageable);
    }
}
