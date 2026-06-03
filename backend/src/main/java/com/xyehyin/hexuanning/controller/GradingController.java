package com.xyehyin.hexuanning.controller;

import com.xyehyin.hexuanning.common.ApiResponse;
import com.xyehyin.hexuanning.common.PageResult;
import com.xyehyin.hexuanning.constant.PermissionConstants;
import com.xyehyin.hexuanning.service.GradingService;
import com.xyehyin.hexuanning.dto.grading.ManualGradingDTO;
import com.xyehyin.hexuanning.vo.grading.GradingResultVO;
import com.xyehyin.hexuanning.vo.grading.GradingReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 判分管理控制器
 */
@Tag(name = "判分管理", description = "判分相关接口")
@RestController
@RequestMapping("/grading")
@RequiredArgsConstructor
public class GradingController extends BaseController {
    private final GradingService gradingService;

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
