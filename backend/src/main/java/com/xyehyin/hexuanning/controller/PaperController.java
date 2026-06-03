package com.xyehyin.hexuanning.controller;

import com.xyehyin.hexuanning.common.ApiResponse;
import com.xyehyin.hexuanning.common.PageResult;
import com.xyehyin.hexuanning.constant.PermissionConstants;
import com.xyehyin.hexuanning.dto.paper.PaperCreateDTO;
import com.xyehyin.hexuanning.dto.paper.PaperUpdateDTO;
import com.xyehyin.hexuanning.dto.paper.PaperQuestionDTO;
import com.xyehyin.hexuanning.dto.paper.SmartPaperDTO;
import com.xyehyin.hexuanning.entity.Paper;
import com.xyehyin.hexuanning.mapper.PaperMapper;
import com.xyehyin.hexuanning.service.PaperService;
import com.xyehyin.hexuanning.vo.paper.PaperDetailVO;
import com.xyehyin.hexuanning.vo.paper.PaperListVO;
import com.xyehyin.hexuanning.vo.paper.PaperQuestionVO;
import com.xyehyin.hexuanning.vo.paper.PaperPreviewVO;
import com.xyehyin.hexuanning.vo.statistics.PaperStatisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import cn.hutool.core.exceptions.StatefulException;
import cn.hutool.http.HttpStatus;

import java.util.List;

/**
 * 试卷管理控制器
 */
@Tag(name = "试卷管理", description = "试卷相关接口")
@RestController
@RequestMapping("/paper")
@RequiredArgsConstructor
public class PaperController extends BaseController {
    private final PaperService paperService;
    private final PaperMapper paperMapper;

