package com.jiangong.nmb.controller;

import com.jiangong.nmb.common.ApiResponse;
import com.jiangong.nmb.common.PageResult;
import com.jiangong.nmb.dto.paper.PaperCreateDTO;
import com.jiangong.nmb.dto.paper.PaperUpdateDTO;
import com.jiangong.nmb.entity.Paper;
import com.jiangong.nmb.mapper.PaperMapper;
import com.jiangong.nmb.service.PaperService;
import com.jiangong.nmb.vo.paper.PaperDetailVO;
import com.jiangong.nmb.vo.paper.PaperListVO;
import com.jiangong.nmb.vo.paper.PaperQuestionVO;
import com.jiangong.nmb.vo.statistics.PaperStatisticsVO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaperControllerTest {

    @Mock
    private PaperService paperService;

    @Mock
    private PaperMapper paperMapper;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private PaperController paperController;

    private Paper testPaper;
    private PaperDetailVO paperDetailVO;
    private PaperListVO paperListVO;
    private PaperStatisticsVO paperStatsVO;

    @BeforeEach
    void setUp() {
        testPaper = new Paper();
        testPaper.setId(1L);
        testPaper.setTitle("测试试卷");
        testPaper.setDescription("测试描述");
        testPaper.setStatus(Paper.PaperStatus.DRAFT);
        testPaper.setTimeLimit(120);

        paperDetailVO = new PaperDetailVO();
        paperDetailVO.setId(1L);
        paperDetailVO.setTitle("测试试卷");
        paperDetailVO.setDescription("测试描述");
        paperDetailVO.setTimeLimit(120);

        paperListVO = new PaperListVO();
        paperListVO.setId(1L);
        paperListVO.setTitle("测试试卷");

        paperStatsVO = new PaperStatisticsVO();
        paperStatsVO.setPaperId(1L);
        paperStatsVO.setPaperTitle("测试试卷");
        paperStatsVO.setTotalAttempts(30);
        paperStatsVO.setAverageScore(78.5);
        paperStatsVO.setScoreDistribution(new HashMap<>());
    }

    @Test
    void testCreatePaper() {
        // Given
        PaperCreateDTO createDTO = new PaperCreateDTO();
        createDTO.setTitle("新试卷");
        createDTO.setDescription("新描述");
        createDTO.setTeacherId(1L);

        when(paperService.createPaper(any(PaperCreateDTO.class))).thenReturn(testPaper);
        when(paperMapper.toPaperDetailVO(any(Paper.class))).thenReturn(paperDetailVO);

        // When
        ApiResponse<PaperDetailVO> result = paperController.createPaper(createDTO);

        // Then
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertEquals("测试试卷", result.getData().getTitle());

        verify(paperService).createPaper(createDTO);
        verify(paperMapper).toPaperDetailVO(testPaper);
    }

    @Test
    void testPage() {
        // Given
        Page<Paper> paperPage = new PageImpl<>(Arrays.asList(testPaper));
        when(paperService.page(any(), any(), any(), any(), any())).thenReturn(paperPage);
        when(paperMapper.toPaperListVO(any(Paper.class))).thenReturn(paperListVO);

        // When
        ApiResponse<PageResult<PaperListVO>> result = paperController.page(null, null, null, 1, 10);

        // Then
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertEquals(1, result.getData().getContent().size());
        assertEquals(1L, result.getData().getTotal());

        verify(paperService).page(null, null, null, 1, 10);
    }

    @Test
    void testGetPaper() {
        // Given
        List<PaperQuestionVO> questions = Arrays.asList(new PaperQuestionVO());
        when(paperService.findByIdOrThrow(1L)).thenReturn(testPaper);
        when(paperMapper.toPaperDetailVO(any(Paper.class))).thenReturn(paperDetailVO);
        when(paperService.listQuestions(anyLong(), anyBoolean())).thenReturn(questions);

        // When
        ApiResponse<PaperDetailVO> result = paperController.getPaper(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertEquals(1L, result.getData().getId());
        assertEquals("测试试卷", result.getData().getTitle());
        assertEquals(120, result.getData().getTimeLimit());

        verify(paperService).findByIdOrThrow(1L);
        verify(paperService).listQuestions(1L, false);
    }

    @Test
    void testUpdatePaper() {
        // Given
        PaperUpdateDTO updateDTO = new PaperUpdateDTO();
        updateDTO.setTitle("更新的试卷");

        when(paperService.updatePaper(eq(1L), eq(updateDTO), isNull())).thenReturn(testPaper);
        when(paperMapper.toPaperDetailVO(any(Paper.class))).thenReturn(paperDetailVO);

        // When
        ApiResponse<PaperDetailVO> result = paperController.updatePaper(1L, updateDTO, request);

        // Then
        assertNotNull(result);
        assertTrue(result.getSuccess());

        verify(paperService).updatePaper(eq(1L), eq(updateDTO), isNull());
    }

    @Test
    void testStatistics() {
        // Given
        when(paperService.statistics(1L)).thenReturn(paperStatsVO);

        // When
        ApiResponse<PaperStatisticsVO> result = paperController.statistics(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertEquals(1L, result.getData().getPaperId());
        assertEquals("测试试卷", result.getData().getPaperTitle());
        assertEquals(30, result.getData().getTotalAttempts());
        assertEquals(78.5, result.getData().getAverageScore());

        verify(paperService).statistics(1L);
    }
}
