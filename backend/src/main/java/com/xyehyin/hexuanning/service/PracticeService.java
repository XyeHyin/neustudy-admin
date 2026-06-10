package com.xyehyin.hexuanning.service;

import cn.hutool.core.exceptions.StatefulException;
import cn.hutool.http.HttpStatus;
import com.xyehyin.hexuanning.entity.PracticeSession;
import com.xyehyin.hexuanning.repository.PracticeSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.xyehyin.hexuanning.dto.practice.PracticeMarkDTO;
import com.xyehyin.hexuanning.dto.practice.PracticeStartDTO;
import com.xyehyin.hexuanning.dto.practice.PracticeSubmitDTO;
import com.xyehyin.hexuanning.vo.practice.PracticeAnswerVO;
import com.xyehyin.hexuanning.vo.practice.PracticeDetailVO;
import com.xyehyin.hexuanning.vo.practice.PracticeOverviewVO;
import com.xyehyin.hexuanning.vo.practice.PracticePaperStatVO;
import com.xyehyin.hexuanning.vo.practice.PracticeQuestionVO;
import com.xyehyin.hexuanning.vo.practice.PracticeRecordVO;
import com.xyehyin.hexuanning.vo.practice.PracticeResultVO;
import com.xyehyin.hexuanning.vo.practice.PracticeSessionVO;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.Map;
import java.util.Comparator;
import java.util.stream.Collectors;

