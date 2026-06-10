package com.xyehyin.hexuanning.config.seed;

import com.xyehyin.hexuanning.constant.PermissionConstants;

import java.util.List;

public record RoleSeed(String name, String description, List<String> permissionCodes) {
    public static List<RoleSeed> defaults() {
        return List.of(teacher(), student());
    }

    private static RoleSeed teacher() {
        return new RoleSeed(
                "teacher",
                "教师",
                List.of(
                        PermissionConstants.DASHBOARD,
                        PermissionConstants.USER_VIEW_SELF,
                        PermissionConstants.USER_EDIT_SELF,
                        PermissionConstants.USER_UPDATE_PASSWORD_SELF,
                        PermissionConstants.COURSE_CREATE_SELF,
                        PermissionConstants.COURSE_VIEW_ALL,
                        PermissionConstants.COURSE_VIEW_SELF,
                        PermissionConstants.COURSE_EDIT_SELF,
                        PermissionConstants.COURSE_DELETE_SELF,
                        PermissionConstants.COURSE_LIST_SELF,
                        PermissionConstants.COURSE_LIST_ALL,
                        PermissionConstants.CATEGORY_LIST_ALL,
                        PermissionConstants.CATEGORY_VIEW_ALL,
                        PermissionConstants.PAPER_CREATE,
                        PermissionConstants.PAPER_LIST_SELF,
                        PermissionConstants.PAPER_LIST_ALL,
                        PermissionConstants.PAPER_VIEW_SELF,
                        PermissionConstants.PAPER_EDIT_SELF,
                        PermissionConstants.PAPER_DELETE_SELF,
                        PermissionConstants.PAPER_PUBLISH_SELF,
                        PermissionConstants.PAPER_ARCHIVE_SELF,
                        PermissionConstants.PAPER_MANAGE_QUESTIONS,
                        PermissionConstants.PAPER_QUESTION_ADD,
                        PermissionConstants.PAPER_QUESTION_REMOVE,
                        PermissionConstants.PAPER_QUESTION_REORDER,
                        PermissionConstants.PAPER_QUESTION_SCORE,
                        PermissionConstants.KNOWLEDGE_POINT_LIST_ALL,
                        PermissionConstants.KNOWLEDGE_POINT_CREATE_SELF,
                        PermissionConstants.KNOWLEDGE_POINT_VIEW_SELF,
                        PermissionConstants.KNOWLEDGE_POINT_EDIT_SELF,
                        PermissionConstants.KNOWLEDGE_POINT_DELETE_SELF,
                        PermissionConstants.KNOWLEDGE_POINT_LIST_SELF,
                        PermissionConstants.QUESTION_CREATE_SELF,
                        PermissionConstants.QUESTION_VIEW_SELF,
                        PermissionConstants.QUESTION_EDIT_SELF,
                        PermissionConstants.QUESTION_DELETE_SELF,
                        PermissionConstants.QUESTION_LIST_SELF,
                        PermissionConstants.QUESTION_LIST_ALL,
                        PermissionConstants.QUESTION_IMPORT,
                        PermissionConstants.QUESTION_EXPORT,
                        PermissionConstants.AI_GENERATE_QUESTION,
                        PermissionConstants.FILE_UPLOAD,
                        PermissionConstants.FILE_VIEW,
                        PermissionConstants.STATISTICS_COURSE,
                        PermissionConstants.STATISTICS_QUESTION,
                        PermissionConstants.STATISTICS_EXERCISE,
                        PermissionConstants.PRACTICE_OVERVIEW_SELF,
                        PermissionConstants.PRACTICE_RESULT_VIEW_ALL,
                        PermissionConstants.PRACTICE_RESULT_VIEW_SELF,
                        PermissionConstants.PRACTICE_RECORD_LIST_ALL,
                        PermissionConstants.PRACTICE_RECORD_LIST_SELF,
                        PermissionConstants.PRACTICE_LIST_SELF,
                        PermissionConstants.PRACTICE_VIEW_SELF,
                        PermissionConstants.PRACTICE_HISTORY_SELF,
                        PermissionConstants.PRACTICE_DETAIL_SELF,
                        PermissionConstants.PRACTICE_SUBMIT,
                        PermissionConstants.PRACTICE_MARK,
                        PermissionConstants.PRACTICE_START,
                        PermissionConstants.PRACTICE_STATISTICS_SELF,
                        PermissionConstants.GRADING_AUTO,
                        PermissionConstants.GRADING_AI,
                        PermissionConstants.GRADING_MANUAL,
                        PermissionConstants.GRADING_VIEW,
                        PermissionConstants.GRADING_REVIEW_LIST,
                        PermissionConstants.STATISTICS_PAPER_VIEW,
                        PermissionConstants.STATISTICS_QUESTION_VIEW,
                        PermissionConstants.STATISTICS_STUDENT_VIEW,
                        PermissionConstants.STATISTICS_OVERVIEW_VIEW
                )
        );
    }

    private static RoleSeed student() {
        return new RoleSeed(
                "student",
                "学生",
                List.of(
                        PermissionConstants.DASHBOARD,
                        PermissionConstants.USER_VIEW_SELF,
                        PermissionConstants.USER_EDIT_SELF,
                        PermissionConstants.USER_UPDATE_PASSWORD_SELF,
                        PermissionConstants.PRACTICE_START,
                        PermissionConstants.PRACTICE_SUBMIT,
                        PermissionConstants.PRACTICE_LIST_SELF,
                        PermissionConstants.PRACTICE_VIEW_SELF,
                        PermissionConstants.PRACTICE_RECORD_LIST_SELF,
                        PermissionConstants.PRACTICE_RESULT_VIEW_SELF,
                        PermissionConstants.PRACTICE_MARK,
                        PermissionConstants.PRACTICE_OVERVIEW_SELF,
                        PermissionConstants.PRACTICE_HISTORY_SELF,
                        PermissionConstants.PRACTICE_DETAIL_SELF,
                        PermissionConstants.PAPER_LIST_PUBLIC,
                        PermissionConstants.PAPER_VIEW_PUBLIC,
                        PermissionConstants.FILE_VIEW,
                        PermissionConstants.FILE_UPLOAD
                )
        );
    }
}
