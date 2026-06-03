package com.xyehyin.hexuanning.service;

import com.xyehyin.hexuanning.repository.*;
import com.xyehyin.hexuanning.vo.statistics.*;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

import com.xyehyin.hexuanning.entity.Paper;
import com.xyehyin.hexuanning.entity.Question;
import com.xyehyin.hexuanning.entity.PracticeSession;
import com.xyehyin.hexuanning.entity.StudentAnswer;
import com.xyehyin.hexuanning.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.EnumMap;

import com.xyehyin.hexuanning.entity.Question.QuestionType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 统计服务
 */
@RequiredArgsConstructor
@Service
public class StatisticsService {
    private final PaperRepository paperRepository;
    private final QuestionRepository questionRepository;
    private final PracticeSessionRepository practiceSessionRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final KnowledgePointRepository knowledgePointRepository;

    public PaperStatisticsVO paperStatistics(Long paperId) {
        Paper paper = paperRepository.findById(paperId).orElse(null);
        if (paper == null) return null;
        List<PracticeSession> sessions = practiceSessionRepository.findAll();
        PaperStatisticsVO vo = new PaperStatisticsVO();
        vo.setPaperId(paperId);
        vo.setPaperTitle(paper.getTitle());
        vo.setTotalAttempts((int) sessions.stream().filter(s -> s.getPaper().getId().equals(paperId)).count());
        vo.setAverageScore(sessions.stream().filter(s -> s.getPaper().getId().equals(paperId) && s.getTotalScore() != null).mapToDouble(PracticeSession::getTotalScore).average().orElse(0));
        vo.setHighestScore(sessions.stream().filter(s -> s.getPaper().getId().equals(paperId) && s.getTotalScore() != null).mapToDouble(PracticeSession::getTotalScore).max().orElse(0));
        vo.setLowestScore(sessions.stream().filter(s -> s.getPaper().getId().equals(paperId) && s.getTotalScore() != null).mapToDouble(PracticeSession::getTotalScore).min().orElse(0));
        vo.setCorrectRate(sessions.stream().filter(s -> s.getPaper().getId().equals(paperId) && s.getCorrectRate() != null).mapToDouble(PracticeSession::getCorrectRate).average().orElse(0));
        return vo;
    }

    public QuestionStatisticsVO questionStatistics(Long questionId) {
        Question q = questionRepository.findById(questionId).orElse(null);
        if (q == null) return null;
        List<StudentAnswer> answers = studentAnswerRepository.findAll();
        int total = 0, correct = 0;
        for (StudentAnswer a : answers) {
            if (a.getQuestion().getId().equals(questionId)) {
                total++;
                if (Boolean.TRUE.equals(a.getCorrect())) correct++;
            }
        }
        QuestionStatisticsVO vo = new QuestionStatisticsVO();
        vo.setQuestionId(questionId);
        vo.setQuestionTitle(q.getTitle());
        vo.setQuestionType(q.getType() != null ? q.getType().name() : null);
        vo.setDifficulty(q.getDifficulty() != null ? q.getDifficulty().name() : null);
        vo.setTotalAttempts(total);
        vo.setCorrectRate(total > 0 ? (double) correct / total : 0.0);
        return vo;
    }

    public StudentStatisticsVO studentStatistics(Long studentId) {
        List<PracticeSession> sessions = practiceSessionRepository.findAll();
        List<PracticeSession> studentSessions = new ArrayList<>();
        String studentName = null;
        for (PracticeSession s : sessions) {
            if (s.getStudent() != null && s.getStudent().getId().equals(studentId)) {
                studentSessions.add(s);
                if (studentName == null && StringUtils.hasText(s.getStudent().getUsername())) {
                    studentName = s.getStudent().getUsername();
                }
            }
        }
        StudentStatisticsVO vo = new StudentStatisticsVO();
        vo.setStudentId(studentId);
        vo.setStudentName(studentName);
        vo.setTotalPractices(studentSessions.size());
        vo.setAverageScore(studentSessions.stream().filter(s -> s.getTotalScore() != null).mapToDouble(PracticeSession::getTotalScore).average().orElse(0));
        List<Double> scoreTrend = new ArrayList<>();
        studentSessions.stream().filter(s -> s.getTotalScore() != null).sorted(Comparator.comparing(PracticeSession::getStartTime)).forEach(s -> scoreTrend.add(s.getTotalScore()));
        vo.setScoreTrend(scoreTrend);
        return vo;
    }


    public List<DashboardStatVO> dashboardStats() {
        int questionCount = (int) questionRepository.count();
        int courseCount = (int) courseRepository.count();
        int userCount = (int) userRepository.count();
        int categoryCount = (int) knowledgePointRepository.count();

        return List.of(
                new DashboardStatVO("questions", "总题目", questionCount, "道", 0),
                new DashboardStatVO("courses", "总课程", courseCount, "门", 0),
                new DashboardStatVO("users", "用户数量", userCount, "人", 0),
                new DashboardStatVO("categories", "知识点", categoryCount, "个", 0)
        );
    }

    public DashboardTrendVO dashboardTrend(String period) {
        int days = 7;
        if ("30d".equals(period)) days = 30;
        else if ("90d".equals(period)) days = 90;
        List<String> categories = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();
            int count = (int) questionRepository.countByCreateTimeBetween(start, end);
            categories.add(date.format(formatter));
            values.add(count);
        }
        return new DashboardTrendVO(categories, values);
    }

    public List<DashboardDistributionVO> dashboardDistribution() {
        EnumMap<QuestionType, Integer> typeCount = new EnumMap<>(QuestionType.class);
        for (QuestionType type : QuestionType.values()) {
            int count = questionRepository.findByType(type).size();
            typeCount.put(type, count);
        }
        List<DashboardDistributionVO> result = new ArrayList<>();
        for (QuestionType type : QuestionType.values()) {
            result.add(new DashboardDistributionVO(type.getDescription(), typeCount.getOrDefault(type, 0)));
        }
        return result;
    }
} 