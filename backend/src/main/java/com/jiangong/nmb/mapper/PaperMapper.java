package com.jiangong.nmb.mapper;

import com.jiangong.nmb.dto.paper.PaperCreateDTO;
import com.jiangong.nmb.dto.paper.PaperUpdateDTO;
import com.jiangong.nmb.entity.Paper;
import com.jiangong.nmb.vo.paper.PaperDetailVO;
import com.jiangong.nmb.vo.paper.PaperListVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * 试卷映射器
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface PaperMapper {
    /**
     * PaperCreateDTO -> Paper
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "teacher", ignore = true)
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "totalScore", ignore = true)
    Paper toPaper(PaperCreateDTO dto);

    /**
     * Paper -> PaperListVO
     */
    @Mapping(source = "teacher.username", target = "teacherName")
    PaperListVO toPaperListVO(Paper paper);

    /**
     * Paper -> PaperDetailVO
     */
    @Mapping(source = "teacher.id", target = "teacherId")
    @Mapping(source = "teacher.username", target = "teacherName")
    PaperDetailVO toPaperDetailVO(Paper paper);

    /**
     * 更新 Paper 从 PaperUpdateDTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "teacher", ignore = true)
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    void updatePaperFromDto(PaperUpdateDTO dto, @MappingTarget Paper paper);
} 