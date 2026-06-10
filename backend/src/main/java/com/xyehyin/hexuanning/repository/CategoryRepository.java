package com.xyehyin.hexuanning.repository;

import com.xyehyin.hexuanning.entity.Category;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Override
    @EntityGraph(attributePaths = {"parent"})
    List<Category> findAll();

    @Override
    @EntityGraph(attributePaths = {"parent"})
    Optional<Category> findById(Long id);

    Category findByName(String name);

    boolean existsCategoryByNameAndParentIsNull(String name);
}
