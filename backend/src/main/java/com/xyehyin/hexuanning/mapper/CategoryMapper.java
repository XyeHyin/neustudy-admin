package com.xyehyin.hexuanning.mapper;

import com.xyehyin.hexuanning.dto.category.CreateCategoryDTO;
import com.xyehyin.hexuanning.dto.category.UpdateCategoryDTO;
import com.xyehyin.hexuanning.entity.Category;
import com.xyehyin.hexuanning.vo.category.CategoryFlatVO;
import com.xyehyin.hexuanning.vo.category.CategoryTreeVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryTreeVO toCategoryTreeVO(Category category);

    @Named("categoryFlat")
    @Mapping(target = "parent", expression = "java(toParentCategoryFlatVO(category.getParent()))")
    CategoryFlatVO toCategoryFlatVO(Category category);

    @Named("parentCategoryFlat")
    @Mapping(target = "parent", ignore = true)
    CategoryFlatVO toParentCategoryFlatVO(Category category);

    @Mapping(target = "enabled", constant = "true")
    Category toCategory(CreateCategoryDTO categoryVO);

    @Mapping(target = "id", ignore = true)
    void updateCategoryFromDto(UpdateCategoryDTO dto, @MappingTarget Category category);
}
