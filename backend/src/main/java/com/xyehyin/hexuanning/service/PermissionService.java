package com.xyehyin.hexuanning.service;

import com.xyehyin.hexuanning.entity.Permission;
import com.xyehyin.hexuanning.entity.Role;
import com.xyehyin.hexuanning.repository.PermissionRepository;
import com.xyehyin.hexuanning.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PermissionService extends BaseService<Permission, Long> {
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    @Override
    protected PermissionRepository getRepository() {
        return permissionRepository;
    }

    public boolean assignPermissions(Role role, List<Long> permissionIds) {
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            return false;
        }
        role.setPermissions(new HashSet<>(permissions));
        roleRepository.save(role);
        return true;
    }
}
