package com.xyehyin.hexuanning.controller;

import cn.hutool.core.exceptions.StatefulException;
import cn.hutool.http.HttpStatus;
import com.xyehyin.hexuanning.common.ApiResponse;
import com.xyehyin.hexuanning.constant.PermissionConstants;
import com.xyehyin.hexuanning.dto.category.CreateCategoryDTO;
import com.xyehyin.hexuanning.dto.category.UpdateCategoryDTO;
import com.xyehyin.hexuanning.entity.Category;
import com.xyehyin.hexuanning.mapper.CategoryMapper;
import com.xyehyin.hexuanning.service.CategoryService;
import com.xyehyin.hexuanning.service.UserService;
import com.xyehyin.hexuanning.vo.category.CategoryFlatVO;
import com.xyehyin.hexuanning.vo.category.CategoryTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Tag(name = "分类管理", description = "分类相关接口")
@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/categories")
public class CategoryController extends BaseController {
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;
    private final UserService userService;

    @Operation(summary = "创建分类", description = "创建分类")
    @PreAuthorize("hasAuthority('" + PermissionConstants.CATEGORY_CREATE_ALL + "')")
    @PostMapping
    public ApiResponse<CategoryFlatVO> create(@RequestBody @Valid CreateCategoryDTO createCategoryDTO) {
        Long parentId = createCategoryDTO.getParentId();
        Category parent = null;
        if (parentId != null) {
            parent = categoryService.findById(parentId);
            if (parent == null) {
                throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "父级分类不存在");
            }
            if (parent.getChildren().stream().anyMatch(c -> c.getName().equals(createCategoryDTO.getName()))) {
                throw new StatefulException(HttpStatus.HTTP_BAD_REQUEST, "父级分类下已存在同名分类");
            }
        } else {
            if (categoryService.isCategoryNameExistsWithNoParent(createCategoryDTO.getName())) {
                throw new StatefulException(HttpStatus.HTTP_CONFLICT, "分类名称已存在");
            }
        }
        Category category = categoryMapper.toCategory(createCategoryDTO);
        category.setParent(parent);
        categoryService.save(category);
        return ApiResponse.success(categoryMapper.toCategoryFlatVO(category));
    }

    @Operation(summary = "删除分类", description = "删除分类")
    @PreAuthorize("hasAuthority('" + PermissionConstants.CATEGORY_DELETE_ALL + "')")
    @DeleteMapping("{categoryId}")
    public ApiResponse<Boolean> delete(@PathVariable Long categoryId) {
        Category category = categoryService.findById(categoryId);
        if (category == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "分类不存在");
        }
        categoryService.deleteById(categoryId);
        return ApiResponse.success(true);
    }

    @Operation(summary = "批量删除分类", description = "批量删除分类")
    @PreAuthorize("hasAuthority('" + PermissionConstants.CATEGORY_DELETE_ALL + "')")
    @DeleteMapping("/batch")
    public ApiResponse<Boolean> batchDelete(@RequestBody List<Long> categoryIds) {
        categoryService.batchDelete(categoryIds);
        return ApiResponse.success(true);
    }

    @Operation(summary = "修改分类", description = "修改分类")
    @PreAuthorize("hasAuthority('" + PermissionConstants.CATEGORY_EDIT_ALL + "')")
    @PutMapping("{categoryId}")
    public ApiResponse<CategoryFlatVO> update(@PathVariable Long categoryId, @RequestBody @Valid UpdateCategoryDTO updateCategoryDTO) {
        Category category = categoryService.findById(categoryId);
        if (category == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "分类不存在");
        }
        Category parent = null;
        Long parentId = updateCategoryDTO.getParentId();
        if (parentId != null) {
            if (parentId.equals(category.getId())) {
                throw new StatefulException(HttpStatus.HTTP_CONFLICT, "不能将分类设置为自己的父级分类");
            }
            parent = categoryService.findById(parentId);
            if (parent == null) {
                throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "父级分类不存在");
            }
            if (parent.getChildren().stream().anyMatch(c -> c.getName().equals(updateCategoryDTO.getName()))) {
                throw new StatefulException(HttpStatus.HTTP_BAD_REQUEST, "父级分类下已存在同名分类");
            }
        }
        category.setParent(parent);
        categoryMapper.updateCategoryFromDto(updateCategoryDTO, category);
        categoryService.save(category);
        return ApiResponse.success(categoryMapper.toCategoryFlatVO(category));
    }

    @Operation(summary = "获取所有分类（树形）", description = "获取所有分类（树形）")
    @PreAuthorize("hasAuthority('" + PermissionConstants.CATEGORY_LIST_ALL + "')")
    @GetMapping("/tree")
    public ApiResponse<List<CategoryTreeVO>> treeList() {
        return ApiResponse.success(categoryService.findAll().stream().filter(c -> c.getParent() == null).map(categoryMapper::toCategoryTreeVO).toList());
    }

    @Operation(summary = "获取所有分类（扁平化）", description = "获取所有分类（扁平化）")
    @PreAuthorize("hasAuthority('" + PermissionConstants.CATEGORY_LIST_ALL + "')")
    @GetMapping
    public ApiResponse<List<CategoryFlatVO>> flatList() {
        return ApiResponse.success(categoryService.findAll().stream().map(categoryMapper::toCategoryFlatVO).toList());
    }
}
