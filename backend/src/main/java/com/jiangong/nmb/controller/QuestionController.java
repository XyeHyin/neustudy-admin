package com.jiangong.nmb.controller;

import cn.hutool.core.exceptions.StatefulException;
import cn.hutool.http.HttpStatus;
import com.jiangong.nmb.common.ApiResponse;
import com.jiangong.nmb.common.PageResult;
import com.jiangong.nmb.constant.PermissionConstants;
import com.jiangong.nmb.dto.question.AIGenerateQuestionDTO;
import com.jiangong.nmb.dto.question.BatchCreateQuestionDTO;
import com.jiangong.nmb.dto.question.CreateQuestionDTO;
import com.jiangong.nmb.dto.question.UpdateQuestionDTO;
import com.jiangong.nmb.entity.KnowledgePoint;
import com.jiangong.nmb.entity.Question;
import com.jiangong.nmb.mapper.QuestionMapper;
import com.jiangong.nmb.service.KnowledgePointService;
import com.jiangong.nmb.service.QuestionService;
import com.jiangong.nmb.vo.question.QuestionDetailVO;
import com.jiangong.nmb.vo.question.QuestionVO;
import com.jiangong.nmb.vo.question.QuestionHistoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 题目管理控制器
 */
@Slf4j
@Tag(name = "题目管理", description = "题目相关接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/questions")
@CrossOrigin
public class QuestionController extends BaseController {

    private final QuestionService questionService;
    private final KnowledgePointService knowledgePointService;
    private final QuestionMapper questionMapper;

    @Operation(summary = "获取所有题目", description = "管理员查看所有题目")
    @PreAuthorize("hasAuthority('" + PermissionConstants.QUESTION_LIST_ALL + "')")
    @GetMapping
    public ApiResponse<List<QuestionVO>> listAll() {
        List<Question> questions = questionService.findAll();
        List<QuestionVO> questionVOs = questions.stream()
                .map(questionMapper::toQuestionVO)
                .toList();
        return ApiResponse.success(questionVOs);
    }

    @Operation(summary = "获取我的题目", description = "教师查看自己的题目")
    @PreAuthorize("hasAuthority('" + PermissionConstants.QUESTION_LIST_SELF + "')")
    @GetMapping("/my")
    public ApiResponse<List<QuestionVO>> listMy(HttpServletRequest request) {
        Long teacherId = getCurrentUserId(request);
        // 这里需要通过关联查询获取教师的所有题目
        // 暂时使用分页查询的逻辑
        Page<Question> questionPage = questionService.findPageByTeacherWithFilters(
                teacherId, 1, Integer.MAX_VALUE, null, null, null, null, null);
        List<QuestionVO> questionVOs = questionPage.getContent().stream()
                .map(questionMapper::toQuestionVO)
                .toList();
        return ApiResponse.success(questionVOs);
    }

    @Operation(summary = "根据知识点获取题目", description = "获取指定知识点的题目")
    @PreAuthorize("hasAuthority('" + PermissionConstants.QUESTION_LIST_ALL + "') or " +
            "hasAuthority('" + PermissionConstants.QUESTION_LIST_SELF + "')")
    @GetMapping("/knowledge-point/{knowledgePointId}")
    public ApiResponse<List<QuestionVO>> listByKnowledgePoint(@PathVariable Long knowledgePointId,
                                                             @RequestParam(required = false) Boolean enabled,
                                                             HttpServletRequest request) {
        KnowledgePoint knowledgePoint = knowledgePointService.findById(knowledgePointId);
        if (knowledgePoint == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "知识点不存在");
        }

        // 检查权限：如果只有查看自己的权限，需要验证所有权
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();

