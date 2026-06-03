package com.xyehyin.hexuanning.controller;

import com.xyehyin.hexuanning.common.ApiResponse;
import com.xyehyin.hexuanning.service.PracticeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import cn.hutool.core.exceptions.StatefulException;
import cn.hutool.http.HttpStatus;
import com.xyehyin.hexuanning.dto.practice.PracticeStartDTO;
import com.xyehyin.hexuanning.dto.practice.PracticeSubmitDTO;
import com.xyehyin.hexuanning.dto.practice.PracticeMarkDTO;
import com.xyehyin.hexuanning.vo.practice.PracticeSessionVO;
import com.xyehyin.hexuanning.vo.practice.PracticeResultVO;
import com.xyehyin.hexuanning.vo.practice.PracticeRecordVO;
import com.xyehyin.hexuanning.vo.practice.PracticeOverviewVO;
import com.xyehyin.hexuanning.common.PageResult;
import com.xyehyin.hexuanning.vo.practice.PracticeDetailVO;
import com.xyehyin.hexuanning.vo.practice.PracticePaperStatVO;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import com.xyehyin.hexuanning.constant.PermissionConstants;

/**
 * 练习管理控制器
 */
@Tag(name = "练习管理", description = "练习流程相关接口")
@RestController
@RequestMapping("/practice")
@RequiredArgsConstructor
public class PracticeController extends BaseController {
    private final PracticeService practiceService;

