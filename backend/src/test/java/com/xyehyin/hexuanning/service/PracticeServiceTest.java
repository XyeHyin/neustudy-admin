package com.xyehyin.hexuanning.service;

import cn.hutool.core.exceptions.StatefulException;
import com.xyehyin.hexuanning.dto.practice.PracticeStartDTO;
import com.xyehyin.hexuanning.entity.*;
import com.xyehyin.hexuanning.repository.*;
import com.xyehyin.hexuanning.vo.practice.PracticeSessionVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PracticeServiceTest {

    @Mock
    private PracticeSessionRepository practiceSessionRepository;

    @Mock
    private PaperRepository paperRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GradingService gradingService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PracticeService practiceService;

    private Paper testPaper;
    private User testUser;
    private PracticeSession testSession;

    @BeforeEach
    void setUp() {
        testPaper = new Paper();
        testPaper.setId(1L);
        testPaper.setTitle("测试试卷");
        testPaper.setStatus(Paper.PaperStatus.PUBLISHED);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("测试学生");

        testSession = new PracticeSession();
        testSession.setId(1L);
        testSession.setPaper(testPaper);
        testSession.setStudent(testUser);
        testSession.setSubmitted(false);
        testSession.setStartTime(LocalDateTime.now());

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

    @Test
    void testStartPractice_Success() {
        // Given
        PracticeStartDTO dto = new PracticeStartDTO();
        dto.setPaperId(1L);

        mockCurrentUser(1L);
        when(paperRepository.findById(1L)).thenReturn(Optional.of(testPaper));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(practiceSessionRepository.findFirstByStudentIdAndPaperIdAndSubmittedFalse(1L, 1L))
                .thenReturn(Optional.empty());
        when(practiceSessionRepository.save(any(PracticeSession.class))).thenReturn(testSession);

        // When
        PracticeSessionVO result = practiceService.startPractice(dto);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getPaperId());
        assertEquals("测试试卷", result.getPaperTitle());
        assertEquals(1L, result.getStudentId());
        assertEquals("测试学生", result.getStudentName());
        assertFalse(result.getSubmitted());

        verify(practiceSessionRepository).save(any(PracticeSession.class));
    }

    @Test
    void testStartPractice_PaperNotFound() {
        // Given
        PracticeStartDTO dto = new PracticeStartDTO();
        dto.setPaperId(999L);

        when(paperRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(StatefulException.class, () -> {
            practiceService.startPractice(dto);
        });
    }

    @Test
    void testStartPractice_PaperNotPublished() {
        // Given
        testPaper.setStatus(Paper.PaperStatus.DRAFT);
        PracticeStartDTO dto = new PracticeStartDTO();
        dto.setPaperId(1L);

        when(paperRepository.findById(1L)).thenReturn(Optional.of(testPaper));

        // When & Then
        StatefulException exception = assertThrows(StatefulException.class, () -> {
            practiceService.startPractice(dto);
        });
        assertTrue(exception.getMessage().contains("当前试卷不可用"));
    }

    @Test
    void testStartPractice_ExistingSession() {
        // Given
        PracticeStartDTO dto = new PracticeStartDTO();
        dto.setPaperId(1L);

        mockCurrentUser(1L);
        when(paperRepository.findById(1L)).thenReturn(Optional.of(testPaper));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(practiceSessionRepository.findFirstByStudentIdAndPaperIdAndSubmittedFalse(1L, 1L))
                .thenReturn(Optional.of(testSession));

        // When & Then
        StatefulException exception = assertThrows(StatefulException.class, () -> {
            practiceService.startPractice(dto);
        });
        assertTrue(exception.getMessage().contains("已存在未提交的练习"));
    }
}
