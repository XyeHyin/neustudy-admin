package com.xyehyin.hexuanning.repository;

import com.xyehyin.hexuanning.entity.Paper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 试卷数据访问接口
 */
@Repository
public interface PaperRepository extends JpaRepository<Paper, Long>, JpaSpecificationExecutor<Paper> {

    @Override
    @EntityGraph(attributePaths = {"teacher"})
    Optional<Paper> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"teacher"})
    Page<Paper> findAll(Specification<Paper> spec, Pageable pageable);
}