    @Operation(summary = "创建试卷", description = "创建试卷")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PAPER_CREATE + "')")
    @PostMapping("/create")
    public ApiResponse<PaperDetailVO> createPaper(@RequestBody @Valid PaperCreateDTO dto) {
        Paper paper = paperService.createPaper(dto);
        return ApiResponse.success(paperMapper.toPaperDetailVO(paper));
    }

    @Operation(summary = "分页查询试卷", description = "分页查询试卷")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PAPER_LIST_SELF + "') or hasAuthority('" + PermissionConstants.PAPER_LIST_ALL + "')")
    @GetMapping("/page")
    public ApiResponse<PageResult<PaperListVO>> page(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        Page<Paper> pageResult = paperService.page(title, teacherId, status, page, size);
        List<PaperListVO> voList = pageResult.getContent().stream().map(paperMapper::toPaperListVO).toList();
        PageResult<PaperListVO> result = new PageResult<>();
        result.setContent(voList);
        result.setTotal(pageResult.getTotalElements());
        result.setPage(pageResult.getNumber() + 1);
        result.setSize(pageResult.getSize());
        return ApiResponse.success(result);
    }

    @Operation(summary = "获取试卷详情", description = "获取试卷详情")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PAPER_VIEW_SELF + "') or hasAuthority('" + PermissionConstants.PAPER_VIEW_ALL + "')")
    @GetMapping("/{id}")
    public ApiResponse<PaperDetailVO> getPaper(@PathVariable Long id) {
        Paper paper = paperService.findByIdOrThrow(id);
        PaperDetailVO vo = paperMapper.toPaperDetailVO(paper);
        vo.setQuestions(paperService.listQuestions(paper.getId(), false));
        vo.setTimeLimit(paper.getTimeLimit());
        return ApiResponse.success(vo);
    }

    @Operation(summary = "编辑试卷", description = "编辑试卷")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PAPER_EDIT_SELF + "') or hasAuthority('" + PermissionConstants.PAPER_EDIT_ALL + "')")
    @PutMapping("/{id}")
    public ApiResponse<PaperDetailVO> updatePaper(@PathVariable Long id, @RequestBody @Valid PaperUpdateDTO dto, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        Paper paper = paperService.updatePaper(id, dto, currentUserId);
        return ApiResponse.success(paperMapper.toPaperDetailVO(paper));
    }

    @Operation(summary = "删除试卷", description = "删除试卷")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PAPER_DELETE_SELF + "') or hasAuthority('" + PermissionConstants.PAPER_DELETE_ALL + "')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePaper(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        paperService.deletePaper(id, currentUserId);
        return ApiResponse.success();
    }

    @Operation(summary = "发布试卷", description = "发布试卷")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PAPER_PUBLISH_SELF + "') or hasAuthority('" + PermissionConstants.PAPER_PUBLISH_ALL + "')")
    @PostMapping("/{id}/publish")
    public ApiResponse<Void> publishPaper(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        paperService.publishPaper(id, currentUserId);
        return ApiResponse.success();
    }

    @Operation(summary = "归档试卷", description = "归档试卷")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PAPER_ARCHIVE_SELF + "') or hasAuthority('" + PermissionConstants.PAPER_ARCHIVE_ALL + "')")
    @PostMapping("/{id}/archive")
    public ApiResponse<Void> archivePaper(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        paperService.archivePaper(id, currentUserId);
        return ApiResponse.success();
    }

    @Operation(summary = "添加题目到试卷", description = "批量添加题目")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PAPER_QUESTION_ADD + "')")
    @PostMapping("/{paperId}/questions")
    public ApiResponse<Void> addQuestions(@PathVariable Long paperId, @RequestBody List<PaperQuestionDTO> dtos, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        paperService.addQuestions(paperId, dtos, currentUserId);
        return ApiResponse.success();
    }

    @Operation(summary = "移除试卷题目", description = "移除指定题目")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PAPER_QUESTION_REMOVE + "')")
    @DeleteMapping("/{paperId}/questions/{questionId}")
    public ApiResponse<Void> removeQuestion(@PathVariable Long paperId, @PathVariable Long questionId, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        paperService.removeQuestion(paperId, questionId, currentUserId);
        return ApiResponse.success();
    }

    @Operation(summary = "调整题目顺序和分值", description = "批量调整")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PAPER_QUESTION_REORDER + "') or hasAuthority('" + PermissionConstants.PAPER_QUESTION_SCORE + "')")
    @PutMapping("/{paperId}/questions")
    public ApiResponse<Void> updateQuestions(@PathVariable Long paperId, @RequestBody List<PaperQuestionDTO> dtos, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        paperService.updateQuestions(paperId, dtos, currentUserId);
        return ApiResponse.success();
    }

    @Operation(summary = "查询试卷题目列表", description = "获取试卷题目列表，可选乱序")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PAPER_VIEW_SELF + "') or hasAuthority('" + PermissionConstants.PAPER_VIEW_ALL + "')")
    @GetMapping("/{paperId}/questions")
    public ApiResponse<List<PaperQuestionVO>> listQuestions(
            @PathVariable Long paperId,
            @Parameter(description = "是否乱序", example = "false") @RequestParam(defaultValue = "false") boolean randomOrder) {
        return ApiResponse.success(paperService.listQuestions(paperId, randomOrder));
    }

    @Operation(summary = "智能组卷", description = "根据条件智能组卷")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PAPER_CREATE + "')")
    @PostMapping("/smart-generate")
    public ApiResponse<PaperDetailVO> smartGenerate(@RequestBody SmartPaperDTO dto, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            throw new StatefulException(HttpStatus.HTTP_UNAUTHORIZED, "用户未登录或无法识别用户");
        }
        Paper paper = paperService.smartGenerate(dto, currentUserId);
        PaperDetailVO vo = paperMapper.toPaperDetailVO(paper);
        vo.setQuestions(paperService.listQuestions(paper.getId(), false));
        vo.setTimeLimit(paper.getTimeLimit());
        return ApiResponse.success(vo);
    }

    @Operation(summary = "试卷预览", description = "获取试卷预览信息")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PAPER_VIEW_SELF + "') or hasAuthority('" + PermissionConstants.PAPER_VIEW_ALL + "')")
    @GetMapping("/{id}/preview")
    public ApiResponse<PaperPreviewVO> previewPaper(@PathVariable Long id) {
        Paper paper = paperService.findByIdOrThrow(id);
        List<PaperQuestionVO> questions = paperService.listQuestions(id, false);
        PaperPreviewVO vo = new PaperPreviewVO();
        vo.setId(paper.getId());
        vo.setTitle(paper.getTitle());
        vo.setDescription(paper.getDescription());
        vo.setTimeLimit(paper.getTimeLimit());
        vo.setQuestions(questions);
        return ApiResponse.success(vo);
    }

    @Operation(summary = "试卷统计", description = "获取试卷统计信息")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PAPER_VIEW_SELF + "') or hasAuthority('" + PermissionConstants.PAPER_VIEW_ALL + "')")
    @GetMapping("/{id}/statistics")
    public ApiResponse<PaperStatisticsVO> statistics(@PathVariable Long id) {
        PaperStatisticsVO vo = paperService.statistics(id);
        return ApiResponse.success(vo);
    }
}
