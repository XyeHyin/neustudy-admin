package com.jiangong.nmb.service;

import com.jiangong.nmb.entity.*;
import com.jiangong.nmb.repository.*;
import com.jiangong.nmb.vo.statistics.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private PaperRepository paperRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private PracticeSessionRepository practiceSessionRepository;

    @Mock
    private StudentAnswerRepository studentAnswerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private KnowledgePointRepository knowledgePointRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    private Paper testPaper;
    private Question testQuestion;
    private PracticeSession testSession;
    private User testStudent;

    @BeforeEach
    void setUp() {
        testPaper = new Paper();
        testPaper.setId(1L);
        testPaper.setTitle("测试试卷");

        testQuestion = new Question();
        testQuestion.setId(1L);
        testQuestion.setTitle("测试题目");
        testQuestion.setType(Question.QuestionType.SINGLE_CHOICE);
        testQuestion.setDifficulty(Question.Difficulty.MEDIUM);

        testStudent = new User();
        testStudent.setId(1L);
        testStudent.setUsername("测试学生");

        testSession = new PracticeSession();
        testSession.setId(1L);
        testSession.setPaper(testPaper);
        testSession.setStudent(testStudent);
        testSession.setTotalScore(85.0);
        testSession.setCorrectRate(0.85);
        testSession.setSubmitted(true);
        testSession.setStartTime(LocalDateTime.now().minusHours(1));
    }

    @Test
    void testPaperStatistics_ValidPaper() {
        // Given
        Long paperId = 1L;
        when(paperRepository.findById(paperId)).thenReturn(Optional.of(testPaper));
        when(practiceSessionRepository.findAll()).thenReturn(Arrays.asList(testSession));

        // When
        PaperStatisticsVO result = statisticsService.paperStatistics(paperId);

        // Then
        assertNotNull(result);
        assertEquals(paperId, result.getPaperId());
        assertEquals("测试试卷", result.getPaperTitle());
        assertEquals(1, result.getTotalAttempts());
        assertEquals(85.0, result.getAverageScore());
        assertEquals(85.0, result.getHighestScore());
        assertEquals(85.0, result.getLowestScore());
        assertEquals(0.85, result.getCorrectRate());
    }

    @Test
    void testPaperStatistics_PaperNotFound() {
        // Given
        Long paperId = 999L;
        when(paperRepository.findById(paperId)).thenReturn(Optional.empty());

        // When
        PaperStatisticsVO result = statisticsService.paperStatistics(paperId);

        // Then
        assertNull(result);
    }

    @Test
    void testQuestionStatistics_ValidQuestion() {
        // Given
        Long questionId = 1L;
        StudentAnswer correctAnswer = new StudentAnswer();
        correctAnswer.setQuestion(testQuestion);
        correctAnswer.setCorrect(true);

        StudentAnswer wrongAnswer = new StudentAnswer();
        wrongAnswer.setQuestion(testQuestion);
        wrongAnswer.setCorrect(false);

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(testQuestion));
        when(studentAnswerRepository.findAll()).thenReturn(Arrays.asList(correctAnswer, wrongAnswer));

        // When
        QuestionStatisticsVO result = statisticsService.questionStatistics(questionId);

        // Then
        assertNotNull(result);
        assertEquals(questionId, result.getQuestionId());
        assertEquals("测试题目", result.getQuestionTitle());
        assertEquals("SINGLE_CHOICE", result.getQuestionType());
        assertEquals("MEDIUM", result.getDifficulty());
        assertEquals(2, result.getTotalAttempts());
        assertEquals(0.5, result.getCorrectRate());
    }

    @Test
    void testDashboardStats() {
        // Given
        when(questionRepository.count()).thenReturn(100L);
        when(courseRepository.count()).thenReturn(10L);
        when(userRepository.count()).thenReturn(50L);
        when(knowledgePointRepository.count()).thenReturn(20L);

        // When
        List<DashboardStatVO> result = statisticsService.dashboardStats();

        // Then
        assertNotNull(result);
        assertEquals(4, result.size());

        DashboardStatVO questionsStats = result.get(0);
        assertEquals("questions", questionsStats.getKey());
        assertEquals("总题目", questionsStats.getLabel());
        assertEquals(100, questionsStats.getValue());
        assertEquals("道", questionsStats.getUnit());
    }
}
