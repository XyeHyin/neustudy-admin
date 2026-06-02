package com.jiangong.nmb.config;

import com.jiangong.nmb.config.properties.AppProperties;
import com.jiangong.nmb.constant.PermissionConstants;
import com.jiangong.nmb.entity.Category;
import com.jiangong.nmb.entity.Permission;
import com.jiangong.nmb.entity.Role;
import com.jiangong.nmb.entity.User;
import com.jiangong.nmb.repository.CategoryRepository;
import com.jiangong.nmb.repository.PermissionRepository;
import com.jiangong.nmb.repository.RoleRepository;
import com.jiangong.nmb.repository.UserRepository;
import com.jiangong.nmb.utils.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        if (permissionRepository.count() == 0) {
            initPermissions();
        }

        // 初始化角色
        initRoles();

        // 初始化管理员用户
        initAdminUser();

        // 初始化分类数据
        initCategories();
    }

    private void initPermissions() {
        // 仪表盘
        permissionRepository.save(new Permission(null, PermissionConstants.DASHBOARD, "仪表盘", new HashSet<>()));

        // 用户管理
        permissionRepository.save(new Permission(null, PermissionConstants.USER_LIST_ALL, "查看所有用户列表", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.USER_VIEW_ALL, "查看任意用户详情", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.USER_VIEW_SELF, "查看自己的信息", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.USER_CREATE_ALL, "创建用户", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.USER_EDIT_ALL, "编辑用户", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.USER_EDIT_SELF, "编辑自己的信息", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.USER_DELETE_ALL, "删除用户", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.USER_UPDATE_STATUS, "修改用户状态", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.USER_UPDATE_PASSWORD_ALL, "修改任意用户密码", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.USER_UPDATE_PASSWORD_SELF, "修改自己密码", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.USER_UPDATE_ROLE, "修改用户角色", new HashSet<>()));

        // 角色管理
        permissionRepository.save(new Permission(null, PermissionConstants.ROLE_LIST_ALL, "查看角色列表", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.ROLE_VIEW_ALL, "查看角色详情", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.ROLE_CREATE_ALL, "新增角色", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.ROLE_EDIT_ALL, "编辑角色", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.ROLE_DELETE_ALL, "删除角色", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.ROLE_ASSIGN_ALL, "分配角色权限", new HashSet<>()));

        // 权限管理
        permissionRepository.save(new Permission(null, PermissionConstants.PERMISSION_LIST_ALL, "查看权限列表", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PERMISSION_VIEW_ALL, "查看权限详情", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PERMISSION_CREATE_ALL, "新增权限", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PERMISSION_EDIT_ALL, "编辑权限", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PERMISSION_DELETE_ALL, "删除权限", new HashSet<>()));

        // 课程管理
        permissionRepository.save(new Permission(null, PermissionConstants.COURSE_LIST_ALL, "查看所有课程", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.COURSE_LIST_SELF, "查看自己的课程", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.COURSE_VIEW_ALL, "查看任意课程详情", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.COURSE_VIEW_SELF, "查看自己的课程详情", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.COURSE_CREATE_ALL, "创建任意课程", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.COURSE_CREATE_SELF, "创建自己的课程", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.COURSE_EDIT_ALL, "编辑任意课程", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.COURSE_EDIT_SELF, "编辑自己的课程", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.COURSE_DELETE_ALL, "删除任意课程", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.COURSE_DELETE_SELF, "删除自己的课程", new HashSet<>()));

        // 知识点管理
        permissionRepository.save(new Permission(null, PermissionConstants.KNOWLEDGE_POINT_LIST_ALL, "查看所有知识点", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.KNOWLEDGE_POINT_LIST_SELF, "查看自己的知识点", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.KNOWLEDGE_POINT_VIEW_ALL, "查看任意知识点详情", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.KNOWLEDGE_POINT_VIEW_SELF, "查看自己的知识点详情", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.KNOWLEDGE_POINT_CREATE_ALL, "创建任意知识点", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.KNOWLEDGE_POINT_CREATE_SELF, "创建自己的知识点", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.KNOWLEDGE_POINT_EDIT_ALL, "编辑任意知识点", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.KNOWLEDGE_POINT_EDIT_SELF, "编辑自己的知识点", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.KNOWLEDGE_POINT_DELETE_ALL, "删除任意知识点", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.KNOWLEDGE_POINT_DELETE_SELF, "删除自己的知识点", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.KNOWLEDGE_POINT_IMPORT, "导入知识点", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.KNOWLEDGE_POINT_EXPORT, "导出知识点", new HashSet<>()));

        // 题目管理
        permissionRepository.save(new Permission(null, PermissionConstants.QUESTION_LIST_ALL, "查看所有题目", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.QUESTION_LIST_SELF, "查看自己的题目", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.QUESTION_VIEW_ALL, "查看任意题目详情", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.QUESTION_VIEW_SELF, "查看自己的题目详情", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.QUESTION_CREATE_ALL, "创建任意题目", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.QUESTION_CREATE_SELF, "创建自己的题目", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.QUESTION_EDIT_ALL, "编辑任意题目", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.QUESTION_EDIT_SELF, "编辑自己的题目", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.QUESTION_DELETE_ALL, "删除任意题目", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.QUESTION_DELETE_SELF, "删除自己的题目", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.QUESTION_IMPORT, "导入题目", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.QUESTION_EXPORT, "导出题目", new HashSet<>()));

        // AI题目生成
        permissionRepository.save(new Permission(null, PermissionConstants.AI_GENERATE_QUESTION, "AI生成题目", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.AI_CONFIG_MANAGE, "AI配置管理", new HashSet<>()));
        // 文件管理
        permissionRepository.save(new Permission(null, PermissionConstants.FILE_UPLOAD, "上传文件", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.FILE_DELETE, "删除文件", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.FILE_VIEW, "查看文件", new HashSet<>()));

        // 系统统计
        permissionRepository.save(new Permission(null, PermissionConstants.STATISTICS_COURSE, "课程统计", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.STATISTICS_QUESTION, "题目统计", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.STATISTICS_EXERCISE, "练习统计", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.STATISTICS_USER, "用户统计", new HashSet<>()));

        // 分类管理
        permissionRepository.save(new Permission(null, PermissionConstants.CATEGORY_LIST_ALL, "查看分类列表", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.CATEGORY_VIEW_ALL, "查看分类详情", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.CATEGORY_CREATE_ALL, "新增分类", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.CATEGORY_EDIT_ALL, "编辑分类", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.CATEGORY_DELETE_ALL, "删除分类", new HashSet<>()));

        // 试卷管理权限
        permissionRepository.save(new Permission(null, PermissionConstants.PAPER_LIST_PUBLIC, "学生查看公开试卷", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PAPER_VIEW_PUBLIC, "学生查看公开试卷详情", new HashSet<>()));

        permissionRepository.save(new Permission(null, PermissionConstants.PAPER_LIST_SELF, "教师查看自己创建的试卷", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PAPER_VIEW_SELF, "教师查看自己创建的试卷详情", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PAPER_CREATE, "教师创建试卷", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PAPER_EDIT_SELF, "教师编辑自己创建的试卷", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PAPER_DELETE_SELF, "教师删除自己创建的试卷", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PAPER_PUBLISH_SELF, "教师发布自己创建的试卷", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PAPER_ARCHIVE_SELF, "教师归档自己创建的试卷", new HashSet<>()));

        permissionRepository.save(new Permission(null, PermissionConstants.PAPER_LIST_ALL, "管理员查看所有试卷", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PAPER_VIEW_ALL, "管理员查看所有试卷详情", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PAPER_EDIT_ALL, "管理员编辑所有试卷", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PAPER_DELETE_ALL, "管理员删除所有试卷", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PAPER_PUBLISH_ALL, "管理员发布所有试卷", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PAPER_ARCHIVE_ALL, "管理员归档所有试卷", new HashSet<>()));

        // 组卷功能权限
        permissionRepository.save(new Permission(null, PermissionConstants.PAPER_MANAGE_QUESTIONS, "管理试卷题目", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PAPER_QUESTION_ADD, "添加题目到试卷", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PAPER_QUESTION_REMOVE, "从试卷移除题目", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PAPER_QUESTION_REORDER, "调整试卷题目顺序", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PAPER_QUESTION_SCORE, "设置试卷题目分值", new HashSet<>()));

        // 判分管理
        permissionRepository.save(new Permission(null, PermissionConstants.GRADING_AUTO, "自动判分", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.GRADING_AI, "AI判分", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.GRADING_MANUAL, "手动判分", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.GRADING_VIEW, "查看判分结果", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.GRADING_REVIEW_LIST, "查看待批改题目列表", new HashSet<>()));

        // 统计管理
        permissionRepository.save(new Permission(null, PermissionConstants.STATISTICS_PAPER_VIEW, "查看试卷统计", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.STATISTICS_QUESTION_VIEW, "查看题目统计", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.STATISTICS_STUDENT_VIEW, "查看学生统计", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.STATISTICS_OVERVIEW_VIEW, "查看统计概览", new HashSet<>()));

        // 练习相关权限
        permissionRepository.save(new Permission(null, PermissionConstants.PRACTICE_START, "开始练习", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PRACTICE_SUBMIT, "提交练习", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PRACTICE_LIST_SELF, "获取所有可用试卷", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PRACTICE_VIEW_SELF, "查看自己的练习会话", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PRACTICE_RECORD_LIST_SELF, "列出自己的练习记录", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PRACTICE_RECORD_LIST_ALL, "列出所有的练习记录", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PRACTICE_RESULT_VIEW_SELF, "查看自己的练习结果", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PRACTICE_RESULT_VIEW_ALL, "查看所有的练习结果", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PRACTICE_MARK, "题目标记", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PRACTICE_OVERVIEW_SELF, "查看自己的练习进度概览", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PRACTICE_HISTORY_SELF, "查看自己的练习历史", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PRACTICE_STATISTICS_SELF, "查看自己的练习统计", new HashSet<>()));
        permissionRepository.save(new Permission(null, PermissionConstants.PRACTICE_DETAIL_SELF, "查看自己的练习题目详情", new HashSet<>()));
    }

    private void initRoles() {
        // 初始化超级管理员角色
        if (roleRepository.count() == 0) {
            List<Permission> all = permissionRepository.findAll();
            Role admin = new Role();
            admin.setName("admin");
            admin.setDescription("超级管理员");
            admin.setPermissions(new HashSet<>(all));
            roleRepository.save(admin);
        }

        // 初始化教师角色
        if (roleRepository.findRoleByName("teacher") == null) {
            Role teacherRole = new Role();
            teacherRole.setName("teacher");
            teacherRole.setDescription("教师");

            Set<Permission> teacherPermissions = new HashSet<>();
            // 仪表盘
            addPermissionIfExists(teacherPermissions, PermissionConstants.DASHBOARD);
            // 个人信息管理
            addPermissionIfExists(teacherPermissions, PermissionConstants.USER_VIEW_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.USER_EDIT_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.USER_UPDATE_PASSWORD_SELF);
            // 课程管理（自己的）
            addPermissionIfExists(teacherPermissions, PermissionConstants.COURSE_CREATE_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.COURSE_VIEW_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.COURSE_EDIT_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.COURSE_DELETE_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.COURSE_LIST_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.COURSE_LIST_ALL);
            // 分类管理
            addPermissionIfExists(teacherPermissions, PermissionConstants.CATEGORY_LIST_ALL);
            addPermissionIfExists(teacherPermissions, PermissionConstants.CATEGORY_VIEW_ALL);

            // 试卷管理权限
            addPermissionIfExists(teacherPermissions, PermissionConstants.PAPER_CREATE);
            addPermissionIfExists(teacherPermissions, PermissionConstants.PAPER_LIST_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.PAPER_LIST_ALL);
            addPermissionIfExists(teacherPermissions, PermissionConstants.PAPER_VIEW_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.PAPER_EDIT_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.PAPER_DELETE_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.PAPER_PUBLISH_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.PAPER_ARCHIVE_SELF);

            // 组卷功能权限
            addPermissionIfExists(teacherPermissions, PermissionConstants.PAPER_MANAGE_QUESTIONS);
            addPermissionIfExists(teacherPermissions, PermissionConstants.PAPER_QUESTION_ADD);
            addPermissionIfExists(teacherPermissions, PermissionConstants.PAPER_QUESTION_REMOVE);
            addPermissionIfExists(teacherPermissions, PermissionConstants.PAPER_QUESTION_REORDER);
            addPermissionIfExists(teacherPermissions, PermissionConstants.PAPER_QUESTION_SCORE);

            // 知识点管理（自己的）
            addPermissionIfExists(teacherPermissions, PermissionConstants.KNOWLEDGE_POINT_LIST_ALL);
            addPermissionIfExists(teacherPermissions, PermissionConstants.KNOWLEDGE_POINT_CREATE_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.KNOWLEDGE_POINT_VIEW_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.KNOWLEDGE_POINT_EDIT_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.KNOWLEDGE_POINT_DELETE_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.KNOWLEDGE_POINT_LIST_SELF);

            // 题目管理（自己的）
            addPermissionIfExists(teacherPermissions, PermissionConstants.QUESTION_CREATE_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.QUESTION_VIEW_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.QUESTION_EDIT_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.QUESTION_DELETE_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.QUESTION_LIST_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.QUESTION_LIST_ALL);
            addPermissionIfExists(teacherPermissions, PermissionConstants.QUESTION_IMPORT);
            addPermissionIfExists(teacherPermissions, PermissionConstants.QUESTION_EXPORT);

            // AI题目生成
            addPermissionIfExists(teacherPermissions, PermissionConstants.AI_GENERATE_QUESTION);

            // 文件管理
            addPermissionIfExists(teacherPermissions, PermissionConstants.FILE_UPLOAD);
            addPermissionIfExists(teacherPermissions, PermissionConstants.FILE_VIEW);

            // 统计权限
            addPermissionIfExists(teacherPermissions, PermissionConstants.STATISTICS_COURSE);
            addPermissionIfExists(teacherPermissions, PermissionConstants.STATISTICS_QUESTION);
            addPermissionIfExists(teacherPermissions, PermissionConstants.STATISTICS_EXERCISE);

            // 练习管理
            addPermissionIfExists(teacherPermissions, PermissionConstants.PRACTICE_OVERVIEW_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.PRACTICE_RESULT_VIEW_ALL);
            addPermissionIfExists(teacherPermissions, PermissionConstants.PRACTICE_RESULT_VIEW_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.PRACTICE_RECORD_LIST_ALL);
            addPermissionIfExists(teacherPermissions, PermissionConstants.PRACTICE_RECORD_LIST_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.PRACTICE_LIST_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.PRACTICE_VIEW_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.PRACTICE_HISTORY_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.PRACTICE_DETAIL_SELF);
            addPermissionIfExists(teacherPermissions, PermissionConstants.PRACTICE_SUBMIT);
            addPermissionIfExists(teacherPermissions, PermissionConstants.PRACTICE_MARK);
            addPermissionIfExists(teacherPermissions, PermissionConstants.PRACTICE_START);
            addPermissionIfExists(teacherPermissions, PermissionConstants.PRACTICE_STATISTICS_SELF);

            // 判分管理
            addPermissionIfExists(teacherPermissions, PermissionConstants.GRADING_AUTO);
            addPermissionIfExists(teacherPermissions, PermissionConstants.GRADING_AI);
            addPermissionIfExists(teacherPermissions, PermissionConstants.GRADING_MANUAL);
            addPermissionIfExists(teacherPermissions, PermissionConstants.GRADING_VIEW);
            addPermissionIfExists(teacherPermissions, PermissionConstants.GRADING_REVIEW_LIST);

            // 统计管理
            addPermissionIfExists(teacherPermissions, PermissionConstants.STATISTICS_PAPER_VIEW);
            addPermissionIfExists(teacherPermissions, PermissionConstants.STATISTICS_QUESTION_VIEW);
            addPermissionIfExists(teacherPermissions, PermissionConstants.STATISTICS_STUDENT_VIEW);
            addPermissionIfExists(teacherPermissions, PermissionConstants.STATISTICS_OVERVIEW_VIEW);

            teacherRole.setPermissions(teacherPermissions);
            roleRepository.save(teacherRole);
        }

        // 初始化学生角色
        if (roleRepository.findRoleByName("student") == null) {
            Role studentRole = new Role();
            studentRole.setName("student");
            studentRole.setDescription("学生");

            Set<Permission> studentPermissions = new HashSet<>();
            // 基础权限
            addPermissionIfExists(studentPermissions, PermissionConstants.DASHBOARD);
            addPermissionIfExists(studentPermissions, PermissionConstants.USER_VIEW_SELF);
            addPermissionIfExists(studentPermissions, PermissionConstants.USER_EDIT_SELF);
            addPermissionIfExists(studentPermissions, PermissionConstants.USER_UPDATE_PASSWORD_SELF);

            // 练习相关权限
            addPermissionIfExists(studentPermissions, PermissionConstants.PRACTICE_START);
            addPermissionIfExists(studentPermissions, PermissionConstants.PRACTICE_SUBMIT);
            addPermissionIfExists(studentPermissions, PermissionConstants.PRACTICE_LIST_SELF);
            addPermissionIfExists(studentPermissions, PermissionConstants.PRACTICE_VIEW_SELF);
            addPermissionIfExists(studentPermissions, PermissionConstants.PRACTICE_RECORD_LIST_SELF);
            addPermissionIfExists(studentPermissions, PermissionConstants.PRACTICE_RESULT_VIEW_SELF);
            addPermissionIfExists(studentPermissions, PermissionConstants.PRACTICE_MARK);
            addPermissionIfExists(studentPermissions, PermissionConstants.PRACTICE_OVERVIEW_SELF);
            addPermissionIfExists(studentPermissions, PermissionConstants.PRACTICE_HISTORY_SELF);
            addPermissionIfExists(studentPermissions, PermissionConstants.PRACTICE_DETAIL_SELF);

            // 试卷查看权限（学生只能查看公开试卷）
            addPermissionIfExists(studentPermissions, PermissionConstants.PAPER_LIST_PUBLIC);
            addPermissionIfExists(studentPermissions, PermissionConstants.PAPER_VIEW_PUBLIC);

            // 文件权限
            addPermissionIfExists(studentPermissions, PermissionConstants.FILE_VIEW);
            addPermissionIfExists(studentPermissions, PermissionConstants.FILE_UPLOAD);

            studentRole.setPermissions(studentPermissions);
            roleRepository.save(studentRole);
        }
    }

    private void addPermissionIfExists(Set<Permission> permissions, String permissionCode) {
        try {
            List<Permission> foundPermissions = permissionRepository.findAll().stream().filter(p -> p.getCode().equals(permissionCode)).toList();

            if (!foundPermissions.isEmpty()) {
                permissions.add(foundPermissions.get(0));
                if (foundPermissions.size() > 1) {
                    log.warn("发现重复权限代码: {}, 共{}个，已选择第一个", permissionCode, foundPermissions.size());
                }
            } else {
                log.warn("权限不存在: {}", permissionCode);
            }
        } catch (Exception e) {
            log.error("添加权限失败: {}, 错误: {}", permissionCode, e.getMessage());
        }
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
