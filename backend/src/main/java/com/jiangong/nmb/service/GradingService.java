package com.jiangong.nmb.service;

import com.jiangong.nmb.entity.GradingResult;
import com.jiangong.nmb.entity.StudentAnswer;
import com.jiangong.nmb.repository.GradingResultRepository;
import com.jiangong.nmb.repository.StudentAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jiangong.nmb.dto.grading.*;
import com.jiangong.nmb.vo.grading.*;
import com.jiangong.nmb.entity.Question;
import com.jiangong.nmb.repository.QuestionRepository;
import com.jiangong.nmb.repository.PaperQuestionRepository;
import com.jiangong.nmb.service.AIGradingService;
import com.jiangong.nmb.repository.UserRepository;
import com.jiangong.nmb.entity.User;
import com.jiangong.nmb.repository.PracticeSessionRepository;
import com.jiangong.nmb.entity.PracticeSession;

import java.time.LocalDateTime;

import com.jiangong.nmb.common.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.stream.Collectors;

import com.jiangong.nmb.entity.PaperQuestion;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 判分服务
 */
@Service
@RequiredArgsConstructor
public class GradingService {
    private final GradingResultRepository gradingResultRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final QuestionRepository questionRepository;
    private final PaperQuestionRepository paperQuestionRepository;
    private final AIGradingService aiGradingService;
    private final UserRepository userRepository;
    private final PracticeSessionRepository practiceSessionRepository;

    @Transactional
    public GradingResult saveGradingResult(GradingResult result) {
        // 判分结果保存业务逻辑
        return gradingResultRepository.save(result);
    }

    public Optional<GradingResult> findById(Long id) {
        return gradingResultRepository.findById(id);
    }

    @Transactional
    public GradingResultVO autoGrading(GradingRequestDTO dto) {
        StudentAnswer answer = studentAnswerRepository.findById(dto.getStudentAnswerId()).orElseThrow();
        Question question = answer.getQuestion();
        if (question == null) {
            question = questionRepository.findById(answer.getQuestion().getId()).orElseThrow();
        }
        // 获取题目分值（优先取试卷分值，否则取题目默认分值）
        final double[] maxScoreArr = {question.getScore() != null ? question.getScore() : 1.0};
        if (answer.getPracticeSession() != null && answer.getPracticeSession().getPaper() != null) {
            Long paperId = answer.getPracticeSession().getPaper().getId();
            Long questionId = question.getId();
            paperQuestionRepository.findByPaperIdAndQuestionId(paperId, questionId).ifPresent(pq -> maxScoreArr[0] = pq.getScore() != null ? pq.getScore() : maxScoreArr[0]);
        }
        double maxScore = maxScoreArr[0];
        double score = 0.0;
        String aiComment = "";
        String aiReason = "";
        boolean isObjective = question.getType() == Question.QuestionType.SINGLE_CHOICE || question.getType() == Question.QuestionType.MULTIPLE_CHOICE || question.getType() == Question.QuestionType.TRUE_FALSE;
        if (isObjective) {
            // 客观题：直接比对答案
            String stdAnswer = question.getAnswer();
            String stuAnswer = dto.getAnswerContent() != null ? dto.getAnswerContent().trim() : "";
            boolean correct = stdAnswer != null && stdAnswer.trim().equalsIgnoreCase(stuAnswer);
            score = correct ? maxScore : 0.0;
            aiComment = correct ? "答案正确" : "答案错误";
            aiReason = aiComment;
            answer.setCorrect(correct);
        } else {
            // 主观题：AI判分
            AIGradingService.AIGradingResult aiResult = aiGradingService.gradeAnswer(question.getContent(), dto.getAnswerContent(), maxScore);
            score = aiResult.score();
            aiComment = aiResult.comment();
            aiReason = aiResult.reason();
            // 对于主观题：如果得分等于满分，则标记正确，否则标记为错误
            answer.setCorrect(Double.compare(score, maxScore) == 0);
        }
        answer.setScore(score);
        answer.setAiComment(aiComment);
        studentAnswerRepository.save(answer);
        GradingResult result = new GradingResult();
        result.setStudentAnswer(answer);
        result.setAiScore(score);
        result.setAiComment(aiComment);
        result.setAiReason(aiReason);
        result.setAiGradingTime(LocalDateTime.now());
        gradingResultRepository.save(result);
        return toVO(result);
    }

