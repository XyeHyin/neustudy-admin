package com.jiangong.nmb.service;

import cn.hutool.core.exceptions.StatefulException;
import com.jiangong.nmb.dto.paper.PaperCreateDTO;
import com.jiangong.nmb.entity.Paper;
import com.jiangong.nmb.entity.User;
import com.jiangong.nmb.mapper.PaperMapper;
import com.jiangong.nmb.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaperServiceTest {

    @Mock
    private PaperRepository paperRepository;

    @Mock
    private PaperMapper paperMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaperQuestionRepository paperQuestionRepository;

    @InjectMocks
    private PaperService paperService;

    private Paper testPaper;
    private User testTeacher;
    private PaperCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        testTeacher = new User();
        testTeacher.setId(1L);
        testTeacher.setUsername("测试教师");

        testPaper = new Paper();
        testPaper.setId(1L);
        testPaper.setTitle("测试试卷");
        testPaper.setStatus(Paper.PaperStatus.DRAFT);
        testPaper.setTeacher(testTeacher);
        testPaper.setTotalScore(0);

        createDTO = new PaperCreateDTO();
        createDTO.setTitle("新试卷");
        createDTO.setDescription("测试描述");
        createDTO.setTeacherId(1L);
    }

    @Test
    void testCreatePaper_Success() {
        // Given
        when(paperMapper.toPaper(createDTO)).thenReturn(testPaper);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(paperRepository.save(any(Paper.class))).thenReturn(testPaper);

        // When
        Paper result = paperService.createPaper(createDTO);

        // Then
        assertNotNull(result);
        assertEquals(testPaper.getId(), result.getId());
        assertEquals(testPaper.getTitle(), result.getTitle());
        assertEquals(Paper.PaperStatus.DRAFT, result.getStatus());
        assertEquals(testTeacher, result.getTeacher());
        assertEquals(0, result.getTotalScore());

        verify(paperRepository).save(any(Paper.class));
    }

    @Test
    void testCreatePaper_TeacherNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(StatefulException.class, () -> {
            paperService.createPaper(createDTO);
        });
    }

    @Test
    void testFindByIdOrThrow_Success() {
        // Given
        Long paperId = 1L;
        when(paperRepository.findById(paperId)).thenReturn(Optional.of(testPaper));

        // When
        Paper result = paperService.findByIdOrThrow(paperId);

        // Then
        assertNotNull(result);
        assertEquals(testPaper, result);
    }

    @Test
    void testFindByIdOrThrow_NotFound() {
        // Given
        Long paperId = 999L;
        when(paperRepository.findById(paperId)).thenReturn(Optional.empty());

        // When & Then
        StatefulException exception = assertThrows(StatefulException.class, () -> {
            paperService.findByIdOrThrow(paperId);
        });
        assertTrue(exception.getMessage().contains("试卷不存在"));
    }

    @Test
    void testPublishPaper_AlreadyPublished() {
        // Given
        testPaper.setStatus(Paper.PaperStatus.PUBLISHED);
        when(paperRepository.findById(1L)).thenReturn(Optional.of(testPaper));

        // When & Then
        StatefulException exception = assertThrows(StatefulException.class, () -> {
            paperService.publishPaper(1L, 1L);
        });
        assertTrue(exception.getMessage().contains("试卷已发布"));
    }

    @Test
    void testPublishPaper_NoQuestions() {
        // Given
        when(paperRepository.findById(1L)).thenReturn(Optional.of(testPaper));
        when(paperQuestionRepository.findByPaperId(1L)).thenReturn(java.util.Collections.emptyList());

        // When & Then
        StatefulException exception = assertThrows(StatefulException.class, () -> {
            paperService.publishPaper(1L, 1L);
        });
        assertTrue(exception.getMessage().contains("试卷没有题目"));
    }
}