import com.xyehyin.hexuanning.entity.User;
import com.xyehyin.hexuanning.entity.Paper;
import com.xyehyin.hexuanning.entity.PaperQuestion;
import com.xyehyin.hexuanning.repository.PaperRepository;
import com.xyehyin.hexuanning.repository.PaperQuestionRepository;
import com.xyehyin.hexuanning.entity.StudentAnswer;
import com.xyehyin.hexuanning.repository.StudentAnswerRepository;
import org.springframework.validation.annotation.Validated;
import com.xyehyin.hexuanning.service.GradingService;
import com.xyehyin.hexuanning.dto.grading.GradingRequestDTO;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.validation.Valid;
import com.xyehyin.hexuanning.entity.Question;
import com.xyehyin.hexuanning.repository.QuestionRepository;
import com.xyehyin.hexuanning.repository.UserRepository;
/**
 * 练习流程服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class PracticeService {
    private final PracticeSessionRepository practiceSessionRepository;
    private final PaperRepository paperRepository;
    private final PaperQuestionRepository paperQuestionRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final GradingService gradingService;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Optional<PracticeSession> findById(Long id) {
        return practiceSessionRepository.findById(id);
    }

    @Transactional
    public PracticeSessionVO startPractice(@Valid PracticeStartDTO dto) {
        Paper paper = paperRepository.findById(dto.getPaperId()).orElseThrow(() -> new StatefulException(HttpStatus.HTTP_NOT_FOUND, "试卷不存在"));

        // 检查试卷状态：只有PUBLISHED状态的试卷可以练习
        if (!Paper.PaperStatus.PUBLISHED.equals(paper.getStatus())) {
            throw new StatefulException(HttpStatus.HTTP_UNAVAILABLE, "当前试卷不可用，只有已发布的试卷可以练习");
        }

        Long userId = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            userId = (Long) authentication.getPrincipal();
        }
        if (userId == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "当前用户不存在");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new StatefulException(HttpStatus.HTTP_NOT_FOUND, "当前用户不存在"));

        // 防止重复开启同一试卷练习 - 使用优化后的查询方法
        Optional<PracticeSession> existingSession = practiceSessionRepository.findFirstByStudentIdAndPaperIdAndSubmittedFalse(user.getId(), paper.getId());
        if (existingSession.isPresent()) {
            throw new StatefulException(HttpStatus.HTTP_CONFLICT, "已存在未提交的练习");
        }

        // 创建新的练习会话
        PracticeSession session = new PracticeSession();
        session.setPaper(paper);
        session.setStudent(user);
        session.setAttempt(1);
        session.setSubmitted(false);
        session.setStartTime(java.time.LocalDateTime.now());
        PracticeSession saved = practiceSessionRepository.save(session);

        // 构建返回数据
        PracticeSessionVO vo = new PracticeSessionVO();
        vo.setId(saved.getId());
        vo.setPaperId(saved.getPaper().getId());
        vo.setPaperTitle(saved.getPaper().getTitle());
        vo.setStudentId(saved.getStudent().getId());
        vo.setStudentName(saved.getStudent().getUsername());
        vo.setAttempt(saved.getAttempt());
        vo.setSubmitted(saved.getSubmitted());
        vo.setStartTime(saved.getStartTime());
        vo.setSubmitTime(saved.getSubmitTime());
        vo.setAnswers(new ArrayList<>());
        return vo;
    }

    @Transactional
    public PracticeResultVO submitPractice(@Validated PracticeSubmitDTO dto) {
        PracticeSession session = practiceSessionRepository.findById(dto.getPracticeSessionId()).orElseThrow(() -> new StatefulException(HttpStatus.HTTP_NOT_FOUND, "练习会话不存在"));

        // 检查是否已提交
        if (Boolean.TRUE.equals(session.getSubmitted())) {
            throw new StatefulException(HttpStatus.HTTP_CONFLICT, "该练习已提交，不能重复提交");
        }

        // 检查是否超时
        boolean isTimeout = false;
        if (session.getStartTime() != null && session.getPaper() != null && session.getPaper().getTimeLimit() != null) {
            java.time.LocalDateTime deadline = session.getStartTime().plusMinutes(session.getPaper().getTimeLimit());
            if (java.time.LocalDateTime.now().isAfter(deadline)) {
                isTimeout = true;
            }
        }

        // 获取该试卷的所有题目，用于检查未提交题目
        List<PaperQuestion> allPaperQuestions = paperQuestionRepository.findByPaperId(session.getPaper().getId());
        Set<Long> submittedQuestionIds = dto.getAnswers().stream().map(PracticeSubmitDTO.AnswerDTO::getQuestionId).collect(java.util.stream.Collectors.toSet());

        double totalScore = 0;
        int correctCount = 0;
        int totalQuestions = allPaperQuestions.size();

        if (isTimeout) {
            // 超时：所有题目都判为错，得分为0
            for (PaperQuestion paperQuestion : allPaperQuestions) {
                StudentAnswer answer = new StudentAnswer();
                answer.setPracticeSession(session);
                answer.setQuestion(paperQuestion.getQuestion());
                answer.setAnswerContent("");
                answer.setMarked(false);
                answer.setScore(0.0);
                answer.setCorrect(false);
                studentAnswerRepository.save(answer);
            }
            // 抛出异常但继续执行
            throw new StatefulException(HttpStatus.HTTP_FORBIDDEN, "超时提交，所有题目判为错误");
        } else {
            // 正常流程
            for (PracticeSubmitDTO.AnswerDTO answerDTO : dto.getAnswers()) {
                StudentAnswer answer = new StudentAnswer();
                answer.setPracticeSession(session);
                Question question = questionRepository.findById(answerDTO.getQuestionId()).orElseThrow(() -> new StatefulException(HttpStatus.HTTP_NOT_FOUND, "题目不存在"));
                answer.setQuestion(question);
                answer.setAnswerContent(answerDTO.getAnswerContent());
                answer.setMarked(answerDTO.getMarked());
                studentAnswerRepository.save(answer);
                GradingRequestDTO gradingDTO = new GradingRequestDTO();
                gradingDTO.setStudentAnswerId(answer.getId());
                gradingDTO.setAnswerContent(answerDTO.getAnswerContent());
                gradingService.autoGrading(gradingDTO);
                StudentAnswer saved = studentAnswerRepository.findById(answer.getId()).orElseThrow(() -> new StatefulException(HttpStatus.HTTP_NOT_FOUND, "答案保存失败"));
                totalScore += saved.getScore() != null ? saved.getScore() : 0.0;
                if (Boolean.TRUE.equals(saved.getCorrect())) correctCount++;
            }
            // 处理未提交的题目（自动为用户创建空答案并标记为错误）
            for (PaperQuestion paperQuestion : allPaperQuestions) {
                Long questionId = paperQuestion.getQuestion().getId();
                if (!submittedQuestionIds.contains(questionId)) {
                    StudentAnswer emptyAnswer = new StudentAnswer();
                    emptyAnswer.setPracticeSession(session);
                    emptyAnswer.setQuestion(paperQuestion.getQuestion());
                    emptyAnswer.setAnswerContent("");
                    emptyAnswer.setMarked(false);
                    emptyAnswer.setScore(0.0);
                    emptyAnswer.setCorrect(false);
                    studentAnswerRepository.save(emptyAnswer);
                }
            }
        }

        // 更新会话信息
        session.setSubmitted(true);
        session.setTotalScore(isTimeout ? 0.0 : totalScore);
        session.setCorrectRate(isTimeout ? 0.0 : (totalQuestions > 0 ? (double) correctCount / totalQuestions : 0.0));
        session.setSubmitTime(java.time.LocalDateTime.now());
        practiceSessionRepository.save(session);

        // 构建返回数据
        PracticeResultVO vo = new PracticeResultVO();
        vo.setPracticeSessionId(session.getId());
        vo.setPaperId(session.getPaper().getId());
        vo.setPaperTitle(session.getPaper().getTitle());
        vo.setTotalScore(session.getTotalScore());
        vo.setCorrectRate(session.getCorrectRate());
        vo.setSubmitTime(session.getSubmitTime());

        List<StudentAnswer> allAnswers = studentAnswerRepository.findByPracticeSessionId(session.getId());
        List<PracticeAnswerVO> answerVOList = new ArrayList<>();
        for (StudentAnswer a : allAnswers) {
            PracticeAnswerVO avo = new PracticeAnswerVO();
            avo.setQuestionId(a.getQuestion().getId());
            Question q = questionRepository.findById(a.getQuestion().getId()).orElse(null);
            if (q != null) {
                avo.setQuestionTitle(q.getTitle());
                avo.setQuestionType(q.getType() != null ? q.getType().name() : null);
            }
            avo.setAnswerContent(a.getAnswerContent());
            avo.setScore(a.getScore());
            avo.setCorrect(a.getCorrect());
            avo.setAiComment(a.getAiComment());
            avo.setMarked(a.getMarked());
            answerVOList.add(avo);
        }
        vo.setAnswers(answerVOList);
        return vo;
    }

    @Transactional(readOnly = true)
    public PracticeSessionVO getPracticeSession(Long id) {
        PracticeSession session = practiceSessionRepository.findById(id).orElseThrow(() -> new StatefulException(HttpStatus.HTTP_NOT_FOUND, "练习会话不存在"));

        // 构建返回数据
        PracticeSessionVO vo = new PracticeSessionVO();
        vo.setId(session.getId());
        vo.setPaperId(session.getPaper().getId());
        vo.setPaperTitle(session.getPaper().getTitle());
        vo.setStudentId(session.getStudent().getId());
        vo.setStudentName(session.getStudent().getUsername());
        vo.setAttempt(session.getAttempt());
        vo.setSubmitted(session.getSubmitted());
        vo.setStartTime(session.getStartTime());
        vo.setSubmitTime(session.getSubmitTime());

        // 组装答题明细 - 使用优化后的查询方法
        List<StudentAnswer> answers = studentAnswerRepository.findByPracticeSessionId(id);
        List<PracticeAnswerVO> answerVOList = new ArrayList<>();

        for (StudentAnswer a : answers) {
            PracticeAnswerVO avo = new PracticeAnswerVO();
            avo.setQuestionId(a.getQuestion().getId());

            // 通过questionId直接查询，避免不必要的null检查
            Question q = questionRepository.findById(a.getQuestion().getId()).orElse(null);
            if (q != null) {
                avo.setQuestionTitle(q.getTitle());
                avo.setQuestionType(q.getType() != null ? q.getType().name() : null);
            }

            avo.setAnswerContent(a.getAnswerContent());
            avo.setScore(a.getScore());
            avo.setCorrect(a.getCorrect());
            avo.setAiComment(a.getAiComment());
            avo.setMarked(a.getMarked());
            answerVOList.add(avo);
        }

        vo.setAnswers(answerVOList);
        return vo;
    }

    @Transactional(readOnly = true)
    public List<PracticeRecordVO> listPracticeRecords(Long paperId, Long studentId, Boolean submitted, Integer page, Integer size) {
        // 根据查询条件构建高效查询
        List<PracticeSession> sessions;

        if (paperId != null && studentId != null && submitted != null) {
            sessions = practiceSessionRepository.findByStudentIdAndPaperIdAndSubmitted(studentId, paperId, submitted);
        } else if (paperId != null && studentId != null) {
            sessions = practiceSessionRepository.findByStudentIdAndPaperId(studentId, paperId);
        } else if (paperId != null) {
            sessions = practiceSessionRepository.findByPaperId(paperId);
        } else if (studentId != null) {
            sessions = practiceSessionRepository.findByStudentId(studentId);
        } else {
            sessions = practiceSessionRepository.findAll();
        }

        // 转换为VO
        List<PracticeRecordVO> list = new ArrayList<>();
        for (PracticeSession s : sessions) {
            // 如果有提交状态过滤条件但未匹配，则跳过
            if (submitted != null && !submitted.equals(s.getSubmitted())) {
                continue;
            }

            PracticeRecordVO vo = new PracticeRecordVO();
            vo.setPracticeSessionId(s.getId());
            vo.setPaperId(s.getPaper().getId());
            vo.setPaperTitle(s.getPaper().getTitle());
            vo.setTotalScore(s.getTotalScore());
            vo.setCorrectRate(s.getCorrectRate());
            vo.setSubmitted(s.getSubmitted());
            vo.setStartTime(s.getStartTime());
            vo.setSubmitTime(s.getSubmitTime());

            // 统计该学生在该试卷下的所有练习成绩（仅对已提交的会话）
            if (Boolean.TRUE.equals(s.getSubmitted()) && s.getStudent() != null && s.getPaper() != null) {
                List<PracticeSession> allSessions = practiceSessionRepository.findByStudentIdAndPaperId(s.getStudent().getId(), s.getPaper().getId());
                Double max = null, min = null, sum = 0.0;
                int count = 0;
                for (PracticeSession as : allSessions) {
                    if (Boolean.TRUE.equals(as.getSubmitted()) && as.getTotalScore() != null) {
                        double score = as.getTotalScore();
                        if (max == null || score > max) max = score;
                        if (min == null || score < min) min = score;
                        sum += score;
                        count++;
                    }
                }
            }
            list.add(vo);
        }

        // 手动进行分页处理
        if (page != null && size != null && size > 0) {
            int fromIndex = (page - 1) * size;
            if (fromIndex < list.size()) {
                int toIndex = Math.min(fromIndex + size, list.size());
                list = list.subList(fromIndex, toIndex);
            } else {
                list = new ArrayList<>();
            }
        }

        return list;
    }

    @Transactional(readOnly = true)
    public PracticeResultVO getPracticeResult(Long id) {
        PracticeSession session = practiceSessionRepository.findById(id).orElseThrow(() -> new StatefulException(HttpStatus.HTTP_NOT_FOUND, "练习会话不存在"));

        // 检查是否已提交
        if (!Boolean.TRUE.equals(session.getSubmitted())) {
            throw new StatefulException(HttpStatus.HTTP_CONFLICT, "练习尚未提交，无法查看结果");
        }

        // 构建返回数据
        PracticeResultVO vo = new PracticeResultVO();
        vo.setPracticeSessionId(session.getId());
        vo.setPaperId(session.getPaper().getId());
        vo.setPaperTitle(session.getPaper().getTitle());
        vo.setTotalScore(session.getTotalScore());
        vo.setCorrectRate(session.getCorrectRate());
        vo.setSubmitTime(session.getSubmitTime());

        // 补充答题明细 - 使用优化后的查询方法
        List<StudentAnswer> answers = studentAnswerRepository.findByPracticeSessionId(id);
        List<PracticeAnswerVO> answerVOList = new ArrayList<>();

        for (StudentAnswer a : answers) {
            PracticeAnswerVO avo = new PracticeAnswerVO();
            avo.setQuestionId(a.getQuestion().getId());

            Question q = questionRepository.findById(a.getQuestion().getId()).orElse(null);
            if (q != null) {
                avo.setQuestionTitle(q.getTitle());
                avo.setQuestionType(q.getType() != null ? q.getType().name() : null);
            }

            avo.setAnswerContent(a.getAnswerContent());
            avo.setScore(a.getScore());
            avo.setCorrect(a.getCorrect());
            avo.setAiComment(a.getAiComment());
            avo.setMarked(a.getMarked());
            answerVOList.add(avo);
        }

        // 设置答案列表
        if (answerVOList.isEmpty()) {
            vo.setAnswers(new ArrayList<>());
        } else {
            vo.setAnswers(answerVOList);
        }
        return vo;
    }

    @Transactional
    public void markQuestion(@Valid PracticeMarkDTO dto) {
        // 验证练习会话存在
        PracticeSession session = practiceSessionRepository.findById(dto.getPracticeSessionId()).orElseThrow(() -> new StatefulException(HttpStatus.HTTP_NOT_FOUND, "练习会话不存在"));

        // 使用优化后的查询方法查找特定答案
        StudentAnswer answer = studentAnswerRepository.findByPracticeSessionIdAndQuestionId(dto.getPracticeSessionId(), dto.getQuestionId()).orElseThrow(() -> new StatefulException(HttpStatus.HTTP_NOT_FOUND, "未找到该题答案"));

        answer.setMarked(dto.getMarked());
        studentAnswerRepository.save(answer);
    }

    @Transactional(readOnly = true)
    public PracticeOverviewVO getPracticeOverview(Long id) {
        PracticeSession session = practiceSessionRepository.findById(id).orElseThrow(() -> new StatefulException(HttpStatus.HTTP_NOT_FOUND, "练习会话不存在"));

        // 使用优化后的查询方法
        List<StudentAnswer> answers = studentAnswerRepository.findByPracticeSessionId(id);
        List<PaperQuestion> paperQuestions = paperQuestionRepository.findByPaperId(session.getPaper().getId());

        // 构建返回数据
        PracticeOverviewVO vo = new PracticeOverviewVO();
        vo.setPracticeSessionId(id);
        vo.setPaperId(session.getPaper().getId());
        vo.setPaperTitle(session.getPaper().getTitle());
        vo.setTotalQuestions(paperQuestions.size());
        vo.setAnsweredQuestions((int) answers.stream().filter(a -> a.getAnswerContent() != null && !a.getAnswerContent().isBlank()).count());
        vo.setMarkedQuestions((int) answers.stream().filter(a -> a.getMarked() != null && a.getMarked()).count());
        vo.setSubmitted(session.getSubmitted());

        // 每题进度
        List<PracticeOverviewVO.QuestionProgress> progressList = new ArrayList<>();
        for (PaperQuestion pq : paperQuestions) {
            PracticeOverviewVO.QuestionProgress qp = new PracticeOverviewVO.QuestionProgress();
            qp.setQuestionId(pq.getQuestion().getId());
            qp.setQuestionTitle(pq.getQuestion().getTitle());

            // 在内存中查找对应答案
            StudentAnswer ans = answers.stream().filter(a -> a.getQuestion().getId().equals(pq.getQuestion().getId())).findFirst().orElse(null);

            qp.setAnswered(ans != null && ans.getAnswerContent() != null && !ans.getAnswerContent().isBlank());
            qp.setMarked(ans != null && ans.getMarked() != null && ans.getMarked());
            progressList.add(qp);
        }

        vo.setQuestionProgressList(progressList);
        return vo;
    }

    /**
     * 查询某学生的练习历史
     */
    @Transactional(readOnly = true)
    public List<PracticeRecordVO> getPracticeHistory(Long studentId) {
        // 使用优化后的查询方法
        List<PracticeSession> sessions = practiceSessionRepository.findByStudentId(studentId);
        List<PracticeRecordVO> list = new ArrayList<>();

        for (PracticeSession s : sessions) {
            PracticeRecordVO vo = new PracticeRecordVO();
            vo.setPracticeSessionId(s.getId());
            vo.setPaperId(s.getPaper().getId());
            vo.setPaperTitle(s.getPaper().getTitle());
            vo.setTotalScore(s.getTotalScore());
            vo.setCorrectRate(s.getCorrectRate());
            vo.setSubmitted(s.getSubmitted());
            vo.setStartTime(s.getStartTime());
            vo.setSubmitTime(s.getSubmitTime());
            list.add(vo);
        }

        return list;
    }

    /**
     * 查询某学生某试卷的练习统计
     */
    @Transactional(readOnly = true)
    public PracticeRecordVO getPracticeStatistics(Long studentId, Long paperId) {
        // 使用优化后的查询方法
        List<PracticeSession> sessions = practiceSessionRepository.findByStudentIdAndPaperId(studentId, paperId);

        if (sessions.isEmpty()) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "未找到练习记录");
        }

        double max = Double.MIN_VALUE, min = Double.MAX_VALUE, sum = 0;
        int count = 0;
        PracticeSession latest = null;

        for (PracticeSession s : sessions) {
            if (s.getTotalScore() != null) {
                double score = s.getTotalScore();
                if (score > max) max = score;
                if (score < min) min = score;
                sum += score;
                count++;
            }

            if (latest == null || (s.getSubmitTime() != null && latest.getSubmitTime() != null && s.getSubmitTime().isAfter(latest.getSubmitTime()))) {
                latest = s;
            }
        }

        PracticeRecordVO vo = new PracticeRecordVO();
        vo.setPaperId(paperId);

        if (latest != null) {
            vo.setPracticeSessionId(latest.getId());
            vo.setPaperTitle(latest.getPaper().getTitle());
            vo.setTotalScore(latest.getTotalScore());
            vo.setCorrectRate(latest.getCorrectRate());
            vo.setSubmitted(latest.getSubmitted());
            vo.setStartTime(latest.getStartTime());
            vo.setSubmitTime(latest.getSubmitTime());
        }

        return vo;
    }

    /**
     * 获取练习详情，包括试卷信息和题目详细信息
     *
     * @param id 练习会话ID
     * @return 练习详情VO
     */
    @Transactional(readOnly = true)
    public PracticeDetailVO getPracticeDetail(Long id) {
        // 获取练习会话
        PracticeSession session = practiceSessionRepository.findById(id).orElseThrow(() -> new StatefulException(HttpStatus.HTTP_NOT_FOUND, "练习会话不存在"));

        // 获取试卷信息
        Paper paper = session.getPaper();
        if (paper == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "试卷不存在");
        }

        // 构建返回数据
        PracticeDetailVO vo = new PracticeDetailVO();
        vo.setPracticeSessionId(session.getId());
        vo.setPaperId(paper.getId());
        vo.setPaperTitle(paper.getTitle());
        vo.setPaperDescription(paper.getDescription());
        vo.setTimeLimit(paper.getTimeLimit());
        vo.setTotalScore(paper.getTotalScore());
        vo.setStartTime(session.getStartTime());
        vo.setSubmitted(session.getSubmitted());

        // 获取试卷题目列表
        List<PaperQuestion> paperQuestions = paperQuestionRepository.findByPaperId(paper.getId());

        // 按题目顺序排序
        paperQuestions.sort(Comparator.comparing(PaperQuestion::getOrderNum));

        // 获取学生已回答的题目
        List<StudentAnswer> studentAnswers = studentAnswerRepository.findByPracticeSessionId(session.getId());
        Map<Long, StudentAnswer> answerMap = studentAnswers.stream().collect(Collectors.toMap(answer -> answer.getQuestion().getId(), answer -> answer, (existing, replacement) -> existing));

        // 组装题目详情
        List<PracticeQuestionVO> questionVOs = new ArrayList<>();
        for (PaperQuestion pq : paperQuestions) {
            Question question = pq.getQuestion();
            if (question == null) continue;

            PracticeQuestionVO questionVO = new PracticeQuestionVO();
            questionVO.setQuestionId(question.getId());
            questionVO.setTitle(question.getTitle());
            questionVO.setContent(question.getContent());
            questionVO.setType(question.getType() != null ? question.getType().name() : null);
            questionVO.setOrderNum(pq.getOrderNum());
            questionVO.setScore(pq.getScore());
            questionVO.setOptions(question.getOptions());
            questionVO.setDifficulty(question.getDifficulty() != null ? question.getDifficulty().name() : null);

            // 设置学生答案（如果已回答）
            StudentAnswer answer = answerMap.get(question.getId());
            if (answer != null) {
                questionVO.setStudentAnswer(answer.getAnswerContent());
                questionVO.setMarked(answer.getMarked());
            } else {
                questionVO.setStudentAnswer(null);
                questionVO.setMarked(false);
            }

            questionVOs.add(questionVO);
        }

        vo.setQuestions(questionVOs);
        return vo;
    }

    /**
     * 获取所有可用试卷及当前用户的练习统计信息
     */
    @Transactional(readOnly = true)
    public List<PracticePaperStatVO> listAvailablePapersWithStats(Long userId) {
        // 查询所有已发布试卷
        List<Paper> papers = paperRepository.findAll().stream().filter(p -> Paper.PaperStatus.PUBLISHED.equals(p.getStatus())).collect(Collectors.toList());
        List<PracticePaperStatVO> result = new ArrayList<>();
        for (Paper paper : papers) {
            PracticePaperStatVO vo = new PracticePaperStatVO();
            vo.setPaperId(paper.getId());
            vo.setPaperTitle(paper.getTitle());
            vo.setPaperDescription(paper.getDescription());
            vo.setTimeLimit(paper.getTimeLimit());
            vo.setTotalScore(paper.getTotalScore());

            // 查询该用户在该试卷下的所有练习记录
            List<PracticeSession> sessions = practiceSessionRepository.findByStudentIdAndPaperId(userId, paper.getId());
            Double min = null, max = null, sum = 0.0;
            int count = 0;
            boolean hasFull = false;
            // 分数趋势
            List<Double> scoreTrend = sessions.stream().filter(s -> Boolean.TRUE.equals(s.getSubmitted()) && s.getTotalScore() != null).sorted(Comparator.comparing(PracticeSession::getSubmitTime, Comparator.nullsLast(Comparator.naturalOrder()))).map(PracticeSession::getTotalScore).toList();
            for (PracticeSession s : sessions) {
                if (Boolean.TRUE.equals(s.getSubmitted()) && s.getTotalScore() != null) {
                    double score = s.getTotalScore();
                    if (min == null || score < min) min = score;
                    if (max == null || score > max) max = score;
                    sum += score;
                    count++;
                    if (paper.getTotalScore() != null && score == paper.getTotalScore()) {
                        hasFull = true;
                    }
                }
            }
            vo.setMinScore(min);
            vo.setMaxScore(max);
            vo.setAvgScore(count > 0 ? sum / count : null);
            vo.setTotalAttempts(count);
            vo.setHasFullScore(hasFull);
            vo.setTeacherName(paper.getTeacher() != null ? paper.getTeacher().getUsername() : null);
            vo.setStatus(paper.getStatus() != null ? paper.getStatus().name() : null);
            vo.setCreateTime(paper.getCreateTime());
            vo.setScoreTrend(scoreTrend);
            result.add(vo);
        }
        return result;
    }
}