        boolean hasViewAll = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals(PermissionConstants.QUESTION_LIST_ALL));

        if (!hasViewAll) {
            Long currentUserId = getCurrentUserId(request);
            if (!knowledgePoint.getCourse().getTeacher().getId().equals(currentUserId)) {
                throw new StatefulException(HttpStatus.HTTP_FORBIDDEN, "您只能查看自己的题目");
            }
        }

        List<Question> questions;
        if (enabled != null) {
            questions = questionService.findByKnowledgePointIdAndEnabled(knowledgePointId, enabled);
        } else {
            questions = questionService.findByKnowledgePointId(knowledgePointId);
        }

        List<QuestionVO> questionVOs = questions.stream()
                .map(questionMapper::toQuestionVO)
                .toList();
        return ApiResponse.success(questionVOs);
    }

    @Operation(summary = "分页获取所有题目", description = "管理员分页查看所有题目")
    @PreAuthorize("hasAuthority('" + PermissionConstants.QUESTION_LIST_ALL + "')")
    @GetMapping("/page")
    public ApiResponse<PageResult<QuestionVO>> pageAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Question.QuestionType type,
            @RequestParam(required = false) Question.Difficulty difficulty,
            @RequestParam(required = false) Long knowledgePointId) {

        Page<Question> questionPage = questionService.findPageWithFilters(
                page, size, keyword, enabled, type, difficulty, knowledgePointId);
        List<QuestionVO> questionVOs = questionPage.getContent().stream()
                .map(questionMapper::toQuestionVO)
                .toList();

        PageResult<QuestionVO> result = new PageResult<>();
        result.setContent(questionVOs);
        result.setTotal(questionPage.getTotalElements());
        result.setPage(page);
        result.setSize(size);
        return ApiResponse.success(result);
    }

    @Operation(summary = "分页获取我的题目", description = "教师分页查看自己的题目")
    @PreAuthorize("hasAuthority('" + PermissionConstants.QUESTION_LIST_SELF + "')")
    @GetMapping("/my/page")
    public ApiResponse<PageResult<QuestionVO>> pageMy(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Question.QuestionType type,
            @RequestParam(required = false) Question.Difficulty difficulty,
            @RequestParam(required = false) Long knowledgePointId) {

        Long teacherId = getCurrentUserId(request);
        Page<Question> questionPage = questionService.findPageByTeacherWithFilters(
                teacherId, page, size, keyword, enabled, type, difficulty, knowledgePointId);
        List<QuestionVO> questionVOs = questionPage.getContent().stream()
                .map(questionMapper::toQuestionVO)
                .toList();

        PageResult<QuestionVO> result = new PageResult<>();
        result.setContent(questionVOs);
        result.setTotal(questionPage.getTotalElements());
        result.setPage(page);
        result.setSize(size);
        return ApiResponse.success(result);
    }

    @Operation(summary = "创建题目", description = "教师创建题目")
    @PreAuthorize("hasAuthority('" + PermissionConstants.QUESTION_CREATE_SELF + "')")
    @PostMapping
    public ApiResponse<QuestionVO> create(@RequestBody @Valid CreateQuestionDTO createQuestionDTO,
                                         HttpServletRequest request) {
        KnowledgePoint knowledgePoint = knowledgePointService.findById(createQuestionDTO.getKnowledgePointId());
        if (knowledgePoint == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "知识点不存在");
        }

        // 检查权限：验证是否是自己的知识点
        Long currentUserId = getCurrentUserId(request);
        if (!knowledgePoint.getCourse().getTeacher().getId().equals(currentUserId)) {
            throw new StatefulException(HttpStatus.HTTP_FORBIDDEN, "您只能为自己的知识点创建题目");
        }

        Question question = questionMapper.toQuestion(createQuestionDTO);
        question.setKnowledgePoint(knowledgePoint);

        question = questionService.save(question);
        return ApiResponse.success(questionMapper.toQuestionVO(question));
    }

    @Operation(summary = "管理员创建题目", description = "管理员为任意知识点创建题目")
    @PreAuthorize("hasAuthority('" + PermissionConstants.QUESTION_CREATE_ALL + "')")
    @PostMapping("/admin")
    public ApiResponse<QuestionVO> createByAdmin(@RequestBody @Valid CreateQuestionDTO createQuestionDTO) {
        KnowledgePoint knowledgePoint = knowledgePointService.findById(createQuestionDTO.getKnowledgePointId());
        if (knowledgePoint == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "知识点不存在");
        }

        Question question = questionMapper.toQuestion(createQuestionDTO);
        question.setKnowledgePoint(knowledgePoint);

        question = questionService.save(question);
        return ApiResponse.success(questionMapper.toQuestionVO(question));
    }

    @Operation(summary = "获取题目详情", description = "获取题目详细信息")
    @PreAuthorize("hasAuthority('" + PermissionConstants.QUESTION_VIEW_ALL + "') or " +
            "hasAuthority('" + PermissionConstants.QUESTION_VIEW_SELF + "')")
    @GetMapping("/{questionId}")
    public ApiResponse<QuestionDetailVO> detail(@PathVariable Long questionId, HttpServletRequest request) {
        Question question = questionService.findById(questionId);
        if (question == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "题目不存在");
        }

        // 检查权限：如果只有查看自己的权限，需要验证所有权
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();

        boolean hasViewAll = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals(PermissionConstants.QUESTION_VIEW_ALL));

        if (!hasViewAll) {
            Long currentUserId = getCurrentUserId(request);
            if (!questionService.isQuestionOwnedByTeacher(questionId, currentUserId)) {
                throw new StatefulException(HttpStatus.HTTP_FORBIDDEN, "您只能查看自己的题目");
            }
        }

        return ApiResponse.success(questionMapper.toQuestionDetailVO(question));
    }

    @Operation(summary = "更新题目", description = "教师更新自己的题目")
    @PreAuthorize("hasAuthority('" + PermissionConstants.QUESTION_EDIT_SELF + "')")
    @PutMapping("/{questionId}")
    public ApiResponse<QuestionVO> update(@PathVariable Long questionId,
                                         @RequestBody @Valid UpdateQuestionDTO updateQuestionDTO,
                                         HttpServletRequest request) {
        Question question = questionService.findById(questionId);
        if (question == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "题目不存在");
        }

        // 检查权限：验证是否是自己的题目
        Long currentUserId = getCurrentUserId(request);
        if (!questionService.isQuestionOwnedByTeacher(questionId, currentUserId)) {
            throw new StatefulException(HttpStatus.HTTP_FORBIDDEN, "您只能编辑自己的题目");
        }

        // 验证新的知识点是否存在且属于自己
        KnowledgePoint newKnowledgePoint = knowledgePointService.findById(updateQuestionDTO.getKnowledgePointId());
        if (newKnowledgePoint == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "知识点不存在");
        }
        if (!newKnowledgePoint.getCourse().getTeacher().getId().equals(currentUserId)) {
            throw new StatefulException(HttpStatus.HTTP_FORBIDDEN, "您只能选择自己的知识点");
        }

        questionMapper.updateQuestionFromDto(updateQuestionDTO, question);
        question.setKnowledgePoint(newKnowledgePoint);

        question = questionService.save(question);
        return ApiResponse.success(questionMapper.toQuestionVO(question));
    }

    @Operation(summary = "管理员更新题目", description = "管理员更新任意题目")
    @PreAuthorize("hasAuthority('" + PermissionConstants.QUESTION_EDIT_ALL + "')")
    @PutMapping("/admin/{questionId}")
    public ApiResponse<QuestionVO> updateByAdmin(@PathVariable Long questionId,
                                                @RequestBody @Valid UpdateQuestionDTO updateQuestionDTO) {
        Question question = questionService.findById(questionId);
        if (question == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "题目不存在");
        }

        // 验证新的知识点是否存在
        KnowledgePoint newKnowledgePoint = knowledgePointService.findById(updateQuestionDTO.getKnowledgePointId());
        if (newKnowledgePoint == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "知识点不存在");
        }

        questionMapper.updateQuestionFromDto(updateQuestionDTO, question);
        question.setKnowledgePoint(newKnowledgePoint);

        question = questionService.save(question);
        return ApiResponse.success(questionMapper.toQuestionVO(question));
    }

    @Operation(summary = "删除题目", description = "教师删除自己的题目")
    @PreAuthorize("hasAuthority('" + PermissionConstants.QUESTION_DELETE_SELF + "')")
    @DeleteMapping("/{questionId}")
    public ApiResponse<Boolean> delete(@PathVariable Long questionId, HttpServletRequest request) {
        Question question = questionService.findById(questionId);
        if (question == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "题目不存在");
        }

        // 检查权限：验证是否是自己的题目
        Long currentUserId = getCurrentUserId(request);
        if (!questionService.isQuestionOwnedByTeacher(questionId, currentUserId)) {
            throw new StatefulException(HttpStatus.HTTP_FORBIDDEN, "您只能删除自己的题目");
        }

        return ApiResponse.success(questionService.delete(questionId));
    }

    @Operation(summary = "管理员删除题目", description = "管理员删除任意题目")
    @PreAuthorize("hasAuthority('" + PermissionConstants.QUESTION_DELETE_ALL + "')")
    @DeleteMapping("/admin/{questionId}")
    public ApiResponse<Boolean> deleteByAdmin(@PathVariable Long questionId) {
        Question question = questionService.findById(questionId);
        if (question == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "题目不存在");
        }
        return ApiResponse.success(questionService.delete(questionId));
    }

    @Operation(summary = "根据知识点删除题目", description = "删除指定知识点下的所有题目")
    @PreAuthorize("hasAuthority('" + PermissionConstants.QUESTION_DELETE_ALL + "')")
    @DeleteMapping("/knowledge-point/{knowledgePointId}")
    public ApiResponse<Boolean> deleteByKnowledgePoint(@PathVariable Long knowledgePointId) {
        // 查找知识点下的所有题目
        List<Question> questions = questionService.findByKnowledgePointId(knowledgePointId);
        
        if (!questions.isEmpty()) {
            List<Long> questionIds = questions.stream()
                    .map(Question::getId)
                    .toList();
            boolean result = questionService.batchDelete(questionIds);
            log.info("删除知识点 {} 下的 {} 道题目", knowledgePointId, questions.size());
            return ApiResponse.success(result);
        }
        
        return ApiResponse.success(true);
    }

    @Operation(summary = "批量删除题目", description = "批量删除题目")
    @PreAuthorize("hasAuthority('" + PermissionConstants.QUESTION_DELETE_ALL + "')")
    @DeleteMapping("/batch")
    public ApiResponse<Boolean> batchDelete(@RequestBody List<Long> questionIds) {
        boolean result = questionService.batchDelete(questionIds);
        return ApiResponse.success(result);
    }

    @Operation(summary = "更新题目状态", description = "启用或禁用题目")
    @PreAuthorize("hasAuthority('" + PermissionConstants.QUESTION_EDIT_SELF + "')")
    @PutMapping("/{questionId}/status")
    public ApiResponse<QuestionVO> updateStatus(@PathVariable Long questionId,
                                               @RequestParam Boolean enabled,
                                               HttpServletRequest request) {
        Question question = questionService.findById(questionId);
        if (question == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "题目不存在");
        }

        // 检查权限：验证是否是自己的题目
        Long currentUserId = getCurrentUserId(request);
        if (!questionService.isQuestionOwnedByTeacher(questionId, currentUserId)) {
            throw new StatefulException(HttpStatus.HTTP_FORBIDDEN, "您只能修改自己的题目状态");
        }

        question = questionService.updateEnabled(questionId, enabled);
        return ApiResponse.success(questionMapper.toQuestionVO(question));
    }

    @Operation(summary = "获取题目类型列表", description = "获取所有题目类型")
    @PreAuthorize("hasAuthority('" + PermissionConstants.QUESTION_LIST_ALL + "') or " +
            "hasAuthority('" + PermissionConstants.QUESTION_LIST_SELF + "')")
    @GetMapping("/types")
    public ApiResponse<List<Question.QuestionType>> getQuestionTypes() {
        return ApiResponse.success(questionService.getAllQuestionTypes());
    }

    @Operation(summary = "获取难度列表", description = "获取所有难度级别")
    @PreAuthorize("hasAuthority('" + PermissionConstants.QUESTION_LIST_ALL + "') or " +
            "hasAuthority('" + PermissionConstants.QUESTION_LIST_SELF + "')")
    @GetMapping("/difficulties")
    public ApiResponse<List<Question.Difficulty>> getDifficulties() {
        return ApiResponse.success(questionService.getAllDifficulties());
    }

    @Operation(summary = "获取题目统计", description = "获取题目统计信息")
    @PreAuthorize("hasAuthority('" + PermissionConstants.STATISTICS_QUESTION + "')")
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics(HttpServletRequest request) {
        Long teacherId = getCurrentUserId(request);

        long totalQuestions = questionService.countByTeacherId(teacherId);
        long enabledQuestions = questionService.findPageByTeacherWithFilters(
                teacherId, 1, Integer.MAX_VALUE, null, true, null, null, null)
                .getTotalElements();
        long disabledQuestions = totalQuestions - enabledQuestions;

        // 按类型统计
        long singleChoiceCount = questionService.findPageByTeacherWithFilters(
                teacherId, 1, Integer.MAX_VALUE, null, null, Question.QuestionType.SINGLE_CHOICE, null, null)
                .getTotalElements();
        long multipleChoiceCount = questionService.findPageByTeacherWithFilters(
                teacherId, 1, Integer.MAX_VALUE, null, null, Question.QuestionType.MULTIPLE_CHOICE, null, null)
                .getTotalElements();
        long fillBlankCount = questionService.findPageByTeacherWithFilters(
                teacherId, 1, Integer.MAX_VALUE, null, null, Question.QuestionType.FILL_BLANK, null, null)
                .getTotalElements();
        long shortAnswerCount = questionService.findPageByTeacherWithFilters(
                teacherId, 1, Integer.MAX_VALUE, null, null, Question.QuestionType.SHORT_ANSWER, null, null)
                .getTotalElements();

        Map<String, Object> statistics = Map.of(
                "totalQuestions", totalQuestions,
                "enabledQuestions", enabledQuestions,
                "disabledQuestions", disabledQuestions,
                "singleChoiceCount", singleChoiceCount,
                "multipleChoiceCount", multipleChoiceCount,
                "fillBlankCount", fillBlankCount,
                "shortAnswerCount", shortAnswerCount
        );

        return ApiResponse.success(statistics);
    }

    /**
     * 批量创建题目DTO
     */
    @Data
    @Schema(description = "批量创建题目DTO")
    public static class BatchCreateQuestionDTO {

        @NotNull(message = "题目列表不能为空")
        @Size(min = 1, message = "至少需要一道题目")
        @Schema(description = "题目列表")
        private List<CreateQuestionDTO> questions;

        @Schema(description = "是否为AI生成", example = "true")
        private Boolean isAiGenerated = false;
    }

    @Operation(summary = "批量创建题目", description = "批量创建题目，支持AI生成题目的保存")
    @PreAuthorize("hasAuthority('" + PermissionConstants.QUESTION_CREATE_SELF + "') or " +
            "hasAuthority('" + PermissionConstants.QUESTION_CREATE_ALL + "')")
    @PostMapping("/batch")
    public ApiResponse<List<QuestionVO>> batchCreate(
            @RequestBody @Valid BatchCreateQuestionDTO batchCreateDTO,
            HttpServletRequest request) {

        log.info("收到批量创建题目请求，数量: {}", batchCreateDTO.getQuestions().size());

        List<QuestionVO> savedQuestions = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // 权限检查
        Long currentUserId = getCurrentUserId(request);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();

        boolean hasCreateAll = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals(PermissionConstants.QUESTION_CREATE_ALL));

        for (int i = 0; i < batchCreateDTO.getQuestions().size(); i++) {
            try {
                CreateQuestionDTO createDTO = batchCreateDTO.getQuestions().get(i);

                // 验证知识点是否存在
                KnowledgePoint knowledgePoint = knowledgePointService.findById(createDTO.getKnowledgePointId());
                if (knowledgePoint == null) {
                    errors.add("第" + (i + 1) + "道题目：知识点不存在");
                    continue;
                }

                // 权限检查：教师只能为自己的知识点创建题目
                if (!hasCreateAll) {
                    if (!knowledgePoint.getCourse().getTeacher().getId().equals(currentUserId)) {
                        errors.add("第" + (i + 1) + "道题目：您只能为自己的知识点创建题目");
                        continue;
                    }
                }

                // 创建题目
                Question question = questionMapper.toQuestion(createDTO);
                question.setKnowledgePoint(knowledgePoint);

                // 设置AI生成标识
                if (batchCreateDTO.getIsAiGenerated() != null) {
                    question.setIsAiGenerated(batchCreateDTO.getIsAiGenerated());
                }

                // 如果是AI生成的题目，添加默认标签
                if (Boolean.TRUE.equals(question.getIsAiGenerated()) &&
                        (question.getTags() == null || question.getTags().trim().isEmpty())) {
                    String defaultTags = generateDefaultTagsForBatch(question, knowledgePoint);
                    question.setTags(defaultTags);
                }

                Question savedQuestion = questionService.save(question);
                savedQuestions.add(questionMapper.toQuestionVO(savedQuestion));

            } catch (Exception e) {
                log.error("保存第{}道题目失败", i + 1, e);
                errors.add("第" + (i + 1) + "道题目：保存失败 - " + e.getMessage());
            }
        }

        log.info("批量创建题目完成，成功: {}, 失败: {}", savedQuestions.size(), errors.size());

        // 如果有错误，在响应中包含错误信息
        if (!errors.isEmpty()) {
            return ApiResponse.error(400, "部分题目创建失败: " + String.join("; ", errors), savedQuestions);
        }

        return ApiResponse.success(savedQuestions);
    }

    /**
     * 为批量创建生成默认标签
     */
    private String generateDefaultTagsForBatch(Question question, KnowledgePoint knowledgePoint) {
        List<String> tags = new ArrayList<>();

        // 添加AI生成标识
        tags.add("AI生成");

        // 添加题目类型标签
        if (question.getType() != null) {
            switch (question.getType()) {
                case SINGLE_CHOICE -> tags.add("单选题");
                case MULTIPLE_CHOICE -> tags.add("多选题");
                case TRUE_FALSE -> tags.add("判断题");
                case FILL_BLANK -> tags.add("填空题");
                case SHORT_ANSWER -> tags.add("简答题");
                case ESSAY -> tags.add("论述题");
            }
        }

        // 添加难度标签
        if (question.getDifficulty() != null) {
            switch (question.getDifficulty()) {
                case EASY -> tags.add("简单");
                case MEDIUM -> tags.add("中等");
                case HARD -> tags.add("困难");
            }
        }

        // 添加知识点名称标签
        if (knowledgePoint.getName() != null) {
            tags.add(knowledgePoint.getName());
        }

        // 添加课程标签
        if (knowledgePoint.getCourse() != null && knowledgePoint.getCourse().getName() != null) {
            tags.add(knowledgePoint.getCourse().getName());
        }

        return String.join(",", tags);
    }

    @Operation(summary = "AI生成题目", description = "基于知识点智能生成题目")
    @PreAuthorize("hasAuthority('" + PermissionConstants.AI_GENERATE_QUESTION + "')")
    @PostMapping("/generate/ai")
    public ApiResponse<List<CreateQuestionDTO>> generateQuestionsWithAI(@Valid @RequestBody AIGenerateQuestionDTO requestDTO,
                                                                        HttpServletRequest request) {
        return ApiResponse.success(questionService.generateQuestionsWithAI(requestDTO));
    }

    @Operation(summary = "获取题目历史版本", description = "分页获取题目修改历史")
    @PreAuthorize("hasAuthority('" + PermissionConstants.QUESTION_VIEW_ALL + "') or hasAuthority('" + PermissionConstants.QUESTION_VIEW_SELF + "')")
    @GetMapping("/{id}/history")
    public ApiResponse<PageResult<QuestionHistoryVO>> getQuestionHistory(
            @PathVariable("id") Long questionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(questionService.getQuestionHistory(questionId, page, size));
    }

    @Operation(summary = "回退题目", description = "将题目回退到指定历史版本")
    @PreAuthorize("hasAuthority('" + PermissionConstants.QUESTION_EDIT_ALL + "') or hasAuthority('" + PermissionConstants.QUESTION_EDIT_SELF + "')")
    @PostMapping("/{id}/history/{revision}")
    public ApiResponse<QuestionVO> revertQuestion(
            @PathVariable("id") Long questionId,
            @PathVariable("revision") Number revision) {
        return ApiResponse.success(questionService.revertQuestion(questionId, revision));
    }
}