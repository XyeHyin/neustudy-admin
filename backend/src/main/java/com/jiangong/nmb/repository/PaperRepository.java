package com.jiangong.nmb.repository;

import com.jiangong.nmb.entity.Paper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 试卷数据访问接口
 */
@Repository
public interface PaperRepository extends JpaRepository<Paper, Long>, JpaSpecificationExecutor<Paper> {
    // 可根据需要添加自定义查询方法
} 