    @Operation(summary = "开始练习", description = "创建新的练习会话")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PRACTICE_START + "')")
    @PostMapping("/start")
    public ApiResponse<PracticeSessionVO> startPractice(
            @RequestBody @Valid PracticeStartDTO dto) {
        return ApiResponse.success(practiceService.startPractice(dto));
    }

    @Operation(summary = "提交练习答案", description = "提交本次练习的所有答题并自动判分")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PRACTICE_SUBMIT + "')")
    @PostMapping("/submit")
    public ApiResponse<PracticeResultVO> submitPractice(
            @RequestBody @Valid PracticeSubmitDTO dto) {
        return ApiResponse.success(practiceService.submitPractice(dto));
    }

    @Operation(summary = "获取练习会话详情", description = "根据ID获取练习会话基本信息和答题状态")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PRACTICE_VIEW_SELF + "')")
    @GetMapping("/{id}")
    public ApiResponse<PracticeSessionVO> getPracticeSession(
            @Parameter(description = "练习会话ID", required = true) @PathVariable Long id) {
        PracticeSessionVO vo = practiceService.getPracticeSession(id);
        if (vo == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "练习会话不存在");
        }
        return ApiResponse.success(vo);
    }

    @Operation(summary = "查询练习记录", description = "按条件查询练习历史记录")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PRACTICE_RECORD_LIST_ALL + "')")
    @GetMapping("/records")
    public ApiResponse<PageResult<PracticeRecordVO>> listPracticeRecords(
            @Parameter(description = "试卷ID") @RequestParam(required = false) Long paperId,
            @Parameter(description = "学生ID") @RequestParam(required = false) Long studentId,
            @Parameter(description = "是否已提交") @RequestParam(required = false) Boolean submitted,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size
    ) {
        List<PracticeRecordVO> records = practiceService.listPracticeRecords(paperId, studentId, submitted, page, size);
        List<PracticeRecordVO> allRecords = practiceService.listPracticeRecords(paperId, studentId, submitted, null, null);
        PageResult<PracticeRecordVO> result = new PageResult<>();
        result.setContent(records);
        result.setTotal(allRecords.size());
        result.setPage(page);
        result.setSize(size);
        return ApiResponse.success(result);
    }

    @Operation(summary = "获取练习结果", description = "获取指定练习会话的成绩与答题明细")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PRACTICE_RESULT_VIEW_SELF + "')")
    @GetMapping("/{id}/result")
    public ApiResponse<PracticeResultVO> getPracticeResult(
            @Parameter(description = "练习会话ID", required = true) @PathVariable Long id) {
        PracticeResultVO vo = practiceService.getPracticeResult(id);
        if (vo == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "练习结果不存在");
        }
        return ApiResponse.success(vo);
    }

    @Operation(summary = "题目标记", description = "对指定题目进行标记（如收藏、疑问等）")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PRACTICE_MARK + "')")
    @PostMapping("/mark")
    public ApiResponse<Void> markQuestion(
            @RequestBody @Valid PracticeMarkDTO dto) {
        practiceService.markQuestion(dto);
        return ApiResponse.success();
    }

    @Operation(summary = "获取练习进度概览", description = "获取练习的整体进度、每题状态等")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PRACTICE_OVERVIEW_SELF + "')")
    @GetMapping("/{id}/overview")
    public ApiResponse<PracticeOverviewVO> getPracticeOverview(
            @Parameter(description = "练习会话ID", required = true) @PathVariable Long id) {
        PracticeOverviewVO vo = practiceService.getPracticeOverview(id);
        if (vo == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "练习会话不存在");
        }
        return ApiResponse.success(vo);
    }

    @Operation(summary = "获取学生练习历史", description = "获取指定学生的所有练习记录")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PRACTICE_HISTORY_SELF + "')")
    @GetMapping("/history/{studentId}")
    public ApiResponse<List<PracticeRecordVO>> getPracticeHistory(
            @Parameter(description = "学生ID", required = true) @PathVariable Long studentId) {
        return ApiResponse.success(practiceService.getPracticeHistory(studentId));
    }

    @Operation(summary = "获取学生试卷练习统计", description = "获取指定学生特定试卷的练习统计信息")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PRACTICE_STATISTICS_SELF + "')")
    @GetMapping("/statistics/{studentId}/{paperId}")
    public ApiResponse<PracticeRecordVO> getPracticeStatistics(
            @Parameter(description = "学生ID", required = true) @PathVariable Long studentId,
            @Parameter(description = "试卷ID", required = true) @PathVariable Long paperId) {
        return ApiResponse.success(practiceService.getPracticeStatistics(studentId, paperId));
    }

    @Operation(summary = "获取练习题目详情", description = "获取练习的完整题目信息，包括题目内容、选项等，用于练习答题页面展示")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PRACTICE_DETAIL_SELF + "')")
    @GetMapping("/{id}/questions")
    public ApiResponse<PracticeDetailVO> getPracticeDetail(
            @Parameter(description = "练习会话ID", required = true) @PathVariable Long id) {
        PracticeDetailVO vo = practiceService.getPracticeDetail(id);
        if (vo == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "练习详情不存在");
        }
        return ApiResponse.success(vo);
    }

    @Operation(summary = "获取当前用户的练习会话列表", description = "分页获取当前用户的练习会话，支持提交状态和试卷筛选")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PRACTICE_RECORD_LIST_SELF + "')")
    @GetMapping("/my-sessions")
    public ApiResponse<PageResult<PracticeRecordVO>> getMyPracticeSessions(
            @RequestParam(value = "paperId", required = false) Long paperId,
            @RequestParam(value = "submitted", required = false) Boolean submitted,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            HttpServletRequest request
    ) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            throw new StatefulException(HttpStatus.HTTP_UNAUTHORIZED, "用户未登录");
        }
        List<PracticeRecordVO> records = practiceService.listPracticeRecords(paperId, currentUserId, submitted, page, size);
        List<PracticeRecordVO> allRecords = practiceService.listPracticeRecords(paperId, currentUserId, submitted, null, null);
        PageResult<PracticeRecordVO> result = new PageResult<>();
        result.setContent(records);
        result.setTotal(allRecords.size());
        result.setPage(page);
        result.setSize(size);
        return ApiResponse.success(result);
    }

    @Operation(summary = "获取所有可用试卷及用户练习统计", description = "返回所有已发布试卷及当前用户的练习统计信息")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PRACTICE_LIST_SELF + "')")
    @GetMapping("/available-papers")
    public ApiResponse<List<PracticePaperStatVO>> getAvailablePapersWithStats(HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            throw new StatefulException(HttpStatus.HTTP_UNAUTHORIZED, "用户未登录");
        }
        List<PracticePaperStatVO> result = practiceService.listAvailablePapersWithStats(currentUserId);
        return ApiResponse.success(result);
    }
}