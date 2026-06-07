package com.xyehyin.hexuanning.service;

import cn.hutool.core.exceptions.StatefulException;
import cn.hutool.http.HttpStatus;
import com.xyehyin.hexuanning.common.PageResult;
import com.xyehyin.hexuanning.entity.KnowledgePoint;
import com.xyehyin.hexuanning.entity.Question;
import com.xyehyin.hexuanning.repository.QuestionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import cn.hutool.json.JSONUtil;
import com.xyehyin.hexuanning.dto.question.AIGenerateQuestionDTO;
import com.xyehyin.hexuanning.dto.question.CreateQuestionDTO;
import dev.langchain4j.model.chat.ChatLanguageModel;
import jakarta.persistence.EntityManager;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import com.xyehyin.hexuanning.vo.question.QuestionHistoryVO;
import com.xyehyin.hexuanning.mapper.QuestionMapper;
import com.xyehyin.hexuanning.vo.question.QuestionDetailVO;
import java.util.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import com.xyehyin.hexuanning.vo.question.QuestionVO;

/**
 * 题目服务类
 */
@Slf4j
@Service
public class QuestionService extends BaseService<Question, Long> {

    private final QuestionRepository questionRepository;
    private final KnowledgePointService knowledgePointService;
    private final ChatLanguageModel chatLanguageModel;
    private final EntityManager entityManager;
    private final QuestionMapper questionMapper;

    public QuestionService(QuestionRepository questionRepository, KnowledgePointService knowledgePointService, ChatLanguageModel chatLanguageModel, EntityManager entityManager, QuestionMapper questionMapper) {
        this.questionRepository = questionRepository;
        this.knowledgePointService = knowledgePointService;
        this.chatLanguageModel = chatLanguageModel;
        this.entityManager = entityManager;
        this.questionMapper = questionMapper;
    }

    @Override
    protected QuestionRepository getRepository() {
        return questionRepository;
    }

    /**
     * 根据知识点ID查找题目
     */
    public List<Question> findByKnowledgePointId(Long knowledgePointId) {
        return questionRepository.findByKnowledgePointId(knowledgePointId);
    }

    /**
     * 根据知识点ID和启用状态查找题目
     */
    public List<Question> findByKnowledgePointIdAndEnabled(Long knowledgePointId, Boolean enabled) {
        return questionRepository.findByKnowledgePointIdAndEnabled(knowledgePointId, enabled);
    }

    /**
     * 根据题目类型查找题目
     */
    public List<Question> findByType(Question.QuestionType type) {
        return questionRepository.findByType(type);
    }

    /**
     * 根据难度查找题目
     */
    public List<Question> findByDifficulty(Question.Difficulty difficulty) {
        return questionRepository.findByDifficulty(difficulty);
    }

    /**
     * 统计知识点的题目数量
     */
    public long countByKnowledgePointId(Long knowledgePointId) {
        return questionRepository.countByKnowledgePointId(knowledgePointId);
    }

    /**
     * 统计教师的题目数量
     */
    public long countByTeacherId(Long teacherId) {
        return questionRepository.countByTeacherId(teacherId);
    }

