package com.xyehyin.hexuanning.config;

import com.xyehyin.hexuanning.config.properties.AppProperties;
import com.xyehyin.hexuanning.config.seed.PermissionSeed;
import com.xyehyin.hexuanning.config.seed.RoleSeed;
import com.xyehyin.hexuanning.entity.Category;
import com.xyehyin.hexuanning.entity.Permission;
import com.xyehyin.hexuanning.entity.Role;
import com.xyehyin.hexuanning.entity.User;
import com.xyehyin.hexuanning.repository.CategoryRepository;
import com.xyehyin.hexuanning.repository.PermissionRepository;
import com.xyehyin.hexuanning.repository.RoleRepository;
import com.xyehyin.hexuanning.repository.UserRepository;
import com.xyehyin.hexuanning.security.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DataInitConfig implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AppProperties appProperties;

    public DataInitConfig(PermissionRepository permissionRepository, RoleRepository roleRepository, UserRepository userRepository, CategoryRepository categoryRepository, AppProperties appProperties) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.appProperties = appProperties;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 初始化权限
        initPermissions();

        // 初始化角色
        initRoles();

        // 初始化管理员用户
        initAdminUser();

        // 初始化分类数据
        initCategories();
    }

    private void initPermissions() {
        Map<String, Permission> permissionsByCode = permissionRepository.findAll().stream()
                .collect(Collectors.toMap(Permission::getCode, permission -> permission, (left, right) -> left));

        for (PermissionSeed seed : PermissionSeed.defaults()) {
            Permission permission = permissionsByCode.get(seed.code());
            if (permission == null) {
                permissionRepository.save(seed.toEntity());
                continue;
            }

            if (!seed.name().equals(permission.getName())) {
                permission.setName(seed.name());
                permissionRepository.save(permission);
            }
        }
    }

    private void initRoles() {
        if (roleRepository.count() == 0) {
            List<Permission> all = permissionRepository.findAll();
            Role admin = new Role();
            admin.setName("admin");
            admin.setDescription("超级管理员");
            admin.setPermissions(new HashSet<>(all));
            roleRepository.save(admin);
        }

        Map<String, List<Permission>> permissionsByCode = permissionRepository.findAll().stream()
                .collect(Collectors.groupingBy(Permission::getCode));

        syncAdminPermissions();
        RoleSeed.defaults().forEach(seed -> createRoleIfMissing(seed, permissionsByCode));
    }

    private void syncAdminPermissions() {
        Role admin = roleRepository.findRoleByName("admin");
        if (admin == null) {
            return;
        }

        Set<Permission> permissions = admin.getPermissions() == null ? new HashSet<>() : new HashSet<>(admin.getPermissions());
        permissions.addAll(permissionRepository.findAll());
        admin.setPermissions(permissions);
        roleRepository.save(admin);
    }

    private void createRoleIfMissing(RoleSeed seed, Map<String, List<Permission>> permissionsByCode) {
        Role existingRole = roleRepository.findRoleByName(seed.name());
        if (existingRole != null) {
            Set<Permission> permissions = existingRole.getPermissions() == null ? new HashSet<>() : new HashSet<>(existingRole.getPermissions());
            permissions.addAll(resolvePermissions(seed, permissionsByCode));
            existingRole.setPermissions(permissions);
            roleRepository.save(existingRole);
            return;
        }

        Role role = new Role();
        role.setName(seed.name());
        role.setDescription(seed.description());
        role.setPermissions(resolvePermissions(seed, permissionsByCode));
        roleRepository.save(role);
    }

    private Set<Permission> resolvePermissions(RoleSeed seed, Map<String, List<Permission>> permissionsByCode) {
        Set<Permission> permissions = new HashSet<>();
        for (String permissionCode : seed.permissionCodes()) {
            List<Permission> foundPermissions = permissionsByCode.get(permissionCode);

            if (foundPermissions == null || foundPermissions.isEmpty()) {
                log.warn("角色 {} 依赖的权限不存在: {}", seed.name(), permissionCode);
                continue;
            }

            permissions.add(foundPermissions.get(0));
            if (foundPermissions.size() > 1) {
                log.warn("发现重复权限代码: {}, 共 {} 个，已选择第一个", permissionCode, foundPermissions.size());
            }
        }
        return permissions;
    }
    private void initAdminUser() {
        // 初始化管理员用户
        AppProperties.Admin adminConfig = appProperties.getAdmin();
        if (userRepository.findByUsername(adminConfig.getUsername()) == null) {
            Role admin = roleRepository.findRoleByName("admin");
            User user = User.builder()
                    .username(adminConfig.getUsername())
                    .password(PasswordUtil.encode(adminConfig.getPassword()))
                    .nickname(adminConfig.getNickname())
                    .role(admin)
                    .enabled(true)
                    .email(adminConfig.getEmail())
                    .phone(adminConfig.getPhone())
                    .build();
            userRepository.save(user);
        }
    }

    private void initCategories() {
        // 如果已经有分类数据，就不初始化了
        if (categoryRepository.count() > 0) {
            return;
        }

        // 创建根分类：课程分类
        Category rootCategory = Category.builder().name("课程分类").description("教育管理系统课程分类根节点").enabled(true).parent(null).build();
        rootCategory = categoryRepository.save(rootCategory);

        // 创建二级分类：年级
        Category gradeCategory = Category.builder().name("年级").description("按年级分类的课程").enabled(true).parent(rootCategory).build();
        gradeCategory = categoryRepository.save(gradeCategory);

        // 创建二级分类：学科
        Category subjectCategory = Category.builder().name("学科").description("按学科分类的课程").enabled(true).parent(rootCategory).build();
        subjectCategory = categoryRepository.save(subjectCategory);

        // 创建年级下的三级分类
        String[] grades = {"小班", "中班", "大班", // 幼儿园
                "小学一年级", "小学二年级", "小学三年级", "小学四年级", "小学五年级", "小学六年级", // 小学
                "初中一年级", "初中二年级", "初中三年级", // 初中
                "高中一年级", "高中二年级", "高中三年级", // 高中
                "大学一年级", "大学二年级", "大学三年级", "大学四年级", // 大学
                "硕士一年级", "硕士二年级", "硕士三年级", // 硕士
                "博士一年级", "博士二年级" // 博士
        };

        for (String grade : grades) {
            Category gradeSubCategory = Category.builder().name(grade).description(grade + "相关课程").enabled(true).parent(gradeCategory).build();
            categoryRepository.save(gradeSubCategory);
        }

        // 创建学科下的三级分类
        String[] subjects = {"数学", "统计学", "物理", "化学", "生物", "地理", "历史", "政治", "语文", "英语", "计算机科学", "信息技术", "美术", "音乐", "体育", "思想品德", "科学", "经济学", "管理学", "法学", "医学", "工学", "农学", "哲学", "文学", "艺术学", "教育学"};

        for (String subject : subjects) {
            Category subjectSubCategory = Category.builder().name(subject).description(subject + "相关课程").enabled(true).parent(subjectCategory).build();
            categoryRepository.save(subjectSubCategory);
        }
    }
}