    @Transactional
    public GradingResultVO aiGrading(AIGradingDTO dto) {
        // AI判分：模拟AI返回分数和评语
        StudentAnswer answer = studentAnswerRepository.findById(dto.getStudentAnswerId()).orElseThrow();
        double aiScore = dto.getAiScore() != null ? dto.getAiScore() : 0.0;
        answer.setScore(aiScore);
        answer.setCorrect(aiScore > 0);
        answer.setAiComment(dto.getAiComment());
        studentAnswerRepository.save(answer);
        GradingResult result = new GradingResult();
        result.setStudentAnswer(answer);
        result.setAiScore(aiScore);
        result.setAiComment(dto.getAiComment());
        result.setAiReason(dto.getAiReason());
        result.setAiGradingTime(LocalDateTime.now());
        gradingResultRepository.save(result);
        return toVO(result);
    }

    /**
     * 批量自动判分，包含客观题与主观题
     */
    @Transactional
    public List<GradingResultVO> batchAutoGrading(BatchGradingRequestDTO dto) {
        List<GradingResultVO> resultVOList = new ArrayList<>();
        List<AIGradingService.AIRequest> aiRequests = new ArrayList<>();
        List<StudentAnswer> subjectiveAnswers = new ArrayList<>();
        List<Double> subjectiveMaxScores = new ArrayList<>();
        for (GradingRequestDTO req : dto.getRequests()) {
            StudentAnswer answer = studentAnswerRepository.findById(req.getStudentAnswerId()).orElseThrow();
            Question question = answer.getQuestion();
            if (question == null) {
                question = questionRepository.findById(answer.getQuestion().getId()).orElseThrow();
            }
            double maxScore = question.getScore() != null ? question.getScore() : 1.0;
            if (answer.getPracticeSession() != null && answer.getPracticeSession().getPaper() != null) {
                Long paperId = answer.getPracticeSession().getPaper().getId();
                Long questionId = question.getId();
                Optional<PaperQuestion> pqOpt = paperQuestionRepository.findByPaperIdAndQuestionId(paperId, questionId);
                if (pqOpt.isPresent()) {
                    Integer scoreTmp = pqOpt.get().getScore();
                    if (scoreTmp != null) {
                        maxScore = scoreTmp;
                    }
                }
            }
            boolean isObjective = question.getType() == Question.QuestionType.SINGLE_CHOICE || question.getType() == Question.QuestionType.MULTIPLE_CHOICE || question.getType() == Question.QuestionType.TRUE_FALSE;
            if (isObjective) {
                String stdAnswer = question.getAnswer();
                String stuAnswer = req.getAnswerContent() != null ? req.getAnswerContent().trim() : "";
                boolean correct = stdAnswer != null && stdAnswer.trim().equalsIgnoreCase(stuAnswer);
                double score = correct ? maxScore : 0.0;
                String comment = correct ? "答案正确" : "答案错误";
                answer.setCorrect(correct);
                answer.setScore(score);
                answer.setAiComment(comment);
                studentAnswerRepository.save(answer);
                GradingResult gradingResult = new GradingResult();
                gradingResult.setStudentAnswer(answer);
                gradingResult.setAiScore(score);
                gradingResult.setAiComment(comment);
                gradingResult.setAiReason(comment);
                gradingResult.setAiGradingTime(LocalDateTime.now());
                gradingResultRepository.save(gradingResult);
                resultVOList.add(toVO(gradingResult));
            } else {
                aiRequests.add(new AIGradingService.AIRequest(question.getContent(), req.getAnswerContent(), maxScore));
                subjectiveAnswers.add(answer);
                subjectiveMaxScores.add(maxScore);
            }
        }
        if (!aiRequests.isEmpty()) {
            List<AIGradingService.AIGradingResult> aiResults = aiGradingService.batchGradeAnswers(aiRequests);
            for (int i = 0; i < aiResults.size(); i++) {
                AIGradingService.AIGradingResult aiRes = aiResults.get(i);
                StudentAnswer answer = subjectiveAnswers.get(i);
                double score = aiRes.score();
                double maxScore = subjectiveMaxScores.get(i);
                String comment = aiRes.comment();
                String aiReason = aiRes.reason();
                answer.setCorrect(Double.compare(score, maxScore) == 0);
                answer.setScore(score);
                answer.setAiComment(comment);
                studentAnswerRepository.save(answer);
                GradingResult gradingResult = new GradingResult();
                gradingResult.setStudentAnswer(answer);
                gradingResult.setAiScore(score);
                gradingResult.setAiComment(comment);
                gradingResult.setAiReason(aiReason);
                gradingResult.setAiGradingTime(LocalDateTime.now());
                gradingResultRepository.save(gradingResult);
                resultVOList.add(toVO(gradingResult));
            }
        }
        return resultVOList;
    }

