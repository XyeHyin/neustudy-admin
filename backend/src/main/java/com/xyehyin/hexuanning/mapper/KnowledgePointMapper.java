package com.xyehyin.hexuanning.mapper;

import com.xyehyin.hexuanning.dto.knowledgepoint.CreateKnowledgePointDTO;
import com.xyehyin.hexuanning.dto.knowledgepoint.UpdateKnowledgePointDTO;
import com.xyehyin.hexuanning.entity.KnowledgePoint;
import com.xyehyin.hexuanning.vo.knowledgepoint.KnowledgePointDetailVO;
import com.xyehyin.hexuanning.vo.knowledgepoint.KnowledgePointSimpleVO;
import com.xyehyin.hexuanning.vo.knowledgepoint.KnowledgePointVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {CourseMapper.class, UserMapper.class})
public interface KnowledgePointMapper {
    KnowledgePointMapper INSTANCE = Mappers.getMapper(KnowledgePointMapper.class);

    /**
     * CreateKnowledgePointDTO -> KnowledgePoint
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    KnowledgePoint toKnowledgePoint(CreateKnowledgePointDTO dto);

    /**
     * KnowledgePoint -> KnowledgePointVO
     */
    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "course.name", target = "courseName")
    @Mapping(target = "difficultyDescription", expression = "java(knowledgePoint.getDifficulty() != null ? knowledgePoint.getDifficulty().getDescription() : null)")
    KnowledgePointVO toKnowledgePointVO(KnowledgePoint knowledgePoint);

    /**
     * KnowledgePoint -> KnowledgePointDetailVO
     */
    @Mapping(source = "course", target = "course")
    @Mapping(target = "difficultyDescription", expression = "java(knowledgePoint.getDifficulty() != null ? knowledgePoint.getDifficulty().getDescription() : null)")
    KnowledgePointDetailVO toKnowledgePointDetailVO(KnowledgePoint knowledgePoint);

    /**
     * KnowledgePoint -> KnowledgePointSimpleVO
     */
    @Mapping(target = "difficultyDescription", expression = "java(knowledgePoint.getDifficulty() != null ? knowledgePoint.getDifficulty().getDescription() : null)")
    KnowledgePointSimpleVO toKnowledgePointSimpleVO(KnowledgePoint knowledgePoint);

    /**
     * 更新 KnowledgePoint 从 UpdateKnowledgePointDTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    void updateKnowledgePointFromDto(UpdateKnowledgePointDTO dto, @MappingTarget KnowledgePoint knowledgePoint);
}