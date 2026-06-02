package com.jiangong.nmb.controller;

import cn.hutool.core.exceptions.StatefulException;
import cn.hutool.http.HttpStatus;
import com.jiangong.nmb.common.ApiResponse;
import com.jiangong.nmb.common.PageResult;
import com.jiangong.nmb.constant.PermissionConstants;
import com.jiangong.nmb.dto.knowledgepoint.CreateKnowledgePointDTO;
import com.jiangong.nmb.dto.knowledgepoint.UpdateKnowledgePointDTO;
import com.jiangong.nmb.entity.Course;
import com.jiangong.nmb.entity.KnowledgePoint;
import com.jiangong.nmb.mapper.KnowledgePointMapper;
import com.jiangong.nmb.service.CourseService;
import com.jiangong.nmb.service.KnowledgePointService;
import com.jiangong.nmb.vo.knowledgepoint.KnowledgePointDetailVO;
import com.jiangong.nmb.vo.knowledgepoint.KnowledgePointVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collection;
import java.util.List;

import com.jiangong.nmb.dto.knowledgepoint.KnowledgePointExportDTO;
import com.jiangong.nmb.utils.ExcelUtils;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@Tag(name = "知识点管理", description = "知识点相关接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/knowledge-points")
@CrossOrigin
public class KnowledgePointController extends BaseController {

    private final KnowledgePointService knowledgePointService;
    private final CourseService courseService;
    private final KnowledgePointMapper knowledgePointMapper;

    @Operation(summary = "获取所有知识点", description = "管理员查看所有知识点")
    @PreAuthorize("hasAuthority('" + PermissionConstants.KNOWLEDGE_POINT_LIST_ALL + "')")
    @GetMapping
    public ApiResponse<List<KnowledgePointVO>> listAll() {
        List<KnowledgePoint> knowledgePoints = knowledgePointService.findAll();
        List<KnowledgePointVO> knowledgePointVOs = knowledgePoints.stream()
                .map(knowledgePointMapper::toKnowledgePointVO)
                .toList();
        return ApiResponse.success(knowledgePointVOs);
    }

    @Operation(summary = "获取我的知识点", description = "教师查看自己的知识点")
    @PreAuthorize("hasAuthority('" + PermissionConstants.KNOWLEDGE_POINT_LIST_SELF + "')")
    @GetMapping("/my")
    public ApiResponse<List<KnowledgePointVO>> listMy(HttpServletRequest request) {
        Long teacherId = getCurrentUserId(request);
        Page<KnowledgePoint> knowledgePointPage = knowledgePointService.findPageByTeacherWithFilters(
                teacherId, 1, Integer.MAX_VALUE, null, null, null, null);
        List<KnowledgePointVO> knowledgePointVOs = knowledgePointPage.getContent().stream()
                .map(knowledgePointMapper::toKnowledgePointVO)
                .toList();
        return ApiResponse.success(knowledgePointVOs);
    }

    @Operation(summary = "根据课程ID获取知识点", description = "获取指定课程的知识点")
    @PreAuthorize("hasAuthority('" + PermissionConstants.KNOWLEDGE_POINT_LIST_ALL + "') or " +
            "hasAuthority('" + PermissionConstants.KNOWLEDGE_POINT_LIST_SELF + "')")
    @GetMapping("/course/{courseId}")
    public ApiResponse<List<KnowledgePointVO>> listByCourse(@PathVariable Long courseId) {
        List<KnowledgePoint> knowledgePoints = knowledgePointService.findByCourseIdOrderByOrderNum(courseId);
        List<KnowledgePointVO> knowledgePointVOs = knowledgePoints.stream()
                .map(knowledgePointMapper::toKnowledgePointVO)
                .toList();
        return ApiResponse.success(knowledgePointVOs);
    }

