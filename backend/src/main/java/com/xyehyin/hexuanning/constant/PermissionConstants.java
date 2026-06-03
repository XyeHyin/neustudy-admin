package com.xyehyin.hexuanning.constant;

/**
 * 权限常量类
 * 定义系统中所有的权限代码
 */
public class PermissionConstants {

    // 仪表盘
    public static final String DASHBOARD = "dashboard";

    // 用户管理
    public static final String USER_LIST_ALL = "user:list:all";
    public static final String USER_VIEW_ALL = "user:view:all";
    public static final String USER_VIEW_SELF = "user:view:self";
    public static final String USER_CREATE_ALL = "user:create:all";
    public static final String USER_EDIT_ALL = "user:edit:all";
    public static final String USER_EDIT_SELF = "user:edit:self";
    public static final String USER_DELETE_ALL = "user:delete:all";
    public static final String USER_UPDATE_STATUS = "user:update:status";
    public static final String USER_UPDATE_PASSWORD_ALL = "user:update:password:all";
    public static final String USER_UPDATE_PASSWORD_SELF = "user:update:password:self";
    public static final String USER_UPDATE_ROLE = "user:update:role";

    // 角色管理
    public static final String ROLE_LIST_ALL = "role:list:all";
    public static final String ROLE_VIEW_ALL = "role:view:all";
    public static final String ROLE_CREATE_ALL = "role:create:all";
    public static final String ROLE_EDIT_ALL = "role:edit:all";
    public static final String ROLE_DELETE_ALL = "role:delete:all";
    public static final String ROLE_ASSIGN_ALL = "role:assign:all";

    // 权限管理
    public static final String PERMISSION_LIST_ALL = "permission:list:all";
    public static final String PERMISSION_VIEW_ALL = "permission:view:all";
    public static final String PERMISSION_CREATE_ALL = "permission:create:all";
    public static final String PERMISSION_EDIT_ALL = "permission:edit:all";
    public static final String PERMISSION_DELETE_ALL = "permission:delete:all";

    // 课程管理
    public static final String COURSE_LIST_ALL = "course:list:all";
    public static final String COURSE_LIST_SELF = "course:list:self";
    public static final String COURSE_VIEW_ALL = "course:view:all";
    public static final String COURSE_VIEW_SELF = "course:view:self";
    public static final String COURSE_CREATE_ALL = "course:create:all";
    public static final String COURSE_CREATE_SELF = "course:create:self";
    public static final String COURSE_EDIT_ALL = "course:edit:all";
    public static final String COURSE_EDIT_SELF = "course:edit:self";
    public static final String COURSE_DELETE_ALL = "course:delete:all";
    public static final String COURSE_DELETE_SELF = "course:delete:self";

    // 知识点管理
    public static final String KNOWLEDGE_POINT_LIST_ALL = "knowledge_point:list:all";
    public static final String KNOWLEDGE_POINT_LIST_SELF = "knowledge_point:list:self";
    public static final String KNOWLEDGE_POINT_VIEW_ALL = "knowledge_point:view:all";
    public static final String KNOWLEDGE_POINT_VIEW_SELF = "knowledge_point:view:self";
    public static final String KNOWLEDGE_POINT_CREATE_ALL = "knowledge_point:create:all";
    public static final String KNOWLEDGE_POINT_CREATE_SELF = "knowledge_point:create:self";
    public static final String KNOWLEDGE_POINT_EDIT_ALL = "knowledge_point:edit:all";
    public static final String KNOWLEDGE_POINT_EDIT_SELF = "knowledge_point:edit:self";
    public static final String KNOWLEDGE_POINT_DELETE_ALL = "knowledge_point:delete:all";
    public static final String KNOWLEDGE_POINT_DELETE_SELF = "knowledge_point:delete:self";
    public static final String KNOWLEDGE_POINT_IMPORT = "knowledge_point:import";
    public static final String KNOWLEDGE_POINT_EXPORT = "knowledge_point:export";

    // 题目管理
    public static final String QUESTION_LIST_ALL = "question:list:all";
    public static final String QUESTION_LIST_SELF = "question:list:self";
    public static final String QUESTION_VIEW_ALL = "question:view:all";
    public static final String QUESTION_VIEW_SELF = "question:view:self";
    public static final String QUESTION_CREATE_ALL = "question:create:all";
    public static final String QUESTION_CREATE_SELF = "question:create:self";
    public static final String QUESTION_EDIT_ALL = "question:edit:all";
    public static final String QUESTION_EDIT_SELF = "question:edit:self";
    public static final String QUESTION_DELETE_ALL = "question:delete:all";
    public static final String QUESTION_DELETE_SELF = "question:delete:self";
    public static final String QUESTION_IMPORT = "question:import";
    public static final String QUESTION_EXPORT = "question:export";

    // AI题目生成
    public static final String AI_GENERATE_QUESTION = "ai:generate:question";
    public static final String AI_CONFIG_MANAGE = "ai:config:manage";