    /**
     * 分页查询所有题目（管理员用）
     */
    public Page<Question> findPageWithFilters(int page, int size, String keyword, Boolean enabled,
                                            Question.QuestionType type, Question.Difficulty difficulty,
                                            Long knowledgePointId) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return questionRepository.findAllWithFilters(keyword, enabled, type, difficulty, knowledgePointId, pageable);
    }

    /**
     * 分页查询教师的题目
     */
    public Page<Question> findPageByTeacherWithFilters(Long teacherId, int page, int size, String keyword,
                                                     Boolean enabled, Question.QuestionType type,
                                                     Question.Difficulty difficulty, Long knowledgePointId) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return questionRepository.findByTeacherIdWithFilters(teacherId, keyword, enabled, type, difficulty,
                knowledgePointId, pageable);
    }

    /**
     * 更新题目启用状态
     */
    @Transactional
    public Question updateEnabled(Long questionId, Boolean enabled) {
        Question question = findById(questionId);
        if (question == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "题目不存在");
        }
        question.setEnabled(enabled);
        return save(question);
    }

    /**
     * 批量删除题目
     */
    @Transactional
    public boolean batchDelete(List<Long> questionIds) {
        try {
            questionRepository.deleteAllById(questionIds);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查题目是否属于指定教师
     */
    public boolean isQuestionOwnedByTeacher(Long questionId, Long teacherId) {
        Question question = findById(questionId);
        return question != null && 
               question.getKnowledgePoint() != null &&
               question.getKnowledgePoint().getCourse() != null &&
               question.getKnowledgePoint().getCourse().getTeacher() != null &&
               question.getKnowledgePoint().getCourse().getTeacher().getId().equals(teacherId);
    }

    /**
     * 获取所有题目类型
     */
    public List<Question.QuestionType> getAllQuestionTypes() {
        return List.of(Question.QuestionType.values());
    }

    /**
     * 获取所有难度级别
     */
    public List<Question.Difficulty> getAllDifficulties() {
        return List.of(Question.Difficulty.values());
    }

    /**
     * AI生成题目
     */
    @Transactional
    public List<CreateQuestionDTO> generateQuestionsWithAI(AIGenerateQuestionDTO requestDTO) {
        try {
            // 1. 获取知识点信息
            KnowledgePoint knowledgePoint = knowledgePointService.findById(requestDTO.getKnowledgePointId());
            if (knowledgePoint == null) {
                throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "知识点不存在，ID: " + requestDTO.getKnowledgePointId());
            }

            // 2. 构造AI Prompt
            String prompt = buildPrompt(knowledgePoint, requestDTO);
            log.info("AI生成题目Prompt: {}", prompt);

            // 3. 调用AI生成题目
            String aiResponse = callAIService(prompt);
            log.info("AI返回结果: {}", aiResponse);

            // 4. 解析AI返回的JSON数组
            List<CreateQuestionDTO> questions = parseAIResponse(aiResponse, requestDTO.getKnowledgePointId());

            // 5. 验证题目类型是否符合要求
            if (requestDTO.getType() != null) {
                for (CreateQuestionDTO question : questions) {
                    validateQuestionType(question, requestDTO.getType());
                }
            }

            // 6. 确保分数多样化
            ensureScoreDiversityForList(questions);

            log.info("成功生成{}道题目", questions.size());
            return questions;

        } catch (Exception e) {
            log.error("AI生成题目失败", e);
            throw new StatefulException(HttpStatus.HTTP_INTERNAL_ERROR, "AI生成题目失败: " + e.getMessage());
        }
    }

    /**
     * AI流式生成时按单题生成，便于前端一题完成就展示一题。
     */
    public CreateQuestionDTO generateSingleQuestionWithAI(AIGenerateQuestionDTO requestDTO, int index, int total) {
        AIGenerateQuestionDTO singleRequest = new AIGenerateQuestionDTO();
        singleRequest.setKnowledgePointId(requestDTO.getKnowledgePointId());
        singleRequest.setType(requestDTO.getType());
        singleRequest.setDifficulty(requestDTO.getDifficulty());
        singleRequest.setCount(1);

        String baseRequirement = requestDTO.getExtraRequirement() == null ? "" : requestDTO.getExtraRequirement().trim();
        String sequenceRequirement = "这是本次批量生成的第 " + index + "/" + total + " 道题，请避免与前面题目重复。";
        singleRequest.setExtraRequirement(baseRequirement.isEmpty()
                ? sequenceRequirement
                : baseRequirement + "；" + sequenceRequirement);

        List<CreateQuestionDTO> questions = generateQuestionsWithAI(singleRequest);
        if (questions.isEmpty()) {
            throw new StatefulException(HttpStatus.HTTP_BAD_GATEWAY, "AI未生成任何题目");
        }
        CreateQuestionDTO question = questions.get(0);
        question.setKnowledgePointId(requestDTO.getKnowledgePointId());
        question.setIsAiGenerated(true);
        return question;
    }

    /**
     * 构造AI Prompt
     */
    private String buildPrompt(KnowledgePoint knowledgePoint, AIGenerateQuestionDTO requestDTO) {
        StringBuilder prompt = new StringBuilder();

        // 基础信息
        prompt.append("请基于知识点【").append(knowledgePoint.getName()).append("】：")
              .append(knowledgePoint.getDescription()).append("，内容要点：")
              .append(knowledgePoint.getContent());

        // 严格限定类型和难度
        String typeRequirement = requestDTO.getType() != null ? requestDTO.getType().name() : "任意类型";
        String difficultyRequirement = requestDTO.getDifficulty() != null ? requestDTO.getDifficulty().name() : "任意难度";

        prompt.append("，严格按照以下要求生成题目：")
              .append("\n- 题目类型：必须是 ").append(typeRequirement)
              .append("\n- 难度级别：必须是 ").append(difficultyRequirement)
              .append("\n- 生成数量：").append(requestDTO.getCount()).append(" 道题目")
              .append("\n- 额外需求：").append(requestDTO.getExtraRequirement() != null ? requestDTO.getExtraRequirement() : "无特殊要求");

        // 根据题目类型给出具体指导
        if (requestDTO.getType() != null) {
            switch (requestDTO.getType()) {
                case SINGLE_CHOICE:
                    prompt.append("\n\n单选题要求：")
                          .append("\n- 提供4个选项（A、B、C、D），只有一个正确答案")
                          .append("\n- options字段必须是有效的JSON数组格式")
                          .append("\n- 分数要求：根据难度随机分配，简单1-3分，中等2-5分，困难3-8分（每道题分数要不同）");
                    break;
                case MULTIPLE_CHOICE:
                    prompt.append("\n\n多选题要求：")
                          .append("\n- 提供4-6个选项（A、B、C、D、E、F），必须有2-4个正确答案")
                          .append("\n- answer字段用逗号分隔多个正确选项（如\"A,C,D\"）")
                          .append("\n- options字段必须是有效的JSON数组格式")
                          .append("\n- 分数要求：根据难度随机分配，简单2-5分，中等3-8分，困难5-12分（每道题分数要不同）");
                    break;
                case TRUE_FALSE:
                    prompt.append("\n\n判断题要求：")
                          .append("\n- 只提供2个选项（A:正确，B:错误），answer字段填\"A\"或\"B\"")
                          .append("\n- options字段必须是有效的JSON数组格式")
                          .append("\n- 分数要求：根据难度随机分配，简单1-2分，中等1-3分，困难2-5分（每道题分数要不同）");
                    break;
                case FILL_BLANK:
                    prompt.append("\n\n填空题要求：")
                          .append("\n- options字段必须为null（不是空字符串）")
                          .append("\n- answer字段填入空缺处的正确答案")
                          .append("\n- 分数要求：根据难度随机分配，简单1-3分，中等2-6分，困难3-10分（每道题分数要不同）");
                    break;
                case SHORT_ANSWER:
                    prompt.append("\n\n简答题要求：")
                          .append("\n- options字段必须为null（不是空字符串）")
                          .append("\n- answer字段填入参考答案要点")
                          .append("\n- 分数要求：根据难度随机分配，简单3-8分，中等5-12分，困难8-18分（每道题分数要不同）");
                    break;
                case ESSAY:
                    prompt.append("\n\n论述题要求：")
                          .append("\n- options字段必须为null（不是空字符串）")
                          .append("\n- answer字段填入详细的参考答案")
                          .append("\n- 分数要求：根据难度随机分配，简单5-12分，中等8-15分，困难10-20分（每道题分数要不同）");
                    break;
            }
        }

        prompt.append("\n\n请严格按照以下JSON格式返回题目数组，确保type字段与要求完全一致，不要添加任何其他文字说明：")
              .append("\n[")
              .append("\n  {")
              .append("\n    \"title\": \"题目标题\",")
              .append("\n    \"content\": \"题目内容描述\",")
              .append("\n    \"type\": \"").append(typeRequirement).append("\",")
              .append("\n    \"difficulty\": \"").append(difficultyRequirement).append("\",");

        // 根据题目类型提供不同的示例
        if (requestDTO.getType() == Question.QuestionType.MULTIPLE_CHOICE) {
            prompt.append("\n    \"options\": \"[{\\\"key\\\":\\\"A\\\",\\\"value\\\":\\\"选项A\\\"},{\\\"key\\\":\\\"B\\\",\\\"value\\\":\\\"选项B\\\"},{\\\"key\\\":\\\"C\\\",\\\"value\\\":\\\"选项C\\\"},{\\\"key\\\":\\\"D\\\",\\\"value\\\":\\\"选项D\\\"}]\",")
                  .append("\n    \"answer\": \"A,C\",")
                  .append("\n    \"explanation\": \"答案解析\",")
                  .append("\n    \"score\": \"在对应难度分数区间内随机选择\",");
        } else if (requestDTO.getType() == Question.QuestionType.TRUE_FALSE) {
            prompt.append("\n    \"options\": \"[{\\\"key\\\":\\\"A\\\",\\\"value\\\":\\\"正确\\\"},{\\\"key\\\":\\\"B\\\",\\\"value\\\":\\\"错误\\\"}]\",")
                  .append("\n    \"answer\": \"A\",")
                  .append("\n    \"explanation\": \"答案解析\",")
                  .append("\n    \"score\": \"在对应难度分数区间内随机选择\",");
        } else if (requestDTO.getType() == Question.QuestionType.SINGLE_CHOICE) {
            prompt.append("\n    \"options\": \"[{\\\"key\\\":\\\"A\\\",\\\"value\\\":\\\"选项A\\\"},{\\\"key\\\":\\\"B\\\",\\\"value\\\":\\\"选项B\\\"},{\\\"key\\\":\\\"C\\\",\\\"value\\\":\\\"选项C\\\"},{\\\"key\\\":\\\"D\\\",\\\"value\\\":\\\"选项D\\\"}]\",")
                  .append("\n    \"answer\": \"A\",")
                  .append("\n    \"explanation\": \"答案解析\",")
                  .append("\n    \"score\": \"在对应难度分数区间内随机选择\",");
        } else if (requestDTO.getType() == Question.QuestionType.FILL_BLANK) {
            prompt.append("\n    \"options\": null,")
                  .append("\n    \"answer\": \"参考答案\",")
                  .append("\n    \"explanation\": \"答案解析\",")
                  .append("\n    \"score\": \"在对应难度分数区间内随机选择\",");
        } else if (requestDTO.getType() == Question.QuestionType.SHORT_ANSWER) {
            prompt.append("\n    \"options\": null,")
                  .append("\n    \"answer\": \"参考答案要点\",")
                  .append("\n    \"explanation\": \"答案解析\",")
                  .append("\n    \"score\": \"在对应难度分数区间内随机选择\",");
        } else if (requestDTO.getType() == Question.QuestionType.ESSAY) {
            prompt.append("\n    \"options\": null,")
                  .append("\n    \"answer\": \"详细的参考答案\",")
                  .append("\n    \"explanation\": \"答案解析\",")
                  .append("\n    \"score\": \"在对应难度分数区间内随机选择\",");
        } else {
            // 任意类型的情况
            prompt.append("\n    \"options\": \"根据题目类型决定：选择题为JSON数组，其他为null\",")
                  .append("\n    \"answer\": \"参考答案\",")
                  .append("\n    \"explanation\": \"答案解析\",")
                  .append("\n    \"score\": \"根据题目类型和难度在1-20分区间内随机选择\",");
        }

        prompt.append("\n    \"tags\": \"标签1,标签2\",")
              .append("\n    \"isAiGenerated\": true,")
              .append("\n    \"enabled\": true")
              .append("\n  }")
              .append("\n]")
              .append("\n\n**重要的分数分配要求**：")
              .append("\n1. type字段必须严格使用：").append(typeRequirement)
              .append("\n2. difficulty字段必须严格使用：").append(difficultyRequirement)
              .append("\n3. **分数必须多样化**：每道题目的score字段必须在对应的分数区间内随机选择，避免所有题目都使用相同分数")
              .append("\n4. 分数区间映射：")
              .append("\n   - 判断题：简单(1-2)，中等(1-3)，困难(2-5)")
              .append("\n   - 单选题：简单(1-3)，中等(2-5)，困难(3-8)")
              .append("\n   - 多选题：简单(2-5)，中等(3-8)，困难(5-12)")
              .append("\n   - 填空题：简单(1-3)，中等(2-6)，困难(3-10)")
              .append("\n   - 简答题：简单(3-8)，中等(5-12)，困难(8-18)")
              .append("\n   - 论述题：简单(5-12)，中等(8-15)，困难(10-20)")
              .append("\n5. options字段规则：")
              .append("\n   - 单选题/多选题/判断题：必须是有效的JSON数组字符串")
              .append("\n   - 填空题/简答题/论述题：必须是null（不是空字符串\"\"）")
              .append("\n6. 如果是多选题，answer字段必须包含多个正确答案，用逗号分隔")
              .append("\n7. **请确保每道题目的分数都不相同，要在指定区间内体现分数的多样性**");

        return prompt.toString();
    }

    /**
     * 调用AI服务
     */
    private String callAIService(String prompt) {
        try {
            String response = chatLanguageModel.generate(prompt);
            return cleanAIResponse(response);

        } catch (Exception e) {
            log.error("调用AI服务失败", e);
            throw new StatefulException(HttpStatus.HTTP_UNAVAILABLE, "调用AI服务失败: " + e.getMessage());
        }
    }

    /**
     * 清理AI响应，提取JSON部分
     */
    private String cleanAIResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            throw new StatefulException(HttpStatus.HTTP_BAD_GATEWAY, "AI返回空响应");
        }

        // 去除前后空白字符
        response = response.trim();

        // 查找JSON数组的开始和结束位置
        int startIndex = response.indexOf('[');
        int endIndex = response.lastIndexOf(']');

        if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
            log.warn("AI响应中未找到有效的JSON数组，原始响应: {}", response);
            throw new StatefulException(HttpStatus.HTTP_BAD_GATEWAY, "AI返回的响应格式不正确，未找到有效的JSON数组");
        }

        String jsonPart = response.substring(startIndex, endIndex + 1);
        log.debug("提取的JSON部分: {}", jsonPart);

        return jsonPart;
    }

    /**
     * 解析AI返回结果
     */
    private List<CreateQuestionDTO> parseAIResponse(String aiResponse, Long knowledgePointId) {
        try {
            List<CreateQuestionDTO> questions = JSONUtil.toList(aiResponse, CreateQuestionDTO.class);

            if (questions == null || questions.isEmpty()) {
                throw new StatefulException(HttpStatus.HTTP_BAD_GATEWAY, "AI未生成任何题目");
            }

            // 设置知识点ID和AI生成标识
            questions.forEach(question -> {
                question.setKnowledgePointId(knowledgePointId);
                question.setIsAiGenerated(true);
                if (question.getEnabled() == null) {
                    question.setEnabled(true);
                }
                if (question.getScore() == null) {
                    question.setScore(1);
                }

                // 验证必需字段
                validateAIQuestion(question);
            });

            return questions;
        } catch (Exception e) {
            log.error("解析AI返回结果失败: {}", aiResponse, e);
            throw new StatefulException(HttpStatus.HTTP_INTERNAL_ERROR, "解析AI返回结果失败: " + e.getMessage());
        }
    }

    /**
     * 验证AI生成的题目字段完整性
     */
    private void validateAIQuestion(CreateQuestionDTO question) {
        if (question.getTitle() == null || question.getTitle().trim().isEmpty()) {
            throw new StatefulException(HttpStatus.HTTP_BAD_REQUEST, "AI生成的题目缺少标题");
        }
        if (question.getType() == null) {
            throw new StatefulException(HttpStatus.HTTP_BAD_REQUEST, "AI生成的题目缺少类型");
        }
        if (question.getDifficulty() == null) {
            throw new StatefulException(HttpStatus.HTTP_BAD_REQUEST, "AI生成的题目缺少难度");
        }
        if (question.getAnswer() == null || question.getAnswer().trim().isEmpty()) {
            throw new StatefulException(HttpStatus.HTTP_BAD_REQUEST, "AI生成的题目缺少答案");
        }

        // 验证分数范围
        if (question.getScore() == null || question.getScore() < 1 || question.getScore() > 20) {
            throw new StatefulException(HttpStatus.HTTP_BAD_REQUEST, "题目分数必须在1-20分之间");
        }

        // 验证多选题的答案格式
        if (question.getType() == Question.QuestionType.MULTIPLE_CHOICE) {
            String answer = question.getAnswer();
            if (!answer.contains(",")) {
                log.warn("多选题答案格式可能不正确，应包含多个选项用逗号分隔: {}", answer);
            }
        }

        // 验证选择题必须有options，非选择题options应该为null或空
        if (question.getType() == Question.QuestionType.SINGLE_CHOICE || 
            question.getType() == Question.QuestionType.MULTIPLE_CHOICE ||
            question.getType() == Question.QuestionType.TRUE_FALSE) {
            // 选择题和判断题必须有options
            if (question.getOptions() == null || question.getOptions().trim().isEmpty()) {
                throw new StatefulException(HttpStatus.HTTP_BAD_REQUEST, "选择题和判断题必须包含选项");
            }
        } else {
            // 填空题、简答题、论述题的options应该为null
            if (question.getOptions() != null && !question.getOptions().trim().isEmpty()) {
                log.warn("非选择题的options应该为null，题目类型: {}, options: {}", 
                        question.getType(), question.getOptions());
                // 自动修正为null
                question.setOptions(null);
            }
        }
    }

    /**
     * 验证AI生成的题目类型是否与用户要求一致
     */
    private void validateQuestionType(CreateQuestionDTO question, Question.QuestionType expectedType) {
        if (expectedType != null && !expectedType.equals(question.getType())) {
            log.warn("AI生成的题目类型({})与用户要求({})不一致，题目: {}", 
                    question.getType(), expectedType, question.getTitle());
            throw new StatefulException(HttpStatus.HTTP_BAD_REQUEST, 
                    String.format("AI生成的题目类型(%s)与用户要求(%s)不一致", question.getType(), expectedType));
        }
    }

    /**
     * 确保题目列表分数多样化
     */
    private void ensureScoreDiversityForList(List<CreateQuestionDTO> questions) {
        for (int i = 0; i < questions.size(); i++) {
            ensureScoreDiversity(questions, i);
        }
    }

    /**
     * 确保分数多样化
     */
    private void ensureScoreDiversity(List<CreateQuestionDTO> questions, int currentIndex) {
        CreateQuestionDTO currentQuestion = questions.get(currentIndex);
        
        // 如果分数为null，设置默认值
        if (currentQuestion.getScore() == null) {
            currentQuestion.setScore(getRandomScoreForQuestion(currentQuestion));
            return;
        }
        
        // 检查是否与前面的题目分数重复
        for (int i = 0; i < currentIndex; i++) {
            CreateQuestionDTO prevQuestion = questions.get(i);
            if (prevQuestion.getScore() != null && 
                prevQuestion.getScore().equals(currentQuestion.getScore()) &&
                prevQuestion.getType() == currentQuestion.getType() &&
                prevQuestion.getDifficulty() == currentQuestion.getDifficulty()) {
                
                // 分数重复，重新分配
                currentQuestion.setScore(getRandomScoreForQuestion(currentQuestion));
                log.debug("检测到分数重复，为第{}道题目重新分配分数: {}", currentIndex + 1, currentQuestion.getScore());
                break;
            }
        }
    }

    /**
     * 根据题目类型和难度随机生成分数
     */
    private Integer getRandomScoreForQuestion(CreateQuestionDTO question) {
        java.util.Random random = new java.util.Random();
        
        if (question.getType() == null || question.getDifficulty() == null) {
            return 1; // 默认分数
        }
        
        return switch (question.getType()) {
            case TRUE_FALSE -> switch (question.getDifficulty()) {
                case EASY -> random.nextInt(2) + 1; // 1-2
                case MEDIUM -> random.nextInt(3) + 1; // 1-3
                case HARD -> random.nextInt(4) + 2; // 2-5
            };
            case SINGLE_CHOICE -> switch (question.getDifficulty()) {
                case EASY -> random.nextInt(3) + 1; // 1-3
                case MEDIUM -> random.nextInt(4) + 2; // 2-5
                case HARD -> random.nextInt(6) + 3; // 3-8
            };
            case MULTIPLE_CHOICE -> switch (question.getDifficulty()) {
                case EASY -> random.nextInt(4) + 2; // 2-5
                case MEDIUM -> random.nextInt(6) + 3; // 3-8
                case HARD -> random.nextInt(8) + 5; // 5-12
            };
            case FILL_BLANK -> switch (question.getDifficulty()) {
                case EASY -> random.nextInt(3) + 1; // 1-3
                case MEDIUM -> random.nextInt(5) + 2; // 2-6
                case HARD -> random.nextInt(8) + 3; // 3-10
            };
            case SHORT_ANSWER -> switch (question.getDifficulty()) {
                case EASY -> random.nextInt(6) + 3; // 3-8
                case MEDIUM -> random.nextInt(8) + 5; // 5-12
                case HARD -> random.nextInt(11) + 8; // 8-18
            };
            case ESSAY -> switch (question.getDifficulty()) {
                case EASY -> random.nextInt(8) + 5; // 5-12
                case MEDIUM -> random.nextInt(8) + 8; // 8-15
                case HARD -> random.nextInt(11) + 10; // 10-20
            };
        };
    }

    /**
     * 分页获取题目的历史版本
     *
     * @param questionId 题目ID
     * @param page 页码，从1开始
     * @param size 每页大小
     * @return 历史版本分页列表
     */
    public PageResult<QuestionHistoryVO> getQuestionHistory(Long questionId, int page, int size) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        List<Number> revisions = reader.getRevisions(Question.class, questionId);
        long total = revisions.size();
        int p = Math.max(page, 1) - 1;
        int from = p * size;
        int to = Math.min(from + size, revisions.size());
        List<QuestionHistoryVO> content = new ArrayList<>();
        for (Number rev : revisions.subList(from, to)) {
            Date revDate = reader.getRevisionDate(rev);
            Question q = reader.find(Question.class, questionId, rev);
            QuestionDetailVO detail = questionMapper.toQuestionDetailVO(q);
            QuestionHistoryVO vo = new QuestionHistoryVO();
            vo.setRevision(rev);
            vo.setRevisionDate(LocalDateTime.ofInstant(revDate.toInstant(), ZoneId.systemDefault()));
            vo.setQuestion(detail);
            content.add(vo);
        }
        return PageResult.<QuestionHistoryVO>builder()
                .content(content)
                .total(total)
                .page(page)
                .size(size)
                .build();
    }

    /**
     * 回退题目到指定版本
     *
     * @param questionId 题目ID
     * @param revision 版本号
     * @return 回退后的题目VO
     */
    @Transactional
    public QuestionVO revertQuestion(Long questionId, Number revision) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        Question q = reader.find(Question.class, questionId, revision);
        // 保存当前实体覆盖数据库记录，产生新的版本
        Question saved = save(q);
        return questionMapper.toQuestionVO(saved);
    }
}
