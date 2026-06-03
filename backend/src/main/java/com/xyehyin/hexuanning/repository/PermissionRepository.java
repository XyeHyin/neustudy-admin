package com.xyehyin.hexuanning.repository;

import com.xyehyin.hexuanning.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Permission findByName(String name);

    Permission findByCode(String code);

    @Query("select p from Permission p join p.roles r where r.id = :roleId")
    List<Permission> findAllByRoleId(Long roleId);
}