    // 文件管理
    public static final String FILE_UPLOAD = "file:upload";
    public static final String FILE_DELETE = "file:delete";
    public static final String FILE_VIEW = "file:view";

    // 系统统计
    public static final String STATISTICS_COURSE = "statistics:course";
    public static final String STATISTICS_QUESTION = "statistics:question";
    public static final String STATISTICS_EXERCISE = "statistics:exercise";
    public static final String STATISTICS_USER = "statistics:user";

    // 分类管理
    public static final String CATEGORY_LIST_ALL = "category:list:all";
    public static final String CATEGORY_VIEW_ALL = "category:view:all";
    public static final String CATEGORY_CREATE_ALL = "category:create:all";
    public static final String CATEGORY_EDIT_ALL = "category:edit:all";
    public static final String CATEGORY_DELETE_ALL = "category:delete:all";

    // 试卷管理权限 - 分层设计
    public static final String PAPER_LIST_PUBLIC = "paper:list:public";           // 学生查看公开试卷
    public static final String PAPER_VIEW_PUBLIC = "paper:view:public";           // 学生查看公开试卷详情

    public static final String PAPER_LIST_SELF = "paper:list:self";               // 教师查看自己创建的试卷
    public static final String PAPER_VIEW_SELF = "paper:view:self";               // 教师查看自己创建的试卷详情
    public static final String PAPER_CREATE = "paper:create";                     // 教师创建试卷
    public static final String PAPER_EDIT_SELF = "paper:edit:self";               // 教师编辑自己创建的试卷
    public static final String PAPER_DELETE_SELF = "paper:delete:self";           // 教师删除自己创建的试卷
    public static final String PAPER_PUBLISH_SELF = "paper:publish:self";         // 教师发布自己创建的试卷
    public static final String PAPER_ARCHIVE_SELF = "paper:archive:self";         // 教师归档自己创建的试卷

    public static final String PAPER_LIST_ALL = "paper:list:all";                 // 管理员查看所有试卷
    public static final String PAPER_VIEW_ALL = "paper:view:all";                 // 管理员查看所有试卷详情
    public static final String PAPER_EDIT_ALL = "paper:edit:all";                 // 管理员编辑所有试卷
    public static final String PAPER_DELETE_ALL = "paper:delete:all";             // 管理员删除所有试卷
    public static final String PAPER_PUBLISH_ALL = "paper:publish:all";           // 管理员发布所有试卷
    public static final String PAPER_ARCHIVE_ALL = "paper:archive:all";           // 管理员归档所有试卷

    // 组卷功能权限
    public static final String PAPER_MANAGE_QUESTIONS = "paper:manage:questions"; // 管理试卷题目
    public static final String PAPER_QUESTION_ADD = "paper:question:add";         // 添加题目到试卷
    public static final String PAPER_QUESTION_REMOVE = "paper:question:remove";   // 从试卷移除题目
    public static final String PAPER_QUESTION_REORDER = "paper:question:reorder"; // 调整试卷题目顺序
    public static final String PAPER_QUESTION_SCORE = "paper:question:score";     // 设置试卷题目分值

    // ================= 判分管理 =================
    public static final String GRADING_AUTO = "grading:auto"; // 自动判分
    public static final String GRADING_AI = "grading:ai"; // AI判分
    public static final String GRADING_MANUAL = "grading:manual"; // 人工复核
    public static final String GRADING_VIEW = "grading:view"; // 查看判分结果
    public static final String GRADING_REVIEW_LIST = "grading:review:list"; // 待人工复核列表

    // ================= 统计管理 =================
    public static final String STATISTICS_PAPER_VIEW = "statistics:paper:view"; // 试卷统计
    public static final String STATISTICS_QUESTION_VIEW = "statistics:question:view"; // 题目统计
    public static final String STATISTICS_STUDENT_VIEW = "statistics:student:view"; // 学生统计
    public static final String STATISTICS_OVERVIEW_VIEW = "statistics:overview:view"; // 总览统计

    // 练习相关权限
    public static final String PRACTICE_START = "practice:start";
    public static final String PRACTICE_SUBMIT = "practice:submit";
    public static final String PRACTICE_LIST_SELF = "practice:list:self";
    public static final String PRACTICE_VIEW_SELF = "practice:view:self";
    public static final String PRACTICE_RECORD_LIST_ALL = "practice:record:list:all";
    public static final String PRACTICE_RECORD_LIST_SELF = "practice:record:list:self";
    public static final String PRACTICE_RESULT_VIEW_ALL = "practice:result:view:all";
    public static final String PRACTICE_RESULT_VIEW_SELF = "practice:result:view:self";
    public static final String PRACTICE_MARK = "practice:mark";
    public static final String PRACTICE_OVERVIEW_SELF = "practice:overview:self";
    public static final String PRACTICE_HISTORY_SELF = "practice:history:self";
    public static final String PRACTICE_STATISTICS_SELF = "practice:statistics:self";
    public static final String PRACTICE_DETAIL_SELF = "practice:detail:self";
}