    @Operation(summary = "分页获取所有知识点", description = "管理员分页查看所有知识点")
    @PreAuthorize("hasAuthority('" + PermissionConstants.KNOWLEDGE_POINT_LIST_ALL + "')")
    @GetMapping("/page")
    public ApiResponse<PageResult<KnowledgePointVO>> pageAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) KnowledgePoint.Difficulty difficulty) {

        Page<KnowledgePoint> knowledgePointPage = knowledgePointService.findPageWithFilters(
                page, size, keyword, enabled, courseId, difficulty);
        List<KnowledgePointVO> knowledgePointVOs = knowledgePointPage.getContent().stream()
                .map(knowledgePointMapper::toKnowledgePointVO)
                .toList();

        PageResult<KnowledgePointVO> result = new PageResult<>();
        result.setContent(knowledgePointVOs);
        result.setTotal(knowledgePointPage.getTotalElements());
        result.setPage(page);
        result.setSize(size);
        return ApiResponse.success(result);
    }

    @Operation(summary = "分页获取我的知识点", description = "教师分页查看自己的知识点")
    @PreAuthorize("hasAuthority('" + PermissionConstants.KNOWLEDGE_POINT_LIST_SELF + "')")
    @GetMapping("/my/page")
    public ApiResponse<PageResult<KnowledgePointVO>> pageMy(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) KnowledgePoint.Difficulty difficulty) {

        Long teacherId = getCurrentUserId(request);
        Page<KnowledgePoint> knowledgePointPage = knowledgePointService.findPageByTeacherWithFilters(
                teacherId, page, size, keyword, enabled, courseId, difficulty);
        List<KnowledgePointVO> knowledgePointVOs = knowledgePointPage.getContent().stream()
                .map(knowledgePointMapper::toKnowledgePointVO)
                .toList();

        PageResult<KnowledgePointVO> result = new PageResult<>();
        result.setContent(knowledgePointVOs);
        result.setTotal(knowledgePointPage.getTotalElements());
        result.setPage(page);
        result.setSize(size);
        return ApiResponse.success(result);
    }

    @Operation(summary = "创建知识点", description = "教师创建知识点")
    @PreAuthorize("hasAuthority('" + PermissionConstants.KNOWLEDGE_POINT_CREATE_SELF + "')")
    @PostMapping
    public ApiResponse<KnowledgePointVO> create(@RequestBody @Valid CreateKnowledgePointDTO createKnowledgePointDTO,
                                                HttpServletRequest request) {
        Long teacherId = getCurrentUserId(request);
        // 验证课程是否存在且属于当前教师
        Course course = courseService.findById(createKnowledgePointDTO.getCourseId());
        if (course == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "课程不存在");
        }
        if (!course.getTeacher().getId().equals(teacherId)) {
            throw new StatefulException(HttpStatus.HTTP_FORBIDDEN, "您只能为自己的课程创建知识点");
        }

        // 检查同一课程下知识点名称是否重复
        if (knowledgePointService.existsByNameAndCourseId(createKnowledgePointDTO.getName(),
                createKnowledgePointDTO.getCourseId())) {
            throw new StatefulException(HttpStatus.HTTP_CONFLICT, "该课程下已存在同名知识点");
        }

        KnowledgePoint knowledgePoint = knowledgePointMapper.toKnowledgePoint(createKnowledgePointDTO);
        knowledgePoint.setCourse(course);

        // 如果没有设置排序号，则设置为最大值+1
        if (knowledgePoint.getOrderNum() == null) {
            Integer maxOrderNum = knowledgePointService.getMaxOrderNumByCourseId(course.getId());
            knowledgePoint.setOrderNum(maxOrderNum + 1);
        }

        knowledgePoint = knowledgePointService.save(knowledgePoint);
        return ApiResponse.success(knowledgePointMapper.toKnowledgePointVO(knowledgePoint));
    }

    @Operation(summary = "管理员创建知识点", description = "管理员为任意课程创建知识点")
    @PreAuthorize("hasAuthority('" + PermissionConstants.KNOWLEDGE_POINT_CREATE_ALL + "')")
    @PostMapping("/admin")
    public ApiResponse<KnowledgePointVO> createByAdmin(@RequestBody @Valid CreateKnowledgePointDTO createKnowledgePointDTO) {
        Course course = courseService.findById(createKnowledgePointDTO.getCourseId());
        if (course == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "课程不存在");
        }

        // 检查同一课程下知识点名称是否重复
        if (knowledgePointService.existsByNameAndCourseId(createKnowledgePointDTO.getName(),
                createKnowledgePointDTO.getCourseId())) {
            throw new StatefulException(HttpStatus.HTTP_CONFLICT, "该课程下已存在同名知识点");
        }

        KnowledgePoint knowledgePoint = knowledgePointMapper.toKnowledgePoint(createKnowledgePointDTO);
        knowledgePoint.setCourse(course);

        // 如果没有设置排序号，则设置为最大值+1
        if (knowledgePoint.getOrderNum() == null) {
            Integer maxOrderNum = knowledgePointService.getMaxOrderNumByCourseId(course.getId());
            knowledgePoint.setOrderNum(maxOrderNum + 1);
        }

        knowledgePoint = knowledgePointService.save(knowledgePoint);
        return ApiResponse.success(knowledgePointMapper.toKnowledgePointVO(knowledgePoint));
    }

    @Operation(summary = "获取知识点详情", description = "获取知识点详细信息")
    @PreAuthorize("hasAuthority('" + PermissionConstants.KNOWLEDGE_POINT_VIEW_ALL + "') or " +
            "hasAuthority('" + PermissionConstants.KNOWLEDGE_POINT_VIEW_SELF + "')")
    @GetMapping("/{knowledgePointId}")
    public ApiResponse<KnowledgePointDetailVO> detail(@PathVariable Long knowledgePointId, HttpServletRequest request) {
        KnowledgePoint knowledgePoint = knowledgePointService.findById(knowledgePointId);
        if (knowledgePoint == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "知识点不存在");
        }

        // 检查权限：如果只有查看自己的权限，需要验证所有权
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();

        boolean hasViewAll = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals(PermissionConstants.KNOWLEDGE_POINT_VIEW_ALL));

        if (!hasViewAll) {
            Long currentUserId = getCurrentUserId(request);
            if (!knowledgePoint.getCourse().getTeacher().getId().equals(currentUserId)) {
                throw new StatefulException(HttpStatus.HTTP_FORBIDDEN, "您只能查看自己的知识点");
            }
        }

        return ApiResponse.success(knowledgePointMapper.toKnowledgePointDetailVO(knowledgePoint));
    }

    @Operation(summary = "更新知识点", description = "教师更新自己的知识点")
    @PreAuthorize("hasAuthority('" + PermissionConstants.KNOWLEDGE_POINT_EDIT_SELF + "')")
    @PutMapping("/{knowledgePointId}")
    public ApiResponse<KnowledgePointVO> update(@PathVariable Long knowledgePointId,
                                                @RequestBody @Valid UpdateKnowledgePointDTO updateKnowledgePointDTO,
                                                HttpServletRequest request) {
        KnowledgePoint knowledgePoint = knowledgePointService.findById(knowledgePointId);
        if (knowledgePoint == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "知识点不存在");
        }

        // 检查权限：验证是否是自己的知识点
        Long currentUserId = getCurrentUserId(request);
        if (!knowledgePoint.getCourse().getTeacher().getId().equals(currentUserId)) {
            throw new StatefulException(HttpStatus.HTTP_FORBIDDEN, "您只能编辑自己的知识点");
        }

        // 检查知识点名称是否重复（排除自身）
        if (!knowledgePoint.getName().equals(updateKnowledgePointDTO.getName()) &&
                knowledgePointService.existsByNameAndCourseId(updateKnowledgePointDTO.getName(),
                        knowledgePoint.getCourse().getId())) {
            throw new StatefulException(HttpStatus.HTTP_CONFLICT, "该课程下已存在同名知识点");
        }

        knowledgePointMapper.updateKnowledgePointFromDto(updateKnowledgePointDTO, knowledgePoint);
        knowledgePoint = knowledgePointService.save(knowledgePoint);
        return ApiResponse.success(knowledgePointMapper.toKnowledgePointVO(knowledgePoint));
    }

    @Operation(summary = "管理员更新知识点", description = "管理员更新任意知识点")
    @PreAuthorize("hasAuthority('" + PermissionConstants.KNOWLEDGE_POINT_EDIT_ALL + "')")
    @PutMapping("/admin/{knowledgePointId}")
    public ApiResponse<KnowledgePointVO> updateByAdmin(@PathVariable Long knowledgePointId,
                                                       @RequestBody @Valid UpdateKnowledgePointDTO updateKnowledgePointDTO) {
        KnowledgePoint knowledgePoint = knowledgePointService.findById(knowledgePointId);
        if (knowledgePoint == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "知识点不存在");
        }

        // 检查知识点名称是否重复（排除自身）
        if (!knowledgePoint.getName().equals(updateKnowledgePointDTO.getName()) &&
                knowledgePointService.existsByNameAndCourseId(updateKnowledgePointDTO.getName(),
                        knowledgePoint.getCourse().getId())) {
            throw new StatefulException(HttpStatus.HTTP_CONFLICT, "该课程下已存在同名知识点");
        }

        knowledgePointMapper.updateKnowledgePointFromDto(updateKnowledgePointDTO, knowledgePoint);
        knowledgePoint = knowledgePointService.save(knowledgePoint);
        return ApiResponse.success(knowledgePointMapper.toKnowledgePointVO(knowledgePoint));
    }

    @Operation(summary = "删除知识点", description = "教师删除自己的知识点")
    @PreAuthorize("hasAuthority('" + PermissionConstants.KNOWLEDGE_POINT_DELETE_SELF + "')")
    @DeleteMapping("/{knowledgePointId}")
    public ApiResponse<Boolean> delete(@PathVariable Long knowledgePointId, HttpServletRequest request) {
        KnowledgePoint knowledgePoint = knowledgePointService.findById(knowledgePointId);
        if (knowledgePoint == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "知识点不存在");
        }

        // 检查权限：验证是否是自己的知识点
        Long currentUserId = getCurrentUserId(request);
        if (!knowledgePoint.getCourse().getTeacher().getId().equals(currentUserId)) {
            throw new StatefulException(HttpStatus.HTTP_FORBIDDEN, "您只能删除自己的知识点");
        }

        return ApiResponse.success(knowledgePointService.delete(knowledgePointId));
    }

    @Operation(summary = "管理员删除知识点", description = "管理员删除任意知识点")
    @PreAuthorize("hasAuthority('" + PermissionConstants.KNOWLEDGE_POINT_DELETE_ALL + "')")
    @DeleteMapping("/admin/{knowledgePointId}")
    public ApiResponse<Boolean> deleteByAdmin(@PathVariable Long knowledgePointId) {
        KnowledgePoint knowledgePoint = knowledgePointService.findById(knowledgePointId);
        if (knowledgePoint == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "知识点不存在");
        }
        return ApiResponse.success(knowledgePointService.delete(knowledgePointId));
    }

    @Operation(summary = "根据课程ID删除知识点", description = "删除指定课程下的所有知识点，用于级联删除")
    @PreAuthorize("hasAuthority('" + PermissionConstants.KNOWLEDGE_POINT_DELETE_ALL + "')")
    @DeleteMapping("/course/{courseId}")
    public ApiResponse<Boolean> deleteByCourseId(@PathVariable Long courseId) {
        boolean result = knowledgePointService.deleteByCourseId(courseId);
        return ApiResponse.success(result);
    }

    @Operation(summary = "批量删除知识点", description = "批量删除知识点")
    @PreAuthorize("hasAuthority('" + PermissionConstants.KNOWLEDGE_POINT_DELETE_ALL + "')")
    @DeleteMapping("/batch")
    public ApiResponse<Boolean> batchDelete(@RequestBody List<Long> knowledgePointIds) {
        boolean result = knowledgePointService.batchDelete(knowledgePointIds);
        return ApiResponse.success(result);
    }

    @Operation(summary = "更新知识点状态", description = "启用或禁用知识点")
    @PreAuthorize("hasAuthority('" + PermissionConstants.KNOWLEDGE_POINT_EDIT_SELF + "')")
    @PutMapping("/{knowledgePointId}/status")
    public ApiResponse<KnowledgePointVO> updateStatus(@PathVariable Long knowledgePointId,
                                                      @RequestParam Boolean enabled,
                                                      HttpServletRequest request) {
        KnowledgePoint knowledgePoint = knowledgePointService.findById(knowledgePointId);
        if (knowledgePoint == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "知识点不存在");
        }

        // 检查权限：验证是否是自己的知识点
        Long currentUserId = getCurrentUserId(request);
        if (!knowledgePoint.getCourse().getTeacher().getId().equals(currentUserId)) {
            throw new StatefulException(HttpStatus.HTTP_FORBIDDEN, "您只能修改自己的知识点状态");
        }

        knowledgePoint = knowledgePointService.updateEnabled(knowledgePointId, enabled);
        return ApiResponse.success(knowledgePointMapper.toKnowledgePointVO(knowledgePoint));
    }

    @Operation(summary = "更新知识点排序", description = "更新知识点排序号")
    @PreAuthorize("hasAuthority('" + PermissionConstants.KNOWLEDGE_POINT_EDIT_SELF + "')")
    @PutMapping("/{knowledgePointId}/order")
    public ApiResponse<KnowledgePointVO> updateOrder(@PathVariable Long knowledgePointId,
                                                     @RequestParam Integer orderNum,
                                                     HttpServletRequest request) {
        KnowledgePoint knowledgePoint = knowledgePointService.findById(knowledgePointId);
        if (knowledgePoint == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "知识点不存在");
        }

        // 检查权限：验证是否是自己的知识点
        Long currentUserId = getCurrentUserId(request);
        if (!knowledgePoint.getCourse().getTeacher().getId().equals(currentUserId)) {
            throw new StatefulException(HttpStatus.HTTP_FORBIDDEN, "您只能修改自己的知识点排序");
        }

        knowledgePoint = knowledgePointService.updateOrder(knowledgePointId, orderNum);
        return ApiResponse.success(knowledgePointMapper.toKnowledgePointVO(knowledgePoint));
    }

    @Operation(summary = "导出知识点", description = "导出知识点到Excel文件")
    @PreAuthorize("hasAuthority('" + PermissionConstants.KNOWLEDGE_POINT_EXPORT + "')")
    @GetMapping("/export")
    public void exportKnowledgePoints(
            HttpServletResponse response,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) KnowledgePoint.Difficulty difficulty,
            HttpServletRequest request) throws IOException {

        List<KnowledgePointExportDTO> data = knowledgePointService.exportKnowledgePoints(
                courseId, enabled, difficulty, null);
        ExcelUtils.exportKnowledgePoints(data, response);
    }

    @Operation(summary = "导出我的知识点", description = "教师导出自己的知识点")
    @PreAuthorize("hasAuthority('" + PermissionConstants.KNOWLEDGE_POINT_EXPORT + "')")
    @GetMapping("/export/my")
    public void exportMyKnowledgePoints(
            HttpServletResponse response,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) KnowledgePoint.Difficulty difficulty,
            HttpServletRequest request) throws IOException {

        Long teacherId = getCurrentUserId(request);
        List<KnowledgePointExportDTO> data = knowledgePointService.exportKnowledgePoints(
                courseId, enabled, difficulty, teacherId);
        ExcelUtils.exportKnowledgePoints(data, response);
    }

    @Operation(summary = "导入知识点", description = "从Excel文件导入知识点")
    @PreAuthorize("hasAuthority('" + PermissionConstants.KNOWLEDGE_POINT_IMPORT + "')")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, Object>> importKnowledgePoints(
            @Parameter(description = "Excel文件", required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")))
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "课程ID", required = true)
            @RequestParam Long courseId,
            HttpServletRequest request) throws IOException {

        if (file.isEmpty()) {
            throw new StatefulException(HttpStatus.HTTP_BAD_REQUEST, "请选择要导入的文件");
        }

        // 验证文件类型
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {
            throw new StatefulException(HttpStatus.HTTP_BAD_REQUEST, "只支持Excel文件(.xlsx)");
        }

        Long teacherId = getCurrentUserId(request);

        // 使用统一的导入方法
        ExcelUtils.ImportResult importResult = ExcelUtils.importKnowledgePointsFromExcel(file);
        Map<String, Object> result = knowledgePointService.importKnowledgePoints(
                importResult.getData(), courseId, teacherId);

        // 分别处理解析警告和业务错误信息
        result.put("parseWarnings", importResult.getWarnings());

        // 如果有解析警告，添加到结果中但不与错误信息混合
        if (!importResult.getWarnings().isEmpty()) {
            result.put("hasParseWarnings", true);
        } else {
            result.put("hasParseWarnings", false);
        }

        return ApiResponse.success(result);
    }

    @Operation(summary = "管理员导入知识点", description = "管理员为任意课程导入知识点")
    @PreAuthorize("hasAuthority('" + PermissionConstants.KNOWLEDGE_POINT_IMPORT + "')")
    @PostMapping(value = "/import/admin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, Object>> importKnowledgePointsByAdmin(
            @Parameter(description = "Excel文件", required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")))
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "课程ID", required = true)
            @RequestParam Long courseId) throws IOException {

        if (file.isEmpty()) {
            throw new StatefulException(HttpStatus.HTTP_BAD_REQUEST, "请选择要导入的文件");
        }

        // 验证文件类型
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {
            throw new StatefulException(HttpStatus.HTTP_BAD_REQUEST, "只支持Excel文件(.xlsx)");
        }

        // 使用统一的导入方法
        ExcelUtils.ImportResult importResult = ExcelUtils.importKnowledgePointsFromExcel(file);
        Map<String, Object> result = knowledgePointService.importKnowledgePoints(
                importResult.getData(), courseId, null);

        // 分别处理解析警告和业务错误信息
        result.put("parseWarnings", importResult.getWarnings());

        // 如果有解析警告，添加到结果中但不与错误信息混合
        if (!importResult.getWarnings().isEmpty()) {
            result.put("hasParseWarnings", true);
        } else {
            result.put("hasParseWarnings", false);
        }

        return ApiResponse.success(result);
    }

    @Operation(summary = "下载导入模板", description = "下载知识点导入模板")
    @PreAuthorize("hasAuthority('" + PermissionConstants.KNOWLEDGE_POINT_IMPORT + "')")
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.generateTemplate(response);
    }
}