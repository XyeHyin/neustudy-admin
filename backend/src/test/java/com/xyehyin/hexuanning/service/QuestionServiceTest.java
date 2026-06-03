package com.xyehyin.hexuanning.service;

import com.xyehyin.hexuanning.entity.Question;
import com.xyehyin.hexuanning.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private KnowledgePointService knowledgePointService;

    @InjectMocks
    private QuestionService questionService;

    private Question testQuestion;

    @BeforeEach
    void setUp() {
        testQuestion = new Question();
        testQuestion.setId(1L);
        testQuestion.setTitle("测试题目");
        testQuestion.setType(Question.QuestionType.SINGLE_CHOICE);
        testQuestion.setDifficulty(Question.Difficulty.MEDIUM);
        testQuestion.setEnabled(true);
    }

    @Test
    void testFindByKnowledgePointId() {
        // Given
        Long knowledgePointId = 1L;
        List<Question> expectedQuestions = Arrays.asList(testQuestion);
        when(questionRepository.findByKnowledgePointId(knowledgePointId)).thenReturn(expectedQuestions);

        // When
        List<Question> result = questionService.findByKnowledgePointId(knowledgePointId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testQuestion, result.get(0));
        verify(questionRepository).findByKnowledgePointId(knowledgePointId);
    }

    @Test
    void testFindByType() {
        // Given
        Question.QuestionType type = Question.QuestionType.SINGLE_CHOICE;
        List<Question> expectedQuestions = Arrays.asList(testQuestion);
        when(questionRepository.findByType(type)).thenReturn(expectedQuestions);

        // When
        List<Question> result = questionService.findByType(type);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testQuestion, result.get(0));
        verify(questionRepository).findByType(type);
    }

    @Test
    void testFindByDifficulty() {
        // Given
        Question.Difficulty difficulty = Question.Difficulty.MEDIUM;
        List<Question> expectedQuestions = Arrays.asList(testQuestion);
        when(questionRepository.findByDifficulty(difficulty)).thenReturn(expectedQuestions);

        // When
        List<Question> result = questionService.findByDifficulty(difficulty);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testQuestion, result.get(0));
        verify(questionRepository).findByDifficulty(difficulty);
    }

    @Test
    void testCountByKnowledgePointId() {
        // Given
        Long knowledgePointId = 1L;
        long expectedCount = 5L;
        when(questionRepository.countByKnowledgePointId(knowledgePointId)).thenReturn(expectedCount);

        // When
        long result = questionService.countByKnowledgePointId(knowledgePointId);

        // Then
        assertEquals(expectedCount, result);
        verify(questionRepository).countByKnowledgePointId(knowledgePointId);
    }

    @Test
    void testGetAllQuestionTypes() {
        // When
        List<Question.QuestionType> result = questionService.getAllQuestionTypes();

        // Then
        assertNotNull(result);
        assertEquals(Question.QuestionType.values().length, result.size());
        assertTrue(result.contains(Question.QuestionType.SINGLE_CHOICE));
        assertTrue(result.contains(Question.QuestionType.MULTIPLE_CHOICE));
        assertTrue(result.contains(Question.QuestionType.TRUE_FALSE));
    }

    @Test
    void testGetAllDifficulties() {
        // When
        List<Question.Difficulty> result = questionService.getAllDifficulties();

        // Then
        assertNotNull(result);
        assertEquals(Question.Difficulty.values().length, result.size());
        assertTrue(result.contains(Question.Difficulty.EASY));
        assertTrue(result.contains(Question.Difficulty.MEDIUM));
        assertTrue(result.contains(Question.Difficulty.HARD));
    }
}
