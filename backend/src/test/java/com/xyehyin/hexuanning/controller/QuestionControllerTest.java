package com.xyehyin.hexuanning.controller;

import com.xyehyin.hexuanning.common.ApiResponse;
import com.xyehyin.hexuanning.constant.PermissionConstants;
import com.xyehyin.hexuanning.dto.question.CreateQuestionDTO;
import com.xyehyin.hexuanning.entity.Question;
import com.xyehyin.hexuanning.entity.KnowledgePoint;
import com.xyehyin.hexuanning.entity.Course;
import com.xyehyin.hexuanning.entity.User;
import com.xyehyin.hexuanning.mapper.QuestionMapper;
import com.xyehyin.hexuanning.service.QuestionService;
import com.xyehyin.hexuanning.service.KnowledgePointService;
import com.xyehyin.hexuanning.vo.question.QuestionVO;
import com.xyehyin.hexuanning.vo.question.QuestionDetailVO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionControllerTest {

    @Mock
    private QuestionService questionService;

    @Mock
    private KnowledgePointService knowledgePointService;

    @Mock
    private QuestionMapper questionMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private QuestionController questionController;

    private Question testQuestion;
    private QuestionVO testQuestionVO;
    private QuestionDetailVO testQuestionDetailVO;
    private KnowledgePoint testKnowledgePoint;
    private Course testCourse;
    private User testTeacher;

    @BeforeEach
    void setUp() {
        testTeacher = new User();
        testTeacher.setId(1L);
        testTeacher.setUsername("teacher");

        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setName("测试课程");
        testCourse.setTeacher(testTeacher);

        testKnowledgePoint = new KnowledgePoint();
        testKnowledgePoint.setId(1L);
        testKnowledgePoint.setName("测试知识点");
        testKnowledgePoint.setCourse(testCourse);

        testQuestion = new Question();
        testQuestion.setId(1L);
        testQuestion.setTitle("测试题目");
        testQuestion.setType(Question.QuestionType.SINGLE_CHOICE);
        testQuestion.setDifficulty(Question.Difficulty.MEDIUM);
        testQuestion.setKnowledgePoint(testKnowledgePoint);

        testQuestionVO = new QuestionVO();
        testQuestionVO.setId(1L);
        testQuestionVO.setTitle("测试题目");
        testQuestionVO.setType(Question.QuestionType.valueOf("SINGLE_CHOICE"));
        testQuestionVO.setDifficulty(Question.Difficulty.valueOf("MEDIUM"));

        testQuestionDetailVO = new QuestionDetailVO();
        testQuestionDetailVO.setId(1L);
        testQuestionDetailVO.setTitle("测试题目");
        testQuestionDetailVO.setType(Question.QuestionType.valueOf("SINGLE_CHOICE"));
        testQuestionDetailVO.setDifficulty(Question.Difficulty.valueOf("MEDIUM"));

    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockCurrentUser(Long userId) {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userId);
    }

    private void mockAuthorities(String... permissions) {
        List<GrantedAuthority> authorities = Arrays.stream(permissions)
                .<GrantedAuthority>map(SimpleGrantedAuthority::new)
                .toList();
        doReturn(authorities).when(authentication).getAuthorities();
    }

    @Test
    void testListAll() {
        // Given
        List<Question> questions = Arrays.asList(testQuestion);
        when(questionService.findAll()).thenReturn(questions);
        when(questionMapper.toQuestionVO(any(Question.class))).thenReturn(testQuestionVO);

        // When
        ApiResponse<List<QuestionVO>> result = questionController.listAll();

        // Then
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertEquals(1, result.getData().size());
        assertEquals("测试题目", result.getData().get(0).getTitle());

        verify(questionService).findAll();
        verify(questionMapper).toQuestionVO(testQuestion);
    }

    @Test
    void testCreate() {
        // Given
        CreateQuestionDTO createDTO = new CreateQuestionDTO();
        createDTO.setTitle("新题目");
        createDTO.setKnowledgePointId(1L);

        mockCurrentUser(1L);
        when(knowledgePointService.findById(1L)).thenReturn(testKnowledgePoint);
        when(questionMapper.toQuestion(any(CreateQuestionDTO.class))).thenReturn(testQuestion);
        when(questionService.save(any(Question.class))).thenReturn(testQuestion);
        when(questionMapper.toQuestionVO(any(Question.class))).thenReturn(testQuestionVO);

        // When
        ApiResponse<QuestionVO> result = questionController.create(createDTO, request);

        // Then
        assertNotNull(result);
        assertTrue(result.getSuccess());

        verify(knowledgePointService).findById(1L);
        verify(questionService).save(any(Question.class));
    }

    @Test
    void testDetail() {
        // Given
        mockCurrentUser(1L);
        mockAuthorities(PermissionConstants.QUESTION_VIEW_SELF);
        when(questionService.findById(1L)).thenReturn(testQuestion);
        when(questionService.isQuestionOwnedByTeacher(1L, 1L)).thenReturn(true);
        when(questionMapper.toQuestionDetailVO(any(Question.class))).thenReturn(testQuestionDetailVO);

        // When
        ApiResponse<QuestionDetailVO> result = questionController.detail(1L, request);

        // Then
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertEquals("测试题目", result.getData().getTitle());

        verify(questionService).findById(1L);
        verify(questionMapper).toQuestionDetailVO(testQuestion);
    }

    @Test
    void testGetQuestionTypes() {
        // Given
        List<Question.QuestionType> types = Arrays.asList(Question.QuestionType.values());
        when(questionService.getAllQuestionTypes()).thenReturn(types);

        // When
        ApiResponse<List<Question.QuestionType>> result = questionController.getQuestionTypes();

        // Then
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertTrue(result.getData().size() > 0);

        verify(questionService).getAllQuestionTypes();
    }

    @Test
    void testGetDifficulties() {
        // Given
        List<Question.Difficulty> difficulties = Arrays.asList(Question.Difficulty.values());
        when(questionService.getAllDifficulties()).thenReturn(difficulties);

        // When
        ApiResponse<List<Question.Difficulty>> result = questionController.getDifficulties();

        // Then
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertTrue(result.getData().size() > 0);

        verify(questionService).getAllDifficulties();
    }
}