    @Transactional
    public GradingResultVO manualGrading(ManualGradingDTO dto, Long teacherId) {
        // 人工复核
        GradingResult result = gradingResultRepository.findById(dto.getGradingResultId()).orElseThrow();
        result.setFinalScore(dto.getFinalScore());
        result.setTeacherComment(dto.getTeacherComment());
        result.setReviewTime(LocalDateTime.now());
        // 记录复核教师
        User teacher = userRepository.findById(teacherId).orElse(null);
        result.setReviewTeacher(teacher);
        gradingResultRepository.save(result);
        // 同步更新学生答案
        StudentAnswer answer = result.getStudentAnswer();
        answer.setScore(dto.getFinalScore());
        studentAnswerRepository.save(answer);
        // 同步更新练习会话总分和正确率
        PracticeSession session = answer.getPracticeSession();
        if (session != null) {
            // 确保加载所有答案
            List<StudentAnswer> answers = session.getAnswers();
            double totalScore = answers.stream().mapToDouble(a -> a.getScore() != null ? a.getScore() : 0).sum();
            long correctCount = answers.stream().filter(a -> Boolean.TRUE.equals(a.getCorrect())).count();
            double correctRate = answers.isEmpty() ? 0.0 : (double) correctCount / answers.size();
            session.setTotalScore(totalScore);
            session.setCorrectRate(correctRate);
            practiceSessionRepository.save(session);
        }
        return toVO(result);
    }

    public GradingResultVO getGradingResult(Long id) {
        GradingResult result = gradingResultRepository.findById(id).orElse(null);
        return result != null ? toVO(result) : null;
    }

    public PageResult<GradingReviewVO> listReview(int page, int size, Long paperId, String questionTitle) {
        int p = Math.max(page, 1) - 1;
        int s = Math.max(size, 1);
        Page<GradingResult> pageResult = gradingResultRepository.findPendingReview(paperId, questionTitle, PageRequest.of(p, s));
        List<GradingReviewVO> voList = pageResult.getContent().stream().map(r -> {
            GradingReviewVO vo = new GradingReviewVO();
            StudentAnswer answer = r.getStudentAnswer();
            vo.setGradingResultId(r.getId());
            vo.setStudentAnswerId(answer.getId());
            vo.setStudentName(answer.getPracticeSession() != null && answer.getPracticeSession().getStudent() != null ? answer.getPracticeSession().getStudent().getUsername() : null);
            vo.setQuestionTitle(answer.getQuestion() != null ? answer.getQuestion().getTitle() : null);
            vo.setAnswerContent(answer.getAnswerContent());
            vo.setAiScore(r.getAiScore());
            vo.setAiComment(r.getAiComment());
            vo.setAiReason(r.getAiReason() != null ? r.getAiReason() : r.getAiComment());
            vo.setAiGradingTime(r.getAiGradingTime());
            vo.setReviewed(false);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.<GradingReviewVO>builder().content(voList).total(pageResult.getTotalElements()).page(page).size(size).build();
    }

    /**
     * 分页查询已批改（人工复核完成）的判分结果
     *
     * @param page 页码，从1开始
     * @param size 每页大小
     * @param paperId 试卷ID，可选
     * @param questionTitle 题目标题关键词，可选
     * @return 分页结果
     */
    public PageResult<GradingResultVO> listReviewed(int page, int size, Long paperId, String questionTitle) {
        int p = Math.max(page, 1) - 1;
        int s = Math.max(size, 1);
        Page<GradingResult> pageResult = gradingResultRepository.findReviewed(paperId, questionTitle, PageRequest.of(p, s));
        List<GradingResultVO> content = pageResult.getContent().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return PageResult.<GradingResultVO>builder()
                .content(content)
                .total(pageResult.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }

    private GradingResultVO toVO(GradingResult result) {
        GradingResultVO vo = new GradingResultVO();
        vo.setGradingResultId(result.getId());
        vo.setStudentAnswerId(result.getStudentAnswer().getId());
        vo.setAiScore(result.getAiScore());
        vo.setAiComment(result.getAiComment());
        vo.setAiReason(result.getAiReason());
        vo.setAiGradingTime(result.getAiGradingTime());
        vo.setFinalScore(result.getFinalScore());
        vo.setTeacherComment(result.getTeacherComment());
        vo.setReviewTeacherName(result.getReviewTeacher() != null ? result.getReviewTeacher().getUsername() : null);
        vo.setReviewTime(result.getReviewTime());
        return vo;
    }

    // 其他判分相关业务方法骨架
} 