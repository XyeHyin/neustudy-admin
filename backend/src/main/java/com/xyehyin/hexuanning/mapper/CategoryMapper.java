package com.xyehyin.hexuanning.mapper;

import com.xyehyin.hexuanning.dto.category.CreateCategoryDTO;
import com.xyehyin.hexuanning.dto.category.UpdateCategoryDTO;
import com.xyehyin.hexuanning.entity.Category;
import com.xyehyin.hexuanning.vo.category.CategoryFlatVO;
import com.xyehyin.hexuanning.vo.category.CategoryTreeVO;
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