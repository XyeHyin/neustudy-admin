package com.jiangong.nmb.controller;

import com.jiangong.nmb.common.ApiResponse;
import com.jiangong.nmb.dto.practice.PracticeStartDTO;
import com.jiangong.nmb.entity.GradingResult;
import com.jiangong.nmb.service.GradingService;
import com.jiangong.nmb.service.PracticeService;
import com.jiangong.nmb.vo.practice.PracticeSessionVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import cn.hutool.core.exceptions.StatefulException;
import cn.hutool.http.HttpStatus;
import com.jiangong.nmb.dto.grading.GradingRequestDTO;
import com.jiangong.nmb.dto.grading.AIGradingDTO;
import com.jiangong.nmb.dto.grading.ManualGradingDTO;
import com.jiangong.nmb.dto.grading.BatchGradingRequestDTO;
import com.jiangong.nmb.vo.grading.GradingResultVO;
import com.jiangong.nmb.common.PageResult;
import com.jiangong.nmb.vo.grading.GradingReviewVO;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import com.jiangong.nmb.constant.PermissionConstants;
import com.jiangong.nmb.controller.BaseController;

/**
 * 判分管理控制器
 */
@Tag(name = "判分管理", description = "判分相关接口")
@RestController
@RequestMapping("/grading")
@RequiredArgsConstructor
public class GradingController extends BaseController {
    private final GradingService gradingService;
    private final PracticeService practiceService;

//    @Operation(summary = "自动判分", description = "对学生答案进行自动判分（客观题/主观题分流）")
//    @PreAuthorize("hasAuthority('" + PermissionConstants.GRADING_AUTO + "')")
//    @PostMapping("/auto")
//    public ApiResponse<GradingResultVO> autoGrading(@RequestBody @Valid GradingRequestDTO dto) {
//        return ApiResponse.success(gradingService.autoGrading(dto));
//    }
//
//    @Operation(summary = "批量自动判分", description = "对多道学生答案进行批量自动判分（包含客观题和主观题）")
//    @PreAuthorize("hasAuthority('" + PermissionConstants.GRADING_AUTO + "')")
//    @PostMapping("/auto/batch")
//    public ApiResponse<List<GradingResultVO>> batchAutoGrading(@RequestBody @Valid BatchGradingRequestDTO dto) {
//        return ApiResponse.success(gradingService.batchAutoGrading(dto));
//    }
//
//    @Operation(summary = "AI判分", description = "对学生答案进行AI判分")
//    @PreAuthorize("hasAuthority('" + PermissionConstants.GRADING_AI + "')")
//    @PostMapping("/ai")
//    public ApiResponse<GradingResultVO> aiGrading(@RequestBody @Valid AIGradingDTO dto) {
//        return ApiResponse.success(gradingService.aiGrading(dto));
//    }

    @Operation(summary = "人工复核", description = "教师对AI判分结果进行人工复核")
    @PreAuthorize("hasAuthority('" + PermissionConstants.GRADING_MANUAL + "')")
    @PostMapping("/manual")
    public ApiResponse<GradingResultVO> manualGrading(@RequestBody @Valid ManualGradingDTO dto,
                                                      @Parameter(description = "复核教师ID", required = true) @RequestParam Long teacherId) {
        return ApiResponse.success(gradingService.manualGrading(dto, teacherId));
    }

    @Operation(summary = "获取判分结果", description = "根据ID获取判分结果详情")
    @PreAuthorize("hasAuthority('" + PermissionConstants.GRADING_VIEW + "')")
    @GetMapping("/{id}")
    public ApiResponse<GradingResultVO> getGradingResult(@Parameter(description = "判分结果ID", required = true) @PathVariable Long id) {
        return ApiResponse.success(gradingService.getGradingResult(id));
    }

    @Operation(summary = "获取待人工复核列表", description = "获取所有待人工复核的AI判分结果，支持页码、试卷ID、题目名称关键词筛选")
    @PreAuthorize("hasAuthority('" + PermissionConstants.GRADING_REVIEW_LIST + "')")
    @GetMapping("/review/list")
    public ApiResponse<PageResult<GradingReviewVO>> listReview(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long paperId,
            @RequestParam(required = false) String questionTitle) {
        return ApiResponse.success(gradingService.listReview(page, size, paperId, questionTitle));
    }

    @Operation(summary = "获取已批改列表", description = "分页查询已人工复核完成的判分结果，支持试卷ID和题目名称关键词筛选")
    @PreAuthorize("hasAuthority('" + PermissionConstants.GRADING_REVIEW_LIST + "')")
    @GetMapping("/reviewed/list")
    public ApiResponse<PageResult<GradingResultVO>> listReviewed(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long paperId,
            @RequestParam(required = false) String questionTitle) {
        return ApiResponse.success(gradingService.listReviewed(page, size, paperId, questionTitle));
    }
} 