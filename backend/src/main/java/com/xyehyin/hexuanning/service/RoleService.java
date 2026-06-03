package com.xyehyin.hexuanning.service;

import com.xyehyin.hexuanning.entity.Role;
import com.xyehyin.hexuanning.repository.PermissionRepository;
import com.xyehyin.hexuanning.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService extends BaseService<Role, Long> {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    protected RoleRepository getRepository() {
        return roleRepository;
    }

    public Role create(Role role, List<Long> permissionIds) {
        role.setPermissions(new HashSet<>(permissionRepository.findAllById(permissionIds)));
        return save(role);
    }

    public boolean delete(Long roleId) {
        return deleteById(roleId);
    }

    public Role findRoleByName(String name) {
        return roleRepository.findRoleByName(name);
    }

    public Role update(Role role, List<Long> permissionIds) {
        Role existingRole = roleRepository.findById(role.getId()).orElse(null);
        if (existingRole == null) {
            return null;
        }
        existingRole.setName(role.getName());
        existingRole.setDescription(role.getDescription());
        existingRole.setPermissions(new HashSet<>(permissionRepository.findAllById(permissionIds)));
        return save(existingRole);
    }
}
