package com.jiangong.nmb.mapper;

import com.jiangong.nmb.dto.category.CreateCategoryDTO;
import com.jiangong.nmb.dto.category.UpdateCategoryDTO;
import com.jiangong.nmb.entity.Category;
import com.jiangong.nmb.vo.category.CategoryFlatVO;
import com.jiangong.nmb.vo.category.CategoryTreeVO;
import jakarta.validation.Valid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryTreeVO toCategoryTreeVO(Category category);

    @Mapping(target = "parent", expression = "java(category.getParent() == null ? null : toCategoryFlatVO(category.getParent()))")
    CategoryFlatVO toCategoryFlatVO(Category category);

    @Mapping(target = "enabled", constant = "true")
    Category toCategory(CreateCategoryDTO categoryVO);

    @Mapping(target = "id", ignore = true)
    void updateCategoryFromDto(UpdateCategoryDTO dto, @MappingTarget Category category);
}