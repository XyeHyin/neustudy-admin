package com.xyehyin.hexuanning.mapper;

import com.xyehyin.hexuanning.dto.question.CreateQuestionDTO;
import com.xyehyin.hexuanning.dto.question.UpdateQuestionDTO;
import com.xyehyin.hexuanning.entity.Question;
import com.xyehyin.hexuanning.vo.question.QuestionDetailVO;
import com.xyehyin.hexuanning.vo.question.QuestionVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * 题目映射器
 */
@Mapper(componentModel = "spring", uses = {KnowledgePointMapper.class})
public interface QuestionMapper {

    /**
     * CreateQuestionDTO 转 Question
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "knowledgePoint", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    Question toQuestion(CreateQuestionDTO createQuestionDTO);

    /**
     * Question 转 QuestionVO
     */
    @Mapping(target = "typeDescription", expression = "java(question.getType() != null ? question.getType().getDescription() : null)")
    @Mapping(target = "difficultyDescription", expression = "java(question.getDifficulty() != null ? question.getDifficulty().getDescription() : null)")
    @Mapping(target = "knowledgePoint", source = "knowledgePoint")
    @Mapping(target = "isAiGenerated", source = "isAiGenerated")
    QuestionVO toQuestionVO(Question question);

    /**
     * Question 转 QuestionDetailVO
     */
    @Mapping(target = "typeDescription", expression = "java(question.getType() != null ? question.getType().getDescription() : null)")
    @Mapping(target = "difficultyDescription", expression = "java(question.getDifficulty() != null ? question.getDifficulty().getDescription() : null)")
    QuestionDetailVO toQuestionDetailVO(Question question);

    /**
     * 更新 Question 从 UpdateQuestionDTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "knowledgePoint", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    void updateQuestionFromDto(UpdateQuestionDTO updateQuestionDTO, @MappingTarget Question question);
}