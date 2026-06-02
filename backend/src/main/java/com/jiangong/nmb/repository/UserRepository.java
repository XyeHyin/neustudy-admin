package com.jiangong.nmb.repository;

import com.jiangong.nmb.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    User findByEmail(String email);

    @Query("SELECT u FROM User u WHERE (:keyword IS NULL OR u.username LIKE %:keyword% OR u.nickname LIKE %:keyword% OR u.email LIKE %:keyword%) AND (:enabled IS NULL OR u.enabled = :enabled)")
    Page<User> findByKeywordAndEnabled(@Param("keyword") String keyword, @Param("enabled") Boolean enabled, Pageable pageable);

    /**
     * 根据角色名称查找用户
     */
    @Query("SELECT u FROM User u JOIN u.role r WHERE r.name = :roleName")
    List<User> findByRolesName(@Param("roleName") String roleName);

    /**
     * 分页查询教师用户（支持关键字搜索）
     */
    @Query("SELECT u FROM User u JOIN u.role r WHERE r.name = 'teacher' " +
           "AND (:keyword IS NULL OR u.username LIKE %:keyword% OR u.email LIKE %:keyword% OR u.nickname LIKE %:keyword%) " +
           "AND (:enabled IS NULL OR u.enabled = :enabled)")
    Page<User> findTeacherPageWithFilters(
            @Param("keyword") String keyword,
            @Param("enabled") Boolean enabled,
            Pageable pageable
    );
}