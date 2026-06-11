package com.xyehyin.hexuanning.service;

import com.xyehyin.hexuanning.entity.Category;
import com.xyehyin.hexuanning.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService extends BaseService<Category, Long> {
    private final CategoryRepository categoryRepository;

    @Override
    protected JpaRepository<Category, Long> getRepository() {
        return categoryRepository;
    }

    public boolean isCategoryNameExistsWithNoParent(String name) {
        return categoryRepository.existsCategoryByNameAndParentIsNull(name);
    }

    public boolean existsByNameAndParentId(String name, Long parentId) {
        if (parentId == null) {
            return categoryRepository.existsCategoryByNameAndParentIsNull(name);
        }
        return categoryRepository.existsCategoryByNameAndParent_Id(name, parentId);
    }

    public boolean existsByNameAndParentIdExcludingId(String name, Long parentId, Long excludedId) {
        if (parentId == null) {
            return categoryRepository.existsCategoryByNameAndParentIsNullAndIdNot(name, excludedId);
        }
        return categoryRepository.existsCategoryByNameAndParent_IdAndIdNot(name, parentId, excludedId);
    }
}
