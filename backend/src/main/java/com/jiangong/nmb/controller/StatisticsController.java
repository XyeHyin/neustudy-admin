package com.jiangong.nmb.controller;

import com.jiangong.nmb.common.ApiResponse;
import com.jiangong.nmb.service.StatisticsService;
import com.jiangong.nmb.vo.statistics.*;
import com.jiangong.nmb.constant.PermissionConstants;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 统计管理控制器
 */
@Tag(name = "统计管理", description = "数据统计相关接口")
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController extends BaseController {
    private final StatisticsService statisticsService;

    // 统计相关接口骨架

    @Operation(summary = "试卷统计", description = "获取指定试卷的统计信息")
    @PreAuthorize("hasAuthority('" + PermissionConstants.STATISTICS_PAPER_VIEW + "')")
    @GetMapping("/paper/{paperId}")
    public ApiResponse<PaperStatisticsVO> paperStatistics(@Parameter(description = "试卷ID", required = true) @PathVariable Long paperId) {
        return ApiResponse.success(statisticsService.paperStatistics(paperId));
    }

    @Operation(summary = "题目统计", description = "获取指定题目的统计信息")
    @PreAuthorize("hasAuthority('" + PermissionConstants.STATISTICS_QUESTION_VIEW + "')")
    @GetMapping("/question/{questionId}")
    public ApiResponse<QuestionStatisticsVO> questionStatistics(@Parameter(description = "题目ID", required = true) @PathVariable Long questionId) {
        return ApiResponse.success(statisticsService.questionStatistics(questionId));
    }

    @Operation(summary = "学生统计", description = "获取指定学生的统计信息")
    @PreAuthorize("hasAuthority('" + PermissionConstants.STATISTICS_STUDENT_VIEW + "')")
    @GetMapping("/student/{studentId}")
    public ApiResponse<StudentStatisticsVO> studentStatistics(@Parameter(description = "学生ID", required = true) @PathVariable Long studentId) {
        return ApiResponse.success(statisticsService.studentStatistics(studentId));
    }


    @Operation(summary = "仪表盘统计概览", description = "获取首页数据统计概览")
    @PreAuthorize("hasAuthority('" + PermissionConstants.DASHBOARD + "')")
    @GetMapping("/dashboard/stats")
    public ApiResponse<List<DashboardStatVO>> dashboardStats() {

        return ApiResponse.success(statisticsService.dashboardStats());
    }

    @Operation(summary = "题目生成趋势图", description = "按周期返回题目生成数量趋势")
    @PreAuthorize("hasAuthority('" + PermissionConstants.DASHBOARD + "')")
    @GetMapping("/dashboard/trend")
    public ApiResponse<DashboardTrendVO> dashboardTrend(@RequestParam String period) {
        return ApiResponse.success(statisticsService.dashboardTrend(period));
    }

    @Operation(summary = "题型分布饼图", description = "返回各题型的占比数据")
    @PreAuthorize("hasAuthority('" + PermissionConstants.DASHBOARD + "')")
    @GetMapping("/dashboard/distribution")
    public ApiResponse<List<DashboardDistributionVO>> dashboardDistribution() {
        return ApiResponse.success(statisticsService.dashboardDistribution());
    }
} 