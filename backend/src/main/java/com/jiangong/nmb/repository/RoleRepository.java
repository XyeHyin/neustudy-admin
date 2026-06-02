package com.jiangong.nmb.repository;

import com.jiangong.nmb.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findRoleByName(String name);

    long countRoleById(Long roldId);
}
